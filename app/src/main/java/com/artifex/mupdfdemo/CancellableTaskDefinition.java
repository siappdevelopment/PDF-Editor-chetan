package com.artifex.mupdfdemo;

import androidx.annotation.Keep;

@Keep
/* loaded from: classes7.dex */
public interface CancellableTaskDefinition<Params, Result> {
    void doCancel();

    void doCleanup();

    Result doInBackground(Params... paramsArr);
}
