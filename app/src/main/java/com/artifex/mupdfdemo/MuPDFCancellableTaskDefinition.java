package com.artifex.mupdfdemo;

import java.util.Objects;

/* loaded from: classes7.dex */
public abstract class MuPDFCancellableTaskDefinition<Params, Result> implements CancellableTaskDefinition<Params, Result> {
    private MuPDFCore.Cookie cookie;

    public abstract Result doInBackground(MuPDFCore.Cookie cookie2, Params... paramsArr);

    public MuPDFCancellableTaskDefinition(MuPDFCore muPDFCore) {
        Objects.requireNonNull(muPDFCore);
        this.cookie = muPDFCore.new Cookie();
    }

    @Override // com.artifex.mupdfdemo.CancellableTaskDefinition
    public void doCancel() {
        MuPDFCore.Cookie cookie = this.cookie;
        if (cookie != null) {
            cookie.abort();
        }
    }

    @Override // com.artifex.mupdfdemo.CancellableTaskDefinition
    public void doCleanup() {
        MuPDFCore.Cookie cookie = this.cookie;
        if (cookie != null) {
            cookie.destroy();
            this.cookie = null;
        }
    }

    @Override // com.artifex.mupdfdemo.CancellableTaskDefinition
    public final Result doInBackground(Params... paramsArr) {
        return doInBackground(this.cookie, paramsArr);
    }
}
