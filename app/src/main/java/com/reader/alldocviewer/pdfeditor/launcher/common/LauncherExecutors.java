package com.reader.alldocviewer.pdfeditor.launcher.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared background executor for launcher work.
 * Avoids creating a new single-thread pool on every load/resume.
 */
public final class LauncherExecutors {
    private static final ExecutorService BG = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "launcher-bg");
        t.setPriority(Thread.NORM_PRIORITY - 1);
        return t;
    });

    private LauncherExecutors() {
    }

    public static void execute(Runnable runnable) {
        if (runnable != null) {
            BG.execute(runnable);
        }
    }
}
