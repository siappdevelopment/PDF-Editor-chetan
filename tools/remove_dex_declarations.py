#!/usr/bin/env python3
"""Remove auto-inserted dex register declaration blocks."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office"
PATTERN = re.compile(
    r"\n\s*// dex registers\n(?:\s*(?:float|int|long|double|boolean|byte|short|char|[\w$.]+)\s+r\d+(?:\s*=\s*[^;]+)?\s*;\s*)+",
    re.MULTILINE,
)


def main():
    changed = 0
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        updated = PATTERN.sub("\n", original)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed += 1
            print(path.relative_to(ROOT.parent.parent.parent.parent))
    print(f"Removed dex declarations from {changed} files")


if __name__ == "__main__":
    main()
