package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public abstract class PKCS7Verifier {
    public static final int PKCS7VerifierDigestFailure = 3;
    public static final int PKCS7VerifierNoCertificate = 2;
    public static final int PKCS7VerifierNoSignature = 1;
    public static final int PKCS7VerifierNotTrusted = 6;
    public static final int PKCS7VerifierOK = 0;
    public static final int PKCS7VerifierSelfSigned = 4;
    public static final int PKCS7VerifierSelfSignedInChain = 5;
    public static final int PKCS7VerifierUnknown = -1;
    private long pointer = newNative(this);

    private native long newNative(PKCS7Verifier pKCS7Verifier);

    public abstract int checkCertificate(byte[] bArr);

    public abstract int checkDigest(FitzInputStream fitzInputStream, byte[] bArr);

    public native void finalize();

    static {
        Context.init();
        Context.init();
    }

    public void destroy() {
        finalize();
    }
}
