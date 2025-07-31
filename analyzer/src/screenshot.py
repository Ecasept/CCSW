import time
from datetime import datetime
from PIL import ImageGrab, Image
import os
from select_server.bounds import Bounds
from logger import log
from config import conf


def get_test_image():
    return Image.open("../test.png")


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
        timestamp = datetime.now().astimezone()

        # Grab the screen
        screenshot = get_screenshot()
        log.debug(f"Screenshot taken at {timestamp.isoformat()}")

        # Call the callback if provided
        if callback:
            callback(screenshot, timestamp)

    except Exception as e:
        log.exception("Failed to take screenshot")


def crop_screenshot(image: Image.Image, bounds: Bounds) -> Image:
    return image.crop((
        bounds.x, bounds.y,
        bounds.x + bounds.width,
        bounds.y + bounds.height
    ))


def start_screenshot_loop(bounds: Bounds, callback):
    """
    Starts a loop to take a screenshot at a regular interval.

    Args:
        callback (function): Callback function to process each screenshot.
                                     Should accept image and timestamp as parameters.
    """
    screenshot_count = 0

    def cb(image, timestamp):
        cropped = crop_screenshot(image, bounds)
        callback(cropped, timestamp)

    while True:
        screenshot_count += 1
        log.info(f"Taking screenshot #{screenshot_count}")
        take_screenshot(cb)
        log.info(
            f"Waiting {conf.CHECK_INTERVAL} seconds until next screenshot...")
        time.sleep(conf.CHECK_INTERVAL)
