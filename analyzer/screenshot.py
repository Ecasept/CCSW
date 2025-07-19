import time
from datetime import datetime
from PIL import ImageGrab, Image
import os
from logger import log
from config import conf


def get_test_image():
    return Image.open("test.png")


def get_screenshot():
    if conf.USE_TEST_IMAGE:
        return get_test_image()
    else:
        return ImageGrab.grab()


def take_screenshot(callback=None):
    """
    Takes a screenshot of the entire screen and saves it with a timestamp.

    Args:
        callback (function, optional): Callback function to process the screenshot.
                                     Should accept image and timestamp as parameters.
    """
    try:
        # Capture the timestamp when screenshot is taken
        timestamp = datetime.now()
        
        # Grab the screen
        log.debug("Taking screenshot of entire screen")
        screenshot = get_screenshot()
        log.debug("Screenshot captured successfully")

        # Call the callback if provided
        if callback:
            log.debug("Processing screenshot with callback function")
            callback(screenshot, timestamp)
        else:
            log.debug("No callback provided, screenshot taken only")

    except Exception as e:
        log.exception("Failed to take screenshot")


def start_screenshot_loop(callback=None):
    """
    Starts a loop to take a screenshot at a regular interval.

    Args:
        callback (function, optional): Callback function to process each screenshot.
                                     Should accept image and timestamp as parameters.
    """
    screenshot_count = 0

    while True:
        screenshot_count += 1
        log.info(f"Taking screenshot #{screenshot_count}")
        take_screenshot(callback)
        log.info(
            f"Waiting {conf.CHECK_INTERVAL} seconds until next screenshot...")
        time.sleep(conf.CHECK_INTERVAL)
