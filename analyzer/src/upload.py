import pycurl
import json
from io import BytesIO
from datetime import datetime
from config import conf
from logger import log
import request


def push_values(goods: list[dict], actions: list[dict], timestamp: datetime):
    """
    Push combined goods data, actions, and timestamp to the server.

    Args:
        goods: List of dictionaries with `value` and `bought` keys
        actions: List of recommended actions
        timestamp: Datetime object representing when the screenshot was taken
    """
    try:
        # Prepare the data payload
        payload = {
            "userId": conf.USER_ID,
            "timestamp": timestamp.isoformat(),
            "goods": goods,
            "actions": actions
        }

        status_code, response_body = request.post(conf.Endpoint.UPDATE, payload)

        if status_code >= 400:
            log.error(
                f"Failed to send data to server. Status: {status_code}, Response: {response_body}")

    except pycurl.error as e:
        log.exception(f"Failed to send data to server: {e}")
    except Exception:
        log.exception("Unexpected error when sending data to server")
