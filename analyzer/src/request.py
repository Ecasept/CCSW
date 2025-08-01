from io import BytesIO
import json
import pycurl
from logger import log


def post(url: str, data: dict, timeout: int = 10) -> tuple:
    """
    Sends a POST request to the specified URL with the given data.
    Args:
        url (str): The URL to send the POST request to.
        data (dict): The data to send in the request body.
    Returns:
        tuple: A tuple containing the HTTP status code and the parsed JSON response.
    """
    payload_json = json.dumps(data)

    buffer = BytesIO()
    c = pycurl.Curl()
    c.setopt(c.URL, url)
    c.setopt(c.POSTFIELDS, payload_json)
    c.setopt(c.HTTPHEADER, ['Content-Type: application/json'])
    c.setopt(c.WRITEDATA, buffer)
    c.setopt(c.TIMEOUT, timeout)

    log.debug(
        f"Sending data to server at {url} with payload: {payload_json[:100]}")
    c.perform()
    status_code = c.getinfo(pycurl.HTTP_CODE)
    response_body = buffer.getvalue().decode('utf-8')
    c.close()
    log.debug(f"Server response ({status_code}): {response_body[:100]}")

    try:
        parsed_body = json.loads(response_body)
    except json.JSONDecodeError as e:
        log.error(f"Failed to parse JSON response: {e}")
        log.debug(f"Response body was: {response_body}")
        parsed_body = None

    return status_code, parsed_body
