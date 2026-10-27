#!/usr/bin/env python3
"""Replace jadx-wrong external constants in Escher property tables."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FILES = [
    ROOT / "app/src/main/java/com/office/alldocumentreader/xs/fc/ddf/EscherProperties.java",
    ROOT / "app/src/main/java/com/office/alldocumentreader/xs/fc/ddf/EscherDump.java",
]

REPLACEMENTS = [
    ("ShapeTypes.Funnel", "GEOTEXT__REVERSEROWORDER"),
    ("ShapeTypes.Gear6", "GEOTEXT__HASTEXTEFFECT"),
    ("ShapeTypes.LeftCircularArrow", "GEOTEXT__KERNCHARACTERS"),
    ("ShapeTypes.LeftRightRibbon", "GEOTEXT__TIGHTORTRACK"),
    ("ShapeTypes.PieWedge", "GEOTEXT__STRETCHTOFITSHAPE"),
    ("ShapeTypes.SwooshArrow", "GEOTEXT__CHARBOUNDINGBOX"),
    ("Frame.SAME_FRAME_EXTENDED", "GEOTEXT__ITALICFONT"),
    ("Frame.APPEND_FRAME", "GEOTEXT__UNDERLINEFONT"),
    ("InternetDomainName.MAX_LENGTH", "GEOTEXT__SHADOWFONT"),
    ("MetaDo.META_SETROP2", "BLIP__BLIPTODISPLAY"),
    ("MetaDo.META_SETRELABS", "BLIP__BLIPFILENAME"),
    ("TIFFConstants.TIFFTAG_CELLLENGTH", "BLIP__BRIGHTNESSSETTING"),
    ("TIFFConstants.TIFFTAG_FILLORDER", "BLIP__GAMMA"),
    ("TIFFConstants.TIFFTAG_DOCUMENTNAME", "BLIP__PICTUREFILLMOD"),
    ("TIFFConstants.TIFFTAG_MAKE", "BLIP__PRINTBLIP"),
    ("TIFFConstants.TIFFTAG_MODEL", "BLIP__PRINTBLIPFILENAME"),
    ("TIFFConstants.TIFFTAG_STRIPOFFSETS", "BLIP__PRINTFLAGS"),
    ("TIFFConstants.TIFFTAG_PRIMARYCHROMATICITIES", "BLIP__PICTUREACTIVE"),
    ("TIFFConstants.TIFFTAG_HALFTONEHINTS", "GEOMETRY__TOP"),
    ("TIFFConstants.TIFFTAG_TILELENGTH", "GEOMETRY__BOTTOM"),
    ("TIFFConstants.TIFFTAG_TILEOFFSETS", "GEOMETRY__SHAPEPATH"),
    ("TIFFConstants.TIFFTAG_TILEBYTECOUNTS", "GEOMETRY__VERTICES"),
    ("TIFFConstants.TIFFTAG_BADFAXLINES", "GEOMETRY__SEGMENTINFO"),
    ("TIFFConstants.TIFFTAG_CLEANFAXDATA", "GEOMETRY__ADJUSTVALUE"),
    ("TIFFConstants.TIFFTAG_CONSECUTIVEBADFAXLINES", "GEOMETRY__ADJUST2VALUE"),
    ("TIFFConstants.TIFFTAG_SUBIFD", "GEOMETRY__ADJUST4VALUE"),
    ("TIFFConstants.TIFFTAG_INKSET", "GEOMETRY__ADJUST6VALUE"),
    ("TIFFConstants.TIFFTAG_NUMBEROFINKS", "GEOMETRY__ADJUST8VALUE"),
    ("TIFFConstants.TIFFTAG_DOTRANGE", "GEOMETRY__ADJUST10VALUE"),
    ("TTAdConstant.DEEPLINK_FALLBACK_CODE", "FILL__RECTBOTTOM"),
    ("TTAdConstant.DEEPLINK_FALLBACK_TYPE_CODE", "FILL__DZTYPE"),
    ("TTAdConstant.LANDING_PAGE_TYPE_CODE", "FILL__SHADEPRESET"),
    ("TTAdConstant.DOWNLOAD_APP_INFO_CODE", "FILL__SHADECOLORS"),
    ("TTAdConstant.DOWNLOAD_URL_CODE", "FILL__ORIGINX"),
    ("TTAdConstant.IMAGE_LIST_CODE", "FILL__ORIGINY"),
    ("TTAdConstant.IMAGE_LIST_SIZE_CODE", "FILL__SHAPEORIGINX"),
    ("TTAdConstant.IMAGE_CODE", "FILL__SHAPEORIGINY"),
    ("TTAdConstant.IMAGE_URL_CODE", "FILL__SHADETYPE"),
    ("TarConstants.XSTAR_MULTIVOLUME_OFFSET", "LINESTYLE__LINESTARTARROWHEAD"),
    ("FloatingActionButton.AUTO_MINI_LARGEST_SCREEN_WIDTH", "LINESTYLE__LINEJOINSTYLE"),
    ("TIFFConstants.TIFFTAG_JPEGIFBYTECOUNT", "SHADOWSTYLE__HIGHLIGHT"),
    ("TIFFConstants.TIFFTAG_JPEGRESTARTINTERVAL", "SHADOWSTYLE__CRMOD"),
    ("TIFFConstants.TIFFTAG_JPEGLOSSLESSPREDICTORS", "SHADOWSTYLE__OFFSETX"),
    ("TIFFConstants.TIFFTAG_JPEGPOINTTRANSFORM", "SHADOWSTYLE__OFFSETY"),
    ("TIFFConstants.TIFFTAG_JPEGQTABLES", "SHADOWSTYLE__SECONDOFFSETX"),
    ("TIFFConstants.TIFFTAG_JPEGDCTABLES", "SHADOWSTYLE__SECONDOFFSETY"),
    ("MetaDo.META_SETTEXTJUSTIFICATION", "SHADOWSTYLE__SCALEYTOX"),
    ("MetaDo.META_SETWINDOWORG", "SHADOWSTYLE__SCALEXTOY"),
    ("MetaDo.META_SETWINDOWEXT", "SHADOWSTYLE__SCALEYTOY"),
    ("MetaDo.META_SETVIEWPORTORG", "SHADOWSTYLE__PERSPECTIVEX"),
    ("MetaDo.META_SETVIEWPORTEXT", "SHADOWSTYLE__PERSPECTIVEY"),
    ("MetaDo.META_OFFSETWINDOWORG", "SHADOWSTYLE__WEIGHT"),
    ("IAnimation.AnimationInformation.ROTATION", "THREEDSTYLE__SKEWANGLE"),
    ("MetaDo.META_CREATEFONTINDIRECT", "THREEDSTYLE__CONSTRAINROTATION"),
    ("MetaDo.META_CREATEBRUSHINDIRECT", "THREEDSTYLE__ROTATIONCENTERAUTO"),
]

# EscherDump references constants from EscherProperties
DUMP_PREFIX = "EscherProperties."


def fix_file(path: Path) -> bool:
    content = path.read_text(encoding="utf-8")
    updated = content
    for old, new in REPLACEMENTS:
        if path.name == "EscherDump.java":
            updated = updated.replace(old, DUMP_PREFIX + new)
        else:
            updated = updated.replace(old, new)
    if updated != content:
        path.write_text(updated, encoding="utf-8")
        return True
    return False


def main():
    for path in FILES:
        if fix_file(path):
            print(f"Fixed {path.name}")
        else:
            print(f"No changes in {path.name}")


if __name__ == "__main__":
    main()
