package com.artifex.mupdf.fitz;

import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

/* loaded from: classes7.dex */
public class PDFObject implements Iterable<PDFObject> {
    public static final PDFObject Null;
    private long pointer;

    private native void deleteArray(int i);

    private native void deleteDictionaryPDFObject(PDFObject pDFObject);

    private native void deleteDictionaryString(String str);

    private native PDFObject getArray(int i);

    private native PDFObject getDictionary(String str, boolean z);

    /* JADX INFO: Access modifiers changed from: private */
    public native PDFObject getDictionaryKey(int i);

    private native void pushBoolean(boolean z);

    private native void pushFloat(float f);

    private native void pushInteger(int i);

    private native void pushPDFObject(PDFObject pDFObject);

    private native void pushString(String str);

    private native void putArrayBoolean(int i, boolean z);

    private native void putArrayFloat(int i, float f);

    private native void putArrayInteger(int i, int i2);

    private native void putArrayPDFObject(int i, PDFObject pDFObject);

    private native void putArrayString(int i, String str);

    private native void putDictionaryPDFObjectBoolean(PDFObject pDFObject, boolean z);

    private native void putDictionaryPDFObjectDate(PDFObject pDFObject, long j);

    private native void putDictionaryPDFObjectFloat(PDFObject pDFObject, float f);

    private native void putDictionaryPDFObjectInteger(PDFObject pDFObject, int i);

    private native void putDictionaryPDFObjectMatrix(PDFObject pDFObject, Matrix matrix);

    private native void putDictionaryPDFObjectPDFObject(PDFObject pDFObject, PDFObject pDFObject2);

    private native void putDictionaryPDFObjectRect(PDFObject pDFObject, Rect rect);

    private native void putDictionaryPDFObjectString(PDFObject pDFObject, String str);

    private native void putDictionaryStringBoolean(String str, boolean z);

    private native void putDictionaryStringDate(String str, long j);

    private native void putDictionaryStringFloat(String str, float f);

    private native void putDictionaryStringInteger(String str, int i);

    private native void putDictionaryStringMatrix(String str, Matrix matrix);

    private native void putDictionaryStringPDFObject(String str, PDFObject pDFObject);

    private native void putDictionaryStringRect(String str, Rect rect);

    private native void putDictionaryStringString(String str, String str2);

    private native void writeRawStreamBuffer(Buffer buffer);

    private native void writeRawStreamString(String str);

    private native void writeStreamBuffer(Buffer buffer);

    private native void writeStreamString(String str);

    public native boolean asBoolean();

    public native byte[] asByteString();

    public native float asFloat();

    public native int asIndirect();

    public native int asInteger();

    public native String asName();

    public native String asString();

    public native boolean equals(PDFObject pDFObject);

    public native void finalize();

    public native boolean isArray();

    public native boolean isBoolean();

    public native boolean isDictionary();

    public native boolean isIndirect();

    public native boolean isInteger();

    public native boolean isName();

    public native boolean isNumber();

    public native boolean isReal();

    public native boolean isStream();

    public native boolean isString();

    public native byte[] readRawStream();

    public native byte[] readStream();

    public native PDFObject resolve();

    public native int size();

    public native String toString(boolean z, boolean z2);

    public native void writeObject(PDFObject pDFObject);

    static {
        Context.init();
        Null = new PDFObject(0L);
    }

    public void destroy() {
        finalize();
    }

    private PDFObject(long j) {
        this.pointer = j;
    }

    public boolean isNull() {
        return this == Null;
    }

    public String toString(boolean z) {
        return toString(z, false);
    }

    public String toString() {
        return toString(false, false);
    }

    public void writeStream(Buffer buffer) {
        writeStreamBuffer(buffer);
    }

    public void writeStream(String str) {
        writeStreamString(str);
    }

    public void writeRawStream(Buffer buffer) {
        writeRawStreamBuffer(buffer);
    }

    public void writeRawStream(String str) {
        writeRawStreamString(str);
    }

    public PDFObject get(int i) {
        return getArray(i);
    }

    public PDFObject get(String str) {
        return getDictionary(str, false);
    }

    public PDFObject get(PDFObject pDFObject) {
        return getDictionary(pDFObject != null ? pDFObject.asName() : null, false);
    }

    public PDFObject getInheritable(String str) {
        return getDictionary(str, true);
    }

    public PDFObject getInheritable(PDFObject pDFObject) {
        return getDictionary(pDFObject != null ? pDFObject.asName() : null, true);
    }

    public void put(int i, boolean z) {
        putArrayBoolean(i, z);
    }

    public void put(int i, int i2) {
        putArrayInteger(i, i2);
    }

    public void put(int i, float f) {
        putArrayFloat(i, f);
    }

    public void put(int i, String str) {
        putArrayString(i, str);
    }

    public void put(int i, PDFObject pDFObject) {
        putArrayPDFObject(i, pDFObject);
    }

    public void put(String str, boolean z) {
        putDictionaryStringBoolean(str, z);
    }

    public void put(String str, int i) {
        putDictionaryStringInteger(str, i);
    }

    public void put(String str, float f) {
        putDictionaryStringFloat(str, f);
    }

    public void put(String str, String str2) {
        putDictionaryStringString(str, str2);
    }

    public void put(String str, PDFObject pDFObject) {
        putDictionaryStringPDFObject(str, pDFObject);
    }

    public void put(String str, Rect rect) {
        putDictionaryStringRect(str, rect);
    }

    public void put(String str, Matrix matrix) {
        putDictionaryStringMatrix(str, matrix);
    }

    public void put(String str, Date date) {
        putDictionaryStringDate(str, date.getTime());
    }

    public void put(PDFObject pDFObject, boolean z) {
        putDictionaryPDFObjectBoolean(pDFObject, z);
    }

    public void put(PDFObject pDFObject, int i) {
        putDictionaryPDFObjectInteger(pDFObject, i);
    }

    public void put(PDFObject pDFObject, float f) {
        putDictionaryPDFObjectFloat(pDFObject, f);
    }

    public void put(PDFObject pDFObject, String str) {
        putDictionaryPDFObjectString(pDFObject, str);
    }

    public void put(PDFObject pDFObject, PDFObject pDFObject2) {
        putDictionaryPDFObjectPDFObject(pDFObject, pDFObject2);
    }

    public void put(PDFObject pDFObject, Rect rect) {
        putDictionaryPDFObjectRect(pDFObject, rect);
    }

    public void put(PDFObject pDFObject, Matrix matrix) {
        putDictionaryPDFObjectMatrix(pDFObject, matrix);
    }

    public void put(PDFObject pDFObject, Date date) {
        putDictionaryPDFObjectDate(pDFObject, date.getTime());
    }

    public void delete(int i) {
        deleteArray(i);
    }

    public void delete(String str) {
        deleteDictionaryString(str);
    }

    public void delete(PDFObject pDFObject) {
        deleteDictionaryPDFObject(pDFObject);
    }

    public void push(boolean z) {
        pushBoolean(z);
    }

    public void push(int i) {
        pushInteger(i);
    }

    public void push(float f) {
        pushFloat(f);
    }

    public void push(String str) {
        pushString(str);
    }

    public void push(PDFObject pDFObject) {
        pushPDFObject(pDFObject);
    }

    @Override // java.lang.Iterable
    public Iterator<PDFObject> iterator() {
        return new PDFObjectIterator(this);
    }

    /* loaded from: classes7.dex */
    public class PDFObjectIterator implements Iterator<PDFObject> {
        private boolean isarray;
        private PDFObject object;
        private int position;

        public PDFObjectIterator(PDFObject pDFObject) {
            this.object = pDFObject;
            this.isarray = pDFObject != null ? pDFObject.isArray() : false;
            this.position = -1;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            PDFObject pDFObject = this.object;
            return pDFObject != null && this.position + 1 < pDFObject.size();
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // java.util.Iterator
        public PDFObject next() {
            PDFObject pDFObject = this.object;
            if (pDFObject == null || this.position >= pDFObject.size()) {
                throw new NoSuchElementException("Object has no more elements");
            }
            int i = this.position + 1;
            this.position = i;
            if (!this.isarray) {
                return this.object.getDictionaryKey(i);
            }
            return this.object.get(i);
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
