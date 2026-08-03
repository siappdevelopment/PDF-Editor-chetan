package com.artifex.mupdfdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import androidx.core.content.ContextCompat;

import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.utils.Const;
import com.reader.alldocviewer.pdfeditor.utils.GlobalCustomToast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;


@SourceDebugExtension({"SMAP\nMuPDFReaderView.kt\nKotlin\n*S Kotlin\n*F\n+ 1 MuPDFReaderView.kt\ncom/artifex/mupdfdemo/MuPDFReaderView\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,929:1\n1#2:930\n*E\n"})
/* loaded from: classes7.dex */
public class MuPDFReaderView extends ReaderView {
    @NotNull
    public static final Companion Companion = new Companion(null);
    public static final float TOUCH_TOLERANCE = 2.0f;
    @Nullable
    private ViewCallbackNew callBackNew;
    @Nullable
    private CheckChangeOrNot checkOrNot;
    @Nullable
    private Runnable onCopyTextCompleted;
    public interface MarkupDeleteListener {
        void onMarkupDeleted(@Nullable PointData deleted);
    }

    @Nullable
    private MarkupDeleteListener onMarkupDeleted;

    public final void setOnMarkupDeleted(@Nullable MarkupDeleteListener listener) {
        this.onMarkupDeleted = listener;
    }
    @NotNull
    private final GestureDetector gestureDetector;
    public GlobalCustomToast globalCustomToast;
    private boolean hasDrawn;
    private boolean hasHighlighted;
    private boolean hasLongPressed;
    private boolean hasMoved;
    private boolean isCheckOnTap;
    private boolean isHandlingTouchEvent;
    private boolean isMoving;
    private float lastX;
    private float lastY;
    @NotNull
    private final Context mContext;
    private boolean mLinksHighlighted;
    @NotNull
    private Mode mMode;
    private float mX;
    private float mY;
    @Nullable
    private PopupWindow magnifierPopup;
    private final int magnifierSize;
    @Nullable
    private ImageView magnifierView;
    private boolean tapDisabled;
    private int tapPageMargin;
    private final float zoomFactor;
    @Nullable
    private MuPDFView mSelectionView;
    @Nullable
    private MuPDFView mAnnotationPageView;
    private int mActiveMarkupColorArgb;
    private float mInkStrokeWidth = 10f;

    private void applyInkStrokeWidthToPageViews(float strokeWidth) {
        applyToChildren(new ViewMapper() {
            @Override
            public void applyToView(View view) {
                if (view instanceof MuPDFView) {
                    ((MuPDFView) view).setPaintStrockWidth(strokeWidth);
                }
            }
        });
    }

    private void applyInkSettingsToPageView(MuPDFView pageView) {
        if (pageView == null) {
            return;
        }
        pageView.setPaintStrockWidth(mInkStrokeWidth);
        pageView.setInkColor(resolveMarkupColorArgb());
    }

    /* loaded from: classes7.dex */
    public interface CheckChangeOrNot {
        void onChangeCheck(boolean z);
    }

    /* loaded from: classes7.dex */
    public interface ViewCallbackNew {
        void onViewTap(boolean z);
    }

    /* loaded from: classes7.dex */
    /*public *//* synthetic *//* class WhenMappings {
        public static final *//* synthetic *//* int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[Mode.values().length];
            try {
                iArr[Mode.Viewing.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            $EnumSwitchMapping$0 = iArr;
        }
    }*/

    public static final class WhenMappings {
        public static final int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[Mode.values().length];
            iArr[Mode.Viewing.ordinal()] = 1;
            $EnumSwitchMapping$0 = iArr;
        }
    }

    public void onHit(@Nullable Hit hit) {
    }

    @NotNull
    public final Context getMContext() {
        return this.mContext;
    }

    public final boolean getHasHighlighted() {
        return this.hasHighlighted;
    }

    public final void setHasHighlighted(boolean z) {
        this.hasHighlighted = z;
    }

    public final float getMX() {
        return this.mX;
    }

    public final void setMX(float f) {
        this.mX = f;
    }

    public final float getMY() {
        return this.mY;
    }

    public final void setMY(float f) {
        this.mY = f;
    }

    public final boolean getMLinksHighlighted() {
        return this.mLinksHighlighted;
    }

    public final void setMLinksHighlighted(boolean z) {
        this.mLinksHighlighted = z;
    }

    @NotNull
    public final Mode getMMode() {
        return this.mMode;
    }

    public final void setMMode(@NotNull Mode mode) {
        Intrinsics.checkNotNullParameter(mode, "<set-?>");
        this.mMode = mode;
    }

    public final boolean getTapDisabled() {
        return this.tapDisabled;
    }

    public final void setTapDisabled(boolean z) {
        this.tapDisabled = z;
    }

    public final int getTapPageMargin() {
        return this.tapPageMargin;
    }

    public final void setTapPageMargin(int i) {
        this.tapPageMargin = i;
    }

    @NotNull
    public final GlobalCustomToast getGlobalCustomToast() {
        GlobalCustomToast globalCustomToast = this.globalCustomToast;
        if (globalCustomToast != null) {
            return globalCustomToast;
        }
        Intrinsics.throwUninitializedPropertyAccessException("globalCustomToast");
        return null;
    }

    public final void setGlobalCustomToast(@NotNull GlobalCustomToast globalCustomToast) {
        Intrinsics.checkNotNullParameter(globalCustomToast, "<set-?>");
        this.globalCustomToast = globalCustomToast;
    }

    @Nullable
    public final ViewCallbackNew getCallBackNew() {
        return this.callBackNew;
    }

    public final void setCallBackNew(@Nullable ViewCallbackNew viewCallbackNew) {
        this.callBackNew = viewCallbackNew;
    }

    @Nullable
    public final CheckChangeOrNot getCheckOrNot() {
        return this.checkOrNot;
    }

    public final void setCheckOrNot(@Nullable CheckChangeOrNot checkChangeOrNot) {
        this.checkOrNot = checkChangeOrNot;
    }

    public final void setCallBack(@NotNull ViewCallbackNew callBack) {
        Intrinsics.checkNotNullParameter(callBack, "callBack");
        this.callBackNew = callBack;
    }

    public final void setCheckChangeOrNot(@NotNull CheckChangeOrNot callBack) {
        Intrinsics.checkNotNullParameter(callBack, "callBack");
        this.checkOrNot = callBack;
    }

    public final void setOnCopyTextCompleted(@Nullable Runnable listener) {
        this.onCopyTextCompleted = listener;
    }

    private void exitCopyTextModeAfterCopy() {
        Const.CopyText = false;
        deSelectText();
        setMode(Mode.Viewing);
        this.isMoving = false;
        this.isHandlingTouchEvent = false;
        invalidate();
        if (onCopyTextCompleted != null) {
            onCopyTextCompleted.run();
        }
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /*public MuPDFReaderView(@NotNull Context context) {
        super(context);
        Intrinsics.checkNotNullParameter(context, "context");
        this.mMode = Mode.Viewing;
        this.gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$gestureDetector$1

            *//* loaded from: classes7.dex *//*
            public *//* synthetic *//* class WhenMappings {
                public static final *//* synthetic *//* int[] $EnumSwitchMapping$0;

                static {
                    int[] iArr = new int[Mode.values().length];
                    try {
                        iArr[Mode.Viewing.ordinal()] = 1;
                    } catch (NoSuchFieldError unused) {
                    }
                    try {
                        iArr[Mode.Selecting.ordinal()] = 2;
                    } catch (NoSuchFieldError unused2) {
                    }
                    $EnumSwitchMapping$0 = iArr;
                }
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public void onLongPress(MotionEvent e) {
                boolean isAnnotationModeActive;
                float f;
                float f2;
                float f3;
                float f4;
                Intrinsics.checkNotNullParameter(e, "e");
                MuPDFReaderView.this.deSelectText();
                isAnnotationModeActive = MuPDFReaderView.this.isAnnotationModeActive();
                if (isAnnotationModeActive) {
                    return;
                }
                MuPDFReaderView.this.setMMode(Mode.Selecting);
                MuPDFReaderView.this.hasLongPressed = true;
                MuPDFReaderView.this.lastX = e.getX();
                MuPDFReaderView.this.lastY = e.getY();
                MuPDFView muPDFView = MuPDFReaderView.this.getPageViewAt(e.getX(), e.getY());
                if (muPDFView == null) {
                    View displayedView = MuPDFReaderView.this.getDisplayedView();
                    muPDFView = displayedView instanceof MuPDFView ? (MuPDFView) displayedView : null;
                }
                if (muPDFView == null) {
                    return;
                }
                MuPDFReaderView.this.mSelectionView = muPDFView;
                MuPDFReaderView.this.selectTextOnPage(
                        muPDFView,
                        MuPDFReaderView.this.lastX,
                        MuPDFReaderView.this.lastY,
                        MuPDFReaderView.this.lastX,
                        MuPDFReaderView.this.lastY);
                MuPDFReaderView.this.performClick();
                MuPDFReaderView.this.invalidate();
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onScroll(MotionEvent motionEvent, MotionEvent e2, float f, float f2) {
                boolean z;
                Intrinsics.checkNotNullParameter(e2, "e2");
                View displayedView = MuPDFReaderView.this.getDisplayedView();
                MuPDFView muPDFView = displayedView instanceof MuPDFView ? (MuPDFView) displayedView : null;
                if (muPDFView == null) {
                    return false;
                }
                z = MuPDFReaderView.this.hasLongPressed;
                if (z) {
                    if (motionEvent != null) {
                        muPDFView.selectText(motionEvent.getX(), motionEvent.getY(), e2.getX(), e2.getY());
                    }
                    return true;
                }
                View displayedView2 = MuPDFReaderView.this.getDisplayedView();
                MuPDFView muPDFView2 = displayedView2 instanceof MuPDFView ? (MuPDFView) displayedView2 : null;
                if (muPDFView2 == null) {
                    return false;
                }
                int i = WhenMappings.$EnumSwitchMapping$0[MuPDFReaderView.this.getMMode().ordinal()];
                if (i == 1) {
                    if (!MuPDFReaderView.this.getTapDisabled()) {
                        MuPDFReaderView.this.onDocMotion();
                    }
                    return super.onScroll(motionEvent, e2, f, f2);
                } else if (i == 2 && motionEvent != null) {
                    muPDFView2.selectText(motionEvent.getX(), motionEvent.getY(), e2.getX(), e2.getY());
                    return true;
                } else {
                    return true;
                }
            }
        });
        this.magnifierSize = SearchBar_androidKt.AnimationExitDurationMillis;
        this.zoomFactor = 2.5f;
        this.mContext = context;
        setup();
    }*/

    public MuPDFReaderView(@NotNull Context context) {
        super(context);

        Intrinsics.checkNotNullParameter(context, "context");

        this.mMode = Mode.Viewing;

        this.gestureDetector = new GestureDetector(
                getContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public void onLongPress(MotionEvent e) {
                        Intrinsics.checkNotNullParameter(e, "e");

                        MuPDFReaderView.this.deSelectText();

                        if (MuPDFReaderView.this.isAnnotationModeActive()) {
                            return;
                        }

                        MuPDFReaderView.this.setMMode(Mode.Selecting);
                        MuPDFReaderView.this.hasLongPressed = true;

                        MuPDFReaderView.this.lastX = e.getX();
                        MuPDFReaderView.this.lastY = e.getY();

                        MuPDFReaderView.this.mSelectionView =
                                MuPDFReaderView.this.getPageViewAt(e.getX(), e.getY());

                        MuPDFView muPDFView = MuPDFReaderView.this.mSelectionView;

                        if (muPDFView == null) {
                            return;
                        }

                        MuPDFReaderView.this.selectTextOnPage(
                                muPDFView,
                                MuPDFReaderView.this.lastX,
                                MuPDFReaderView.this.lastY,
                                MuPDFReaderView.this.lastX,
                                MuPDFReaderView.this.lastY
                        );

                        MuPDFReaderView.this.performClick();
                        MuPDFReaderView.this.invalidate();
                    }

                    @Override
                    public boolean onScroll(
                            MotionEvent motionEvent,
                            MotionEvent e2,
                            float distanceX,
                            float distanceY
                    ) {

                        Intrinsics.checkNotNullParameter(e2, "e2");

                        View displayedView = MuPDFReaderView.this.getDisplayedView();

                        MuPDFView muPDFView =
                                displayedView instanceof MuPDFView
                                        ? (MuPDFView) displayedView
                                        : null;

                        if (muPDFView == null) {
                            return false;
                        }

                        if (MuPDFReaderView.this.hasLongPressed) {

                            if (motionEvent != null) {
                                MuPDFView pageView = MuPDFReaderView.this.mSelectionView;
                                if (pageView == null) {
                                    pageView = MuPDFReaderView.this.getPageViewAt(
                                            e2.getX(), e2.getY());
                                }
                                if (pageView != null) {
                                    MuPDFReaderView.this.selectTextOnPage(
                                            pageView,
                                            motionEvent.getX(),
                                            motionEvent.getY(),
                                            e2.getX(),
                                            e2.getY()
                                    );
                                }
                            }

                            return true;
                        }

                        View displayedView2 =
                                MuPDFReaderView.this.getDisplayedView();

                        MuPDFView muPDFView2 =
                                displayedView2 instanceof MuPDFView
                                        ? (MuPDFView) displayedView2
                                        : null;

                        if (muPDFView2 == null) {
                            return false;
                        }

                        Mode mode = MuPDFReaderView.this.getMMode();

                        if (mode == Mode.Viewing) {

                            if (!MuPDFReaderView.this.getTapDisabled()) {
                                MuPDFReaderView.this.onDocMotion();
                            }

                            return super.onScroll(
                                    motionEvent,
                                    e2,
                                    distanceX,
                                    distanceY
                            );

                        } else if (mode == Mode.Selecting
                                && motionEvent != null) {

                            MuPDFView pageView = MuPDFReaderView.this.getPageViewAt(
                                    e2.getX(), e2.getY());
                            if (pageView != null) {
                                MuPDFReaderView.this.mSelectionView = pageView;
                                MuPDFReaderView.this.selectTextOnPage(
                                        pageView,
                                        motionEvent.getX(),
                                        motionEvent.getY(),
                                        e2.getX(),
                                        e2.getY()
                                );
                            }

                            return true;
                        }

                        return true;
                    }
                }
        );

        // SearchBar_androidKt.AnimationExitDurationMillis remove
        this.magnifierSize = 200;

        this.zoomFactor = 2.5f;
        this.mContext = context;
        this.mActiveMarkupColorArgb = ContextCompat.getColor(context, R.color.color_picker_3);

        setup();
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /*public MuPDFReaderView(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkNotNullParameter(context, "context");
        this.mMode = Mode.Viewing;
        this.gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$gestureDetector$1

            *//* loaded from: classes7.dex *//*
            public *//* synthetic *//* class WhenMappings {
                public static final *//* synthetic *//* int[] $EnumSwitchMapping$0;

                static {
                    int[] iArr = new int[Mode.values().length];
                    try {
                        iArr[Mode.Viewing.ordinal()] = 1;
                    } catch (NoSuchFieldError unused) {
                    }
                    try {
                        iArr[Mode.Selecting.ordinal()] = 2;
                    } catch (NoSuchFieldError unused2) {
                    }
                    $EnumSwitchMapping$0 = iArr;
                }
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public void onLongPress(MotionEvent e) {
                boolean isAnnotationModeActive;
                float f;
                float f2;
                float f3;
                float f4;
                Intrinsics.checkNotNullParameter(e, "e");
                MuPDFReaderView.this.deSelectText();
                isAnnotationModeActive = MuPDFReaderView.this.isAnnotationModeActive();
                if (isAnnotationModeActive) {
                    return;
                }
                MuPDFReaderView.this.setMMode(Mode.Selecting);
                MuPDFReaderView.this.hasLongPressed = true;
                MuPDFReaderView.this.lastX = e.getX();
                MuPDFReaderView.this.lastY = e.getY();
                MuPDFView muPDFView = MuPDFReaderView.this.getPageViewAt(e.getX(), e.getY());
                if (muPDFView == null) {
                    View displayedView = MuPDFReaderView.this.getDisplayedView();
                    muPDFView = displayedView instanceof MuPDFView ? (MuPDFView) displayedView : null;
                }
                if (muPDFView == null) {
                    return;
                }
                MuPDFReaderView.this.mSelectionView = muPDFView;
                MuPDFReaderView.this.selectTextOnPage(
                        muPDFView,
                        MuPDFReaderView.this.lastX,
                        MuPDFReaderView.this.lastY,
                        MuPDFReaderView.this.lastX,
                        MuPDFReaderView.this.lastY);
                MuPDFReaderView.this.performClick();
                MuPDFReaderView.this.invalidate();
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onScroll(MotionEvent motionEvent, MotionEvent e2, float f, float f2) {
                boolean z;
                Intrinsics.checkNotNullParameter(e2, "e2");
                View displayedView = MuPDFReaderView.this.getDisplayedView();
                MuPDFView muPDFView = displayedView instanceof MuPDFView ? (MuPDFView) displayedView : null;
                if (muPDFView == null) {
                    return false;
                }
                z = MuPDFReaderView.this.hasLongPressed;
                if (z) {
                    if (motionEvent != null) {
                        muPDFView.selectText(motionEvent.getX(), motionEvent.getY(), e2.getX(), e2.getY());
                    }
                    return true;
                }
                View displayedView2 = MuPDFReaderView.this.getDisplayedView();
                MuPDFView muPDFView2 = displayedView2 instanceof MuPDFView ? (MuPDFView) displayedView2 : null;
                if (muPDFView2 == null) {
                    return false;
                }
                int i = WhenMappings.$EnumSwitchMapping$0[MuPDFReaderView.this.getMMode().ordinal()];
                if (i == 1) {
                    if (!MuPDFReaderView.this.getTapDisabled()) {
                        MuPDFReaderView.this.onDocMotion();
                    }
                    return super.onScroll(motionEvent, e2, f, f2);
                } else if (i == 2 && motionEvent != null) {
                    muPDFView2.selectText(motionEvent.getX(), motionEvent.getY(), e2.getX(), e2.getY());
                    return true;
                } else {
                    return true;
                }
            }
        });
        this.magnifierSize = SearchBar_androidKt.AnimationExitDurationMillis;
        this.zoomFactor = 2.5f;
        this.mContext = context;
        setup();
    }*/

    public final void releaseBitmaps(@NotNull MuPDFReaderView view) {
        Intrinsics.checkNotNullParameter(view, "view");
        recyclerBitmap(view);
    }

    public final void setMode(@NotNull Mode m) {
        Intrinsics.checkNotNullParameter(m, "m");
        this.mMode = m;
    }

    public final void prepareForDeleteMode() {
        deSelectText();
        this.hasLongPressed = false;
        this.hasMoved = false;
        this.isHandlingTouchEvent = false;
        this.mSelectionView = null;
        this.mAnnotationPageView = null;
        this.mMode = Mode.Viewing;
    }

    public final void clearInteractionStateAfterMarkupDelete() {
        this.mAnnotationPageView = null;
        this.mSelectionView = null;
        this.isHandlingTouchEvent = false;
        this.hasLongPressed = false;
        this.hasMoved = false;
        this.isMoving = false;
        this.mMode = Mode.Viewing;
    }

    private boolean canEditSavedMarkups() {
        return !Const.highlight
                && !Const.underline
                && !Const.strikeOut
                && !Const.inkCheck
                && !Const.CopyText
                && !Const.deleteText;
    }

    private static boolean hasSavePointsEntries() {
        for (List<PointData> pageMarkups : Const.savePoints.values()) {
            if (pageMarkups != null && !pageMarkups.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnySavedMarkups() {
        if (hasSavePointsEntries()) {
            return true;
        }
        return false;
    }

    private boolean ensureSavedMarkupsAvailable(MuPDFView pageView) {
        if (pageView instanceof MuPDFPageView) {
            MuPDFPageView page = (MuPDFPageView) pageView;
            if (!page.hasSavedMarkups() && page.hasPdfMarkupAnnotations()) {
                page.refreshSavePointsForPageFromMuPdf();
            }
            return page.hasSavedMarkups();
        }
        return hasSavePointsEntries();
    }

    private boolean tryShowSavedMarkupDeletePopup(float screenX, float screenY, float rawX, float rawY) {
        if (!canEditSavedMarkups()) {
            return false;
        }
        MuPDFView pageView = getPageViewAt(screenX, screenY);
        if (pageView == null) {
            View displayedView = getDisplayedView();
            pageView = displayedView instanceof MuPDFView ? (MuPDFView) displayedView : null;
        }
        if (pageView == null) {
            return false;
        }
        if (!ensureSavedMarkupsAvailable(pageView)) {
            return false;
        }
        if (!resolveSavedMarkupSelection(pageView, screenX, screenY)) {
            return false;
        }
        mAnnotationPageView = pageView;
        showPopupWindowDelete(rawX, rawY);
        Const.checkEditorNot = true;
        return true;
    }

    public final void setup() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Object systemService = this.mContext.getSystemService((Context.WINDOW_SERVICE));
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.view.WindowManager");
        ((WindowManager) systemService).getDefaultDisplay().getMetrics(displayMetrics);
        int i = (int) displayMetrics.xdpi;
        this.tapPageMargin = i;
        if (i < 80) {
            this.tapPageMargin = 80;
        }
        int i2 = this.tapPageMargin;
        int i3 = displayMetrics.widthPixels;
        if (i2 > i3 / 6) {
            this.tapPageMargin = i3 / 6;
        }
    }

    @Nullable
    private MuPDFView getPageViewAt(float screenX, float screenY) {
        View target = getViewAtScreenPoint(screenX, screenY);
        if (target instanceof MuPDFView) {
            return (MuPDFView) target;
        }
        View displayed = getDisplayedView();
        return displayed instanceof MuPDFView ? (MuPDFView) displayed : null;
    }

    @Nullable
    private MuPDFView getActiveSelectionView() {
        if (mSelectionView != null) {
            return mSelectionView;
        }
        View displayed = getDisplayedView();
        return displayed instanceof MuPDFView ? (MuPDFView) displayed : null;
    }

    private void selectTextOnPage(@Nullable MuPDFView page, float x1, float y1, float x2, float y2) {
        if (page == null) {
            return;
        }
        ensurePageTextLoaded(page);
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            page.selectText(toDocX(x1), toDocY(y1), toDocX(x2), toDocY(y2));
        } else {
            page.selectText(x1, y1, x2, y2);
        }
    }

    private int resolveMarkupColorArgb() {
        return mActiveMarkupColorArgb;
    }

    private int resolveMarkupColorFromConst(int colorResId) {
        return ContextCompat.getColor(getContext(), colorResId);
    }

    private void applyMarkupSelection(@Nullable MuPDFView page, Annotation.Type type) {
        if (page == null) {
            return;
        }
        ensurePageTextLoaded(page);
        int color = resolveMarkupColorArgb();
        page.markupSelection(type, color);
    }

    private void applyMarkupSelectionFromConst(@Nullable MuPDFView page, Annotation.Type type, int colorResId) {
        if (page == null) {
            return;
        }
        ensurePageTextLoaded(page);
        page.markupSelection(type, resolveMarkupColorFromConst(colorResId));
    }

    private void ensurePageTextLoaded(@Nullable MuPDFView page) {
        if (page instanceof MuPDFPageView) {
            ((MuPDFPageView) page).ensureTextLoaded();
        }
    }

    @Nullable
    private MuPDFView getActiveAnnotationView() {
        if (mAnnotationPageView != null) {
            return mAnnotationPageView;
        }
        return getActiveSelectionView();
    }

    private Hit passClickOnPage(@Nullable MuPDFView page, float screenX, float screenY) {
        if (page == null) {
            return Hit.Nothing;
        }
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            return page.passClickEvent(toDocX(screenX), toDocY(screenY));
        }
        return page.passClickEvent(screenX, screenY);
    }

    @Nullable
    private LinkInfo hitLinkOnPage(@Nullable MuPDFView page, float screenX, float screenY) {
        if (page == null) {
            return null;
        }
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            return page.hitLink(toDocX(screenX), toDocY(screenY));
        }
        return page.hitLink(screenX, screenY);
    }

    private boolean tryShowAnnotationDeletePopup(float screenX, float screenY, float rawX, float rawY) {
        if (!Const.deleteText) {
            return false;
        }
        MuPDFView pageView = getPageViewAt(screenX, screenY);
        if (pageView == null) {
            View displayedView = getDisplayedView();
            pageView = displayedView instanceof MuPDFView ? (MuPDFView) displayedView : null;
        }
        if (pageView == null) {
            return false;
        }
        if (!resolveSavedMarkupSelection(pageView, screenX, screenY)) {
            return false;
        }
        mAnnotationPageView = pageView;
        showPopupWindowDelete(rawX, rawY);
        return true;
    }

    private boolean resolveSavedMarkupSelection(@Nullable MuPDFView pageView, float screenX, float screenY) {
        if (pageView == null) {
            return false;
        }
        ensurePageTextLoaded(pageView);
        if (pageView instanceof MuPDFPageView) {
            MuPDFPageView page = (MuPDFPageView) pageView;
            if (!page.hasSavedMarkups() && page.hasPdfMarkupAnnotations()) {
                page.refreshSavePointsForPageFromMuPdf();
            }
            if (!page.hasSavedMarkups()) {
                return false;
            }
            if (page.selectSavedMarkupFromTextSelection()) {
                return true;
            }
        }
        Hit hit = passClickOnPage(pageView, screenX, screenY);
        return hit == Hit.Annotation;
    }

    private boolean resolveDeleteSelection(@Nullable MuPDFView pageView, float screenX, float screenY) {
        return resolveSavedMarkupSelection(pageView, screenX, screenY);
    }

    @Override // com.artifex.mupdfdemo.ReaderView, android.view.GestureDetector.OnGestureListener
    public boolean onSingleTapUp(@NotNull MotionEvent e) {
        Intrinsics.checkNotNullParameter(e, "e");
        if (this.isHandlingTouchEvent) {
            return false;
        }
        MuPDFView pageView = getPageViewAt(e.getX(), e.getY());
        if (pageView == null) {
            View displayedView = getDisplayedView();
            pageView = displayedView instanceof MuPDFView ? (MuPDFView) displayedView : null;
        }
        if (pageView == null) {
            return super.onSingleTapUp(e);
        }
        if (Const.deleteText) {
            tryShowAnnotationDeletePopup(e.getX(), e.getY(), e.getRawX(), e.getRawY());
            return super.onSingleTapUp(e);
        }
        if (tryShowSavedMarkupDeletePopup(e.getX(), e.getY(), e.getRawX(), e.getRawY())) {
            return super.onSingleTapUp(e);
        }
        deSelectText();
        boolean z = !this.isCheckOnTap;
        this.isCheckOnTap = z;
        ViewCallbackNew viewCallbackNew = this.callBackNew;
        if (viewCallbackNew != null) {
            viewCallbackNew.onViewTap(z);
        }
        Hit passClickEvent = passClickOnPage(pageView, e.getX(), e.getY());
        onHit(passClickEvent);
        if (passClickEvent == Hit.Nothing) {
            pageView.deselectAnnotation();
            LinkInfo hitLink = hitLinkOnPage(pageView, e.getX(), e.getY());
            if (hitLink != null) {
                Intrinsics.checkNotNull(hitLink);
                hitLink.acceptVisitor(new LinkInfoVisitor() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$onSingleTapUp$2
                    @Override // com.artifex.mupdfdemo.LinkInfoVisitor
                    public void visitRemote(LinkInfoRemote li) {
                        Intrinsics.checkNotNullParameter(li, "li");
                    }

                    @Override // com.artifex.mupdfdemo.LinkInfoVisitor
                    public void visitInternal(LinkInfoInternal li) {
                        Intrinsics.checkNotNullParameter(li, "li");
                        MuPDFReaderView.this.setDisplayedViewIndex(li.pageNumber);
                    }

                    @Override // com.artifex.mupdfdemo.LinkInfoVisitor
                    public void visitExternal(LinkInfoExternal li) {
                        Intrinsics.checkNotNullParameter(li, "li");
                        MuPDFReaderView.this.getMContext().startActivity(new Intent("android.intent.action.VIEW", Uri.parse(li.url)));
                    }
                });
            }
        }
        return super.onSingleTapUp(e);
    }

    @Override // com.artifex.mupdfdemo.ReaderView, android.view.GestureDetector.OnGestureListener
    public void onLongPress(@NotNull MotionEvent e) {
        Intrinsics.checkNotNullParameter(e, "e");
        deSelectText();
        if (isAnnotationModeActive()) {
            return;
        }
        this.mMode = Mode.Selecting;
        this.hasLongPressed = true;
        this.lastX = e.getX();
        this.lastY = e.getY();
        this.mSelectionView = getPageViewAt(e.getX(), e.getY());
        MuPDFView muPDFView = this.mSelectionView;
        if (muPDFView == null) {
            View displayedView = getDisplayedView();
            muPDFView = displayedView instanceof MuPDFView ? (MuPDFView) displayedView : null;
        }
        if (muPDFView != null) {
            selectTextOnPage(muPDFView, this.lastX, this.lastY, this.lastX, this.lastY);
            performClick();
            invalidate();
        }
    }

    @Override // com.artifex.mupdfdemo.ReaderView, android.view.GestureDetector.OnGestureListener
    public boolean onScroll(@Nullable MotionEvent e1, @NotNull MotionEvent e2, float distanceX, float distanceY) {
        Intrinsics.checkNotNullParameter(e2, "e2");
        if (isScaling() || e2.getPointerCount() > 1) {
            return true;
        }
        if (this.hasLongPressed) {
            MuPDFView muPDFView = this.mSelectionView;
            if (muPDFView == null) {
                muPDFView = getPageViewAt(e2.getX(), e2.getY());
            }
            if (muPDFView != null && e1 != null) {
                selectTextOnPage(muPDFView, e1.getX(), e1.getY(), e2.getX(), e2.getY());
            }
            return true;
        }
        MuPDFView pageView = getPageViewAt(e2.getX(), e2.getY());
        if (pageView == null) {
            pageView = (MuPDFView) getDisplayedView();
        }
        if (this.mMode == Mode.Viewing) {
            if (!this.tapDisabled) {
                onDocMotion();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
        if (this.mMode == Mode.Selecting && pageView != null && e1 != null) {
            this.mSelectionView = pageView;
            selectTextOnPage(pageView, e1.getX(), e1.getY(), e2.getX(), e2.getY());
            return true;
        }
        return true;
    }

    @Override // com.artifex.mupdfdemo.ReaderView, android.view.View
    public boolean onTouchEvent(@NotNull MotionEvent event) {
        Intrinsics.checkNotNullParameter(event, "event");
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            this.tapDisabled = false;
        }

        // Pinch zoom must go directly to ReaderView without any custom single-touch handling.
        if (event.getPointerCount() > 1 || isScaling()) {
            return super.onTouchEvent(event);
        }

        if (this.hasLongPressed) {
            if (Const.deleteText) {
                this.hasLongPressed = false;
                return true;
            }
            float x = event.getX();
            float y = event.getY();
            if (action == MotionEvent.ACTION_DOWN) {
                this.hasHighlighted = false;
                this.isMoving = false;
                showMagnifier(x, y);
            } else if (action == MotionEvent.ACTION_UP) {
                hideMagnifier();
                this.hasLongPressed = false;
                this.mMode = Mode.Viewing;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$$ExternalSyntheticLambda8
                    @Override // java.lang.Runnable
                    public final void run() {
                        MuPDFReaderView.onTouchEvent$lambda$1(MuPDFReaderView.this);
                    }
                }, 100L);
                if (!tryShowSavedMarkupDeletePopup(event.getX(), event.getY(), event.getRawX(), event.getRawY())) {
                    showPopupWindowOnLongpress(event.getRawX(), event.getRawY());
                }
                this.isMoving = false;
            } else if (action == MotionEvent.ACTION_MOVE) {
                this.isMoving = true;
                MuPDFView muPDFView = this.mSelectionView;
                if (muPDFView == null) {
                    muPDFView = getPageViewAt(event.getX(), event.getY());
                }
                if (muPDFView != null) {
                    selectTextOnPage(muPDFView, this.lastX, this.lastY, event.getX(), event.getY());
                }
                this.isHandlingTouchEvent = true;
                showMagnifier(x, y);
                updateMagnifier(x, y);
            }
            return true;
        } else if (Const.deleteText && this.mMode == Mode.Viewing) {
            return super.onTouchEvent(event);
        } else {
            Mode mode = this.mMode;
            if (mode == Mode.Drawing) {
                View drawPage = getViewAtScreenPoint(event.getX(), event.getY());
                if (drawPage instanceof MuPDFView) {
                    applyInkSettingsToPageView((MuPDFView) drawPage);
                }
                CheckChangeOrNot checkChangeOrNot = this.checkOrNot;
                if (checkChangeOrNot != null) {
                    checkChangeOrNot.onChangeCheck(true);
                }
                Const.checkEditorNot = true;
                float x2 = event.getX();
                float y2 = event.getY();
                int action2 = event.getAction();
                if (action2 == 0) {
                    touch_start(x2, y2);
                    showMagnifier(x2, y2);
                } else if (action2 == 1) {
                    touch_up();
                    this.isMoving = false;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$$ExternalSyntheticLambda9
                        @Override // java.lang.Runnable
                        public final void run() {
                            MuPDFReaderView.onTouchEvent$lambda$2(MuPDFReaderView.this);
                        }
                    }, 100L);
                    hideMagnifier();
                } else if (action2 == 2) {
                    touch_move(x2, y2);
                    this.isMoving = true;
                    this.isHandlingTouchEvent = true;
                    updateMagnifier(x2, y2);
                }
                return true;
            } else if (mode == Mode.Selecting) {
                float x3 = event.getX();
                float y3 = event.getY();
                int action3 = event.getAction();
                if (action3 == 0) {
                    this.hasHighlighted = false;
                    this.isMoving = false;
                    this.isHandlingTouchEvent = false;
                    this.hasMoved = false;
                    this.lastX = x3;
                    this.lastY = y3;
                    this.mSelectionView = getPageViewAt(x3, y3);
                    MuPDFView pageView = this.mSelectionView;
                    if (pageView != null) {
                        pageView.setInkColor(resolveMarkupColorArgb());
                        ensurePageTextLoaded(pageView);
                        selectTextOnPage(pageView, x3, y3, x3, y3);
                    }
                    showMagnifier(x3, y3);
                } else if (action3 == 1) {
                    Const.startPointX = x3;
                    Const.endPointY = y3;
                    hideMagnifier();
                    MuPDFView selectionView = getActiveSelectionView();
                    if (Const.deleteText) {
                        if (tryShowAnnotationDeletePopup(x3, y3, event.getRawX(), event.getRawY())) {
                            this.isHandlingTouchEvent = true;
                            new Handler(Looper.getMainLooper()).postDelayed(
                                    () -> MuPDFReaderView.this.isHandlingTouchEvent = false,
                                    100L);
                        }
                    } else if (canEditSavedMarkups()
                            && tryShowSavedMarkupDeletePopup(x3, y3, event.getRawX(), event.getRawY())) {
                        this.isHandlingTouchEvent = true;
                        new Handler(Looper.getMainLooper()).postDelayed(
                                () -> MuPDFReaderView.this.isHandlingTouchEvent = false,
                                100L);
                    } else if (this.isMoving && this.hasMoved) {
                        if (Const.highlight) {
                            applyMarkupSelection(selectionView, Annotation.Type.HIGHLIGHT);
                            CheckChangeOrNot checkChangeOrNot2 = this.checkOrNot;
                            if (checkChangeOrNot2 != null) {
                                checkChangeOrNot2.onChangeCheck(true);
                            }
                            Const.checkEditorNot = true;
                        } else if (Const.underline) {
                            applyMarkupSelection(selectionView, Annotation.Type.UNDERLINE);
                            CheckChangeOrNot checkChangeOrNot3 = this.checkOrNot;
                            if (checkChangeOrNot3 != null) {
                                checkChangeOrNot3.onChangeCheck(true);
                            }
                            Const.checkEditorNot = true;
                        } else if (Const.strikeOut) {
                            applyMarkupSelection(selectionView, Annotation.Type.STRIKEOUT);
                            CheckChangeOrNot checkChangeOrNot4 = this.checkOrNot;
                            if (checkChangeOrNot4 != null) {
                                checkChangeOrNot4.onChangeCheck(true);
                            }
                            Const.checkEditorNot = true;
                        } else if (Const.CopyText) {
                            showPopupWindowCopy(event.getRawX(), event.getRawY());
                        }
                    } else if (!Const.deleteText) {
                        tryShowAnnotationDeletePopup(x3, y3, event.getRawX(), event.getRawY());
                    }
                    this.isMoving = false;
                    this.isHandlingTouchEvent = false;
                } else if (action3 == 2) {
                    this.isMoving = true;
                    this.isHandlingTouchEvent = true;
                    this.hasMoved = true;
                    MuPDFView pageView = this.mSelectionView;
                    if (pageView == null) {
                        pageView = getPageViewAt(x3, y3);
                        this.mSelectionView = pageView;
                    }
                    if (pageView != null) {
                        pageView.setInkColor(resolveMarkupColorArgb());
                        ensurePageTextLoaded(pageView);
                        selectTextOnPage(pageView, this.lastX, this.lastY, x3, y3);
                    }
                    updateMagnifier(x3, y3);
                }
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onTouchEvent$lambda$1(MuPDFReaderView this$0) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        this$0.isHandlingTouchEvent = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onTouchEvent$lambda$2(MuPDFReaderView this$0) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        this$0.isHandlingTouchEvent = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean isAnnotationModeActive() {
        return Const.highlight || Const.underline || Const.strikeOut || Const.CopyText || Const.deleteText;
    }

    @NotNull
    public final GestureDetector getGestureDetector() {
        return this.gestureDetector;
    }

    @Override // com.artifex.mupdfdemo.ReaderView, android.view.GestureDetector.OnGestureListener
    public boolean onFling(@Nullable MotionEvent motionEvent, @NotNull MotionEvent e2, float f, float f2) {
        Intrinsics.checkNotNullParameter(e2, "e2");
        if (WhenMappings.$EnumSwitchMapping$0[this.mMode.ordinal()] == 1) {
            return super.onFling(motionEvent, e2, f, f2);
        }
        return true;
    }

    @Override // com.artifex.mupdfdemo.ReaderView, android.view.ScaleGestureDetector.OnScaleGestureListener
    public boolean onScaleBegin(@NotNull ScaleGestureDetector d) {
        Intrinsics.checkNotNullParameter(d, "d");
        this.tapDisabled = true;
        return super.onScaleBegin(d);
    }

    @Override // com.artifex.mupdfdemo.ReaderView, android.view.ScaleGestureDetector.OnScaleGestureListener
    public boolean onScale(@NotNull ScaleGestureDetector d) {
        Intrinsics.checkNotNullParameter(d, "d");
        return super.onScale(d);
    }

    @Override // com.artifex.mupdfdemo.ReaderView, android.view.ScaleGestureDetector.OnScaleGestureListener
    public void onScaleEnd(@NotNull ScaleGestureDetector d) {
        Intrinsics.checkNotNullParameter(d, "d");
        super.onScaleEnd(d);
        this.tapDisabled = false;
    }

    @SuppressLint({"InflateParams"})
    private final void showPopupWindowOnLongpress(float f, float f2) {
        setGlobalCustomToast(new GlobalCustomToast(this.mContext));
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.custom_popuplongpress_menu, (ViewGroup) null);
        final PopupWindow popupWindow = new PopupWindow(inflate, -2, -2);
        if (Const.isDarkMode) {
            ((LinearLayout) inflate.findViewById(R.id.llMain)).setBackgroundResource(R.drawable.bg_popup_dark);
        } else {
            ((LinearLayout) inflate.findViewById(R.id.llMain)).setBackgroundResource(R.drawable.bg_popup);
        }
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        popupWindow.showAtLocation(this, 0, (int) f, (int) f2);
        ((ImageView) inflate.findViewById(R.id.imStrike)).setOnClickListener(new OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MuPDFReaderView.showPopupWindowOnLongpress$lambda$3(MuPDFReaderView.this, popupWindow, view);
            }
        });
        ((ImageView) inflate.findViewById(R.id.imUnderLine)).setOnClickListener(new OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MuPDFReaderView.showPopupWindowOnLongpress$lambda$4(MuPDFReaderView.this, popupWindow, view);
            }
        });
        ((ImageView) inflate.findViewById(R.id.imHighLite)).setOnClickListener(new OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MuPDFReaderView.showPopupWindowOnLongpress$lambda$5(MuPDFReaderView.this, popupWindow, view);
            }
        });
        ((ImageView) inflate.findViewById(R.id.imCopy)).setOnClickListener(new OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MuPDFReaderView.showPopupWindowOnLongpress$lambda$6(MuPDFReaderView.this, popupWindow, view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPopupWindowOnLongpress$lambda$3(MuPDFReaderView this$0, PopupWindow popupWindow, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        Intrinsics.checkNotNullParameter(popupWindow, "$popupWindow");
        MuPDFView displayedView = this$0.getActiveSelectionView();
        if (displayedView != null) {
            this$0.applyMarkupSelectionFromConst(
                    displayedView,
                    Annotation.Type.STRIKEOUT,
                    Const.INSTANCE.getColorResIdStrikeOut());
        }
        CheckChangeOrNot checkChangeOrNot = this$0.checkOrNot;
        if (checkChangeOrNot != null) {
            checkChangeOrNot.onChangeCheck(true);
        }
        Const.checkEditorNot = true;
        popupWindow.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPopupWindowOnLongpress$lambda$4(MuPDFReaderView this$0, PopupWindow popupWindow, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        Intrinsics.checkNotNullParameter(popupWindow, "$popupWindow");
        MuPDFView displayedView = this$0.getActiveSelectionView();
        if (displayedView != null) {
            this$0.applyMarkupSelectionFromConst(
                    displayedView,
                    Annotation.Type.UNDERLINE,
                    Const.INSTANCE.getColorResIdUnderline());
        }
        CheckChangeOrNot checkChangeOrNot = this$0.checkOrNot;
        if (checkChangeOrNot != null) {
            checkChangeOrNot.onChangeCheck(true);
        }
        Const.checkEditorNot = true;
        popupWindow.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPopupWindowOnLongpress$lambda$5(MuPDFReaderView this$0, PopupWindow popupWindow, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        Intrinsics.checkNotNullParameter(popupWindow, "$popupWindow");
        MuPDFView displayedView = this$0.getActiveSelectionView();
        if (displayedView != null) {
            this$0.applyMarkupSelectionFromConst(
                    displayedView,
                    Annotation.Type.HIGHLIGHT,
                    Const.INSTANCE.getColorResIdHighlite());
        }
        CheckChangeOrNot checkChangeOrNot = this$0.checkOrNot;
        if (checkChangeOrNot != null) {
            checkChangeOrNot.onChangeCheck(true);
        }
        Const.checkEditorNot = true;
        popupWindow.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPopupWindowOnLongpress$lambda$6(MuPDFReaderView this$0, PopupWindow popupWindow, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        Intrinsics.checkNotNullParameter(popupWindow, "$popupWindow");
        MuPDFView displayedView = this$0.getActiveSelectionView();
        if (displayedView != null) {
            displayedView.copySelection();
        }
        GlobalCustomToast globalCustomToast = this$0.getGlobalCustomToast();
        String string = this$0.mContext.getString(R.string.copied);
        Intrinsics.checkNotNullExpressionValue(string, "getString(...)");
        globalCustomToast.show(string, GlobalCustomToast.Position.TOP_CENTER, 2000L, true, 12.0f, Integer.valueOf((int) R.color.black), Integer.valueOf((int) R.drawable.ic_right), Integer.valueOf((int) R.color.green_toast_light), Integer.valueOf((int) R.color.green_toast_border), Integer.valueOf((int) R.color.green_icon_color));
        popupWindow.dismiss();
    }

    @SuppressLint({"InflateParams"})
    private final void showPopupWindowDelete(final float f, final float f2) {
        setGlobalCustomToast(new GlobalCustomToast(this.mContext));
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.custom_popupdelete_menu, (ViewGroup) null);
        final PopupWindow popupWindow = new PopupWindow(inflate, -2, -2);
        if (Const.isDarkMode) {
            ((LinearLayout) inflate.findViewById(R.id.llMain)).setBackgroundResource(R.drawable.bg_popup_dark);
        } else {
            ((LinearLayout) inflate.findViewById(R.id.llMain)).setBackgroundResource(R.drawable.bg_popup);
        }
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        popupWindow.showAtLocation(this, 0, (int) f, (int) f2);
        ((ImageView) inflate.findViewById(R.id.imDelete)).setOnClickListener(new OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MuPDFReaderView.showPopupWindowDelete$lambda$7(MuPDFReaderView.this, popupWindow, view);
            }
        });
        ((ImageView) inflate.findViewById(R.id.imCopy)).setOnClickListener(new OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MuPDFReaderView.showPopupWindowDelete$lambda$8(MuPDFReaderView.this, f, f2, popupWindow, view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPopupWindowDelete$lambda$7(MuPDFReaderView this$0, PopupWindow popupWindow, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        Intrinsics.checkNotNullParameter(popupWindow, "$popupWindow");
        MuPDFView targetView = this$0.getActiveAnnotationView();
        if (targetView != null) {
            targetView.deleteSelectedAnnotation();
        }
        PointData deleted = targetView instanceof MuPDFPageView
                ? ((MuPDFPageView) targetView).getLastDeletedMarkup()
                : null;
        MarkupDeleteListener onMarkupDeleted = this$0.onMarkupDeleted;
        if (onMarkupDeleted != null && deleted != null) {
            onMarkupDeleted.onMarkupDeleted(deleted);
        } else {
            GlobalCustomToast globalCustomToast = this$0.getGlobalCustomToast();
            String string = this$0.mContext.getString(R.string.pdf_creation_failed);
            Intrinsics.checkNotNullExpressionValue(string, "getString(...)");
            globalCustomToast.show(string, GlobalCustomToast.Position.TOP_CENTER, 2000L, true, 12.0f, Integer.valueOf((int) R.color.black), Integer.valueOf((int) R.drawable.ic_right), Integer.valueOf((int) R.color.red_toast_background), Integer.valueOf((int) R.color.red_toast_border), Integer.valueOf((int) R.color.red));
        }
        this$0.clearInteractionStateAfterMarkupDelete();
        popupWindow.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPopupWindowDelete$lambda$8(MuPDFReaderView this$0, float f, float f2, PopupWindow popupWindow, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        Intrinsics.checkNotNullParameter(popupWindow, "$popupWindow");
        MuPDFView targetView = this$0.getActiveAnnotationView();
        if (targetView != null) {
            targetView.copyTextFromSelectBox(f, f2);
        }
        GlobalCustomToast globalCustomToast = this$0.getGlobalCustomToast();
        String string = this$0.mContext.getString(R.string.copied);
        Intrinsics.checkNotNullExpressionValue(string, "getString(...)");
        globalCustomToast.show(string, GlobalCustomToast.Position.TOP_CENTER, 2000L, true, 12.0f, Integer.valueOf((int) R.color.black), Integer.valueOf((int) R.drawable.ic_right), Integer.valueOf((int) R.color.green_toast_light), Integer.valueOf((int) R.color.green_toast_border), Integer.valueOf((int) R.color.green_icon_color));
        popupWindow.dismiss();
    }

    @SuppressLint({"InflateParams"})
    private final void showPopupWindowCopy(float f, float f2) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.custom_popup_menu, (ViewGroup) null);
        final PopupWindow popupWindow = new PopupWindow(inflate, -2, -2);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        popupWindow.showAtLocation(this, 0, (int) f, (int) f2);
        ((LinearLayout) inflate.findViewById(R.id.llMain)).setOnClickListener(new OnClickListener() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MuPDFReaderView.showPopupWindowCopy$lambda$9(MuPDFReaderView.this, popupWindow, view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void showPopupWindowCopy$lambda$9(MuPDFReaderView this$0, PopupWindow popupWindow, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        Intrinsics.checkNotNullParameter(popupWindow, "$popupWindow");
        this$0.setGlobalCustomToast(new GlobalCustomToast(this$0.mContext));
        MuPDFView muPDFView = this$0.getActiveSelectionView();
        if (muPDFView != null) {
            muPDFView.copySelection();
        }
        popupWindow.dismiss();
        GlobalCustomToast globalCustomToast = this$0.getGlobalCustomToast();
        String string = this$0.mContext.getString(R.string.copied);
        Intrinsics.checkNotNullExpressionValue(string, "getString(...)");
        globalCustomToast.show(string, GlobalCustomToast.Position.TOP_CENTER, 2000L, true, 12.0f, Integer.valueOf((int) R.color.black), Integer.valueOf((int) R.drawable.ic_right), Integer.valueOf((int) R.color.green_toast_light), Integer.valueOf((int) R.color.green_toast_border), Integer.valueOf((int) R.color.green_icon_color));
        this$0.exitCopyTextModeAfterCopy();
    }

    public final void deSelectText() {
        MuPDFView muPDFView = mSelectionView;
        if (muPDFView != null) {
            muPDFView.deselectText();
        }
        mSelectionView = null;
        View displayedView = getDisplayedView();
        MuPDFView centerView = displayedView instanceof MuPDFView ? (MuPDFView) displayedView : null;
        if (centerView != null && centerView != muPDFView) {
            centerView.deselectText();
        }
        View displayedView2 = getDisplayedView(-1);
        MuPDFView muPDFView2 = displayedView2 instanceof MuPDFView ? (MuPDFView) displayedView2 : null;
        View displayedView3 = getDisplayedView(1);
        MuPDFView muPDFView3 = displayedView3 instanceof MuPDFView ? (MuPDFView) displayedView3 : null;
        if (muPDFView2 != null) {
            muPDFView2.deselectText();
        }
        if (muPDFView3 != null) {
            muPDFView3.deselectText();
        }
    }

    public final void touch_start(float f, float f2) {
        View displayedView = getViewAtScreenPoint(f, f2);
        Intrinsics.checkNotNull(displayedView, "null cannot be cast to non-null type com.artifex.mupdfdemo.MuPDFView");
        MuPDFView pageView = (MuPDFView) displayedView;
        applyInkSettingsToPageView(pageView);
        float localX = toDocX(f);
        float localY = toDocY(f2);
        pageView.startDraw(localX, localY);
        this.mX = f;
        this.mY = f2;
        this.hasDrawn = false;
    }

    public final void touch_move(float f, float f2) {
        float abs = (float) Math.abs(f2 - this.mY);
        if (((float) Math.abs(f - this.mX)) >= 2.0f || abs >= 2.0f) {
            View displayedView = getViewAtScreenPoint(f, f2);
            Intrinsics.checkNotNull(displayedView, "null cannot be cast to non-null type com.artifex.mupdfdemo.MuPDFView");
            MuPDFView pageView = (MuPDFView) displayedView;
            applyInkSettingsToPageView(pageView);
            float localX = toDocX(f);
            float localY = toDocY(f2);
            pageView.continueDraw(localX, localY);
            this.mX = f;
            this.mY = f2;
            this.hasDrawn = true;
        }
    }

    public final void touch_up() {
        View displayedView = getViewAtScreenPoint(this.mX, this.mY);
        Intrinsics.checkNotNull(displayedView, "null cannot be cast to non-null type com.artifex.mupdfdemo.MuPDFView");
        MuPDFView pageView = (MuPDFView) displayedView;
        applyInkSettingsToPageView(pageView);
        pageView.saveDraw();
    }

    public final void setPaintStrockWidth(float f) {
        mInkStrokeWidth = Math.max(1f, f);
        applyInkStrokeWidthToPageViews(mInkStrokeWidth);
    }

    public final void loadAnnotation() {
        applyToChildren(new ViewMapper() {
            @Override
            public void applyToView(View view) {
                if (view instanceof MuPDFView) {
                    ((MuPDFView) view).loadAnnotation();
                }
            }
        });
    }

    @Override // com.artifex.mupdfdemo.ReaderView
    public void onChildSetup(int i, @NotNull View v) {
        Intrinsics.checkNotNullParameter(v, "v");
        if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber == i) {
            MuPDFView muPDFView = (MuPDFView) v;
            muPDFView.setSearchBoxes(SearchTaskResult.get().searchBoxes);
            muPDFView.setSearchBoxesPrim(SearchTaskResult.get().searchBoxesPrim);
        } else {
            MuPDFView muPDFView2 = (MuPDFView) v;
            muPDFView2.setSearchBoxes(null);
            muPDFView2.setSearchBoxesPrim(null);
        }
        MuPDFView muPDFView3 = (MuPDFView) v;
        muPDFView3.setLinkHighlighting(this.mLinksHighlighted);
        muPDFView3.setPaintStrockWidth(this.mInkStrokeWidth);
        muPDFView3.setChangeReporter(new Runnable() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                MuPDFReaderView.onChildSetup$lambda$10(MuPDFReaderView.this);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onChildSetup$lambda$10(MuPDFReaderView this$0) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        this$0.applyToChildren(new ViewMapper() { // from class: com.artifex.mupdfdemo.MuPDFReaderView$onChildSetup$1$1
            @Override // com.artifex.mupdfdemo.ReaderView.ViewMapper
            public void applyToView(View view) {
                Intrinsics.checkNotNullParameter(view, "view");
                ((MuPDFView) view).update();
            }
        });
    }

    @Override // com.artifex.mupdfdemo.ReaderView
    public void onMoveOffChild(int i) {
        View view = getView(i);
        if (view != null) {
            ((MuPDFView) view).deselectAnnotation();
        }
    }

    @Override // com.artifex.mupdfdemo.ReaderView
    public void onMoveToChild(int i) {
        if (SearchTaskResult.get() == null || SearchTaskResult.get().pageNumber == i) {
            return;
        }
        SearchTaskResult.set(null);
        resetupChildren();
    }

    @Override // com.artifex.mupdfdemo.ReaderView
    public void onSettle(@NotNull View v) {
        Intrinsics.checkNotNullParameter(v, "v");
        ((MuPDFView) v).updateHq(false);
    }

    @Override // com.artifex.mupdfdemo.ReaderView
    public void onUnsettle(@NotNull View v) {
        Intrinsics.checkNotNullParameter(v, "v");
        ((MuPDFView) v).removeHq();
    }

    @Override // com.artifex.mupdfdemo.ReaderView
    public void onNotInUse(@NotNull View v) {
        Intrinsics.checkNotNullParameter(v, "v");
        ((MuPDFView) v).releaseResources();
    }

    public final void undo() {
        View displayedView = getDisplayedView();
        Intrinsics.checkNotNull(displayedView, "null cannot be cast to non-null type com.artifex.mupdfdemo.MuPDFView");
        ((MuPDFView) displayedView).undo();
    }

    public final void redo() {
        View displayedView = getDisplayedView();
        Intrinsics.checkNotNull(displayedView, "null cannot be cast to non-null type com.artifex.mupdfdemo.MuPDFView");
        ((MuPDFView) displayedView).redo();
    }

    public final void setActiveMarkupColorArgb(int argb) {
        setInkColorResolved(argb);
    }

    public final void setInkColor(int i) {
        setInkColorResolved(i);
    }

    public final void setInkColorResolved(int argb) {
        mActiveMarkupColorArgb = argb;
        MuPDFView view = getActiveSelectionView();
        if (view == null) {
            View displayedView = getDisplayedView();
            view = displayedView instanceof MuPDFView ? (MuPDFView) displayedView : null;
        }
        if (view != null) {
            view.setInkColor(argb);
        }
    }

    /* JADX WARN: Failed to restore enum class, 'enum' modifier and super class removed */
    /* JADX WARN: Unknown enum class pattern. Please report as an issue! */
    /* loaded from: classes7.dex */
    /*public static final class Mode {
        private static final *//* synthetic *//* EnumEntries $ENTRIES;
        private static final *//* synthetic *//* Mode[] $VALUES;
        public static final Mode Viewing = new Mode("Viewing", 0);
        public static final Mode Selecting = new Mode("Selecting", 1);
        public static final Mode Drawing = new Mode("Drawing", 2);
        public static final Mode Signature = new Mode("Signature", 3);

        private static final *//* synthetic *//* Mode[] $values() {
            return new Mode[]{Viewing, Selecting, Drawing, Signature};
        }

        @NotNull
        public static EnumEntries<Mode> getEntries() {
            return $ENTRIES;
        }

        private Mode(String str, int i) {
        }

        static {
            Mode[] $values = $values();
            $VALUES = $values;
            $ENTRIES = EnumEntriesKt.enumEntries($values);
        }

        public static Mode valueOf(String str) {
            return (Mode) Enum.valueOf(Mode.class, str);
        }

        public static Mode[] values() {
            return (Mode[]) $VALUES.clone();
        }
    }*/

    public enum Mode {
        Viewing,
        Selecting,
        Drawing,
        Signature
    }

    /* loaded from: classes7.dex */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }

    public final void scrollToPage(int i) {
        if (i >= 0 && i < getPageCount()) {
            setDisplayedViewIndex(i);
        }
        loadAnnotation();
    }

    public final int getPageCount() {
        Adapter adapter = getAdapter();
        if (adapter != null) {
            return adapter.getCount();
        }
        return 0;
    }

    private final void showMagnifier(float f, float f2) {
        if (this.magnifierPopup == null) {
            this.magnifierView = new ImageView(getContext());
            ImageView imageView = this.magnifierView;
            int i = this.magnifierSize;
            this.magnifierPopup = new PopupWindow(imageView, i, i);
        }
        int[] iArr = new int[2];
        getLocationOnScreen(iArr);
        int i2 = iArr[0] + ((int) f);
        int i3 = iArr[1] + ((int) f2);
        updateMagnifier(f, f2);
        PopupWindow popupWindow = this.magnifierPopup;
        if (popupWindow != null) {
            popupWindow.showAtLocation(this, 0, i2, i3 - this.magnifierSize);
        }
    }

    private final void updateMagnifier(float f, float f2) {
        Bitmap captureZoomedBitmap = captureZoomedBitmap(f, f2);
        ImageView imageView = this.magnifierView;
        if (imageView != null) {
            imageView.setImageBitmap(captureZoomedBitmap);
        }
        PopupWindow popupWindow = this.magnifierPopup;
        if (popupWindow != null) {
            int i = this.magnifierSize;
            popupWindow.update((int) f, ((int) f2) - 200, i, i);
        }
    }

    private final void hideMagnifier() {
        PopupWindow popupWindow = this.magnifierPopup;
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        this.magnifierPopup = null;
    }

    private final Bitmap captureZoomedBitmap(float f, float f2) {
        int width = getWidth();
        int height = getHeight();

        Bitmap.Config config = Bitmap.Config.ARGB_8888;

        Bitmap createBitmap = Bitmap.createBitmap(width, height, config);
        Intrinsics.checkNotNullExpressionValue(createBitmap, "createBitmap(...)");

        draw(new Canvas(createBitmap));

        int i = (int) (this.magnifierSize / this.zoomFactor);
        float f3 = i / 2.0f;
        float f4 = i;

        int x = (int) Math.max(
                0f,
                Math.min(f - f3, getWidth() - f4)
        );

        int y = (int) Math.max(
                0f,
                Math.min(f2 - f3, getHeight() - f4)
        );

        Bitmap createBitmap2 = Bitmap.createBitmap(
                createBitmap,
                x,
                y,
                i,
                i
        );

        Intrinsics.checkNotNullExpressionValue(createBitmap2, "createBitmap(...)");

        int size = this.magnifierSize;

        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(
                createBitmap2,
                size,
                size,
                true
        );

        Intrinsics.checkNotNullExpressionValue(
                createScaledBitmap,
                "createScaledBitmap(...)"
        );

        Bitmap createBitmap3 = Bitmap.createBitmap(
                size,
                size,
                config
        );

        Intrinsics.checkNotNullExpressionValue(
                createBitmap3,
                "createBitmap(...)"
        );

        Canvas canvas = new Canvas(createBitmap3);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        canvas.drawBitmap(
                createScaledBitmap,
                0.0f,
                0.0f,
                paint
        );

        return getRoundedRectangleBitmapWithShadow(
                createBitmap3,
                350.0f,
                200.0f,
                0.0f,
                120.0f,
                ContextCompat.getColor(this.mContext, R.color.light_gray),
                20.0f,
                this.mContext
        );
    }

    /*private final Bitmap captureZoomedBitmap(float f, float f2) {
        int width = getWidth();
        int height = getHeight();
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap createBitmap = Bitmap.createBitmap(width, height, config);
        Intrinsics.checkNotNullExpressionValue(createBitmap, "createBitmap(...)");
        draw(new Canvas(createBitmap));
        int i = (int) (this.magnifierSize / this.zoomFactor);
        float f3 = i / 2;
        float f4 = i;
        Bitmap createBitmap2 = Bitmap.createBitmap(createBitmap, (int) RangesKt___RangesKt.coerceIn(f - f3, 0.0f, getWidth() - f4), (int) RangesKt___RangesKt.coerceIn(f2 - f3, 0.0f, getHeight() - f4), i, i);
        Intrinsics.checkNotNullExpressionValue(createBitmap2, "createBitmap(...)");
        int i2 = this.magnifierSize;
        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(createBitmap2, i2, i2, true);
        Intrinsics.checkNotNullExpressionValue(createScaledBitmap, "createScaledBitmap(...)");
        int i3 = this.magnifierSize;
        Bitmap createBitmap3 = Bitmap.createBitmap(i3, i3, config);
        Intrinsics.checkNotNullExpressionValue(createBitmap3, "createBitmap(...)");
        Canvas canvas = new Canvas(createBitmap3);
        Paint paint = new Paint(1);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawBitmap(createScaledBitmap, 0.0f, 0.0f, paint);
        return getRoundedRectangleBitmapWithShadow(createBitmap3, 350.0f, 200.0f, 0.0f, 120.0f, ContextCompat.getColor(this.mContext, R.color.light_gray), 20.0f, this.mContext);
    }*/

    private final Bitmap getRoundedRectangleBitmapWithShadow(Bitmap bitmap, float f, float f2, float f3, float f4, int i, float f5, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int applyDimension = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, f, displayMetrics);
        int applyDimension2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, f2, displayMetrics);
        float applyDimension3 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, f3, displayMetrics);
        float applyDimension4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, f4, displayMetrics);
        float applyDimension5 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, f5, displayMetrics);
        int i2 = (int) (2 * applyDimension5);
        Bitmap createBitmap = Bitmap.createBitmap(applyDimension + i2, i2 + applyDimension2, Bitmap.Config.ARGB_8888);
        Intrinsics.checkNotNullExpressionValue(createBitmap, "createBitmap(...)");
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawColor(0);
        RectF rectF = new RectF(applyDimension5, applyDimension5, applyDimension + applyDimension5, applyDimension2 + applyDimension5);
        Paint paint = new Paint(1);
        paint.setColor(i);
        paint.setStyle(Paint.Style.FILL);
        paint.setMaskFilter(new BlurMaskFilter(applyDimension5, BlurMaskFilter.Blur.NORMAL));
        canvas.drawRoundRect(rectF, applyDimension4, applyDimension4, paint);
        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, applyDimension, applyDimension2, true);
        Intrinsics.checkNotNullExpressionValue(createScaledBitmap, "createScaledBitmap(...)");
        Paint paint2 = new Paint(1);
        Shader.TileMode tileMode = Shader.TileMode.CLAMP;
        paint2.setShader(new BitmapShader(createScaledBitmap, tileMode, tileMode));
        canvas.drawRoundRect(rectF, applyDimension4, applyDimension4, paint2);
        if (applyDimension3 > 0.0f) {
            Paint paint3 = new Paint(1);
            paint3.setStyle(Paint.Style.STROKE);
            paint3.setStrokeWidth(applyDimension3);
            paint3.setColor(-16777216);
            canvas.drawRoundRect(rectF, applyDimension4, applyDimension4, paint3);
        }
        return createBitmap;
    }
}
