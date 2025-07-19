import logging
import os
from datetime import datetime
from pathlib import Path


class Logger:
    """
    A simple logging wrapper that provides easy-to-use logging functionality.
    Supports both console and file logging with configurable levels.
    """

    def __init__(self, name="CookieClickerBot", level=logging.INFO, log_to_file=True, log_dir="logs"):
        self.logger = logging.getLogger(name)
        self.logger.setLevel(level)

        # Clear existing handlers to avoid duplicates
        self.logger.handlers.clear()

        # Create formatter
        formatter = logging.Formatter(
            '%(asctime)s - %(name)s - %(levelname)s - %(message)s',
            datefmt='%Y-%m-%d %H:%M:%S'
        )

        # Console handler
        console_handler = logging.StreamHandler()
        console_handler.setLevel(level)
        console_handler.setFormatter(formatter)
        self.logger.addHandler(console_handler)

        # File handler (optional)
        if log_to_file:
            # Create logs directory if it doesn't exist
            log_path = Path(log_dir)
            log_path.mkdir(exist_ok=True)

            # Create log file with timestamp
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            log_file = log_path / f"{name}_{timestamp}.log"

            file_handler = logging.FileHandler(log_file)
            file_handler.setLevel(level)
            file_handler.setFormatter(formatter)
            self.logger.addHandler(file_handler)

            self.info(f"Logging to file: {log_file}")

    def debug(self, message):
        """Log a debug message"""
        self.logger.debug(message)

    def info(self, message):
        """Log an info message"""
        self.logger.info(message)

    def warning(self, message):
        """Log a warning message"""
        self.logger.warning(message)

    def warn(self, message):
        """Alias for warning()"""
        self.warning(message)

    def error(self, message):
        """Log an error message"""
        self.logger.error(message)

    def critical(self, message):
        """Log a critical message"""
        self.logger.critical(message)

    def exception(self, message):
        """Log an exception with traceback"""
        self.logger.exception(message + ":")

    def set_level(self, level):
        """Change the logging level"""
        self.logger.setLevel(level)
        for handler in self.logger.handlers:
            handler.setLevel(level)


# Create a default logger instance that can be imported directly
log = Logger(level=logging.DEBUG)
