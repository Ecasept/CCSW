import upload
from simulation.simulation import get_quartiles
from config import conf
from enum import Enum
from logger import log

MAPPING = {
    0.01: 0,
    0.05: 1,
    0.1: 2,
    0.25: 3,
    0.5: 4,
    0.75: 5,
    0.9: 6,
    0.95: 7,
    0.99: 8
}


class GoodState(Enum):
    SHOULD_WAIT = 0
    SHOULD_BUY = 1
    SHOULD_SELL = 2


# Fixed symbol ordering matching app/server
SYMBOLS = [
    "CRL", "CHC", "BTR", "SUG", "NUT", "SLT", "VNL", "EGG", "CNM",
    "CRM", "JAM", "WCH", "HNY", "CKI", "RCP", "SBD", "PBL", "YOU"
]
SYMBOL_TO_INDEX = {s: i for i, s in enumerate(SYMBOLS)}

# Track previous state by symbol
prev_good_state = {s: GoodState.SHOULD_WAIT for s in SYMBOLS}


def sell_thresh(index: int):
    """Get the sell threshold for a good by index."""
    return get_quartiles()[index][MAPPING[conf.SELL_QUARTILE]]


def buy_thresh(index: int):
    """Get the buy threshold for a good by index."""
    return get_quartiles()[index][MAPPING[conf.BUY_QUARTILE]]


def analyze_values(goods: dict, timestamp):
    """
    Analyzes the values of goods and determines the notifications to send to the user.

    Args:
        goods: Map keyed by symbol to a dict with 'value' and 'bought' keys
        timestamp: Datetime object representing when the screenshot was taken
    """
    actions = []

    for symbol, data in goods.items():
        index = SYMBOL_TO_INDEX.get(symbol)
        if index is None:
            log.warn(f"Symbol {symbol} not recognized, skipping analysis.")
            continue
        value, bought = data['value'], data['bought']
        sell_threshold = sell_thresh(index)
        buy_threshold = buy_thresh(index)
        prev_state = prev_good_state[symbol]
        if value < buy_threshold:
            cur_state = GoodState.SHOULD_BUY
        elif value > sell_threshold:
            cur_state = GoodState.SHOULD_SELL
        else:
            cur_state = GoodState.SHOULD_WAIT

        def action(threshold: float, action_type: str):
            actions.append({
                "symbol": symbol,
                "value": value,
                "thresh": threshold,
                "type": action_type
            })

        if bought:
            if prev_state != GoodState.SHOULD_SELL and cur_state == GoodState.SHOULD_SELL:
                # Good changed to SHOULD_SELL so post a send action
                action(sell_threshold, "sell")
            elif prev_state == GoodState.SHOULD_SELL and cur_state != GoodState.SHOULD_SELL:
                # Good went out of SHOULD_SELL state, so post a missed sell action
                action(sell_threshold, "missed_sell")
        else:
            if prev_state != GoodState.SHOULD_BUY and cur_state == GoodState.SHOULD_BUY:
                # Good changed to SHOULD_BUY so post a buy action
                action(buy_threshold, "buy")
            elif prev_state == GoodState.SHOULD_BUY and cur_state != GoodState.SHOULD_BUY:
                # Good went out of SHOULD_BUY state, so post a missed buy action
                action(buy_threshold, "missed_buy")
        prev_good_state[symbol] = cur_state

    upload.push_values(goods, actions, timestamp)
