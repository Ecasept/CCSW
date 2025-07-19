import screenshot
import ocr
from logger import log
from simulation.simulation import init_sim_data


def main():
    """Main function to start the Cookie Clicker bot with logging."""
    log.info("Starting Cookie Clicker Stock Market Bot")

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
        log.info("Cookie Clicker Stock Market Bot shutdown complete")


if __name__ == "__main__":
    main()
