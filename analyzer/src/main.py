
import os
# Set cwd to the script's directory
os.chdir(os.path.dirname(os.path.abspath(__file__)))

import ocr  # noqa: E402
import screenshot  # noqa: E402
from logger import log  # noqa: E402
from select_server.server import get_bounds, Bounds  # noqa: E402
from simulation.simulation import init_sim_data  # noqa: E402
from config import conf  # noqa: E402


def main():
    """Main function to start the Cookie Clicker bot with logging."""
    log.info("Starting CCSW Bot")

    try:
        if not init_sim_data():
            return
        bounds = Bounds(0, 0, 0, 0) if conf.MOCK_DATA else get_bounds()
        screenshot.start_screenshot_loop(
            bounds, ocr.process_screenshot_callback)
    except KeyboardInterrupt:
        log.info("Bot stopped by user (Ctrl+C)")
    except Exception as e:
        log.exception("Unexpected error occurred")
    finally:
        log.info("CCSW Bot shutdown complete")
