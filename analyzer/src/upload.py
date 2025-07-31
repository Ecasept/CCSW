import pycurl
import json
from io import BytesIO
from datetime import datetime
from config import conf
from logger import log


def push_values(vals: list[int], bought: list[bool], actions: list[dict], timestamp: datetime):
    """
    Push values, bought status, actions, and timestamp to the server.

    Args:
        vals: List of current stock values
        bought: List of boolean indicating if stock is owned
        actions: List of recommended actions
        timestamp: Datetime object representing when the screenshot was taken
    """
    try:
        # Prepare the data payload
        payload = {
            "userId": conf.USER_ID,
            "timestamp": timestamp.isoformat(),
            "values": vals,
            "bought": bought,
            "actions": actions
        }
        payload_json = json.dumps(payload)

        # Make the POST request to the server using pycurl
        url = conf.Endpoint.UPDATE
        buffer = BytesIO()
        c = pycurl.Curl()
        c.setopt(c.URL, url)
        c.setopt(c.POSTFIELDS, payload_json)
        c.setopt(c.HTTPHEADER, ['Content-Type: application/json'])
        c.setopt(c.WRITEDATA, buffer)
        c.setopt(c.TIMEOUT, 10)

        log.debug(f"Sending data to server at {url} with payload: {payload}")
        c.perform()
        status_code = c.getinfo(pycurl.HTTP_CODE)
        response_body = buffer.getvalue().decode('utf-8')
        c.close()

        log.debug(f"Server response ({status_code}): {response_body}")

        if status_code >= 400:
            log.error(f"Failed to send data to server. Status: {status_code}, Response: {response_body}")

    except pycurl.error as e:
        log.exception(f"Failed to send data to server: {e}")
    except Exception:
        log.exception("Unexpected error when sending data to server")