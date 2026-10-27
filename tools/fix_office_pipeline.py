#!/usr/bin/env python3
"""One-shot office decompiler fix pipeline."""
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PY = sys.executable


def run(script: str):
    print(f"==> {script}")
    subprocess.check_call([PY, str(ROOT / script)])


def main():
    run("fix_decompiled_garbage.py")
    run("fix_bridge_methods.py")
    run("fix_decompiled_phase2_safe.py")
    run("fix_decompiled_garbage.py")
    print("Pipeline complete. Apply manual CellFormatType/SubdocumentType/IOffice fixes if needed.")


if __name__ == "__main__":
    main()
