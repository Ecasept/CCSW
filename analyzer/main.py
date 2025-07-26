from simulation.simulation import init_sim_data
from logger import log
import screenshot

log.info("Importing OCR module")
import ocr  # noqa: E402

# Set cwd to the script's directory
import os
os.chdir(os.path.dirname(os.path.abspath(__file__)))

def main():
    """Main function to start the Cookie Clicker bot with logging."""
    log.info("Starting CCSW Bot")

    try:
        if not init_sim_data():
            return
        screenshot.start_screenshot_loop(
            callback=ocr.process_screenshot_callback)
    except KeyboardInterrupt:
        log.info("Bot stopped by user (Ctrl+C)")
    except Exception as e:
        log.exception("Unexpected error occurred")
    finally:
        log.info("CCSW Bot shutdown complete")


if __name__ == "__main__":
    main()
