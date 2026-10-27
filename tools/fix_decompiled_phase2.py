#!/usr/bin/env python3
"""Second-pass fixes for decompiled office Java sources."""
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
)

# Remove synthetic bridge methods entirely.
BRIDGE_METHOD_RE = re.compile(
    r"\n\s*public /\* bridge \*/ /\* synthetic \*/[\s\S]*?\n\s*\}\n",
    re.MULTILINE,
)

# Drop synthetic interface constructor parameters like FooIA fooIA.
SYNTHETIC_IA_PARAM_RE = re.compile(r",\s*\w+IA\s+\w+IA\b")
SYNTHETIC_IA_ONLY_PARAM_RE = re.compile(r"\(\s*\w+IA\s+\w+IA\s*\)")

# AnonymousClass1 duplicate Parcelable methods when class is enum-like broken pattern.
DUPLICATE_PARCEL_METHODS = (
    "createFromParcel",
    "newArray",
)


def strip_bad_imports(content: str) -> str:
    lines = []
    for line in content.splitlines():
        if line.startswith("import "):
            if any(prefix in line for prefix in BAD_IMPORT_PREFIXES):
                continue
        lines.append(line)
    return "\n".join(lines) + ("\n" if content.endswith("\n") else "")


def fix_synthetic_constructors(content: str) -> str:
    content = SYNTHETIC_IA_PARAM_RE.sub("", content)
    content = re.sub(
        r"public /\* synthetic \*/ ([\w<>]+\([^)]*\))",
        r"private \1",
        content,
    )
    return content


def remove_bridge_methods(content: str) -> str:
    prev = None
    while prev != content:
        prev = content
        content = BRIDGE_METHOD_RE.sub("\n", content)
    return content


def remove_duplicate_named_methods(content: str) -> str:
    """Remove duplicate method definitions with identical signatures in a file."""
    method_sig_re = re.compile(
        r"(?P<indent>\n[ \t]*)"
        r"(?P<modifiers>(?:public|protected|private)(?:\s+static)?\s+)"
        r"(?P<ret>[\w\[\]<>.,\s]+?)\s+"
        r"(?P<name>\w+)\s*"
        r"\((?P<params>[^)]*)\)\s*"
        r"(?P<extras>(?:throws\s+[\w.,\s]+)?\s*)"
        r"\{",
        re.MULTILINE,
    )

    matches = list(method_sig_re.finditer(content))
    seen = {}
    removals = []
    for m in matches:
        key = (m.group("name"), re.sub(r"\s+", "", m.group("params")))
        if key in seen:
            removals.append((m.start(), m.end()))
        else:
            seen[key] = m.start()

    if not removals:
        return content

    # Remove later duplicates by walking brace depth from opening brace.
    out = content
    offset = 0
    for start, open_brace in sorted(removals, key=lambda x: x[0]):
        start -= offset
        open_brace -= offset
        depth = 1
        i = open_brace + 1
        while i < len(out) and depth:
            if out[i] == "{":
                depth += 1
            elif out[i] == "}":
                depth -= 1
            i += 1
        out = out[:start] + out[i:]
        offset += i - start
    return out


def main():
    changed = 0
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        updated = original
        updated = strip_bad_imports(updated)
        updated = fix_synthetic_constructors(updated)
        updated = remove_bridge_methods(updated)
        updated = remove_duplicate_named_methods(updated)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed += 1
    print(f"Updated {changed} files")


if __name__ == "__main__":
    main()
