package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public abstract class PKCS7Signer {
    private long pointer = newNative(this);

    private native long newNative(PKCS7Signer pKCS7Signer);

    public native void finalize();

    public abstract int maxDigest();

    public abstract PKCS7DistinguishedName name();

    public abstract byte[] sign(FitzInputStream fitzInputStream);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }
}
