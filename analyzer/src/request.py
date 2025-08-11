from dataclasses import dataclass
from io import BytesIO
import json
from typing import Any
import pycurl
from logger import log
from config import conf


@dataclass
class Result:
    success: bool
    error: str = None
    data: Any = None


class Response:
    def __init__(self, status_code: int, parsed_json: Any, error_message: str):
        self.status_code = status_code
        self.parsed_json = parsed_json
        self.error_message = error_message

    def get_response_error(self):
        # Check type of parsed json
        if not isinstance(self.parsed_json, dict):
            return "Response is not a valid JSON object"
        success = self.parsed_json.get("success", None)
        if success is None:
            return "Response does not contain 'success' key"
        if not success:
            if "error" not in self.parsed_json:
                return "Response does not contain 'error' key"
            msg = self.parsed_json["error"]
            if self.status_code is not None and self.status_code != 200:
                return f"Error {self.status_code}: {msg}"
            return f"Error: {msg}"
        return None

    def as_result(self) -> Result:
        if self.error_message:
            if self.status_code is not None and self.status_code != 200:
                return Result(
                    success=False,
                    error=f"Error {self.status_code}: {self.error_message}"
                )
            return Result(success=False, error=self.error_message)
        err = self.get_response_error()
        if err:
            return Result(success=False, error=err)
        if "data" not in self.parsed_json:
            return Result(success=False, error="Response does not contain 'data' key")
        return Result(success=True, data=self.parsed_json["data"])


def post(url: str, data: dict, timeout: int = 10, auth: bool = True) -> Response:
    """
    Sends a POST request to the specified URL with the given data.
    Args:
        url (str): The URL to send the POST request to.
        data (dict): The data to send in the request body.
    """
    payload_json = json.dumps(data)

    buffer = BytesIO()
    c = pycurl.Curl()
    c.setopt(c.URL, url)
    c.setopt(c.POSTFIELDS, payload_json)
    c.setopt(c.USERAGENT, conf.USER_AGENT)

    headers = ["Content-Type: application/json"]

    if auth:
        headers.append(f"Authorization: Bearer {conf.SESSION_TOKEN}")

    c.setopt(c.HTTPHEADER, headers)
    c.setopt(c.WRITEDATA, buffer)
    c.setopt(c.TIMEOUT, timeout)

    log.debug(
        f"Sending data to server at {url} with payload: {payload_json[:100]}")
    try:
        c.perform()
        status_code = c.getinfo(pycurl.HTTP_CODE)
        response_body = buffer.getvalue().decode('utf-8')
    except pycurl.error as e:
        log.error(f"Request failed: {e}")
        c.close()
        return Response(None, None, f"Request failed: {e}")
    c.close()
    log.debug(f"Server response ({status_code}): {response_body[:100]}")

    try:
        parsed_body = json.loads(response_body)
    except json.JSONDecodeError as e:
        log.error(f"Failed to parse JSON response: {e}")
        log.debug(f"Response body was: {response_body}")
        return Response(status_code, None, f"Failed to parse json: {response_body}")

    return Response(status_code, parsed_body, None)
