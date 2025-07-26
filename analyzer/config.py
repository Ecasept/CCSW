import pyjson5
CONF_FILE = "../config.jsonc"


class Config:
    SELL_QUARTILE: float
    BUY_QUARTILE: float
    USER_ID: str
    SERVER_URL: str
    CHECK_INTERVAL: int
    USE_TEST_IMAGE: bool

    class Endpoints:
        def __init__(self, server_url):
            self.UPDATE = f"{server_url}/api/update"
    Endpoint: Endpoints

    def __init__(self, conf_file=CONF_FILE):
        self.load_config(conf_file)

    def load_config(self, conf_file):
        with open(conf_file, "r") as file:
            config = pyjson5.load(file)

            # Trading thresholds
            self.SELL_QUARTILE = config["sellQuartile"]
            self.BUY_QUARTILE = config["buyQuartile"]

            # System settings
            self.USER_ID = config["userId"]
            self.SERVER_URL = config["serverUrl"]
            self.CHECK_INTERVAL = config["checkInterval"]
            self.USE_TEST_IMAGE = config["useTestImage"]

            # Endpoints
            self.Endpoint = self.Endpoints(self.SERVER_URL)


conf = Config()
