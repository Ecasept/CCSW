from datetime import datetime
from config import conf
from logger import log
import request


def push_values(goods: dict, actions: list[dict], timestamp: datetime):
    """
    Push combined goods data, actions, and timestamp to the server.

    Args:
        goods: Map keyed by symbol with `value` and `bought` keys
        actions: List of recommended actions
        timestamp: Datetime object representing when the screenshot was taken
    """
    # Prepare the data payload
    payload = {
        "instanceId": conf.INSTANCE_ID,
        "timestamp": timestamp.isoformat(),
        "goods": goods,
        "actions": actions
    }

    res = request.post(
        conf.Endpoint.UPDATE, payload).as_result()

    if not res.success:
        log.error(f"Failed to push values to server: {res.error}")
