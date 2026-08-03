#!/usr/bin/env python3
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office"
IA_PARAM = re.compile(r",\s*\w+IA\s+\w+IA\b")


def main():
    changed = 0
    for path in ROOT.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        updated = IA_PARAM.sub("", text)
        updated = re.sub(r",\s*null\)", ")", updated)
        if updated != text:
            path.write_text(updated, encoding="utf-8")
            changed += 1
    print(f"Updated {changed} files")


if __name__ == "__main__":
    main()
