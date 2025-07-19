import requests
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
        
        # Make the POST request to the server
        url = conf.SERVER_URL + "/api/data"
        log.debug(f"Sending data to {url}")
        
        response = requests.post(url, json=payload, timeout=10)
        response.raise_for_status()  # Raise an exception for bad status codes
        
        log.info(f"Successfully sent data to server. Response: {response.status_code}")
        log.debug(f"Server response: {response.text}")
        
    except requests.exceptions.RequestException as e:
        log.exception(f"Failed to send data to server")
    except Exception as e:
        log.exception("Unexpected error when sending data to server")
