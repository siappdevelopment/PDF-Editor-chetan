#!/usr/bin/env python3
"""Targeted batch fixes for known jadx decompilation errors."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "java" / "com" / "office"

REPLACEMENTS = [
    ("VectorFormat.DEFAULT_SEPARATOR", '", "'),
    ("compare2(", "compare("),
    ("MetaDo.META_SETROP2", "260"),
    ("NotificationCompat.WearableExtender.KEY_BACKGROUND", '"background"'),
    ('MailTo.MAILTO', '"mailto:"'),
    ("XmlErrorCodes.DECIMAL", '"decimal"'),
    ("XmlErrorCodes.LONG", '"long"'),
    ("FirebaseAnalytics.Param.CHARACTER", '"character"'),
    ("Annotation.PAGE", '"page"'),
    ("Parser.Tokenizer.FF", "'\\f'"),
    ("BaseParser.NULL", '"null"'),
    ("HSLFSlideShow.POWERPOINT_DOCUMENT", '"PowerPoint Document"'),
    ("BidiFormatter.DirectionalityEstimator.DIR_TYPE_CACHE_SIZE", "0x00000700"),
    ("Opcodes.ASM7", "0x00070000"),
    ("Frame.UNINITIALIZED_KIND", "0x00c00000"),
    ("CpioConstants.C_ISSOCK", "0xC000"),
    ("HWPFShapeFactory.createShape(m7100$$Nest$mgetEscherShapeRecordContainer)", "HWPFShapeFactory.createShape(m7100$$Nest$mgetEscherShapeRecordContainer, null)"),
    ("HtmlTags.STYLE", '"style"'),
    ("HtmlTags.TABLE", '"table"'),
    ("HtmlTags.ALIGN_LEFT", '"left"'),
    ("HtmlTags.ALIGN_CENTER", '"center"'),
    ("HtmlTags.ALIGN_RIGHT", '"right"'),
    ("HtmlTags.ALIGN_TOP", '"top"'),
    ("HtmlTags.ALIGN_BOTTOM", '"bottom"'),
    ("HtmlTags.ALIGN_MIDDLE", '"middle"'),
    ("HtmlTags.TR", '"tr"'),
    ("HtmlTags.P", '"p"'),
    ("HtmlTags.BEFORE", '"before"'),
    ("HtmlTags.AFTER", '"after"'),
    ("HtmlTags.WIDTH", '"width"'),
    ("HtmlTags.HEIGHT", '"height"'),
    ("HtmlTags.ALIGN", '"align"'),
    ("HtmlTags.FONTSIZE", '"fontsize"'),
    ("HtmlTags.U", '"u"'),
    ("HtmlTags.STRIKE", '"strike"'),
    ("HtmlTags.TEXTALIGN", '"text-align"'),
    ("return ItemTouchHelper.ACTION_MODE_DRAG_MASK;", "return 0x00FF0000;"),
    ("return TIFFConstants.COMPRESSION_IT8LW;", "return 0x00008080;"),
    ("ItemTouchHelper.ACTION_MODE_DRAG_MASK", "0xFF0000"),
    ("this._textRuns.get(r2.size() - 1)", "this._textRuns.get(this._textRuns.size() - 1)"),
    ("this._paragraphs.get(r3.size() - 1)", "this._paragraphs.get(this._paragraphs.size() - 1)"),
]

CREATE_SHAPE_RE = re.compile(
    r"HWPFShapeFactory\.createShape\(((?!null)[^)]+)\)(?!,)"
)


def fix_hwpf_shape(content: str) -> str:
    """Fix r0 register in getAnchor - assign anchor records to locals."""
    if "r0.getCol1()" not in content:
        return content
    content = content.replace(
        "if (((EscherChildAnchorRecord) ShapeKit.getEscherChild(this.escherContainer, -4081)) == null) {\n"
        "                    if (((EscherClientAnchorRecord) ShapeKit.getEscherChild(this.escherContainer, -4080)) != null) {\n"
        "                        rectangle2 = new Rectangle();\n"
        "                        rectangle2.x = (int) (((r0.getCol1() * f) * 96.0f) / 914400.0f);\n"
        "                        rectangle2.y = (int) (((r0.getFlag() * f2) * 96.0f) / 914400.0f);\n"
        "                        rectangle2.width = (int) ((((r0.getDx1() - r0.getCol1()) * f) * 96.0f) / 914400.0f);\n"
        "                        rectangle2.height = (int) ((((r0.getRow1() - r0.getFlag()) * f2) * 96.0f) / 914400.0f);\n"
        "                    }\n"
        "                } else {\n"
        "                    rectangle2 = new Rectangle();\n"
        "                    rectangle2.x = (int) (((r0.getDx1() * f) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.y = (int) (((r0.getDy1() * f2) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.width = (int) ((((r0.getDx2() - r0.getDx1()) * f) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.height = (int) ((((r0.getDy2() - r0.getDy1()) * f2) * 96.0f) / 914400.0f);\n"
        "                }",
        "EscherChildAnchorRecord childAnchor = (EscherChildAnchorRecord) ShapeKit.getEscherChild(this.escherContainer, -4081);\n"
        "                if (childAnchor == null) {\n"
        "                    EscherClientAnchorRecord clientAnchor = (EscherClientAnchorRecord) ShapeKit.getEscherChild(this.escherContainer, -4080);\n"
        "                    if (clientAnchor != null) {\n"
        "                        rectangle2 = new Rectangle();\n"
        "                        rectangle2.x = (int) (((clientAnchor.getCol1() * f) * 96.0f) / 914400.0f);\n"
        "                        rectangle2.y = (int) (((clientAnchor.getFlag() * f2) * 96.0f) / 914400.0f);\n"
        "                        rectangle2.width = (int) ((((clientAnchor.getDx1() - clientAnchor.getCol1()) * f) * 96.0f) / 914400.0f);\n"
        "                        rectangle2.height = (int) ((((clientAnchor.getRow1() - clientAnchor.getFlag()) * f2) * 96.0f) / 914400.0f);\n"
        "                    }\n"
        "                } else {\n"
        "                    rectangle2 = new Rectangle();\n"
        "                    rectangle2.x = (int) (((childAnchor.getDx1() * f) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.y = (int) (((childAnchor.getDy1() * f2) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.width = (int) ((((childAnchor.getDx2() - childAnchor.getDx1()) * f) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.height = (int) ((((childAnchor.getDy2() - childAnchor.getDy1()) * f2) * 96.0f) / 914400.0f);\n"
        "                }",
    )
    content = content.replace(
        "if (((EscherClientAnchorRecord) ShapeKit.getEscherChild(this.escherContainer, -4080)) != null) {\n"
        "                    rectangle2 = new Rectangle();\n"
        "                    rectangle2.x = (int) (((r0.getCol1() * f) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.y = (int) (((r0.getFlag() * f2) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.width = (int) ((((r0.getDx1() - r0.getCol1()) * f) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.height = (int) ((((r0.getRow1() - r0.getFlag()) * f2) * 96.0f) / 914400.0f);\n"
        "                }",
        "EscherClientAnchorRecord clientAnchor2 = (EscherClientAnchorRecord) ShapeKit.getEscherChild(this.escherContainer, -4080);\n"
        "                if (clientAnchor2 != null) {\n"
        "                    rectangle2 = new Rectangle();\n"
        "                    rectangle2.x = (int) (((clientAnchor2.getCol1() * f) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.y = (int) (((clientAnchor2.getFlag() * f2) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.width = (int) ((((clientAnchor2.getDx1() - clientAnchor2.getCol1()) * f) * 96.0f) / 914400.0f);\n"
        "                    rectangle2.height = (int) ((((clientAnchor2.getRow1() - clientAnchor2.getFlag()) * f2) * 96.0f) / 914400.0f);\n"
        "                }",
    )
    return content


def fix_property_java(content: str) -> str:
    if "r0[0].getData()" in content:
        content = content.replace(
            "return r0[0].getData().length * this.blocks.length;",
            "byte[][] blockData = getBlocks();\n            return blockData[0].getData().length * this.blocks.length;",
        )
    return content


def fix_docx_reader_loops(content: str) -> str:
    content = content.replace(
        "for (Element element2 : rootElement.elements(\"num\")) {",
        "for (Object obj : rootElement.elements(\"num\")) {\n            Element element2 = (Element) obj;",
    )
    content = content.replace(
        "for (Element element4 : rootElement.elements(\"abstractNum\")) {",
        "for (Object obj2 : rootElement.elements(\"abstractNum\")) {\n            Element element4 = (Element) obj2;",
    )
    return content


def main():
    changed = 0
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        updated = original
        for old, new in REPLACEMENTS:
            updated = updated.replace(old, new)
        updated = CREATE_SHAPE_RE.sub(r"HWPFShapeFactory.createShape(\1, null)", updated)
        if path.name == "HWPFShape.java":
            updated = fix_hwpf_shape(updated)
        if path.name == "Property.java" and "filesystem" in str(path):
            updated = fix_property_java(updated)
        if path.name == "DOCXReader.java":
            updated = fix_docx_reader_loops(updated)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed += 1
            print(path.relative_to(ROOT.parent.parent.parent.parent))
    print(f"Updated {changed} files")


if __name__ == "__main__":
    main()
