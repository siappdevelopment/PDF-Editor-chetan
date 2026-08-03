#!/usr/bin/env python3
"""Safe second-pass fixes (imports + synthetic constructors only)."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office"

BAD_IMPORT_PREFIXES = (
    "com.google.firebase.",
    "com.facebook.",
    "com.google.api.",
    "org.chromium.",
    "org.jacoco.",
    "androidx.profileinstaller.",
    "androidx.core.view.PointerIconCompat",
    "androidx.constraintlayout.core.",
    "androidx.webkit.",
    "com.google.gson.",
)

def main():
    changed = 0
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        lines = []
        for line in original.splitlines():
            if line.startswith("import "):
                if any(prefix in line for prefix in BAD_IMPORT_PREFIXES):
                    continue
            lines.append(line)
        updated = "\n".join(lines) + ("\n" if original.endswith("\n") else "")
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed += 1
    print(f"Updated {changed} files")


if __name__ == "__main__":
    main()
