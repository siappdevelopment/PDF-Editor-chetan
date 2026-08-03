#!/usr/bin/env python3
"""Remove jadx bridge/synthetic stub methods and constructors."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office"

BRIDGE_METHOD_RE = re.compile(
    r"\n[ \t]*public /\* bridge \*/ /\* synthetic \*/[^\{]+\{[^\}]*\}\n",
    re.MULTILINE,
)

# Constructors that only delegate to this(...) with identical args.
STUB_CTOR_RE = re.compile(
    r"\n[ \t]*private [\w.<>,\s\[\]]+\([^)]*\)\s*\{\s*this\([^)]*\);\s*\}\n",
    re.MULTILINE,
)

def strip_orphan_overrides(content: str) -> str:
    content = re.sub(
        r"(?:^[ \t]*@Override[^\n]*\n(?:^[ \t]*\n)*){2,}",
        "",
        content,
        flags=re.MULTILINE,
    )
    content = re.sub(
        r"^[ \t]*@Override[^\n]*\n(?=(?:^[ \t]*\n)*^[ \t]*static\s*\{)",
        "",
        content,
        flags=re.MULTILINE,
    )
    return content


def main():
    changed = 0
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        updated = original
        prev = None
        while prev != updated:
            prev = updated
            updated = BRIDGE_METHOD_RE.sub("\n", updated)
        updated = STUB_CTOR_RE.sub("\n", updated)
        updated = strip_orphan_overrides(updated)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed += 1
    print(f"Updated {changed} files")


if __name__ == "__main__":
    main()
