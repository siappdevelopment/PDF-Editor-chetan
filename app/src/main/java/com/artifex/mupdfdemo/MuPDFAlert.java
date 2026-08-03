package com.artifex.mupdfdemo;

/* loaded from: classes7.dex */
public class MuPDFAlert {
    public final ButtonGroupType buttonGroupType;
    public ButtonPressed buttonPressed;
    public final IconType iconType;
    public final String message;
    public final String title;

    /* loaded from: classes7.dex */
    public enum ButtonGroupType {
        Ok,
        OkCancel,
        YesNo,
        YesNoCancel
    }

    /* loaded from: classes7.dex */
    public enum ButtonPressed {
        None,
        Ok,
        Cancel,
        No,
        Yes
    }

    /* loaded from: classes7.dex */
    public enum IconType {
        Error,
        Warning,
        Question,
        Status
    }

    public MuPDFAlert(String str, IconType iconType2, ButtonGroupType buttonGroupType2, String str2, ButtonPressed buttonPressed2) {
        this.message = str;
        this.iconType = iconType2;
        this.buttonGroupType = buttonGroupType2;
        this.title = str2;
        this.buttonPressed = buttonPressed2;
    }
}
