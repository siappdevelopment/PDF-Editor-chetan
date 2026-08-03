package com.artifex.mupdfdemo;

/* loaded from: classes7.dex */
public abstract class CallbackApplication {

    /* loaded from: classes7.dex */
    public interface MuPDFCallbackInterface {
        void callbackMethod(String str);
    }

    /* loaded from: classes7.dex */
    public static final class MuPDFCallbackClass {
        public static MuPDFCallbackInterface callback = null;
        private static String gaiStr = "";

        public static void sendGaiView(String str) {
            MuPDFCallbackInterface muPDFCallbackInterface = callback;
            if (muPDFCallbackInterface != null) {
                gaiStr = str;
                muPDFCallbackInterface.callbackMethod(str);
            }
        }
    }
}
