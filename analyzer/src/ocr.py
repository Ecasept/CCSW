from io import BytesIO
from analyze import analyze_values
from PIL import Image
from logger import log
import request
from config import conf
import base64


MOCK_DATA = {
    'goods': {
        'CRL': {'value': 25.44, 'bought': True},
        'CHC': {'value': 44.83, 'bought': True},
        'BTR': {'value': 40.48, 'bought': False},
        'SUG': {'value': 19.49, 'bought': True},
        'NUT': {'value': 25.07, 'bought': False},
        'SLT': {'value': 10.33, 'bought': True},
        'VNL': {'value': 132.46, 'bought': True},
        'EGG': {'value': 135.11, 'bought': False},
        'CNM': {'value': 41.16, 'bought': False},
        'CRM': {'value': 136.76, 'bought': True},
        'JAM': {'value': 97.89, 'bought': False},
        'WCH': {'value': 177.25, 'bought': False},
        'HNY': {'value': 13.74, 'bought': True},
        'CKI': {'value': 162.87, 'bought': False},
        'RCP': {'value': 110.16, 'bought': False},
        'SBD': {'value': 151.28, 'bought': False},
        'PBL': {'value': 170.4, 'bought': True},
        'YOU': {'value': 170.23, 'bought': False}
    }
}


def process_screenshot_callback(screenshot_image, timestamp):
    """
    Callback function to process a screenshot.

    Args:
        screenshot_image (PIL.Image): The screenshot image
        timestamp (datetime): When the screenshot was taken
    """
    if conf.MOCK_DATA:
        analyze_values(MOCK_DATA['goods'], timestamp)
        return
    return process_ai(screenshot_image, timestamp)


def to_base64(image: Image.Image) -> str:
    """
    Convert a PIL Image to a base64 encoded string.
    """
    buffer = BytesIO()
    return base64.b64encode(buffer.getvalue()).decode('utf-8')


def process_ai(screenshot_image: Image.Image, timestamp):

    screenshot_image.save("screenshot.png", format="PNG")

    data = {
        "image": to_base64(screenshot_image),
        "instanceId": conf.INSTANCE_ID,
    }
    res = request.post(
        conf.Endpoint.IMG_PROCESS, data, timeout=100).as_result()
    if not res.success:
        log.error(
            f"Failed to process image with AI. {res.error}")
        return

    analyze_values(res.data["goods"], timestamp)
