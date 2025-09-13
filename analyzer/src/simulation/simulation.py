import subprocess
from logger import log


def read_quartiles():
    with open("simulation/simulation_results.txt", "r") as file:
        data = file.read().strip()
        global _QUARTILES
        _QUARTILES = [[float(num) for num in string.split(" ")]
                      for string in data.split("\n")]
        log.info("Simulation data initialized successfully")


def init_sim_data():
    try:
        read_quartiles()
        return True
    except FileNotFoundError:
        if run_simulation():
            read_quartiles()
            return True
        return False


def get_quartiles():
    return _QUARTILES


_QUARTILES: list[list[float]] = []


def run_simulation():
    """
    This function runs the simulation.
    """

    # Check if tsx is installed
    process = subprocess.run(  # Change to the simulation directory
        ["npm", "list", "tsx"], capture_output=True, text=True, cwd="simulation")
    if "tsx@" not in process.stdout:
        log.error(
            "tsx is not installed. Please install go to the `simulation` directory and run `npm install`.")
        return False

    log.info("Running stock market simulation...")
    # Run the TypeScript simulation file
    process = subprocess.run(["npx", "tsx", "stockmarketsim.ts"],
                             cwd="simulation")
    if process.returncode != 0:
        log.error("Simulation failed to run.")
        return False
    return True
