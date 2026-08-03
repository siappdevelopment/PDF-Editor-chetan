package com.artifex.mupdfdemo;

/* loaded from: classes7.dex */
abstract class PassClickResultVisitor {
    public abstract void visitChoice(PassClickResultChoice passClickResultChoice);

    public abstract void visitSignature(PassClickResultSignature passClickResultSignature);

    public abstract void visitText(PassClickResultText passClickResultText);
}
