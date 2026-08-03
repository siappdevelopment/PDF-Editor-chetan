package com.artifex.mupdfdemo;

import android.os.AsyncTask;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/* loaded from: classes7.dex */
public class CancellableAsyncTaskNew<Params, Result> {
    private final AsyncTask<Params, Void, Result> asyncTask;
    private final CancellableTaskDefinition<Params, Result> ourTask;

    public void onPostExecute(Result result) {
    }

    public void onPreExecute() {
    }

    public CancellableAsyncTaskNew(final CancellableTaskDefinition<Params, Result> task) {
        if (task == null) {
            throw new IllegalArgumentException();
        }
        this.ourTask = task;
        this.asyncTask = new AsyncTask<Params, Void, Result>() { // from class: com.artifex.mupdfdemo.CancellableAsyncTaskNew.1
            @Override // android.os.AsyncTask
            public Result doInBackground(Params... params) {
                return (Result) task.doInBackground(params);
            }

            @Override // android.os.AsyncTask
            public void onPreExecute() {
                CancellableAsyncTaskNew.this.onPreExecute();
            }

            @Override // android.os.AsyncTask
            public void onPostExecute(Result result) {
                CancellableAsyncTaskNew.this.onPostExecute(result);
                task.doCleanup();
            }

            @Override // android.os.AsyncTask
            public void onCancelled(Result result) {
                task.doCleanup();
            }
        };
    }

    public void cancel() {
        this.asyncTask.cancel(true);
        this.ourTask.doCancel();
        try {
            this.asyncTask.get();
        } catch (InterruptedException | CancellationException | ExecutionException unused) {
        }
    }

    public void execute(Params... params) {
        this.asyncTask.execute(params);
    }
}
