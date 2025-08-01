import upload
from simulation.simulation import get_quartiles
from config import conf

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


def sell_thresh(good: int):
    """Get the sell threshold for a good."""
    return get_quartiles()[good][MAPPING[conf.SELL_QUARTILE]]


def buy_thresh(good: int):
    """Get the buy threshold for a good."""
    return get_quartiles()[good][MAPPING[conf.BUY_QUARTILE]]


def analyze_values(values: list[int], bought: list[bool], timestamp):
    actions = []

    for i, value in enumerate(values):
        sell = sell_thresh(i)
        buy = buy_thresh(i)
        if value < buy and not bought[i]:
            actions.append({
                "good": i,
                "value": value,
                "thresh": buy,
                "type": "buy"
            })
        elif value > sell and bought[i]:
            actions.append({
                "good": i,
                "value": value,
                "thresh": sell,
                "type": "sell"
            })

    upload.push_values(values, bought, actions, timestamp)
