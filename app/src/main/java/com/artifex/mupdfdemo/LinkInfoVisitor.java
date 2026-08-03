package com.artifex.mupdfdemo;

/* loaded from: classes7.dex */
public abstract class LinkInfoVisitor {
    public abstract void visitExternal(LinkInfoExternal linkInfoExternal);

    public abstract void visitInternal(LinkInfoInternal linkInfoInternal);

    public abstract void visitRemote(LinkInfoRemote linkInfoRemote);
}
