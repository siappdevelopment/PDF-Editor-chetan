package com.artifex.mupdfdemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.utils.Const;
import com.reader.alldocviewer.pdfeditor.utils.MessageEventKt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;



@SuppressLint({"ViewConstructor"})
/* loaded from: classes7.dex */
public class MuPDFPageView extends PageView implements MuPDFView {
    public Runnable changeReporter;
    private AsyncTask<Object, Void, Void> mAddInk;
    private AsyncTask<Object, Void, Void> mAddStrikeOut;
    public Annotation[] mAnnotations;
    private final AlertDialog.Builder mChoiceEntryBuilder;
    public final MuPDFCore mCore;
    private AsyncTask<Integer, Void, Void> mDeleteAnnotation;
    public EditText mEditText;
    public FilePicker.FilePickerSupport mFilePickerSupport;
    private AsyncTask<Void, Void, Annotation[]> mLoadAnnotations;
    private AsyncTask<Void, Void, RectF[]> mLoadWidgetAreas;
    private ImageView mPageView;
    private AsyncTask<Void, Void, PassClickResult> mPassClick;
    private final AlertDialog mPasswordEntry;
    private AlertDialog.Builder mPasswordEntryBuilder;
    public EditText mPasswordText;
    private int mSelectedAnnotationIndex;
    private boolean mSelectedAnnotationIsSession;
    public AsyncTask<String, Void, Void> mSetWidgetChoice;
    public AsyncTask<String, Void, Boolean> mSetWidgetText;
    public AlertDialog.Builder mSignatureReportBuilder;
    private final AlertDialog.Builder mSigningDialogBuilder;
    private final AlertDialog mTextEntry;
    private AlertDialog.Builder mTextEntryBuilder;
    public RectF[] mWidgetAreas;
    protected int previosPageNumber;

    @Override // com.artifex.mupdfdemo.PageView, com.artifex.mupdfdemo.MuPDFView
    public int getPage() {
        return 0;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void getSelectedpage() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void setScale(float f) {
    }

    public MuPDFPageView(final Context c, final MuPDFCore core, final Point parentSize, final Bitmap sharedHqBm) {
        super(c, parentSize, sharedHqBm);
        this.previosPageNumber = 0;
        this.mSelectedAnnotationIndex = -1;
        this.mCore = core;
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        this.mTextEntryBuilder = builder;
        builder.setTitle(getContext().getString(R.string.fill_out_text_field));
        EditText editText = (EditText) ((LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.edittext_entry, (ViewGroup) null);
        this.mEditText = editText;
        this.mTextEntryBuilder.setView(editText);
        this.mTextEntryBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });
        this.mTextEntryBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.2
            @Override // android.content.DialogInterface.OnClickListener
            @SuppressLint({"StaticFieldLeak"})
            public void onClick(final DialogInterface dialog, final int which) {
                MuPDFPageView.this.mSetWidgetText = new AsyncTask<String, Void, Boolean>() { // from class: com.artifex.mupdfdemo.MuPDFPageView.2.1
                    @Override // android.os.AsyncTask
                    public Boolean doInBackground(final String... arg0) {
                        MuPDFPageView muPDFPageView = MuPDFPageView.this;
                        return Boolean.valueOf(muPDFPageView.mCore.setFocusedWidgetText(muPDFPageView.mPageNumber, arg0[0]));
                    }

                    @Override // android.os.AsyncTask
                    public void onPostExecute(final Boolean result) {
                        MuPDFPageView.this.changeReporter.run();
                        if (result.booleanValue()) {
                            return;
                        }
                        MuPDFPageView muPDFPageView = MuPDFPageView.this;
                        muPDFPageView.invokeTextDialog(muPDFPageView.mEditText.getText().toString());
                    }
                };
                MuPDFPageView muPDFPageView = MuPDFPageView.this;
                muPDFPageView.mSetWidgetText.execute(muPDFPageView.mEditText.getText().toString());
            }
        });
        this.mTextEntry = this.mTextEntryBuilder.create();
        AlertDialog.Builder builder2 = new AlertDialog.Builder(c);
        this.mChoiceEntryBuilder = builder2;
        builder2.setTitle(getContext().getString(R.string.choose_value));
        AlertDialog.Builder builder3 = new AlertDialog.Builder(c);
        this.mSigningDialogBuilder = builder3;
        builder3.setTitle(getContext().getString(R.string.select_certificate_and_sign));
        builder3.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });
        builder3.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(final DialogInterface dialog, final int which) {
            }
        });
        AlertDialog.Builder builder4 = new AlertDialog.Builder(c);
        this.mSignatureReportBuilder = builder4;
        builder4.setTitle(getContext().getString(R.string.signature_checked));
        this.mSignatureReportBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.5
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });
        EditText editText2 = new EditText(c);
        this.mPasswordText = editText2;
        editText2.setInputType(128);
        this.mPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        AlertDialog.Builder builder5 = new AlertDialog.Builder(c);
        this.mPasswordEntryBuilder = builder5;
        builder5.setTitle(R.string.enter_password);
        this.mPasswordEntryBuilder.setView(this.mPasswordText);
        this.mPasswordEntryBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.6
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });
        this.mPasswordEntry = this.mPasswordEntryBuilder.create();
    }

    public MuPDFPageView(Context context, FilePicker.FilePickerSupport filePickerSupport, MuPDFCore muPDFCore, Point point, Bitmap bitmap) {
        super(context, point, bitmap, muPDFCore);
        this.previosPageNumber = 0;
        this.mSelectedAnnotationIndex = -1;
        this.mFilePickerSupport = filePickerSupport;
        this.mCore = muPDFCore;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("MuPDF: " + getContext().getString(R.string.fill_out_text_field));
        EditText editText = (EditText) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.textentry, (ViewGroup) null);
        this.mEditText = editText;
        builder.setView(editText);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.7
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.8
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MuPDFPageView.this.mSetWidgetText = new AsyncTask<String, Void, Boolean>() { // from class: com.artifex.mupdfdemo.MuPDFPageView.8.1
                    @Override // android.os.AsyncTask
                    public Boolean doInBackground(String... strArr) {
                        MuPDFPageView muPDFPageView = MuPDFPageView.this;
                        return Boolean.valueOf(muPDFPageView.mCore.setFocusedWidgetText(muPDFPageView.mPageNumber, strArr[0]));
                    }

                    @Override // android.os.AsyncTask
                    public void onPostExecute(Boolean bool) {
                        MuPDFPageView.this.changeReporter.run();
                        if (bool.booleanValue()) {
                            return;
                        }
                        MuPDFPageView muPDFPageView = MuPDFPageView.this;
                        muPDFPageView.invokeTextDialog(muPDFPageView.mEditText.getText().toString());
                    }
                };
                MuPDFPageView muPDFPageView = MuPDFPageView.this;
                muPDFPageView.mSetWidgetText.execute(muPDFPageView.mEditText.getText().toString());
            }
        });
        this.mTextEntry = builder.create();
        AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
        this.mChoiceEntryBuilder = builder2;
        builder2.setTitle("MuPDF: " + getContext().getString(R.string.choose_value));
        AlertDialog.Builder builder3 = new AlertDialog.Builder(context);
        this.mSigningDialogBuilder = builder3;
        builder3.setTitle("Select certificate and sign?");
        builder3.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.9
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder3.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.10
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                new FilePicker(MuPDFPageView.this.mFilePickerSupport) { // from class: com.artifex.mupdfdemo.MuPDFPageView.10.1
                    @Override // com.artifex.mupdfdemo.FilePicker
                    public void onPick(Uri uri) {
                        MuPDFPageView.this.signWithKeyFile(uri);
                    }
                }.pick();
            }
        });
        AlertDialog.Builder builder4 = new AlertDialog.Builder(context);
        this.mSignatureReportBuilder = builder4;
        builder4.setTitle("Signature checked");
        this.mSignatureReportBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.11
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        EditText editText2 = new EditText(context);
        this.mPasswordText = editText2;
        editText2.setInputType(128);
        this.mPasswordText.setTransformationMethod(new PasswordTransformationMethod());
        AlertDialog.Builder builder5 = new AlertDialog.Builder(context);
        builder5.setTitle(R.string.enter_password);
        builder5.setView(this.mPasswordText);
        builder5.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.12
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        this.mPasswordEntry = builder5.create();
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void refresh() {
        update();
    }

    public void showSigningDialog() {
        AlertDialog.Builder builder = this.mSigningDialogBuilder;
        if (builder != null) {
            builder.create().show();
        }
    }

    public void signWithKeyFile(final Uri uri) {
        this.mPasswordEntry.getWindow().setSoftInputMode(5);
        this.mPasswordEntry.setButton(-1, "Sign", new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.13
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                MuPDFPageView muPDFPageView = MuPDFPageView.this;
                muPDFPageView.signWithKeyFileAndPassword(uri, muPDFPageView.mPasswordText.getText().toString());
            }
        });
        this.mPasswordEntry.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void signWithKeyFileAndPassword(final Uri uri, final String password) {
        new AsyncTask<Void, Void, Boolean>() { // from class: com.artifex.mupdfdemo.MuPDFPageView.14
            @Override // android.os.AsyncTask
            public Boolean doInBackground(Void... params) {
                return Boolean.valueOf(MuPDFPageView.this.mCore.signFocusedSignature(Uri.decode(uri.getEncodedPath()), password));
            }

            @Override // android.os.AsyncTask
            public void onPostExecute(Boolean result) {
                if (result.booleanValue()) {
                    MuPDFPageView.this.changeReporter.run();
                    return;
                }
                MuPDFPageView.this.mPasswordText.setText("");
                MuPDFPageView.this.signWithKeyFile(uri);
            }
        }.execute(new Void[0]);
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public LinkInfo hitLink(float f, float f2) {
        float width = (this.mSourceScale * getWidth()) / this.mSize.x;
        float left = (f - getLeft()) / width;
        float top = (f2 - getTop()) / width;
        LinkInfo[] linkInfoArr = this.mLinks;
        if (linkInfoArr == null) {
            return null;
        }
        for (LinkInfo linkInfo : linkInfoArr) {
            if (linkInfo.rect.contains(left, top)) {
                return linkInfo;
            }
        }
        return null;
    }

    public void invokeTextDialog(String str) {
        this.mEditText.setText(str);
        this.mTextEntry.getWindow().setSoftInputMode(5);
        this.mTextEntry.show();
    }

    private void invokeChoiceDialog(final String[] options) {
        this.mChoiceEntryBuilder.setItems(options, new DialogInterface.OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFPageView.15
            @Override // android.content.DialogInterface.OnClickListener
            @SuppressLint({"StaticFieldLeak"})
            public void onClick(DialogInterface dialog, int which) {
                MuPDFPageView.this.mSetWidgetChoice = new AsyncTask<String, Void, Void>() { // from class: com.artifex.mupdfdemo.MuPDFPageView.15.1
                    @Override // android.os.AsyncTask
                    public Void doInBackground(String... params) {
                        MuPDFPageView.this.mCore.setFocusedWidgetChoiceSelected(new String[]{params[0]});
                        return null;
                    }

                    @Override // android.os.AsyncTask
                    public void onPostExecute(Void result) {
                        MuPDFPageView.this.changeReporter.run();
                    }
                };
                MuPDFPageView.this.mSetWidgetChoice.execute(options[which]);
            }
        });
        this.mChoiceEntryBuilder.create().show();
    }

    private void invokeSignatureCheckingDialog() {
        new AsyncTask<Void, Void, String>() { // from class: com.artifex.mupdfdemo.MuPDFPageView.16
            @Override // android.os.AsyncTask
            public String doInBackground(Void... params) {
                return MuPDFPageView.this.mCore.checkFocusedSignature();
            }

            @Override // android.os.AsyncTask
            public void onPostExecute(String result) {
                AlertDialog create = MuPDFPageView.this.mSignatureReportBuilder.create();
                create.setMessage(result);
                create.show();
            }
        }.execute(new Void[0]);
    }

    public void invokeSigningDialog() {
        this.mSigningDialogBuilder.create().show();
    }

    public void warnNoSignatureSupport() {
        AlertDialog create = this.mSignatureReportBuilder.create();
        create.setTitle("App built with no signature support");
        create.show();
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void setChangeReporter(Runnable runnable) {
        this.changeReporter = runnable;
    }

    public String getTextAt(float f, float f2) {
        String str = null;
        if (this.mCore == null) {
            return null;
        }
        float width = (this.mSourceScale * getWidth()) / this.mSize.x;
        float left = (f - getLeft()) / width;
        float top = (f2 - getTop()) / width;
        TextWord[][] textLines = this.mCore.textLines(this.mPageNumber);
        if (textLines == null) {
            return null;
        }
        int length = textLines.length;
        float f3 = Float.MAX_VALUE;
        for (int i = 0; i < length; i++) {
            TextWord[] textWordArr = textLines[i];
            int length2 = textWordArr.length;
            int i2 = 0;
            while (i2 < length2) {
                TextWord textWord = textWordArr[i2];
                TextWord[][] textWordArr2 = textLines;
                String str2 = str;
                float f4 = left;
                float sqrt = (float) Math.sqrt(Math.pow(((textWord.toRect().left + textWord.toRect().right) / 2.0f) - left, 2.0d) + Math.pow(((textWord.toRect().top + textWord.toRect().bottom) / 2.0f) - top, 2.0d));
                if (sqrt < f3) {
                    str = textWord.f122w;
                    f3 = sqrt;
                } else {
                    str = str2;
                }
                i2++;
                textLines = textWordArr2;
                left = f4;
            }
        }
        return str;
    }

    public void ensureTextLoaded() {
        if (this.mText == null && this.mCore != null) {
            this.mText = getText();
        }
    }

    private PointData mPendingDeleteMarkup;
    private PointData mLastDeletedMarkup;

    private void rememberPendingDeleteMarkup(PointData markup) {
        this.mPendingDeleteMarkup = markup;
    }

    private void clearPendingDeleteMarkup() {
        this.mPendingDeleteMarkup = null;
    }

    public PointData getLastDeletedMarkup() {
        return this.mLastDeletedMarkup;
    }

    private static void removeMarkupFromSession(PointData pointData) {
        if (pointData == null) {
            return;
        }
        synchronized (Const.MARKUP_SESSION_LOCK) {
            List<PointData> pageMarkups = Const.savePoints.get(Integer.valueOf(pointData.pageNumber));
            if (pageMarkups != null) {
                pageMarkups.remove(pointData);
                if (pageMarkups.isEmpty()) {
                    Const.savePoints.remove(Integer.valueOf(pointData.pageNumber));
                }
            }
            Const.insertionOrderList.remove(pointData);
        }
        Const.redoStack.clear();
        Const.redoStack.add(pointData);
    }

    private static PointData findSavePointByAnnotationIndex(List<PointData> savedMarkups, int annotationIndex) {
        if (savedMarkups == null) {
            return null;
        }
        for (PointData data : savedMarkups) {
            if (data != null && data.muPdfAnnotationIndex == annotationIndex) {
                return data;
            }
            if (data != null && data.annotationIndex == annotationIndex) {
                return data;
            }
        }
        return null;
    }

    public static PointData findSavePointForMuPdfAnnotation(
            List<PointData> savedMarkups,
            Annotation annotation) {
        if (savedMarkups == null || annotation == null) {
            return null;
        }
        PointData best = null;
        float bestArea = Float.MAX_VALUE;
        for (PointData data : savedMarkups) {
            if (data == null || data.type != annotation.type) {
                continue;
            }
            if (!matchesMuPdfAnnotation(annotation, data)) {
                continue;
            }
            float width = Math.abs(annotation.right - annotation.left);
            float height = Math.abs(annotation.bottom - annotation.top);
            float area = width * height;
            if (area < bestArea) {
                bestArea = area;
                best = data;
            }
        }
        return best;
    }

    public static void syncSavePointsMuPdfIndices(MuPDFCore core) {
        if (core == null) {
            return;
        }
        for (Map.Entry<Integer, List<PointData>> entry : Const.savePoints.entrySet()) {
            syncPageSavePointsMuPdfIndices(core, entry.getKey(), entry.getValue());
        }
    }

    private static void syncPageSavePointsMuPdfIndices(
            MuPDFCore core,
            int pageNumber,
            List<PointData> markups) {
        if (markups == null || markups.isEmpty()) {
            return;
        }
        Annotation[] annotations = core.getAnnoations(pageNumber);
        if (annotations == null) {
            for (PointData data : markups) {
                if (data != null) {
                    data.muPdfAnnotationIndex = -1;
                }
            }
            return;
        }
        boolean[] used = new boolean[annotations.length];
        for (PointData data : markups) {
            if (data == null) {
                continue;
            }
            data.muPdfAnnotationIndex = -1;
            int bestIndex = -1;
            float bestArea = Float.MAX_VALUE;
            for (int i = 0; i < annotations.length; i++) {
                if (used[i]) {
                    continue;
                }
                Annotation annotation = annotations[i];
                if (annotation == null || !matchesMuPdfAnnotation(annotation, data)) {
                    continue;
                }
                float width = Math.abs(annotation.right - annotation.left);
                float height = Math.abs(annotation.bottom - annotation.top);
                float area = width * height;
                if (area < bestArea) {
                    bestArea = area;
                    bestIndex = i;
                }
            }
            if (bestIndex >= 0) {
                data.muPdfAnnotationIndex = bestIndex;
                used[bestIndex] = true;
            }
        }
    }

    private static void removeFromSavePointsByAnnotationIndex(int pageNumber, int annotationIndex) {
        List<PointData> pageMarkups = Const.savePoints.get(Integer.valueOf(pageNumber));
        if (pageMarkups == null) {
            return;
        }
        for (int i = pageMarkups.size() - 1; i >= 0; i--) {
            PointData data = pageMarkups.get(i);
            if (data != null
                    && (data.muPdfAnnotationIndex == annotationIndex
                            || data.annotationIndex == annotationIndex)) {
                removeMarkupFromSession(data);
                return;
            }
        }
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    @SuppressLint({"StaticFieldLeak"})
    public Hit passClickEvent(float f, float f2) {
        if (this.mAnnotations == null && this.mCore != null) {
            this.mAnnotations = this.mCore.getAnnoations(this.mPageNumber);
        }
        PointF contentPoint = toPageContentCoords(f, f2);
        float left = contentPoint.x;
        float top = contentPoint.y;
        List<PointData> savedMarkups = Const.savePoints.get(Integer.valueOf(this.mPageNumber));
        int savedIndex = findSavedMarkupIndexAt(left, top);
        if (savedIndex >= 0) {
            Log.i("deleteSelectedAnnotation", "passClickEvent saved markup: " + savedIndex);
            this.mSelectedAnnotationIndex = savedIndex;
            this.mSelectedAnnotationIsSession = true;
            if (savedMarkups != null && savedIndex < savedMarkups.size()) {
                PointData selected = savedMarkups.get(savedIndex);
                if (selected != null && selected.muPdfAnnotationIndex < 0) {
                    selected.muPdfAnnotationIndex = resolveMuPdfAnnotationIndex(selected);
                }
                rememberPendingDeleteMarkup(selected);
            }
            setSelectedSavedMarkupIndex(savedIndex);
            return Hit.Annotation;
        }
        clearPendingDeleteMarkup();
        setSelectedSavedMarkupIndex(-1);
        if (this.mAnnotations != null) {
            int i2 = 0;
            while (true) {
                Annotation[] annotationArr = this.mAnnotations;
                if (i2 >= annotationArr.length) {
                    break;
                }
                Annotation annotation = annotationArr[i2];
                if (annotationHits(annotation, left, top)) {
                    int typeIndex = AnonymousClass26.$SwitchMap$com$artifex$mupdfdemo$Annotation$Type[annotation.type.ordinal()];
                    if (typeIndex == 1 || typeIndex == 2 || typeIndex == 3 || typeIndex == 4 || typeIndex == 5) {
                        Log.i("deleteSelectedAnnotation", "passClickEvent: " + i2);
                        this.mSelectedAnnotationIndex = i2;
                        this.mSelectedAnnotationIsSession = false;
                        PointData matched = findSavePointForMuPdfAnnotation(savedMarkups, annotation);
                        if (matched != null) {
                            matched.muPdfAnnotationIndex = i2;
                            this.mSelectedAnnotationIsSession = true;
                        }
                        rememberPendingDeleteMarkup(matched);
                        setItemSelectBox(new RectF(annotation));
                        return Hit.Annotation;
                    }
                }
                i2++;
            }
        }
        this.mSelectedAnnotationIndex = -1;
        this.mSelectedAnnotationIsSession = false;
        if (!this.mCore.javascriptSupported()) {
            return Hit.Nothing;
        }
        boolean z = false;
        if (this.mWidgetAreas != null) {
            int i3 = 0;
            while (true) {
                RectF[] rectFArr = this.mWidgetAreas;
                if (i3 >= rectFArr.length || z) {
                    break;
                }
                if (rectFArr[i3].contains(left, top)) {
                    z = true;
                }
                i3++;
            }
        }
        if (!z) {
            return Hit.Nothing;
        }
        try {
            this.mPassClick.execute(new Void[0]);
            return Hit.Widget;
        } catch (Exception unused) {
            return Hit.Nothing;
        }
    }

    private static boolean annotationHits(Annotation annotation, float x, float y) {
        if (annotation == null) {
            return false;
        }
        float left = Math.min(annotation.left, annotation.right) - 16f;
        float right = Math.max(annotation.left, annotation.right) + 16f;
        float top = Math.min(annotation.top, annotation.bottom) - 16f;
        float bottom = Math.max(annotation.top, annotation.bottom) + 16f;
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    private int findSavedMarkupIndexAt(float x, float y) {
        List<PointData> savedMarkups = Const.savePoints.get(Integer.valueOf(this.mPageNumber));
        if (savedMarkups == null || savedMarkups.isEmpty()) {
            return -1;
        }
        for (int index = savedMarkups.size() - 1; index >= 0; index--) {
            PointData markup = savedMarkups.get(index);
            if (hitTestSavedMarkup(markup, x, y)) {
                return index;
            }
        }
        return findSavedMarkupOverlappingTextAt(x, y, savedMarkups);
    }

    private int findSavedMarkupOverlappingTextAt(float x, float y, List<PointData> savedMarkups) {
        ensureTextLoaded();
        if (this.mText == null) {
            return -1;
        }
        RectF tapRect = new RectF(x - 4f, y - 4f, x + 4f, y + 4f);
        for (TextWord[] line : this.mText) {
            if (line == null) {
                continue;
            }
            for (TextWord word : line) {
                if (word == null || !RectF.intersects(tapRect, word)) {
                    continue;
                }
                for (int index = savedMarkups.size() - 1; index >= 0; index--) {
                    if (markupIntersectsWord(savedMarkups.get(index), word)) {
                        return index;
                    }
                }
            }
        }
        return -1;
    }

    private static boolean markupIntersectsWord(PointData markup, TextWord word) {
        if (markup == null || word == null) {
            return false;
        }
        if (markup.type == Annotation.Type.INK && markup.inkPaths != null) {
            for (PointF[] stroke : markup.inkPaths) {
                RectF strokeBounds = boundsOfStroke(stroke);
                if (strokeBounds != null && RectF.intersects(strokeBounds, word)) {
                    return true;
                }
            }
            return false;
        }
        if (markup.quadPoints == null) {
            return false;
        }
        for (int i = 0; i + 3 < markup.quadPoints.length; i += 4) {
            RectF quadBounds = boundsOfQuad(markup.quadPoints, i);
            if (quadBounds != null && RectF.intersects(quadBounds, word)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSavedMarkups() {
        List<PointData> savedMarkups = Const.savePoints.get(Integer.valueOf(this.mPageNumber));
        return savedMarkups != null && !savedMarkups.isEmpty();
    }

    public boolean selectSavedMarkupFromTextSelection() {
        if (this.mSelectBox == null || this.mSelectBox.isEmpty()) {
            return false;
        }
        List<PointData> savedMarkups = Const.savePoints.get(Integer.valueOf(this.mPageNumber));
        if (savedMarkups == null || savedMarkups.isEmpty()) {
            return false;
        }
        for (int index = savedMarkups.size() - 1; index >= 0; index--) {
            PointData markup = savedMarkups.get(index);
            if (!markupIntersectsSelection(markup, this.mSelectBox)) {
                continue;
            }
            this.mSelectedAnnotationIndex = index;
            this.mSelectedAnnotationIsSession = true;
            if (markup.muPdfAnnotationIndex < 0) {
                markup.muPdfAnnotationIndex = resolveMuPdfAnnotationIndex(markup);
            }
            rememberPendingDeleteMarkup(markup);
            setSelectedSavedMarkupIndex(index);
            return true;
        }
        return false;
    }

    private static boolean markupIntersectsSelection(PointData markup, RectF selection) {
        if (markup == null || selection == null || selection.isEmpty()) {
            return false;
        }
        if (markup.type == Annotation.Type.INK && markup.inkPaths != null) {
            for (PointF[] stroke : markup.inkPaths) {
                RectF strokeBounds = boundsOfStroke(stroke);
                if (strokeBounds != null && RectF.intersects(strokeBounds, selection)) {
                    return true;
                }
            }
            return false;
        }
        if (markup.quadPoints == null) {
            return false;
        }
        for (int i = 0; i + 3 < markup.quadPoints.length; i += 4) {
            RectF quadBounds = boundsOfQuad(markup.quadPoints, i);
            if (quadBounds != null && RectF.intersects(quadBounds, selection)) {
                return true;
            }
        }
        return false;
    }

    /* renamed from: com.artifex.mupdfdemo.MuPDFPageView$26  reason: invalid class name */
    /* loaded from: classes7.dex */
    public static /* synthetic */ class AnonymousClass26 {
        static final /* synthetic */ int[] $SwitchMap$com$artifex$mupdfdemo$Annotation$Type;

        static {
            int[] iArr = new int[Annotation.Type.values().length];
            $SwitchMap$com$artifex$mupdfdemo$Annotation$Type = iArr;
            try {
                iArr[Annotation.Type.HIGHLIGHT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$artifex$mupdfdemo$Annotation$Type[Annotation.Type.UNDERLINE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$artifex$mupdfdemo$Annotation$Type[Annotation.Type.SQUIGGLY.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$artifex$mupdfdemo$Annotation$Type[Annotation.Type.STRIKEOUT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$artifex$mupdfdemo$Annotation$Type[Annotation.Type.INK.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public boolean copyTextFromSelectBox(float f, float f2) {
        String textFromAnnotation = null;
        if (this.mSelectedAnnotationIndex != -1) {
            if (this.mSelectedAnnotationIsSession) {
                List<PointData> savedMarkups = Const.savePoints.get(Integer.valueOf(this.mPageNumber));
                if (savedMarkups != null && this.mSelectedAnnotationIndex >= 0 && this.mSelectedAnnotationIndex < savedMarkups.size()) {
                    textFromAnnotation = getTextFromPointData(savedMarkups.get(this.mSelectedAnnotationIndex));
                }
            } else if (this.mAnnotations != null && this.mSelectedAnnotationIndex >= 0 && this.mSelectedAnnotationIndex < this.mAnnotations.length) {
                textFromAnnotation = getTextFromAnnotation(this.mSelectedAnnotationIndex);
            }
        }
        if (textFromAnnotation == null || textFromAnnotation.isEmpty()) {
            return false;
        }
        ((ClipboardManager) this.mContext.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("MuPDF", textFromAnnotation));
        setItemSelectBox(null);
        return true;
    }

    private String getTextFromPointData(PointData data) {
        if (data == null || data.quadPoints == null || data.quadPoints.length == 0) {
            return null;
        }
        TextWord[][] textLines = getText();
        if (textLines == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        HashSet<String> hashSet = new HashSet<>();
        for (TextWord[] line : textLines) {
            for (TextWord word : line) {
                for (int i = 0; i + 3 < data.quadPoints.length; i += 4) {
                    RectF quadBounds = boundsOfQuad(data.quadPoints, i);
                    if (quadBounds != null) {
                        quadBounds.inset(-2f, -2f);
                        if (RectF.intersects(quadBounds, word.toRect())) {
                            if (hashSet.add(word.f122w)) {
                                sb.append(word.f122w).append(" ");
                            }
                            break;
                        }
                    }
                }
            }
        }
        return sb.toString().trim();
    }



    public String getTextFromAnnotation(int annotationIndex) {
        Annotation[] annotationArr;
        MuPDFCore muPDFCore = this.mCore;
        if (muPDFCore == null || (annotationArr = this.mAnnotations) == null || annotationIndex < 0 || annotationIndex >= annotationArr.length) {
            return null;
        }
        Annotation annotation = annotationArr[annotationIndex];
        float f = ((RectF) annotation).left;
        float f2 = ((RectF) annotation).right;
        float f3 = ((RectF) annotation).top;
        float f4 = ((RectF) annotation).bottom;
        TextWord[][] textLines = muPDFCore.textLines(this.mPageNumber);
        if (textLines == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        HashSet hashSet = new HashSet();
        int length = textLines.length;
        for (int i = 0; i < length; i++) {
            TextWord[] textWordArr = textLines[i];
            int length2 = textWordArr.length;
            int i2 = 0;
            while (i2 < length2) {
                TextWord textWord = textWordArr[i2];
                float f5 = textWord.toRect().left;
                float f6 = textWord.toRect().right;
                float f7 = textWord.toRect().top;
                TextWord[][] textWordArr2 = textLines;
                float f8 = textWord.toRect().bottom;
                if (f5 >= f && f6 <= f2 && f7 >= f3 && f8 <= f4 && hashSet.add(textWord.f122w)) {
                    sb.append(textWord.f122w);
                    sb.append(" ");
                }
                i2++;
                textLines = textWordArr2;
            }
        }
        return sb.toString().trim();
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    @SuppressLint({"WrongConstant"})
    public boolean copySelection() {
        final StringBuilder sb = new StringBuilder();
        processSelectedText(new TextProcessor() { // from class: com.artifex.mupdfdemo.MuPDFPageView.17
            StringBuilder line;

            @Override // com.artifex.mupdfdemo.TextProcessor
            public void onStartLine() {
                this.line = new StringBuilder();
            }

            @Override // com.artifex.mupdfdemo.TextProcessor
            public void onWord(TextWord textWord) {
                if (this.line.length() > 0) {
                    this.line.append(' ');
                }
                this.line.append(textWord.f122w);
            }

            @Override // com.artifex.mupdfdemo.TextProcessor
            public void onEndLine() {
                if (sb.length() > 0) {
                    sb.append(10);
                }
                sb.append((CharSequence) this.line);
            }
        });
        if (sb.length() == 0) {
            return false;
        }
        ((ClipboardManager) this.mContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("MuPDF", sb));
        deselectText();
        return true;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    @SuppressLint({"StaticFieldLeak"})
    public boolean markupSelection(final Annotation.Type type) {
        return markupSelection(type, getInkColor());
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    @SuppressLint({"StaticFieldLeak"})
    public boolean markupSelection(final Annotation.Type type, final int color) {
        setInkColor(color);
        ensureTextLoaded();
        final ArrayList<PointF> arrayList = new ArrayList<>();
        processSelectedText(new TextProcessor() {
            private RectF lineRect = null;

            @Override
            public void onStartLine() {
                lineRect = null;
            }

            @Override
            public void onWord(TextWord word) {
                if (lineRect == null) {
                    lineRect = new RectF(word);
                } else {
                    lineRect.union(word);
                }
            }

            @Override
            public void onEndLine() {
                if (lineRect != null && !lineRect.isEmpty()) {
                    arrayList.add(new PointF(lineRect.left, lineRect.bottom));
                    arrayList.add(new PointF(lineRect.right, lineRect.bottom));
                    arrayList.add(new PointF(lineRect.right, lineRect.top));
                    arrayList.add(new PointF(lineRect.left, lineRect.top));
                }
            }
        });
        Log.i("markupSelection", "markupSelection33: " + arrayList);
        if (arrayList.isEmpty()) {
            return false;
        }
        addMarkup((PointF[]) arrayList.toArray(new PointF[0]), type, color);
        deselectText();
        return true;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void redo() {
        redoLastUndo();
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void undo() {
        loadAnnotation();
        deleteSelectedAnnotationUndo();
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    @SuppressLint({"StaticFieldLeak"})
    public void deleteSelectedAnnotation() {
        Log.i("deleteSelectedAnnotation", "Selected index: " + this.mSelectedAnnotationIndex + ", Task: " + this.mDeleteAnnotation);
        PointData deleteTarget = this.mPendingDeleteMarkup;
        if (deleteTarget == null && this.mSelectedAnnotationIsSession) {
            List<PointData> pageMarkups = Const.savePoints.get(Integer.valueOf(this.mPageNumber));
            if (pageMarkups != null
                    && this.mSelectedAnnotationIndex >= 0
                    && this.mSelectedAnnotationIndex < pageMarkups.size()) {
                deleteTarget = pageMarkups.get(this.mSelectedAnnotationIndex);
            }
        }
        if (deleteTarget == null && !this.mSelectedAnnotationIsSession && this.mSelectedAnnotationIndex >= 0) {
            if (this.mAnnotations == null && this.mCore != null) {
                this.mAnnotations = this.mCore.getAnnoations(this.mPageNumber);
            }
            if (this.mAnnotations != null
                    && this.mSelectedAnnotationIndex < this.mAnnotations.length) {
                deleteTarget = findSavePointForMuPdfAnnotation(
                        Const.savePoints.get(Integer.valueOf(this.mPageNumber)),
                        this.mAnnotations[this.mSelectedAnnotationIndex]);
            }
            if (deleteTarget == null) {
                deleteTarget = findSavePointByAnnotationIndex(
                        Const.savePoints.get(Integer.valueOf(this.mPageNumber)),
                        this.mSelectedAnnotationIndex);
            }
            if (deleteTarget == null && this.mAnnotations != null && this.mSelectedAnnotationIndex < this.mAnnotations.length) {
                Annotation ann = this.mAnnotations[this.mSelectedAnnotationIndex];
                PointF[] dummyQuads = new PointF[] {
                        new PointF(ann.left, ann.bottom),
                        new PointF(ann.right, ann.bottom),
                        new PointF(ann.right, ann.top),
                        new PointF(ann.left, ann.top)
                };
                deleteTarget = new PointData(dummyQuads, ann.type, 0, this.mPageNumber, this.mSelectedAnnotationIndex);
                deleteTarget.muPdfAnnotationIndex = this.mSelectedAnnotationIndex;
            }
        }
        if (deleteTarget == null && this.mSelectedAnnotationIndex == -1) {
            return;
        }
        this.mLastDeletedMarkup = deleteTarget;
        AsyncTask<Integer, Void, Void> asyncTask = this.mDeleteAnnotation;
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        deselectText();
        clearPendingDeleteMarkup();
        setSelectedSavedMarkupIndex(-1);
        setItemSelectBox(null);
        this.mSelectedAnnotationIndex = -1;
        this.mSelectedAnnotationIsSession = false;
    }

    private void finishDeleteRefresh() {
        deselectText();
        refreshSavedMarkups();
        update();
    }

    public void ensureSavePointsMatchPdfAnnotations() {
        refreshSavePointsForPageFromMuPdf();
    }

    public boolean hasPdfMarkupAnnotations() {
        if (this.mCore == null) {
            return false;
        }
        Annotation[] annotations = this.mCore.getAnnoations(this.mPageNumber);
        if (annotations == null) {
            return false;
        }
        for (Annotation annotation : annotations) {
            if (annotation != null && isEditableMarkupType(annotation.type)) {
                return true;
            }
        }
        return false;
    }

    public void refreshSavePointsForPageFromMuPdf() {
        if (this.mCore == null) {
            return;
        }
        Annotation[] annotations = this.mCore.getAnnoations(this.mPageNumber);
        synchronized (Const.MARKUP_SESSION_LOCK) {
            List<PointData> existing = Const.savePoints.get(Integer.valueOf(this.mPageNumber));
            List<PointData> rebuilt = new ArrayList<>();
            if (existing != null) {
                for (PointData data : existing) {
                    if (data != null && Const.insertionOrderList.contains(data)) {
                        rebuilt.add(data);
                    }
                }
            }
            if (annotations != null) {
                for (int i = 0; i < annotations.length; i++) {
                    Annotation annotation = annotations[i];
                    if (annotation == null || !isEditableMarkupType(annotation.type)) {
                        continue;
                    }
                    PointData matched = findSavePointForMuPdfAnnotation(existing, annotation);
                    PointData data;
                    if (matched != null && !Const.insertionOrderList.contains(matched)) {
                        data = matched;
                    } else if (matched != null) {
                        continue;
                    } else {
                        data = createPointDataFromAnnotation(annotation, this.mPageNumber, i);
                    }
                    data.muPdfAnnotationIndex = i;
                    if (!rebuilt.contains(data)) {
                        rebuilt.add(data);
                    }
                }
            }
            if (rebuilt.isEmpty()) {
                Const.savePoints.remove(Integer.valueOf(this.mPageNumber));
            } else {
                Const.savePoints.put(Integer.valueOf(this.mPageNumber), rebuilt);
            }
        }
        this.mAnnotations = this.mCore.getAnnoations(this.mPageNumber);
    }

    private void pruneStaleSavePointsForPage(int pageNumber) {
        if (this.mCore == null) {
            return;
        }
        Annotation[] annotations = this.mCore.getAnnoations(pageNumber);
        synchronized (Const.MARKUP_SESSION_LOCK) {
            List<PointData> pageMarkups = Const.savePoints.get(Integer.valueOf(pageNumber));
            if (pageMarkups == null) {
                return;
            }
            for (int i = pageMarkups.size() - 1; i >= 0; i--) {
                PointData data = pageMarkups.get(i);
                if (data == null) {
                    pageMarkups.remove(i);
                    continue;
                }
                if (Const.insertionOrderList.contains(data)) {
                    continue;
                }
                boolean found = false;
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        if (annotation != null && matchesMuPdfAnnotation(annotation, data)) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    pageMarkups.remove(i);
                }
            }
            if (pageMarkups.isEmpty()) {
                Const.savePoints.remove(Integer.valueOf(pageNumber));
            }
        }
    }

    private static boolean isEditableMarkupType(Annotation.Type type) {
        return type == Annotation.Type.HIGHLIGHT
                || type == Annotation.Type.UNDERLINE
                || type == Annotation.Type.STRIKEOUT
                || type == Annotation.Type.INK;
    }

    public void invalidateAnnotationCache() {
        this.mAnnotations = null;
    }

    private int resolveMuPdfAnnotationIndex(PointData target) {
        if (this.mAnnotations == null && this.mCore != null) {
            this.mAnnotations = this.mCore.getAnnoations(this.mPageNumber);
        }
        if (this.mAnnotations == null) {
            return -1;
        }
        if (target != null) {
            for (int i = 0; i < this.mAnnotations.length; i++) {
                Annotation annotation = this.mAnnotations[i];
                if (annotation != null && matchesMuPdfAnnotation(annotation, target)) {
                    target.muPdfAnnotationIndex = i;
                    return i;
                }
            }
        }
        if (!this.mSelectedAnnotationIsSession
                && this.mSelectedAnnotationIndex >= 0
                && this.mSelectedAnnotationIndex < this.mAnnotations.length) {
            return this.mSelectedAnnotationIndex;
        }
        return -1;
    }

    private void reSyncSavePointsMuPdfIndicesForPage(int pageNumber) {
        if (this.mCore == null) {
            return;
        }
        syncPageSavePointsMuPdfIndices(
                this.mCore,
                pageNumber,
                Const.savePoints.get(Integer.valueOf(pageNumber)));
    }

    static boolean matchesMuPdfAnnotation(Annotation annotation, PointData target) {
        if (annotation == null || target == null || annotation.type != target.type) {
            return false;
        }
        float left = Math.min(annotation.left, annotation.right);
        float right = Math.max(annotation.left, annotation.right);
        float top = Math.min(annotation.top, annotation.bottom);
        float bottom = Math.max(annotation.top, annotation.bottom);
        RectF annotRect = new RectF(left, top, right, bottom);
        annotRect.inset(-8f, -8f);
        if (target.type == Annotation.Type.INK && target.inkPaths != null) {
            for (PointF[] stroke : target.inkPaths) {
                RectF strokeBounds = boundsOfStroke(stroke);
                if (strokeBounds != null && RectF.intersects(annotRect, strokeBounds)) {
                    return true;
                }
            }
            return false;
        }
        if (target.quadPoints == null) {
            return false;
        }
        for (int i = 0; i + 3 < target.quadPoints.length; i += 4) {
            RectF quadBounds = boundsOfQuad(target.quadPoints, i);
            if (quadBounds != null && RectF.intersects(annotRect, quadBounds)) {
                return true;
            }
        }
        return false;
    }

    public static PointF getStartPoint(PointF[] quadPoints) {
        if (quadPoints == null || quadPoints.length == 0) {
            return null;
        }
        PointF pointF = quadPoints[0];
        PointF pointF2 = new PointF(pointF.x, pointF.y);
        for (PointF pointF3 : quadPoints) {
            float f = pointF3.y;
            float f2 = pointF2.y;
            if (f < f2 || (f == f2 && pointF3.x < pointF2.x)) {
                pointF2 = pointF3;
            }
        }
        return pointF2;
    }

    public static PointF getEndPoint(PointF[] quadPoints) {
        if (quadPoints == null || quadPoints.length == 0) {
            return null;
        }
        PointF pointF = quadPoints[0];
        PointF pointF2 = new PointF(pointF.x, pointF.y);
        for (PointF pointF3 : quadPoints) {
            float f = pointF3.y;
            float f2 = pointF2.y;
            if (f > f2 || (f == f2 && pointF3.x > pointF2.x)) {
                pointF2 = pointF3;
            }
        }
        return pointF2;
    }

    @SuppressLint({"StaticFieldLeak"})
    public synchronized void deleteSelectedAnnotationUndo() {
    }

    public synchronized void redoLastUndo() {
        List<PointData> list = Const.redoStack;
        if (list.isEmpty()) {
            Log.e("redo", "No redo actions available.");
            return;
        }
        PointData remove = list.remove(list.size() - 1);
        int i = remove.pageNumber;
        synchronized (Const.MARKUP_SESSION_LOCK) {
            Map<Integer, List<PointData>> map = Const.savePoints;
            List<PointData> list2 = map.get(Integer.valueOf(i));
            if (list2 == null) {
                list2 = new ArrayList<>();
                map.put(Integer.valueOf(i), list2);
            }
            list2.add(remove);
            Const.insertionOrderList.add(remove);
        }
        refreshSavedMarkups();
        if (!list.isEmpty()) {
            int i2 = list.get(list.size() - 1).pageNumber;
            Log.i("redoLastUndo", "redoLastUndo: " + i2);
            if (i2 != i) {
                MessageEventKt.sendEvent("changeCurrentPage", Integer.valueOf(i2));
            }
        }
    }

    private static boolean hitTestSavedMarkup(PointData data, float x, float y) {
        if (data == null) {
            return false;
        }
        if (data.type == Annotation.Type.INK && data.inkPaths != null) {
            float hitSlop = 14f;
            for (PointF[] stroke : data.inkPaths) {
                RectF strokeBounds = boundsOfStroke(stroke);
                if (strokeBounds == null) {
                    continue;
                }
                strokeBounds.inset(-hitSlop, -hitSlop);
                if (strokeBounds.contains(x, y)) {
                    return true;
                }
            }
            return false;
        }
        if (data.quadPoints == null) {
            return false;
        }
        for (int i = 0; i + 3 < data.quadPoints.length; i += 4) {
            RectF quadBounds = boundsOfQuad(data.quadPoints, i);
            if (quadBounds == null) {
                continue;
            }
            if (data.type == Annotation.Type.UNDERLINE || data.type == Annotation.Type.STRIKEOUT) {
                quadBounds.inset(-24f, -32f);
            } else {
                quadBounds.inset(-16f, -16f);
            }
            if (quadBounds.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    private static RectF boundsOfQuad(PointF[] quadPoints, int start) {
        RectF bounds = null;
        for (int i = start; i < start + 4 && i < quadPoints.length; i++) {
            PointF point = quadPoints[i];
            if (point != null) {
                if (bounds == null) {
                    bounds = new RectF(point.x, point.y, point.x, point.y);
                } else {
                    bounds.union(point.x, point.y);
                }
            }
        }
        return (bounds == null || bounds.isEmpty()) ? null : bounds;
    }

    private static RectF boundsOfStroke(PointF[] stroke) {
        if (stroke == null || stroke.length == 0) {
            return null;
        }
        RectF bounds = null;
        for (PointF point : stroke) {
            if (point == null) {
                continue;
            }
            if (bounds == null) {
                bounds = new RectF(point.x, point.y, point.x, point.y);
            } else {
                bounds.union(point.x, point.y);
            }
        }
        return bounds;
    }

    private static RectF boundsForSavedMarkup(PointData data) {
        if (data == null) {
            return null;
        }
        if (data.type == Annotation.Type.INK && data.inkPaths != null) {
            RectF bounds = null;
            for (PointF[] stroke : data.inkPaths) {
                if (stroke == null) {
                    continue;
                }
                for (PointF point : stroke) {
                    if (point == null) {
                        continue;
                    }
                    if (bounds == null) {
                        bounds = new RectF(point.x, point.y, point.x, point.y);
                    } else {
                        bounds.union(point.x, point.y);
                    }
                }
            }
            return bounds;
        }
        if (data.quadPoints == null || data.quadPoints.length == 0) {
            return null;
        }
        RectF bounds = null;
        for (PointF point : data.quadPoints) {
            if (point != null) {
                if (bounds == null) {
                    bounds = new RectF(point.x, point.y, point.x, point.y);
                } else {
                    bounds.union(point.x, point.y);
                }
            }
        }
        return bounds;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void deselectAnnotation() {
        this.mSelectedAnnotationIndex = -1;
        this.mSelectedAnnotationIsSession = false;
        setSelectedSavedMarkupIndex(-1);
        setItemSelectBox(null);
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void loadAnnotation() {
        loadAnnotations();
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    @SuppressLint({"StaticFieldLeak"})
    public boolean saveDraw() {
        PointF[][] draw = getDraw();
        if (draw == null) {
            return false;
        }
        AsyncTask<Object, Void, Void> asyncTask = this.mAddInk;
        if (asyncTask != null) {
            asyncTask.cancel(true);
            this.mAddInk = null;
        }
        PointData pointData = new PointData(draw, Annotation.Type.INK, getInkColor(), this.mPageNumber, -1);
        pointData.inkThickness = getPaintStrokeWidth();
        synchronized (Const.MARKUP_SESSION_LOCK) {
            Const.redoStack.clear();
            Map<Integer, List<PointData>> map = Const.savePoints;
            List<PointData> list = map.get(Integer.valueOf(this.mPageNumber));
            if (list == null) {
                list = new ArrayList<>();
                map.put(Integer.valueOf(this.mPageNumber), list);
            }
            list.add(pointData);
            Const.insertionOrderList.add(pointData);
        }
        AsyncTask<Object, Void, Void> asyncTask2 = new AsyncTask<Object, Void, Void>() { // from class: com.artifex.mupdfdemo.MuPDFPageView.21
            @Override // android.os.AsyncTask
            public Void doInBackground(final Object... params) {
                return null;
            }

            @Override // android.os.AsyncTask
            public void onPostExecute(final Void result) {
                MuPDFPageView.this.refreshSavedMarkups();
            }
        };
        this.mAddInk = asyncTask2;
        asyncTask2.execute(draw, Integer.valueOf(getInkColor()), Float.valueOf(getInkThickness()));
        cancelDraw();
        return true;
    }

    @Override // com.artifex.mupdfdemo.PageView
    public CancellableTaskDefinition<Void, Void> getDrawPageTask(final Bitmap bitmap, final int i, final int i2, final int i3, final int i4, final int i5, final int i6) {
        return new MuPDFCancellableTaskDefinition<Void, Void>(this.mCore) { // from class: com.artifex.mupdfdemo.MuPDFPageView.22
            @Override // com.artifex.mupdfdemo.MuPDFCancellableTaskDefinition
            public Void doInBackground(MuPDFCore.Cookie cookie, Void... voidArr) {
                if (cookie != null) {
                    try {
                        MuPDFPageView muPDFPageView = MuPDFPageView.this;
                        muPDFPageView.mCore.drawPage(bitmap, muPDFPageView.mPageNumber, i, i2, i3, i4, i5, i6, cookie);
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                return null;
            }
        };
    }

    @Override // com.artifex.mupdfdemo.PageView
    public CancellableTaskDefinition<Void, Void> getUpdatePageTask(final Bitmap bitmap, final int i, final int i2, final int i3, final int i4, final int i5, final int i6) {
        return new MuPDFCancellableTaskDefinition<Void, Void>(this.mCore) { // from class: com.artifex.mupdfdemo.MuPDFPageView.23
            @Override // com.artifex.mupdfdemo.MuPDFCancellableTaskDefinition
            public Void doInBackground(MuPDFCore.Cookie cookie, Void... voidArr) {
                if (cookie != null) {
                    try {
                        MuPDFPageView muPDFPageView = MuPDFPageView.this;
                        muPDFPageView.mCore.updatePage(bitmap, muPDFPageView.mPageNumber, i, i2, i3, i4, i5, i6, cookie);
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                return null;
            }
        };
    }

    @Override // com.artifex.mupdfdemo.PageView
    public LinkInfo[] getLinkInfo() {
        try {
            int i = this.mPageNumber;
            this.previosPageNumber = i;
            return this.mCore.getPageLinks(i);
        } catch (Exception unused) {
            return this.mCore.getPageLinks(this.previosPageNumber);
        }
    }

    @Override // com.artifex.mupdfdemo.PageView
    public TextWord[][] getText() {
        TextWord[][] rawText = this.mCore.textLines(this.mPageNumber);
        return sortTextLinesHorizontally(rawText);
    }

    private TextWord[][] sortTextLinesHorizontally(TextWord[][] rawText) {
        if (rawText == null || rawText.length == 0) {
            return rawText;
        }
        
        ArrayList<TextWord> flatWords = new ArrayList<>();
        for (TextWord[] line : rawText) {
            if (line == null) continue;
            for (TextWord word : line) {
                if (word != null) {
                    flatWords.add(word);
                }
            }
        }
        
        if (flatWords.isEmpty()) {
            return rawText;
        }
        
        Collections.sort(flatWords, new Comparator<TextWord>() {
            @Override
            public int compare(TextWord w1, TextWord w2) {
                return Float.compare(((RectF) w1).top, ((RectF) w2).top);
            }
        });
        
        ArrayList<ArrayList<TextWord>> lines = new ArrayList<>();
        for (TextWord word : flatWords) {
            RectF wordRect = (RectF) word;
            boolean added = false;
            
            for (ArrayList<TextWord> line : lines) {
                float lineTop = Float.MAX_VALUE;
                float lineBottom = Float.MIN_VALUE;
                for (TextWord lw : line) {
                    RectF lwRect = (RectF) lw;
                    if (lwRect.top < lineTop) lineTop = lwRect.top;
                    if (lwRect.bottom > lineBottom) lineBottom = lwRect.bottom;
                }
                
                float wordHeight = wordRect.bottom - wordRect.top;
                float overlapTop = Math.max(wordRect.top, lineTop);
                float overlapBottom = Math.min(wordRect.bottom, lineBottom);
                float overlap = overlapBottom - overlapTop;
                
                if (overlap > 0.5f * wordHeight) {
                    line.add(word);
                    added = true;
                    break;
                }
            }
            
            if (!added) {
                ArrayList<TextWord> newLine = new ArrayList<>();
                newLine.add(word);
                lines.add(newLine);
            }
        }
        
        for (ArrayList<TextWord> line : lines) {
            Collections.sort(line, new Comparator<TextWord>() {
                @Override
                public int compare(TextWord w1, TextWord w2) {
                    return Float.compare(((RectF) w1).left, ((RectF) w2).left);
                }
            });
        }
        
        Collections.sort(lines, new Comparator<ArrayList<TextWord>>() {
            @Override
            public int compare(ArrayList<TextWord> l1, ArrayList<TextWord> l2) {
                float t1 = 0;
                for (TextWord w : l1) t1 += ((RectF) w).top;
                t1 /= l1.size();
                
                float t2 = 0;
                for (TextWord w : l2) t2 += ((RectF) w).top;
                t2 /= l2.size();
                
                return Float.compare(t1, t2);
            }
        });
        
        TextWord[][] sortedText = new TextWord[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            ArrayList<TextWord> line = lines.get(i);
            sortedText[i] = line.toArray(new TextWord[0]);
        }
        
        return sortedText;
    }

    @Override // com.artifex.mupdfdemo.PageView
    public void addMarkup(final PointF[] quadPoints, final Annotation.Type type, int color) {
        PointRangeUtil.PointRange startAndEndPoint = PointRangeUtil.getStartAndEndPoint(quadPoints);
        if (startAndEndPoint != null) {
            Log.d("addMarkupCheck", "Start Point: (" + startAndEndPoint.startPoint.x + ", " + startAndEndPoint.startPoint.y + ")");
            Log.d("addMarkupCheck", "End Point: (" + startAndEndPoint.endPoint.x + ", " + startAndEndPoint.endPoint.y + ")");
        }
        if (quadPoints == null) {
            Log.d("addMarkup", "quadPoints is null");
        } else {
            for (int i = 0; i < quadPoints.length; i++) {
                PointF pointF = quadPoints[i];
                Log.d("addMarkup", "Point " + i + ": (" + pointF.x + ", " + pointF.y + ")");
            }
        }
        PointData pointData = new PointData(quadPoints, type, color, this.mPageNumber, this.mSelectedAnnotationIndex);
        synchronized (Const.MARKUP_SESSION_LOCK) {
            Const.redoStack.clear();
            Map<Integer, List<PointData>> map = Const.savePoints;
            List<PointData> list = map.get(Integer.valueOf(this.mPageNumber));
            if (list == null) {
                list = new ArrayList<>();
                map.put(Integer.valueOf(this.mPageNumber), list);
            }
            list.add(pointData);
            Const.insertionOrderList.add(pointData);
        }
        try {
            Log.i("addMarkup", "addMarkup: " + quadPoints);
            refreshSavedMarkups();
        } catch (Exception unused) {
        }
        if (this.mAnnotations != null) {
            Log.i("addMarkup", "addMarkupbottom: " + this.mAnnotations.length);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    @SuppressLint({"StaticFieldLeak"})
    public void loadAnnotations() {
        this.mAnnotations = null;
        AsyncTask<Void, Void, Annotation[]> asyncTask = this.mLoadAnnotations;
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        AsyncTask<Void, Void, Annotation[]> asyncTask2 = new AsyncTask<Void, Void, Annotation[]>() { // from class: com.artifex.mupdfdemo.MuPDFPageView.24
            @Override // android.os.AsyncTask
            public Annotation[] doInBackground(Void... params) {
                MuPDFPageView muPDFPageView = MuPDFPageView.this;
                return muPDFPageView.mCore.getAnnoations(muPDFPageView.mPageNumber);
            }

            @Override // android.os.AsyncTask
            public void onPostExecute(Annotation[] result) {
                MuPDFPageView.this.mAnnotations = result;
                MuPDFPageView.this.importAnnotationsToSession(result);
                MuPDFPageView.this.refreshSavedMarkups();
                MuPDFPageView.this.update();
            }
        };
        this.mLoadAnnotations = asyncTask2;
        asyncTask2.execute(new Void[0]);
    }

    private void importAnnotationsToSession(Annotation[] annotations) {
        if (Const.markupLoadedFromPdf) {
            return;
        }
        if (annotations == null || annotations.length == 0 || this.mPageNumber < 0) {
            return;
        }
        List<PointData> saved = Const.savePoints.get(Integer.valueOf(this.mPageNumber));
        if (saved != null && !saved.isEmpty()) {
            return;
        }
        if (saved == null) {
            saved = new ArrayList<>();
            Const.savePoints.put(Integer.valueOf(this.mPageNumber), saved);
        }
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            if (annotation == null) {
                continue;
            }
            int typeIndex = AnonymousClass26.$SwitchMap$com$artifex$mupdfdemo$Annotation$Type[annotation.type.ordinal()];
            if (typeIndex != 1 && typeIndex != 2 && typeIndex != 3 && typeIndex != 4 && typeIndex != 5) {
                continue;
            }
            if (hasImportedAnnotation(saved, i)) {
                continue;
            }
            saved.add(createPointDataFromAnnotation(annotation, this.mPageNumber, i));
        }
    }

    private static boolean hasImportedAnnotation(List<PointData> saved, int annotationIndex) {
        for (PointData data : saved) {
            if (data != null && data.annotationIndex == annotationIndex) {
                return true;
            }
        }
        return false;
    }

    private static PointData createPointDataFromAnnotation(Annotation annotation, int pageNumber, int annotationIndex) {
        float left = Math.min(annotation.left, annotation.right);
        float right = Math.max(annotation.left, annotation.right);
        float top = Math.min(annotation.top, annotation.bottom);
        float bottom = Math.max(annotation.top, annotation.bottom);
        PointF[] quads = new PointF[] {
                new PointF(left, bottom),
                new PointF(right, bottom),
                new PointF(right, top),
                new PointF(left, top),
        };
        int color;
        switch (annotation.type) {
            case UNDERLINE:
                color = 0xFF0000FF;
                break;
            case STRIKEOUT:
                color = 0xFFFF0000;
                break;
            case INK:
                color = 0xFF000000;
                break;
            case HIGHLIGHT:
            default:
                color = 0xFFFFFF00;
                break;
        }
        return new PointData(quads, annotation.type, color, pageNumber, annotationIndex);
    }

    @SuppressLint({"StaticFieldLeak"})
    private void invalidatePage(int pageNumber) {
        if (pageNumber >= 0) {
            invalidate();
        }
    }

    private void refreshLastPage(int pageNumber) {
        getTotalPages();
    }

    private int getTotalPages() {
        return this.mCore.countPages();
    }

    @SuppressLint({"StaticFieldLeak"})
    public void setPage(final int page, PointF size, Bitmap bitmap) {
        AsyncTask<Void, Void, RectF[]> asyncTask = new AsyncTask<Void, Void, RectF[]>() { // from class: com.artifex.mupdfdemo.MuPDFPageView.25
            @Override // android.os.AsyncTask
            public RectF[] doInBackground(Void... arg0) {
                return MuPDFPageView.this.mCore.getWidgetAreas(page);
            }

            @Override // android.os.AsyncTask
            public void onPostExecute(RectF[] result) {
                MuPDFPageView.this.mWidgetAreas = result;
            }
        };
        this.mLoadWidgetAreas = asyncTask;
        asyncTask.execute(new Void[0]);
        try {
            this.mPageView.setImageBitmap(bitmap);
        } catch (Exception unused) {
        }
        super.setPage(page, size);
        loadAnnotations();
    }

    @Override // com.artifex.mupdfdemo.PageView, com.artifex.mupdfdemo.MuPDFView
    public void releaseResources() {
        AsyncTask<Void, Void, PassClickResult> asyncTask = this.mPassClick;
        if (asyncTask != null) {
            asyncTask.cancel(true);
            this.mPassClick = null;
        }
        AsyncTask<Void, Void, RectF[]> asyncTask2 = this.mLoadWidgetAreas;
        if (asyncTask2 != null) {
            asyncTask2.cancel(true);
            this.mLoadWidgetAreas = null;
        }
        AsyncTask<Void, Void, Annotation[]> asyncTask3 = this.mLoadAnnotations;
        if (asyncTask3 != null) {
            asyncTask3.cancel(true);
            this.mLoadAnnotations = null;
        }
        AsyncTask<String, Void, Boolean> asyncTask4 = this.mSetWidgetText;
        if (asyncTask4 != null) {
            asyncTask4.cancel(true);
            this.mSetWidgetText = null;
        }
        AsyncTask<String, Void, Void> asyncTask5 = this.mSetWidgetChoice;
        if (asyncTask5 != null) {
            asyncTask5.cancel(true);
            this.mSetWidgetChoice = null;
        }
        AsyncTask<Object, Void, Void> asyncTask6 = this.mAddStrikeOut;
        if (asyncTask6 != null) {
            asyncTask6.cancel(true);
            this.mAddStrikeOut = null;
        }
        AsyncTask<Integer, Void, Void> asyncTask7 = this.mDeleteAnnotation;
        if (asyncTask7 != null) {
            asyncTask7.cancel(true);
            this.mDeleteAnnotation = null;
        }
        super.releaseResources();
    }
}
