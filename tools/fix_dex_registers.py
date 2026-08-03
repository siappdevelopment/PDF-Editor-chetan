#!/usr/bin/env python3
"""Insert missing dex register variable declarations in decompiled Java methods."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office" / "alldocumentreader"

REGISTER_RE = re.compile(r"\br(\d+)\b")
FLOAT_HINT = re.compile(r"floatValue|\.0f|\bf\b|Math\.toRadians|arcTo")
NEW_TYPE_RE = re.compile(r"\br(\d+)\s*=\s*new\s+([\w$.]+)\s*[\(;]")
ASSIGN_RE = re.compile(r"^\s*r(\d+)\s*=")


def find_method_blocks(content: str):
    """Yield (start_brace_index, end_brace_index) for each method body."""
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
        yield brace, j
        i = j


def declared_registers(body: str):
    declared = set()
    for m in re.finditer(
        r"(?:float|int|long|double|boolean|byte|short|char|Object|[\w$.]+)\s+((?:r\d+\s*(?:=\s*[^,;]+)?\s*,\s*)*r\d+)",
        body,
    ):
        for reg in REGISTER_RE.findall(m.group(1)):
            declared.add(f"r{reg}")
    for m in re.finditer(r"(?:float|int|long|double|boolean|byte|short|char|Object)\s+(r\d+)\b", body):
        declared.add(m.group(1))
    return declared


def infer_register_types(body: str, registers: set[str]):
    types: dict[str, str] = {}
    for m in NEW_TYPE_RE.finditer(body):
        types[f"r{m.group(1)}"] = m.group(2)

    for line in body.splitlines():
        am = ASSIGN_RE.match(line)
        if not am:
            continue
        reg = f"r{am.group(1)}"
        if reg in types:
            continue
        rhs = line.split("=", 1)[1]
        if FLOAT_HINT.search(rhs):
            types[reg] = "float"
        elif re.search(r"\btrue\b|\bfalse\b", rhs):
            types[reg] = "boolean"
        elif re.search(r"\b0L\b|L\)|long", rhs):
            types[reg] = "long"
        elif re.search(r"\b0\.0\b|double|Math\.", rhs) and "floatValue" not in rhs:
            types[reg] = "double"
        else:
            types[reg] = "int"

    for reg in registers:
        types.setdefault(reg, "int")
    return types


def build_declarations(types: dict[str, str]) -> str:
    grouped: dict[str, list[str]] = {}
    for reg in sorted(types, key=lambda r: int(r[1:])):
        grouped.setdefault(types[reg], []).append(reg)

    parts = []
    defaults = {
        "float": "0f",
        "int": "0",
        "long": "0L",
        "double": "0.0",
        "boolean": "false",
    }
    for type_name in ("float", "int", "long", "double", "boolean"):
        regs = grouped.get(type_name, [])
        if regs:
            init = defaults[type_name]
            parts.append(f"{type_name} " + ", ".join(f"{r} = {init}" for r in regs))

    for type_name, regs in grouped.items():
        if type_name in defaults:
            continue
        for reg in regs:
            parts.append(f"{type_name} {reg} = null")

    return "        // dex registers\n        " + "; ".join(parts) + ";\n" if parts else ""


def fix_file(path: Path) -> bool:
    content = path.read_text(encoding="utf-8")
    if "r0" not in content and not REGISTER_RE.search(content):
        return False

    changed = False
    offset = 0
    new_content = content
    for brace, end in find_method_blocks(content):
        brace += offset
        end += offset
        body = new_content[brace + 1 : end - 1]
        if not REGISTER_RE.search(body):
            continue
        declared = declared_registers(body)
        used = {f"r{n}" for n in REGISTER_RE.findall(body)}
        missing = sorted(used - declared, key=lambda r: int(r[1:]))
        if not missing:
            continue
        types = infer_register_types(body, set(missing))
        decl = build_declarations({r: types[r] for r in missing})
        insert_at = brace + 1
        new_content = new_content[:insert_at] + "\n" + decl + new_content[insert_at:]
        offset += len(decl) + 1
        changed = True

    if changed:
        path.write_text(new_content, encoding="utf-8")
    return changed


def main():
    count = 0
    for path in ROOT.rglob("*.java"):
        if fix_file(path):
            count += 1
            print(path.relative_to(ROOT.parents[4]))
    print(f"Fixed {count} files")


if __name__ == "__main__":
    main()
