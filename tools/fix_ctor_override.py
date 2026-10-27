#!/usr/bin/env python3
"""Remove orphan @Override annotations placed before constructors by jadx."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office"

CTOR_RE = re.compile(
    r"^\s*(?:public|protected|private)\s+(?:/\* synthetic \*/\s+)?\w+\s*\("
)
ANNOTATION_RE = re.compile(r"^\s*@\w+")
OVERRIDE_RE = re.compile(r"^\s*@Override\b")


def next_substantive(lines, start):
    i = start
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()
        if not stripped:
            i += 1
            continue
        if ANNOTATION_RE.match(line) and not OVERRIDE_RE.match(line):
            i += 1
            continue
        return i, line
    return None, None


def fix_content(content: str) -> tuple[str, int]:
    lines = content.splitlines(keepends=True)
    removed = 0
    i = 0
    while i < len(lines):
        if not OVERRIDE_RE.match(lines[i]):
            i += 1
            continue
        j, next_line = next_substantive(lines, i + 1)
        if j is not None and CTOR_RE.match(next_line):
            del lines[i]
            removed += 1
            continue
        i += 1
    return "".join(lines), removed


def main():
    total_removed = 0
    changed_files = 0
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8", errors="replace")
        fixed, removed = fix_content(original)
        if removed:
            path.write_text(fixed, encoding="utf-8")
            changed_files += 1
            total_removed += removed
            print(f"{path.relative_to(ROOT)}: removed {removed}")
    print(f"Done: {changed_files} files, {total_removed} orphan @Override lines removed")


if __name__ == "__main__":
    main()
