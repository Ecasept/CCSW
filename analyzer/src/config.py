import json5
from json5.loader import load, ModelLoader
from json5.dumper import dump, ModelDumper
from json5.model import JSONText, JSONObject, DoubleQuotedString
import os

CONF_FILE = "../../config.jsonc"


class Config:
    SELL_QUARTILE: float
    BUY_QUARTILE: float
    SERVER_URL: str
    CHECK_INTERVAL: int
    USE_TEST_IMAGE: bool
    BOUNDS_SERVER_PORT: int
    MOCK_DATA: bool
    INSTANCE_ID: str
    API_KEY: str
    SESSION_TOKEN: str
    USER_AGENT: str

    GOOD_COUNT = 18

    class Endpoints:
        def __init__(self, server_url):
            self.UPDATE = f"{server_url}/api/update"
            self.IMG_PROCESS = f"{server_url}/api/process"
            self.CREATE_INSTANCE = f"{server_url}/api/auth/instance"
            self.CREATE_SESSION = f"{server_url}/api/auth/session"
    Endpoint: Endpoints

    def __init__(self, conf_file=CONF_FILE):
        self.config_file = conf_file

    def load_config(self):
        return self._load_config(self.config_file)

    def _set_config_prop(self, config, attr_name, json_name, default=None):
        """
        Set a configuration property from the loaded JSON config.
        If the property is not found, it calls the `default` function
        if it is not `None`, or raises an error if `default` is `None`.
        """
        if json_name in config:
            setattr(self, attr_name, config[json_name])
        elif default is not None:
            setattr(self, attr_name, default())
        else:
            self.loading_error = f"Error: Missing configuration property '{json_name}'. Please edit the config file."

    def _load_config(self, conf_file):
        self.loading_error = None
        try:
            with open(conf_file, "r") as file:
                config = json5.load(file)
        except FileNotFoundError:
            return f"Error: Configuration file '{conf_file}' not found."

        # Trading thresholds
        self._set_config_prop(config, "SELL_QUARTILE", "sellQuartile")
        self._set_config_prop(config, "BUY_QUARTILE", "buyQuartile")

        # System settings
        self._set_config_prop(config, "SERVER_URL", "serverUrl")
        self._set_config_prop(config, "CHECK_INTERVAL", "checkInterval")
        self._set_config_prop(config, "USE_TEST_IMAGE", "useTestImage")
        self._set_config_prop(
            config, "BOUNDS_SERVER_PORT", "boundsServerPort")
        self._set_config_prop(config, "MOCK_DATA", "mockData")
        self._set_config_prop(config, "USER_AGENT", "userAgent")

        # Instance configuration
        self._set_config_prop(config, "INSTANCE_ID",
                              "instanceId", default=lambda: None)
        self._set_config_prop(config, "API_KEY",
                              "apiKey", default=lambda: None)

        # Endpoints
        self.Endpoint = self.Endpoints(self.SERVER_URL)
        return self.loading_error

    def is_instance_configured(self):
        """
        Check if instance ID and API key are configured.
        """
        return (self.INSTANCE_ID is not None and self.API_KEY is not None)

    def add_instance_config(self, instance_id, api_key):
        """
        Add instance ID and API key to the configuration file.
        """
        success = True
        success &= self.add_property("instanceId", instance_id)
        success &= self.add_property("apiKey", api_key)

        if success:
            # Reload the config to update the current instance
            self.load_config()
        return success

    def add_property(self, name, str_value):
        """
        Edits the json configuration file to add a new string property.
        The property is added as a new key-value pair to a singular JSON object.
        """
        with open(self.config_file, "r") as file:
            model = load(file, loader=ModelLoader())
        if type(model) is not JSONText:
            print("Error: Could not parse the config file.")
            return False
        obj = model.value
        if type(obj) is not JSONObject:
            print("Error: The root of the config file is not a JSON object.")
            return False
        if len(obj.keys) <= 0 or len(obj.values) <= 0:
            print("Error: No keys or values found in the JSON object.")
            return False

        prev_key = obj.keys[-1]
        prev_val = obj.values[-1]

        # Create a new key-value pair
        key = DoubleQuotedString(
            characters=name, raw_value=f'"{name}"')
        # Use the same whitespace as the previous key
        key.wsc_before = prev_key.wsc_before
        key.wsc_after = ""

        value = DoubleQuotedString(
            characters=str_value, raw_value=f'"{str_value}"')
        value.wsc_before = " "

        # Move the whitespace after the previous value to after the new value
        value.wsc_after = prev_val.wsc_after
        prev_val.wsc_after = ""

        obj.keys.append(key)
        obj.values.append(value)

        with open(self.config_file, "w") as file:
            dump(model, file, dumper=ModelDumper())
        return True

    def login(self) -> str | None:
        """
        Login to the server using the instance ID and API key.
        Sets the session token if successful.
        Returns:
            str | None: An error message if login fails, or None if successful.
        """
        if not self.is_instance_configured():
            return "Instance ID or API key are not configured."

        import request
        res = request.post(
            self.Endpoint.CREATE_SESSION,
            {
                "instanceId": self.INSTANCE_ID,
                "apiKey": self.API_KEY,
                "type": "apiKey"
            },
            auth=False
        ).as_result()
        if not res.success:
            return res.error
        self.SESSION_TOKEN = res.data
        if not self.SESSION_TOKEN:
            return "Session token not found in response."
        return None


conf = Config()
