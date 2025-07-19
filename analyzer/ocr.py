from analyze import analyze_values
import easyocr
import numpy as np
from PIL import Image
import os
from logger import log
import time
import server

GOOD_COUNT = 18
CONFIDENCE_THRESHOLD = 0.2

# Initialize EasyOCR reader (only needs to be done once)
reader = None


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
    Callback function to process a screenshot with OCR.

    Args:
        screenshot_image (PIL.Image): The screenshot image
        timestamp (datetime): When the screenshot was taken
    """

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

    if len(values) != GOOD_COUNT:
        log.warning(
            f"Expected {GOOD_COUNT} values, found {len(values)} - skipping")
        return
    if len(bought) != GOOD_COUNT:
        log.warning(
            f"Expected {GOOD_COUNT} stock counts, found {len(bought)} - skipping")
        return
    analyze_values(values, bought, timestamp)
