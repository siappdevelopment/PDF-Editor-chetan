package com.artifex.mupdf.fitz;

import java.io.InputStream;
import java.util.Date;

/* loaded from: classes7.dex */
public class PDFDocument extends Document {
    public static final int LANGUAGE_UNSET = 0;
    public static final int LANGUAGE_ja = 37;
    public static final int LANGUAGE_ko = 416;
    public static final int LANGUAGE_ur = 507;
    public static final int LANGUAGE_urd = 3423;
    public static final int LANGUAGE_zh = 242;
    public static final int LANGUAGE_zh_Hans = 14093;
    public static final int LANGUAGE_zh_Hant = 14822;
    public static final int PAGE_LABEL_ALPHA_LC = 97;
    public static final int PAGE_LABEL_ALPHA_UC = 65;
    public static final int PAGE_LABEL_DECIMAL = 68;
    public static final int PAGE_LABEL_NONE = 0;
    public static final int PAGE_LABEL_ROMAN_LC = 114;
    public static final int PAGE_LABEL_ROMAN_UC = 82;

    /* loaded from: classes7.dex */
    public interface JsEventListener {
        public static final int BUTTON_CANCEL = 2;
        public static final int BUTTON_GROUP_OK = 0;
        public static final int BUTTON_GROUP_OK_CANCEL = 1;
        public static final int BUTTON_GROUP_YES_NO = 2;
        public static final int BUTTON_GROUP_YES_NO_CANCEL = 3;
        public static final int BUTTON_NO = 3;
        public static final int BUTTON_NONE = 0;
        public static final int BUTTON_OK = 1;
        public static final int BUTTON_YES = 4;

        /* loaded from: classes7.dex */
        public static class AlertResult {
            public int buttonPressed;
            public boolean checkboxChecked;
        }

        AlertResult onAlert(PDFDocument pDFDocument, String str, String str2, int i, int i2, boolean z, String str3, boolean z2);
    }

    private native PDFObject addPageBuffer(Rect rect, int i, PDFObject pDFObject, Buffer buffer);

    private native PDFObject addPageString(Rect rect, int i, PDFObject pDFObject, String str);

    private native PDFObject addStreamBuffer(Buffer buffer, Object obj, boolean z);

    private native PDFObject addStreamString(String str, Object obj, boolean z);

    public static native String appendExplicitDestToURI(String str, LinkDestination linkDestination);

    public static native String appendNamedDestToURI(String str, String str2);

    public static native String formatURIFromPathAndExplicitDest(String str, LinkDestination linkDestination);

    public static native String formatURIFromPathAndNamedDest(String str, String str2);

    private static native long newNative();

    public native void abandonOperation();

    public native PDFObject addCJKFont(Font font, int i, int i2, boolean z);

    public native PDFObject addEmbeddedFile(String str, String str2, Buffer buffer, long j, long j2, boolean z);

    public native PDFObject addFont(Font font);

    public native PDFObject addImage(Image image);

    public native PDFObject addObject(PDFObject pDFObject);

    public native PDFObject addSimpleFont(Font font, int i);

    public native void beginImplicitOperation();

    public native void beginOperation(String str);

    public native void calculate();

    public native boolean canBeSavedIncrementally();

    public native boolean canRedo();

    public native boolean canUndo();

    public native int countObjects();

    public native int countSignatures();

    public native int countUnsavedVersions();

    public native int countVersions();

    public native PDFObject createObject();

    public native void deleteObject(int i);

    public native void deletePage(int i);

    public native void deletePageLabels(int i);

    public native void disableJs();

    public native void enableJournal();

    public native void enableJs();

    public native void endOperation();

    @Override // com.artifex.mupdf.fitz.Document
    public native void finalize();

    public native PDFObject findPage(int i);

    public native PDFEmbeddedFileParams getEmbeddedFileParams(PDFObject pDFObject);

    public native int getLanguage();

    public native PDFObject getTrailer();

    public native int getVersion();

    public native PDFObject graftObject(PDFObject pDFObject);

    public native void graftPage(int i, PDFDocument pDFDocument, int i2);

    public native boolean hasUnsavedChanges();

    public native void insertPage(int i, PDFObject pDFObject);

    public native boolean isJsSupported();

    @Override // com.artifex.mupdf.fitz.Document
    public boolean isPDF() {
        return true;
    }

    public native boolean isRedacted();

    public native Buffer loadEmbeddedFileContents(PDFObject pDFObject);

    public native void loadJournal(String str);

    public native void loadJournalWithStream(SeekableInputStream seekableInputStream);

    public native void nativeSaveWithStream(SeekableInputOutputStream seekableInputOutputStream, String str);

    public native PDFObject newArray();

    public native PDFObject newBoolean(boolean z);

    public native PDFObject newByteString(byte[] bArr);

    public native PDFObject newDictionary();

    public native PDFObject newIndirect(int i, int i2);

    public native PDFObject newInteger(int i);

    public native PDFObject newName(String str);

    public native PDFObject newNull();

    public native PDFGraftMap newPDFGraftMap();

    public native PDFObject newReal(float f);

    public native PDFObject newString(String str);

    public native void rearrangePages(int[] iArr);

    public native void redo();

    public native void save(String str, String str2);

    public native void saveJournal(String str);

    public native void saveJournalWithStream(SeekableOutputStream seekableOutputStream);

    public native void setJsEventListener(JsEventListener jsEventListener);

    public native void setLanguage(int i);

    public native void setPageLabels(int i, int i2, String str, int i3);

    public native void undo();

    public native int undoRedoPosition();

    public native String undoRedoStep(int i);

    public native int undoRedoSteps();

    public native int validateChangeHistory();

    public native boolean verifyEmbeddedFileChecksum(PDFObject pDFObject);

    public native boolean wasLinearized();

    public native boolean wasPureXFA();

    public native boolean wasRepaired();

    static {
        Context.init();
    }

    public PDFDocument(long j) {
        super(j);
    }

    public PDFDocument() {
        super(newNative());
    }

    public void deleteObject(PDFObject pDFObject) {
        deleteObject(pDFObject.asIndirect());
    }

    public PDFObject addRawStream(Buffer buffer, Object obj) {
        return addStreamBuffer(buffer, obj, true);
    }

    public PDFObject addStream(Buffer buffer, Object obj) {
        return addStreamBuffer(buffer, obj, false);
    }

    public PDFObject addRawStream(String str, Object obj) {
        return addStreamString(str, obj, true);
    }

    public PDFObject addStream(String str, Object obj) {
        return addStreamString(str, obj, false);
    }

    public PDFObject addRawStream(Buffer buffer) {
        return addStreamBuffer(buffer, null, true);
    }

    public PDFObject addStream(Buffer buffer) {
        return addStreamBuffer(buffer, null, false);
    }

    public PDFObject addRawStream(String str) {
        return addStreamString(str, null, true);
    }

    public PDFObject addStream(String str) {
        return addStreamString(str, null, false);
    }

    public PDFObject addPage(Rect rect, int i, PDFObject pDFObject, Buffer buffer) {
        return addPageBuffer(rect, i, pDFObject, buffer);
    }

    public PDFObject addPage(Rect rect, int i, PDFObject pDFObject, String str) {
        return addPageString(rect, i, pDFObject, str);
    }

    public void save(SeekableInputOutputStream seekableInputOutputStream, String str) {
        nativeSaveWithStream(seekableInputOutputStream, str);
    }

    public boolean hasAcroForm() {
        return getTrailer().get("Root").get("AcroForm").get("Fields").size() > 0;
    }

    public boolean hasXFAForm() {
        return !getTrailer().get("Root").get("AcroForm").get("XFA").isNull();
    }

    public static String formatURIFromPathAndDest(String str, String str2) {
        return formatURIFromPathAndNamedDest(str, str2);
    }

    public static String formatURIFromPathAndDest(String str, LinkDestination linkDestination) {
        return formatURIFromPathAndExplicitDest(str, linkDestination);
    }

    public static String appendDestToURI(String str, String str2) {
        return appendNamedDestToURI(str, str2);
    }

    public static String appendDestToURI(String str, LinkDestination linkDestination) {
        return appendExplicitDestToURI(str, linkDestination);
    }

    public PDFObject addEmbeddedFile(String str, String str2, InputStream inputStream, Date date, Date date2, boolean z) {
        Buffer buffer = new Buffer();
        buffer.writeFromStream(inputStream);
        return addEmbeddedFile(str, str2, buffer, date != null ? date.getTime() : -1L, date2 != null ? date2.getTime() : -1L, z);
    }

    /* loaded from: classes7.dex */
    public static class PDFEmbeddedFileParams {
        public final Date creationDate;
        public final String filename;
        public final String mimetype;
        public final Date modificationDate;
        public final int size;

        public PDFEmbeddedFileParams(String str, String str2, int i, long j, long j2) {
            this.filename = str;
            this.mimetype = str2;
            this.size = i;
            this.creationDate = new Date(j);
            this.modificationDate = new Date(j2);
        }
    }
}
