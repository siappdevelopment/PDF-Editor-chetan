#!/usr/bin/env python3
"""Add null second argument to single-arg actionEvent() calls."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office"
PATTERN = re.compile(r"\.actionEvent\(([^,)]+)\)")

def fix_content(content: str) -> tuple[str, int]:
    count = 0

    def repl(match):
        nonlocal count
        count += 1
        return f".actionEvent({match.group(1)}, null)"

    return PATTERN.sub(repl, content), count

def main():
    total = 0
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8", errors="replace")
        fixed, count = fix_content(original)
        if count:
            path.write_text(fixed, encoding="utf-8")
            total += count
            print(f"{path.relative_to(ROOT)}: {count}")
    print(f"Fixed {total} actionEvent calls")

if __name__ == "__main__":
    main()
