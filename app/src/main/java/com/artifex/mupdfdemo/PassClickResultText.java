package com.artifex.mupdfdemo;

/* loaded from: classes7.dex */
class PassClickResultText extends PassClickResult {
    public final String text;

    public PassClickResultText(boolean z, String str) {
        super(z);
        this.text = str;
    }

    @Override // com.artifex.mupdfdemo.PassClickResult
    public void acceptVisitor(PassClickResultVisitor passClickResultVisitor) {
        passClickResultVisitor.visitText(this);
    }
}
