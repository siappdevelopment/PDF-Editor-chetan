#!/usr/bin/env python3
"""Remove incorrect dex register declarations added by fix_dex_registers.py."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office"


def find_method_bodies(content: str):
    i = 0
    n = len(content)
    while i < n:
        m = re.search(r"\)\s*\{", content[i:])
        if not m:
            break
        brace = i + m.end() - 1
        depth = 1
        j = brace + 1
        while j < n and depth:
            if content[j] == "{":
                depth += 1
            elif content[j] == "}":
                depth -= 1
            j += 1
        yield brace, j, content[brace + 1 : j - 1]
        i = j


def fix_method_body(body: str) -> str:
    object_regs = set()
    for m in re.finditer(r"\b(r\d+)\.(hasNext|next|iterator|getName|getText|elements|attribute)\b", body):
        object_regs.add(m.group(1))

    if not object_regs:
        return body

    lines = body.splitlines(keepends=True)
    out = []
    for line in lines:
        stripped = line.strip()
        if re.match(r"^(?:float|int|long|double|boolean|byte|short|char)\s+r\d+", stripped):
            declared = set(re.findall(r"\br\d+\b", stripped))
            if declared & object_regs:
                continue
        out.append(line)
    return "".join(out)


def main():
    changed = 0
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        updated = original
        parts = []
        last = 0
        for start, end, body in find_method_bodies(original):
            parts.append(original[last:start + 1])
            fixed_body = fix_method_body(body)
            parts.append(fixed_body)
            last = end - 1
        parts.append(original[last:])
        updated = "".join(parts)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed += 1
    print(f"Fixed {changed} files")


if __name__ == "__main__":
    main()
