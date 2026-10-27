package com.artifex.mupdfdemo;

import android.os.AsyncTask;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/* loaded from: classes7.dex */
public class CancellableAsyncTask<Params, Result> {
    private final AsyncTask<Params, Void, Result> asyncTask;
    private final CancellableTaskDefinition<Params, Result> ourTask;

    public void onPostExecute(Result result) {
    }

    public void onPreExecute() {
    }

    public CancellableAsyncTask(final CancellableTaskDefinition<Params, Result> cancellableTaskDefinition) {
        if (cancellableTaskDefinition != null) {
            this.ourTask = cancellableTaskDefinition;
            this.asyncTask = new AsyncTask<Params, Void, Result>() { // from class: com.artifex.mupdfdemo.CancellableAsyncTask.1
                @Override // android.os.AsyncTask
                public Result doInBackground(Params... paramsArr) {
                    return (Result) cancellableTaskDefinition.doInBackground(paramsArr);
                }

                @Override // android.os.AsyncTask
                public void onPreExecute() {
                    CancellableAsyncTask.this.onPreExecute();
                }

                @Override // android.os.AsyncTask
                public void onPostExecute(Result result) {
                    CancellableAsyncTask.this.onPostExecute(result);
                    cancellableTaskDefinition.doCleanup();
                }

                @Override // android.os.AsyncTask
                public void onCancelled(Result result) {
                    CancellableAsyncTask.this.ourTask.doCleanup();
                }
            };
            return;
        }
        throw new IllegalArgumentException();
    }

    public void cancelAndWait() {
        this.asyncTask.cancel(true);
        this.ourTask.doCancel();
        try {
            this.asyncTask.get();
        } catch (InterruptedException | CancellationException | ExecutionException unused) {
        }
    }

    public void execute(Params... paramsArr) {
        this.asyncTask.execute(paramsArr);
    }
}
