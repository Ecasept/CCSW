import upload
from simulation.simulation import get_quartiles
from config import conf
from enum import Enum

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


prev_good_state = [GoodState.SHOULD_WAIT] * conf.GOOD_COUNT


def sell_thresh(good: int):
    """Get the sell threshold for a good."""
    return get_quartiles()[good][MAPPING[conf.SELL_QUARTILE]]


def buy_thresh(good: int):
    """Get the buy threshold for a good."""
    return get_quartiles()[good][MAPPING[conf.BUY_QUARTILE]]


def analyze_values(goods: list[dict], timestamp):
    """
    Analyzes the values of goods and determines the notifications to send to the user.

    Args:
        goods: List of dictionaries with 'value' and 'bought' keys
        timestamp: Datetime object representing when the screenshot was taken
    """
    actions = []

    for i, good in enumerate(goods):
        value, bought = good['value'], good['bought']
        sell_threshold = sell_thresh(i)
        buy_threshold = buy_thresh(i)
        prev_state = prev_good_state[i]
        cur_state = None
        if value < buy_threshold:
            cur_state = GoodState.SHOULD_BUY
        elif value > sell_threshold:
            cur_state = GoodState.SHOULD_SELL
        else:
            cur_state = GoodState.SHOULD_WAIT

        def action(threshold: float, action_type: str):
            actions.append({
                "good": i,
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
        prev_good_state[i] = cur_state

    upload.push_values(goods, actions, timestamp)
