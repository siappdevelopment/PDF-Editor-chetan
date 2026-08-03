package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Context {
    private static boolean inited = false;
    private static final Object lock;
    private static Log log;

    /* loaded from: classes7.dex */
    public interface Log {
        void error(String str);

        void warning(String str);
    }

    /* loaded from: classes7.dex */
    public static class Version {
        public int major;
        public int minor;
        public int patch;
        public String version;
    }

    public static native void disableICC();

    public static native void emptyStore();

    public static native void enableICC();

    public static native Version getVersion();

    private static native int initNative();

    public static native void setAntiAliasLevel(int i);

    public static native void setUserCSS(String str);

    public static native boolean shrinkStore(int i);

    public static native void useDocumentCSS(boolean z);

    static {
        init();
        lock = new Object();
    }

    public static void init() {
        if (inited) {
            return;
        }
        inited = true;
        try {
            try {
                System.loadLibrary("mupdf_java");
            } catch (UnsatisfiedLinkError unused) {
                System.loadLibrary("mupdf_java64");
            }
        } catch (UnsatisfiedLinkError unused2) {
            System.loadLibrary("mupdf_java32");
        }
        if (initNative() < 0) {
            throw new RuntimeException("cannot initialize mupdf library");
        }
    }

    public static void setLog(Log log2) {
        synchronized (lock) {
            log = log2;
        }
    }
}
