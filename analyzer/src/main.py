from config import conf  # noqa: E402
err = conf.load_config()
if err:
    print(f"Error loading config: {err}")
    exit(1)

import ocr  # noqa: E402
import screenshot  # noqa: E402
from logger import log  # noqa: E402
from select_server import run_server, Bounds  # noqa: E402
from simulation.simulation import init_sim_data  # noqa: E402
from config import conf  # noqa: E402


def main():
    """Main function to start the Cookie Clicker bot with logging."""
    log.info("Starting CCSW Bot")

    try:
        if not init_sim_data():
            return
        bounds = run_server(screenshot.take_screenshot, not conf.MOCK_DATA)
        screenshot.start_screenshot_loop(
            bounds, ocr.process_screenshot_callback)
    except KeyboardInterrupt:
        log.info("Bot stopped by user (Ctrl+C)")
    except Exception:
        log.exception("Unexpected error occurred")
    finally:
        log.info("CCSW Bot shutdown complete")
