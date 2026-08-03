package com.artifex.mupdfdemo;

import java.util.concurrent.Executor;

/* loaded from: classes7.dex */
public class ThreadPerTaskExecutor implements Executor {
    @Override // java.util.concurrent.Executor
    public void execute(Runnable runnable) {
        new Thread(runnable).start();
    }
}
