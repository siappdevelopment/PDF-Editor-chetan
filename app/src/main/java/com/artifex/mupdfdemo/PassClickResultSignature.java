package com.artifex.mupdfdemo;

/* loaded from: classes7.dex */
class PassClickResultSignature extends PassClickResult {
    public final SignatureState state;

    public PassClickResultSignature(boolean z, int i) {
        super(z);
        this.state = SignatureState.values()[i];
    }

    @Override // com.artifex.mupdfdemo.PassClickResult
    public void acceptVisitor(PassClickResultVisitor passClickResultVisitor) {
        passClickResultVisitor.visitSignature(this);
    }
}
