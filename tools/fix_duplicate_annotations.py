#!/usr/bin/env python3
"""Collapse consecutive duplicate @Override/@NonNull lines left by bridge stripping."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office"


def dedupe_lines(content: str) -> str:
    lines = content.splitlines(keepends=True)
    out = []
    prev_stripped = None
    for line in lines:
        stripped = line.strip()
        if stripped.startswith("@Override") or stripped.startswith("@NonNull"):
            key = stripped.split("//", 1)[0].strip()
            if key == prev_stripped:
                continue
            prev_stripped = key
        else:
            prev_stripped = None
        out.append(line)
    return "".join(out)


def main():
    changed = 0
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        updated = dedupe_lines(original)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed += 1
    print(f"Updated {changed} files")


if __name__ == "__main__":
    main()
