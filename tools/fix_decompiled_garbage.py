#!/usr/bin/env python3
"""Replace jadx garbage references in decompiled office Java sources."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office"

REPLACEMENTS = [
    ("org.apache.poi.openxml4j.opc.PackagingURIHelper.RELATIONSHIP_PART_SEGMENT_NAME", '"_rels"'),
    ("org.apache.poi.openxml4j.opc.PackagingURIHelper.RELATIONSHIP_PART_EXTENSION_NAME", '".rels"'),
    ("org.apache.poi.openxml4j.opc.PackagingURIHelper.PACKAGE_PROPERTIES_SEGMENT_NAME", '"docProps"'),
    ("org.apache.poi.openxml4j.opc.PackagingURIHelper.PACKAGE_CORE_PROPERTIES_NAME", '"core.xml"'),
    ("PdfWriter.VERSION_1_2", "'2'"),
    ("PdfWriter.VERSION_1_3", "'3'"),
    ("PdfWriter.VERSION_1_4", "'4'"),
    ("PdfWriter.VERSION_1_5", "'5'"),
    ("PdfWriter.VERSION_1_6", "'6'"),
    ("PdfWriter.VERSION_1_7", "'7'"),
    ("PathNodeKt.ArcToKey", "'A'"),
    ("PathNodeKt.CurveToKey", "'C'"),
    ("PathNodeKt.HorizontalToKey", "'H'"),
    ("PathNodeKt.VerticalToKey", "'V'"),
    ("PathNodeKt.MoveToKey", "'M'"),
    ("PathNodeKt.RelativeMoveToKey", "'m'"),
    ("PathNodeKt.RelativeReflectiveCurveToKey", "'s'"),
    ("PathNodeKt.ReflectiveCurveToKey", "'S'"),
    ("PathNodeKt.RelativeArcToKey", "'a'"),
    ("ProcessIdUtil.DEFAULT_PROCESSID", '"-"'),
    ("ProcessIdUtil.PERIOD", '"."'),
    ("ParameterizedMessage.ERROR_MSG_SEPARATOR", '":"'),
    ("ParameterizedMessage.ERROR_SEPARATOR", '","'),
    ("CCTDestination.EXTRAS_DELIMITER", '","'),
    ("MotionUtils.EASING_TYPE_FORMAT_START", '"("'),
    (""]"", '")"'),
    (""]"", '"]"'),
    ("Barcode128.CODE_AC_TO_B", "'d'"),
    ("Barcode128.CODE_BC_TO_A", "'e'"),
    ("Barcode128.FNC1_INDEX", "'f'"),
    ("public enum AnonymousClass", "public static final class AnonymousClass"),
    ("DevicePublicKeyStringDef.NONE", '"none"'),
    ("BaseDotsIndicator.DEFAULT_POINT_COLOR", "-16777216"),
    ("SlotTableKt.Mark_Mask", "256"),
    ("ConstraintsKt.MaxAllowedForMinNonFocusBits", "65535"),
    ("SearchBar_androidKt.AnimationExitDurationMillis", "350"),
    ("ProgressIndicatorKt.FirstLineTailDuration", "400"),
    ("Float16Kt.Fp16ExponentMax", "63488"),
    ("Integer.MAX_VALUE", "Integer.MAX_VALUE"),
    ("androidx.collection.Integer.MAX_VALUE", "Integer.MAX_VALUE"),
    ("CMSAttributeTableGenerator.CONTENT_TYPE", '"contentType"'),
    ("SignatureConfig.SIGNATURE_TIME_FORMAT", '"yyyy-MM-dd\'T\'HH:mm:ss\'Z\'"'),
]

BAD_IMPORT_PREFIXES = (
    "com.tom_roush.",
    "com.tbuonomo.",
    "com.google.common.",
    "com.google.android.gms.",
    "org.apache.poi.",
    "org.apache.logging.",
    "androidx.compose.",
    "androidx.navigation.",
    "androidx.constraintlayout.core.motion.",
    "com.reader.alldocviewer.pdfeditorartifex.",
    "com.bytedance.",
    "org.objectweb.asm.",
    "org.bouncycastle.",
    "org.apache.xmlbeans.",
    "com.google.android.datatransport.",
    "com.google.android.material.",
    "com.reader.alldocviewer.pdfeditoritextpdf.",
    "kotlin.jvm.internal.ShortCompanionObject",
    "org.apache.commons.compress.",
    "org.apache.commons.math3.",
)

IMPORT_RE = re.compile(r"^import .+;\s*$")


def fix_content(content: str) -> str:
    for old, new in REPLACEMENTS:
        content = content.replace(old, new)
    lines = []
    for line in content.splitlines():
        if line.startswith("import "):
            bad = False
            for prefix in BAD_IMPORT_PREFIXES:
                if prefix in line:
                    bad = True
                    break
            if bad:
                continue
        lines.append(line)
    return "\n".join(lines) + ("\n" if content.endswith("\n") else "")


def main():
    changed = 0
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        updated = fix_content(original)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed += 1
    print(f"Updated {changed} files")


if __name__ == "__main__":
    main()
