import io
import threading
from .bounds import Bounds
from screenshot import take_screenshot
from PIL import Image
from http.server import BaseHTTPRequestHandler, HTTPServer
from datetime import datetime
from config import conf
from logger import log
import json

bounds: Bounds = None


def get_bounds():
    img: Image.Image = None
    timestamp: datetime = None

    def callback(_img, _timestamp):
        nonlocal img, timestamp
        img, timestamp = _img, _timestamp
    take_screenshot(callback)
    run_server(img)
    return bounds


def get_bounds_html():
    PATH = "select_server/bounds.html"
    with open(PATH, "br") as file:
        html_content = file.read()
    return html_content


class BoundsServer(BaseHTTPRequestHandler):
    def __init__(self,
                 img: Image.Image,
                 *args, **kwargs):
        self.img = img
        super().__init__(*args, **kwargs)

    def do_GET(self):
        log.debug(f"Received GET request for {self.path}")
        if self.path == "/bounds":
            self.serve_bounds_selection()
        elif self.path == "/img":
            self.serve_screenshot()
        elif self.path == "/":
            # Redirect to bounds selection page
            self.send_response(302)
            self.send_header("Location", "/bounds")
            self.end_headers()

    def do_POST(self):
        log.debug(f"Received POST request for {self.path}")
        if self.path == "/bounds":
            self.save_bounds_selection()

    def serve_bounds_selection(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/html")
        self.end_headers()
        html_content = get_bounds_html()
        self.wfile.write(html_content)

    def save_bounds_selection(self):
        # Get data from the request body
        content_length = int(self.headers["Content-Length"])
        post_data = self.rfile.read(content_length).decode("utf-8")
        log.debug(f"Received bounds data: {post_data}")
        try:
            data = json.loads(post_data)
            global bounds
            bounds = Bounds(data["x"], data["y"],
                            data["width"], data["height"])
        except (json.JSONDecodeError, KeyError) as e:
            log.error(f"Invalid bounds data: {e}")
            self.send_response(400)
            self.end_headers()
            return
        self.send_response(200)
        self.end_headers()

        # Shutdown the server
        threading.Thread(target=self.server.shutdown).start()

    def serve_screenshot(self):
        # Get PNG bytes
        bytes_array = io.BytesIO()
        self.img.save(bytes_array, format='PNG')
        data = bytes_array.getvalue()

        self.send_response(200)
        self.send_header("Content-Type", "image/png")
        self.end_headers()
        self.wfile.write(data)


def run_server(img: Image.Image):
    HOST, PORT = "localhost", conf.BOUNDS_SERVER_PORT
    create_server = lambda *args, **kwargs: BoundsServer(img, *args, **kwargs)
    with HTTPServer((HOST, PORT), create_server) as server:
        BOLD = "\033[1m"
        RESET = "\033[0m"
        log.info(
            f"{BOLD}Open http://{HOST}:{PORT}/bounds in your browser to select bounds{RESET}")
        server.serve_forever()
