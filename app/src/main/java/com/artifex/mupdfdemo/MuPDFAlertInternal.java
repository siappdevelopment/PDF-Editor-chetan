package com.artifex.mupdfdemo;

/* loaded from: classes7.dex */
public class MuPDFAlertInternal {
    public final int buttonGroupType;
    public int buttonPressed;
    public final int iconType;
    public final String message;
    public final String title;

    public MuPDFAlertInternal(String str, int i, int i2, String str2, int i3) {
        this.message = str;
        this.iconType = i;
        this.buttonGroupType = i2;
        this.title = str2;
        this.buttonPressed = i3;
    }

    public MuPDFAlertInternal(MuPDFAlert muPDFAlert) {
        this.message = muPDFAlert.message;
        this.iconType = muPDFAlert.iconType.ordinal();
        this.buttonGroupType = muPDFAlert.buttonGroupType.ordinal();
        this.title = muPDFAlert.message;
        this.buttonPressed = muPDFAlert.buttonPressed.ordinal();
    }

    public MuPDFAlert toAlert() {
        return new MuPDFAlert(this.message, MuPDFAlert.IconType.values()[this.iconType], MuPDFAlert.ButtonGroupType.values()[this.buttonGroupType], this.title, MuPDFAlert.ButtonPressed.values()[this.buttonPressed]);
    }
}
