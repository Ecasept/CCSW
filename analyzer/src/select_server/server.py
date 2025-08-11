import io
import threading
from .bounds import Bounds
from PIL import Image
from http.server import BaseHTTPRequestHandler, HTTPServer
from datetime import datetime
from config import conf
from logger import log
from request import post
import json
import html

bounds: Bounds = None

# Holds last login error (None means success)
login_error: str | None = None


def run_server(take_screenshot_func, prompt_for_bounds: bool):
    """
    Run the server to handle instance configuration and (optionally) bounds selection.
    Accepts a screenshot function (takes a callback(image, timestamp)).
    Returns the selected Bounds object (or a default 0,0,0,0 if bounds not prompted).
    """
    img: Image.Image = None
    timestamp: datetime = None

    if conf.is_instance_configured():
        global login_error
        login_error = conf.login()

    def callback(_img, _timestamp):
        nonlocal img, timestamp
        img, timestamp = _img, _timestamp
    take_screenshot_func(callback)

    _run_server(img, prompt_for_bounds)

    if not prompt_for_bounds:
        return Bounds(0, 0, 0, 0)
    return bounds


def get_bounds_html():
    PATH = "select_server/bounds.html"
    with open(PATH, "br") as file:
        html_content = file.read()
    return html_content


def get_instance_html():
    PATH = "select_server/instance.html"
    with open(PATH, "br") as file:
        html_content = file.read()
    return html_content


def get_logged_in_instance_html():
    """Return the logged-in instance page with dynamic status (success or error)."""
    PATH = "select_server/instance_logged_in.html"
    with open(PATH, "r") as file:
        html_template = file.read()

    instance_id = html.escape(conf.INSTANCE_ID)
    success = "false" if login_error else "true"
    error_msg = html.escape(login_error) if login_error else ""

    html_content = (html_template
                    .replace("{{instance_id}}", instance_id)
                    .replace("{{login_success}}", success)
                    .replace("{{login_error_message}}", error_msg))
    return html_content.encode("utf-8")


class BoundsServer(BaseHTTPRequestHandler):
    def __init__(self,
                 img: Image.Image,
                 prompt_for_bounds: bool,
                 *args, **kwargs):
        self.img = img
        self.prompt_for_bounds = prompt_for_bounds
        super().__init__(*args, **kwargs)

    def parse_json_request(self):
        try:
            content_length = int(self.headers.get("Content-Length", 0))
            post_data = self.rfile.read(content_length).decode("utf-8")
            return json.loads(post_data), None
        except json.JSONDecodeError:
            return None, "Invalid JSON data"
        except Exception as e:
            return None, f"Error parsing request: {str(e)}"

    def redirect_to(self, path):
        self.send_response(302)
        self.send_header("Location", path)
        self.end_headers()

    def do_GET(self):
        log.debug(f"Received GET request for {self.path}")
        if self.path == "/bounds":
            if not self.prompt_for_bounds:
                # If no bounds selection is needed, we are finished
                self.stop_server()

            if not conf.is_instance_configured() or login_error:
                # Need to configure instance first
                self.redirect_to("/instance")
            else:
                self.serve_bounds_selection()
        elif self.path == "/instance":
            self.serve_instance_configuration()
        elif self.path == "/img":
            self.serve_screenshot()
        elif self.path == "/":
            self.redirect_to("/instance")

    def do_POST(self):
        log.debug(f"Received POST request for {self.path}")
        if self.path == "/bounds":
            self.save_bounds_selection()
        elif self.path == "/api/create-instance":
            self.create_instance()
        elif self.path == "/api/add-to-config":
            self.add_to_config()
        elif self.path == "/api/reload-config":
            self.reload_config()

    def serve_instance_configuration(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/html")
        self.end_headers()

        if conf.is_instance_configured():
            html_content = get_logged_in_instance_html()
        else:
            html_content = get_instance_html()

        self.wfile.write(html_content)

    def serve_bounds_selection(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/html")
        self.end_headers()
        html_content = get_bounds_html()
        self.wfile.write(html_content)

    def create_instance(self):
        data, error = self.parse_json_request()
        if error:
            self.send_json_response(400, {"error": error})
            return

        instance_name = data.get("name", "").strip()
        instance_id = data.get("id", "").strip()

        if not instance_name:
            self.send_json_response(
                400, {"error": "Instance name is required"})
            return

        log.info(f"Creating instance '{instance_name}' on remote server")

        try:
            payload = {"name": instance_name, "id": instance_id}
            result = post(conf.Endpoint.CREATE_INSTANCE, payload, auth=False).as_result()

            if not result.success:
                log.error(f"Request error: {result.error}")
                self.send_json_response(
                    500, {"error": f"Request failed: {result.error}"})
                return

            log.info(
                f"Instance created successfully: {result.data['instance']['id']}")
            self.send_json_response(200, result.data)

        except Exception as e:
            log.exception(f"Error creating instance: {e}")
            self.send_json_response(
                500, {"error": "Server error while creating instance"})

    def add_to_config(self):
        data, error = self.parse_json_request()
        if error:
            self.send_json_response(400, {"error": error})
            return

        instance_id = data.get("instanceId", "").strip()
        api_key = data.get("apiKey", "").strip()

        if not instance_id or not api_key:
            self.send_json_response(
                400, {"error": "Both instance ID and API key are required"})
            return

        success = conf.add_instance_config(instance_id, api_key)

        if success:
            log.info(f"Added instance configuration: {instance_id}")
            self.send_json_response(200, {"success": True})
        else:
            self.send_json_response(
                500, {"error": "Failed to update configuration file"})

    def reload_config(self):
        global login_error
        load_error = conf.load_config()

        if load_error:
            raise Exception(
                f"Can't recover from config load error: {load_error}")

        if conf.is_instance_configured():
            err = conf.login()
            if err:
                login_error = err
                log.error(f"Login error: {err}")
        else:
            login_error = None

        self.send_json_response(200, None)

    def send_json_response(self, status_code, data):
        response_data = json.dumps(data).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(response_data)))
        self.end_headers()
        self.wfile.write(response_data)

    def save_bounds_selection(self):
        # Get data from the request body
        data, error = self.parse_json_request()
        if error:
            log.error(f"Invalid bounds data: {error}")
            self.send_response(400)
            self.end_headers()
            return

        log.debug(f"Received bounds data: {data}")
        try:
            global bounds
            bounds = Bounds(data["x"], data["y"],
                            data["width"], data["height"])
        except KeyError as e:
            log.error(f"Missing bounds data: {e}")
            self.send_json_response(
                400, {"error": f"Missing bounds data: {str(e)}"})
            return

        self.send_response(200)
        self.end_headers()

        self.stop_server()

    def stop_server(self):
        threading.Thread(target=self.server.shutdown).start()

    def serve_screenshot(self):
        bytes_array = io.BytesIO()
        self.img.save(bytes_array, format='PNG')
        data = bytes_array.getvalue()

        self.send_response(200)
        self.send_header("Content-Type", "image/png")
        self.end_headers()
        self.wfile.write(data)


def _run_server(img: Image.Image, prompt_for_bounds: bool):
    HOST, PORT = "localhost", conf.BOUNDS_SERVER_PORT

    def create_server(*args, **kwargs):
        return BoundsServer(img, prompt_for_bounds, *args, **kwargs)
    with HTTPServer((HOST, PORT), create_server) as server:
        BOLD = "\033[1m"
        RESET = "\033[0m"
        log.info(
            f"{BOLD}Open http://{HOST}:{PORT}/ in your browser to configure instance and select bounds{RESET}")
        server.serve_forever()
