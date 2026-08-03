package com.artifex.mupdfdemo;

import com.artifex.mupdf.fitz.Cookie;

/* loaded from: classes7.dex */
public abstract class MuPDFCancellableTaskDefinitionNew<Params, Result> implements CancellableTaskDefinition<Params, Result> {
    private Cookie cookie = new Cookie();

    public abstract Result doInBackground(Cookie cookie, Params... params);

    @Override // com.artifex.mupdfdemo.CancellableTaskDefinition
    public void doCancel() {
        Cookie cookie = this.cookie;
        if (cookie == null) {
            return;
        }
        cookie.abort();
    }

    @Override // com.artifex.mupdfdemo.CancellableTaskDefinition
    public void doCleanup() {
        Cookie cookie = this.cookie;
        if (cookie == null) {
            return;
        }
        cookie.destroy();
        this.cookie = null;
    }

    @Override // com.artifex.mupdfdemo.CancellableTaskDefinition
    public final Result doInBackground(Params... params) {
        return doInBackground(this.cookie, params);
    }
}
