import sys
import os
SRC_DIR_NAME = "src"
this_dir = os.path.dirname(os.path.abspath(__file__))
src_dir = os.path.join(this_dir, SRC_DIR_NAME)
# Make it so that modules in the src directory can import each other without the `src.` prefix
sys.path.append(src_dir)
# Change the current working directory to the src directory
os.chdir(src_dir)
import src.main  # noqa: E402
if __name__ == "__main__":
    src.main.main()
