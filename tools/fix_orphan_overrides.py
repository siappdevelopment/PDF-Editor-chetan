#!/usr/bin/env python3
"""Remove @Override annotations left without methods after bridge stripping."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office"

OVERRIDE_ONLY_RE = re.compile(
    r"^[ \t]*@Override[^\n]*\n(?=[ \t]*(@Override|static\s*\{|public |private |protected |class |interface |enum |\}))",
    re.MULTILINE,
)


def main():
    changed = 0
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        updated = original
        prev = None
        while prev != updated:
            prev = updated
            updated = OVERRIDE_ONLY_RE.sub("", updated)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed += 1
    print(f"Updated {changed} files")


if __name__ == "__main__":
    main()
