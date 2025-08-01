import pycurl
import json
from io import BytesIO
from datetime import datetime
from config import conf
from logger import log
import request


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

        status_code, _ = request.post(conf.Endpoint.UPDATE, payload)

        if status_code >= 400:
            log.error(
                f"Failed to send data to server. Status: {status_code}, Response: {response_body}")

    except pycurl.error as e:
        log.exception(f"Failed to send data to server: {e}")
    except Exception:
        log.exception("Unexpected error when sending data to server")
