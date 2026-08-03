import os

ROOT = r"f:\HiteshAndroid\PdfReader\app\src\main\java\com\office\alldocumentreader"
REPLACEMENTS = [
    ("TypedValues.AttributesType.S_FRAME", '"frame"'),
    ("TypedValues.Custom.S_STRING", '"string"'),
    ("TypedValues.PositionType.TYPE_POSITION_TYPE", "510"),
    ("TypedValues.TransitionType.TYPE_FROM", "701"),
    ("TypedValues.TransitionType.TYPE_TO", "702"),
    ("TypedValues.TransitionType.TYPE_AUTO_TRANSITION", "704"),
    ("TypedValues.TransitionType.TYPE_INTERPOLATOR", "705"),
    ("TypedValues.TransitionType.TYPE_STAGGERED", "706"),
    ("TypedValues.TransitionType.TYPE_TRANSITION_FLAGS", "707"),
    ("TypedValues.Custom.TYPE_FLOAT", "901"),
    ("TypedValues.Custom.TYPE_COLOR", "902"),
    ("TypedValues.Custom.TYPE_STRING", "903"),
    ("TypedValues.Custom.TYPE_BOOLEAN", "904"),
    ("TypedValues.Custom.TYPE_REFERENCE", "906"),
    ("TypedValues.CycleType.TYPE_WAVE_PHASE /* 425 */", "425"),
    ("TypedValues.CycleType.TYPE_WAVE_PHASE", "425"),
    ("TypedValues.MotionType.TYPE_QUANTIZE_INTERPOLATOR_ID", "612"),
]

updated = 0
for dirpath, _, files in os.walk(ROOT):
    for name in files:
        if not name.endswith(".java"):
            continue
        path = os.path.join(dirpath, name)
        with open(path, "r", encoding="utf-8") as handle:
            text = handle.read()
        original = text
        for old, new in REPLACEMENTS:
            text = text.replace(old, new)
        if text != original:
            with open(path, "w", encoding="utf-8") as handle:
                handle.write(text)
            updated += 1
            print(path)

print(f"updated {updated} files")
