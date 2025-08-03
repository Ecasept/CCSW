from io import BytesIO
from analyze import analyze_values
import numpy as np
from PIL import Image
from logger import log
import time
import request
from config import conf
import base64
if conf.USE_LEGACY_OCR:
    log.info("Importing OCR module")
    import easyocr

CONFIDENCE_THRESHOLD = 0.2

# Initialize EasyOCR reader (only needs to be done once)
reader = None

MOCK_DATA = {'bought': [True, True, False, True, False, True, True, False, False, True, False, False, True, False, False, False, True, False], 'values': [
    25.44, 44.83, 40.48, 19.49, 25.07, 10.33, 132.46, 135.11, 41.16, 136.76, 97.89, 177.25, 13.74, 162.87, 110.16, 151.28, 170.4, 170.23]}


def get_ocr_reader():
    """
    Get or initialize the EasyOCR reader.

    Returns:
        easyocr.Reader: The OCR reader instance
    """
    global reader
    if reader is None:
        log.info("Initializing EasyOCR reader...")
        # Initialize with English language support
        # You can add more languages like ['en', 'ch_sim'] for Chinese support
        # Set gpu=True if you have CUDA
        reader = easyocr.Reader(['en'], gpu=False)
        log.info("EasyOCR reader initialized successfully")
    return reader


def ocr_pil_image(image):
    """
    Performs OCR on a PIL Image object and returns the extracted text.

    Args:
        image (PIL.Image): PIL Image object

    Returns:
        str: Extracted text from the image
    """
    try:
        start = time.time()

        # Convert PIL image to numpy array for EasyOCR
        image_np = np.array(image)

        # Get OCR reader
        ocr_reader = get_ocr_reader()

        log.debug("Performing OCR on image...")

        # Perform OCR
        results = ocr_reader.readtext(image_np)

        # Extract text from results
        # EasyOCR returns a list of (bbox, text, confidence) tuples
        extracted_texts = []
        for (bbox, text, confidence) in results:
            # Only include text with reasonable confidence
            if confidence > CONFIDENCE_THRESHOLD:
                extracted_texts.append(text)

        # Join all extracted text with newlines
        text = '\n'.join(extracted_texts)

        end = time.time()
        log.debug(f"OCR processing time: {end - start:.2f} seconds")
        log.debug(
            f"EasyOCR found {len(results)} text regions, {len(extracted_texts)} with confidence > {CONFIDENCE_THRESHOLD}")
        return text

    except Exception as e:
        log.exception("Error performing OCR on PIL image")
        return ""


def process_screenshot_callback(screenshot_image, timestamp):
    """
    Callback function to process a screenshot.

    Args:
        screenshot_image (PIL.Image): The screenshot image
        timestamp (datetime): When the screenshot was taken
    """
    if conf.MOCK_DATA:
        analyze_values(MOCK_DATA['values'], MOCK_DATA['bought'], timestamp)
        return
    if conf.USE_LEGACY_OCR:
        return process_ocr(screenshot_image, timestamp)
    else:
        return process_ai(screenshot_image, timestamp)


def process_ocr(screenshot_image, timestamp):
    # Perform OCR on the image
    extracted_text = ocr_pil_image(screenshot_image)
    values = []
    bought = []

    # For all instances of "value:", extract the value
    for line in extracted_text.splitlines():
        if "value:" in line.lower():
            # Remove leading dollar sign
            value = line.split("value:")[-1].strip()[1:]
            if value.replace('.', '', 1).isdigit():
                values.append(float(value))
            else:
                log.warning(f"Found non-numeric value: {value}")
        elif "stock:" in line.lower():
            count = line.split("stock:")[-1].strip()
            if count.startswith("0"):
                bought.append(False)
            else:
                bought.append(True)

    if len(values) != conf.GOOD_COUNT:
        log.warning(
            f"Expected {conf.GOOD_COUNT} values, found {len(values)} - skipping")
        return
    if len(bought) != conf.GOOD_COUNT:
        log.warning(
            f"Expected {conf.GOOD_COUNT} stock counts, found {len(bought)} - skipping")
        return
    analyze_values(values, bought, timestamp)


def to_base64(image: Image.Image) -> str:
    """
    Convert a PIL Image to a base64 encoded string.
    """
    buffer = BytesIO()
    image.save(buffer, format="PNG")
    return base64.b64encode(buffer.getvalue()).decode('utf-8')


def process_ai(screenshot_image, timestamp):
    data = {
        "image": to_base64(screenshot_image),
    }
    status_code, response_body = request.post(
        conf.Endpoint.IMG_PROCESS, data, timeout=100)
    if status_code >= 400:
        log.error(
            f"Failed to process image with AI. Status: {status_code}, Response: {response_body}")
        return
    success = response_body.get("success", False)
    if not success:
        log.error(
            f"AI processing failed: {response_body.get('error', 'Unknown error')}")
        return
    data = response_body["data"]

    analyze_values(
        data["values"],
        data["bought"],
        timestamp
    )
