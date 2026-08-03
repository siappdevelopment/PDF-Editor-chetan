package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Document {
    public static final String META_ENCRYPTION = "encryption";
    public static final String META_FORMAT = "format";
    public static final String META_INFO_AUTHOR = "info:Author";
    public static final String META_INFO_CREATIONDATE = "info:CreationDate";
    public static final String META_INFO_CREATOR = "info:Creator";
    public static final String META_INFO_KEYWORDS = "info:Keywords";
    public static final String META_INFO_MODIFICATIONDATE = "info:ModDate";
    public static final String META_INFO_PRODUCER = "info:Producer";
    public static final String META_INFO_SUBJECT = "info:Subject";
    public static final String META_INFO_TITLE = "info:Title";
    public static final int PERMISSION_ACCESSBILITY = 121;
    public static final int PERMISSION_ANNOTATE = 110;
    public static final int PERMISSION_ASSEMBLE = 97;
    public static final int PERMISSION_COPY = 99;
    public static final int PERMISSION_EDIT = 101;
    public static final int PERMISSION_FORM = 102;
    public static final int PERMISSION_PRINT = 112;
    public static final int PERMISSION_PRINT_HQ = 104;
    protected long pointer;

    public static native Document openNativeWithBuffer(String str, byte[] bArr, byte[] bArr2);

    public static native Document openNativeWithPath(String str, String str2);

    public static native Document openNativeWithPathAndStream(String str, SeekableInputStream seekableInputStream);

    public static native Document openNativeWithStream(String str, SeekableInputStream seekableInputStream, SeekableInputStream seekableInputStream2);

    public static native boolean recognize(String str);

    public native PDFDocument asPDF();

    public native boolean authenticatePassword(String str);

    public native int countChapters();

    public native int countPages(int i);

    public native void finalize();

    public native Location findBookmark(long j);

    public native String formatLinkURI(LinkDestination linkDestination);

    public native String getMetaData(String str);

    public native boolean hasPermission(int i);

    public boolean isPDF() {
        return false;
    }

    public native boolean isReflowable();

    public native boolean isUnencryptedPDF();

    public native void layout(float f, float f2, float f3);

    public native Outline[] loadOutline();

    public native Page loadPage(int i, int i2);

    public native long makeBookmark(int i, int i2);

    public native boolean needsPassword();

    public native OutlineIterator outlineIterator();

    public native void outputAccelerator(SeekableOutputStream seekableOutputStream);

    public native Location resolveLink(String str);

    public native LinkDestination resolveLinkDestination(String str);

    public native void saveAccelerator(String str);

    public native Quad[][] search(int i, int i2, String str);

    public native void setMetaData(String str, String str2);

    public native boolean supportsAccelerator();

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public Document(long j) {
        this.pointer = j;
    }

    public static Document openDocument(String str) {
        return openNativeWithPath(str, null);
    }

    public static Document openDocument(String str, String str2) {
        return openNativeWithPath(str, str2);
    }

    public static Document openDocument(String str, SeekableInputStream seekableInputStream) {
        return openNativeWithPathAndStream(str, seekableInputStream);
    }

    public static Document openDocument(byte[] bArr, String str) {
        return openNativeWithBuffer(str, bArr, null);
    }

    public static Document openDocument(byte[] bArr, String str, byte[] bArr2) {
        return openNativeWithBuffer(str, bArr, bArr2);
    }

    public static Document openDocument(SeekableInputStream seekableInputStream, String str) {
        return openNativeWithStream(str, seekableInputStream, null);
    }

    public static Document openDocument(SeekableInputStream seekableInputStream, String str, SeekableInputStream seekableInputStream2) {
        return openNativeWithStream(str, seekableInputStream, seekableInputStream2);
    }

    public int countPages() {
        int countChapters = countChapters();
        int i = 0;
        for (int i2 = 0; i2 < countChapters; i2++) {
            i += countPages(i2);
        }
        return i;
    }

    public Page loadPage(Location location) {
        return loadPage(location.chapter, location.page);
    }

    public Page loadPage(int i) {
        int countChapters = countChapters();
        int i2 = 0;
        int i3 = 0;
        while (i2 < countChapters) {
            int countPages = countPages(i2) + i3;
            if (i < countPages) {
                return loadPage(i2, i - i3);
            }
            i2++;
            i3 = countPages;
        }
        throw new IllegalArgumentException("page number out of range");
    }

    public Location lastPage() {
        int countChapters = 0;
        return new Location(countChapters() - 1, countPages(countChapters) - 1);
    }

    public Location nextPage(Location location) {
        int countPages = countPages(location.chapter);
        int i = location.page;
        if (i + 1 == countPages) {
            int countChapters = countChapters();
            int i2 = location.chapter;
            return i2 + 1 < countChapters ? new Location(i2 + 1, 0) : location;
        }
        return new Location(location.chapter, i + 1);
    }

    public Location previousPage(Location location) {
        int i = location.page;
        if (i == 0) {
            int i2 = location.chapter;
            if (i2 > 0) {
                return new Location(location.chapter - 1, countPages(i2 - 1) - 1);
            }
            return location;
        }
        return new Location(location.chapter, i - 1);
    }

    public Location clampLocation(Location location) {
        int i = location.chapter;
        int i2 = location.page;
        int countChapters = countChapters();
        if (i < 0) {
            i = 0;
        }
        if (i >= countChapters) {
            i = countChapters - 1;
        }
        int countPages = countPages(i);
        if (i2 < 0) {
            i2 = 0;
        }
        if (i2 >= countPages) {
            i2 = countPages - 1;
        }
        return (location.chapter == i && location.page == i2) ? location : new Location(i, i2);
    }

    public Location locationFromPageNumber(int i) {
        int countChapters = countChapters();
        int i2 = 0;
        if (i < 0) {
            i = 0;
        }
        int i3 = 0;
        int i4 = 0;
        while (i2 < countChapters) {
            i3 = countPages(i2);
            int i5 = i4 + i3;
            if (i < i5) {
                return new Location(i2, i - i4);
            }
            i2++;
            i4 = i5;
        }
        return new Location(i2 - 1, i3 - 1);
    }

    public int pageNumberFromLocation(Location location) {
        int countChapters = countChapters();
        if (location == null) {
            return -1;
        }
        int i = 0;
        for (int i2 = 0; i2 < countChapters; i2++) {
            if (i2 == location.chapter) {
                return i + location.page;
            }
            i += countPages(i2);
        }
        return -1;
    }

    public Location resolveLink(Outline outline) {
        return resolveLink(outline.uri);
    }

    public Location resolveLink(Link link) {
        return resolveLink(link.getURI());
    }

    public LinkDestination resolveLinkDestination(OutlineIterator.OutlineItem outlineItem) {
        return resolveLinkDestination(outlineItem.uri);
    }

    public LinkDestination resolveLinkDestination(Outline outline) {
        return resolveLinkDestination(outline.uri);
    }

    public LinkDestination resolveLinkDestination(Link link) {
        return resolveLinkDestination(link.getURI());
    }

    public long makeBookmark(Location location) {
        return makeBookmark(location.chapter, location.page);
    }
}
