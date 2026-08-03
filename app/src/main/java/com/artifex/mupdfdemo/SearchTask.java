package com.artifex.mupdfdemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;

import com.reader.alldocviewer.pdfeditor.R;


/* loaded from: classes7.dex */
public abstract class SearchTask {
    private static final int SEARCH_PROGRESS_DELAY = 200;
    public final AlertDialog.Builder mAlertBuilder;
    public final Context mContext;
    public final MuPDFCore mCore;
    public final Handler mHandler = new Handler();
    private AsyncTask<Void, Integer, SearchTaskResult> mSearchTask;

    public abstract void onTextFound(SearchTaskResult searchTaskResult);

    public SearchTask(Context context, MuPDFCore muPDFCore) {
        this.mContext = context;
        this.mCore = muPDFCore;
        this.mAlertBuilder = new AlertDialog.Builder(context);
    }

    public void stop() {
        AsyncTask<Void, Integer, SearchTaskResult> asyncTask = this.mSearchTask;
        if (asyncTask != null) {
            asyncTask.cancel(true);
            this.mSearchTask = null;
        }
    }

    @SuppressLint({"StaticFieldLeak"})
    public void go(final String text, final int direction, int displayPage, int searchPage) {
        if (this.mCore == null) {
            return;
        }
        stop();
        if (searchPage != -1) {
            displayPage = searchPage + direction;
        }
        final int i = displayPage;
        final ProgressDialogX progressDialogX = new ProgressDialogX(this.mContext);
        progressDialogX.setProgressStyle(1);
        progressDialogX.setTitle(this.mContext.getString(R.string.searching_));
        progressDialogX.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.artifex.mupdfdemo.SearchTask.1
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialog) {
                SearchTask.this.stop();
            }
        });
        progressDialogX.setMax(this.mCore.countPages());
        AsyncTask<Void, Integer, SearchTaskResult> asyncTask = new AsyncTask<Void, Integer, SearchTaskResult>() { // from class: com.artifex.mupdfdemo.SearchTask.2
            @Override // android.os.AsyncTask
            public SearchTaskResult doInBackground(Void... params) {
                int i2 = i;
                while (i2 >= 0 && i2 < SearchTask.this.mCore.countPages() && !isCancelled()) {
                    publishProgress(Integer.valueOf(i2));
                    RectF[] searchPage2 = SearchTask.this.mCore.searchPage(i2, text);
                    if (searchPage2 != null && searchPage2.length > 0) {
                        return new SearchTaskResult(text, i2, searchPage2, null);
                    }
                    i2 += direction;
                }
                return null;
            }

            @Override // android.os.AsyncTask
            public void onPostExecute(SearchTaskResult result) {
                progressDialogX.cancel();
                if (result != null) {
                    SearchTask.this.onTextFound(result);
                    return;
                }
                SearchTask.this.mAlertBuilder.setTitle(SearchTaskResult.get() == null ? R.string.text_not_found : R.string.no_further_occurrences_found);
                AlertDialog create = SearchTask.this.mAlertBuilder.create();
                create.setButton(-1, SearchTask.this.mContext.getString(R.string.ok), (DialogInterface.OnClickListener) null);
                create.show();
            }

            @Override // android.os.AsyncTask
            public void onCancelled() {
                progressDialogX.cancel();
            }

            @Override // android.os.AsyncTask
            public void onProgressUpdate(Integer... values) {
                progressDialogX.setProgress(values[0].intValue());
            }

            @Override // android.os.AsyncTask
            public void onPreExecute() {
                super.onPreExecute();
                SearchTask.this.mHandler.postDelayed(new Runnable() { // from class: com.artifex.mupdfdemo.SearchTask.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (progressDialogX.isCancelled()) {
                            return;
                        }
                        progressDialogX.show();
                        progressDialogX.setProgress(i);
                    }
                }, 200L);
            }
        };
        this.mSearchTask = asyncTask;
        asyncTask.execute(new Void[0]);
    }
}
