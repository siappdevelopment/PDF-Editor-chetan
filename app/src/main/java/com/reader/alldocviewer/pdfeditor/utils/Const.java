package com.reader.alldocviewer.pdfeditor.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.view.View;

import com.reader.alldocviewer.pdfeditor.R;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.PointData;
import com.reader.alldocviewer.pdfeditor.createpdf.PageType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.NoWhenBranchMatchedException;
import kotlin.Pair;
import kotlin.jvm.JvmField;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;

@SourceDebugExtension({"SMAP\nConst.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Const.kt\ncom/office/allreader/utils/Const\n+ 2 Bitmap.kt\nandroidx/core/graphics/BitmapKt\n*L\n1#1,656:1\n84#2,6:657\n*S KotlinDebug\n*F\n+ 1 Const.kt\ncom/office/allreader/utils/Const\n*L\n466#1:657,6\n*E\n"})

public final class Const {
    @NotNull
    public static final String ADD_PASSWORD = "AddPassword";
    @NotNull
    public static final String ADD_WATERMARK = "ADD_WATERMARK";
    @NotNull
    public static final String ALL_FILE = "ALL_FILE";
    private static int ALL_FILE_TAB = 0;
    @NotNull
    public static final String COMPRESS_PDF = "CompressPDF";
    @NotNull
    public static final String CREATE_PDF = "CREATE_PDF";
    @NotNull
    public static final String CREATE_ZIP = "CREATE_ZIP";
    @NotNull
    public static final String Catch_Folder_Name = "DocumentReaderRestore12";
    private static boolean CheckTheme = false;
    @JvmField
    public static boolean CopyText = false;
    @NotNull
    public static final String DOCUMENT_TYPE = "DOCUMENT_TYPE";
    @NotNull
    public static final String DROPBOX_ACCESS_TOKEN = "DROPBOX_ACCESS_TOKEN";
    @NotNull
    public static final String EXCEL_FILE = "EXCEL_FILE";
    @JvmField
    public static boolean ExternalEditFilesOpen = false;
    @JvmField
    public static boolean ExternalFilesOpen = false;
    @JvmField
    public static boolean ExternalFilesViewOpen = false;
    @NotNull
    public static final String FAVOURITES = "FAVOURITES";
    @NotNull
    public static final String FAV_VIEW_ACTIVITY = "FAV_VIEW_ACTIVITY";
    @NotNull
    public static final String FILE_IMAGES = ".zip";
    @NotNull
    public static final String FILE_TYPE_CSV = ".csv";
    @NotNull
    public static final String FILE_TYPE_DIRECTORIES = "FILE_TYPE_DIRECTORIES";
    @NotNull
    public static final String FILE_TYPE_DOC = ".doc";
    @NotNull
    public static final String FILE_TYPE_DOCX = ".docx";
    @NotNull
    public static final String FILE_TYPE_HTML = ".html";
    @NotNull
    public static final String FILE_TYPE_JPEG = ".jpeg";
    @NotNull
    public static final String FILE_TYPE_JPG = ".jpg";
    @NotNull
    public static final String FILE_TYPE_JSON = ".json";
    @NotNull
    public static final String FILE_TYPE_PDF = ".pdf";
    @NotNull
    public static final String FILE_TYPE_PDF_UPPERCASE = ".PDF";
    @NotNull
    public static final String FILE_TYPE_PNG = ".png";
    @NotNull
    public static final String FILE_TYPE_PPT = ".ppt";
    @NotNull
    public static final String FILE_TYPE_PPTX = ".pptx";
    @NotNull
    public static final String FILE_TYPE_TXT = ".txt";
    @NotNull
    public static final String FILE_TYPE_XLS = ".xls";
    @NotNull
    public static final String FILE_TYPE_XLSX = ".xlsx";
    @NotNull
    public static final String FILE_TYPE_XML = ".xml";
    @NotNull
    public static final String FILE_TYPE_ZIP = ".zip";
    @NotNull
    public static final String GRID_ORIENTATION = "GRID_ORIENTATION";
    @NotNull
    public static final String IMAGE_FILE = "IMAGE_FILE";
    @NotNull
    public static final String IMAGE_TO_PDF = "IMAGE_TO_PDF";
    @NotNull
    public static final String INVERT_PDF = "InvertPdf";
    @NotNull
    public static final String IS_FILE_LOCKED = "IS_FILE_LOCKED";
    @NotNull
    public static final String IS_FILE_UNLOCKED = "IS_FILE_UNLOCKED";
    @NotNull
    public static final String IS_FIRST_TIME_AD_OPEN_AD = "IS_FIRST_TIME_AD_OPEN_AD";
    @NotNull
    public static final String IS_SHOW_AD_OPEN_AD = "IS_SHOW_AD_OPEN_AD";
    @NotNull
    public static final String LANGUAGE = "LANGUAGE";
    @NotNull
    public static final String MERGE_PDF = "MergePdf";
    @NotNull
    public static final String MORE_APP = "MORE_APP";
    @NotNull
    public static final String OTHER_FILE = "OTHER_FILE";
    @NotNull
    public static final String OTHER_VIEW_ACTIVITY = "OTHER_VIEW_ACTIVITY";
    @NotNull
    public static final String PDF_FILE = "PDF_FILE";
    @NotNull
    public static final String PDF_VIEW_ACTIVITY = "PDF_VIEW_ACTIVITY";
    @NotNull
    public static final String PPT_FILE = "PPT_FILE";
    @NotNull
    public static final String PRINT_VIEW_ACTIVITY = "PRINT_VIEW_ACTIVITY";
    @NotNull
    public static final String RATE_US_DIALOG = "RATE_US_DIALOG";
    @NotNull
    public static final String RECENT = "RECENT";
    @NotNull
    public static final String RECENT_VIEW_ACTIVITY = "RECENT_VIEW_ACTIVITY";
    @NotNull
    public static final String RECYCLE_BIN = "RECYCLE_BIN";
    @NotNull
    public static final String REMOVE_PASSWORD = "RemovePassword";
    @NotNull
    public static final String ROTATE_PAGES = "RotatePages";
    @NotNull
    public static final String SHOW_COUNT_HINT = "SHOW_COUNT_HINT";
    @NotNull
    public static final String SHOW_ORIENTATION_HINT = "SHOW_ORIENTATION_HINT";
    @NotNull
    public static final String SHOW_PAGE_BY_PAGE_HINT = "SHOW_PAGE_BY_PAGE_HINT";
    @NotNull
    public static final String SORT_BY_NAME = "SORT_BY_NAME";
    @NotNull
    public static final String SORT_BY_SIZE = "SORT_BY_SIZE";
    @NotNull
    public static final String SORT_BY_TIME = "SORT_BY_TIME";
    @NotNull
    public static final String SORT_BY_TYPE = "SORT_BY_TYPE";
    @NotNull
    public static final String SPLASH_COUNTER = "SplashCounter";
    @NotNull
    public static final String SPLASH_VALUE = "SPLASH_VALUE";
    @NotNull
    public static final String SPLIT_PDF = "SplitPdf";
    @NotNull
    public static final String TEXT_VIEW_ACTIVITY = "TEXT_VIEW_ACTIVITY";
    @NotNull
    public static final String THEME_TYPE = "THEME_TYPE";
    @NotNull
    public static final String THEME_VALUE = "THEME_VALUE";
    @NotNull
    public static final String TXT_FILE = "TXT_FILE";
    @NotNull
    public static final String WITHOUT_ZIP_FILE = "WITHOUT_ZIP_FILE";
    @NotNull
    public static final String WORD_FILE = "WORD_FILE";
    @NotNull
    public static final String ZIP = "ZIP";
    @NotNull
    public static final String ZIP_FILE = "ZIP_FILE";
    private static boolean addWidget4;
    private static boolean checkDeleteOrNot;
    @JvmField
    public static boolean checkEditClick;
    @JvmField
    public static boolean checkEditorNot;
    @JvmField
    public static boolean markupLoadedFromPdf;
    private static boolean checkdiaogopn;
    private static boolean checknotification;
    private static boolean checknotificationdialog;
    private static boolean checkprogress;
    private static boolean checkstatus;
    private static int counter;
    private static int countersplash;
    private static boolean createPdfOrNot;
    private static int currentPage;
    @JvmField
    public static boolean deleteText;
    @JvmField
    public static float endPointY;
    private static long fileDate;
    @Nullable
    private static Uri fileUri;
    private static boolean fromSplash;
    @JvmField
    public static boolean highlight;
    @JvmField
    public static boolean inkCheck;
    private static boolean isAppOpenShowShow;
    @JvmField
    public static boolean isDarkMode;
    private static boolean isDialogOpen;
    private static boolean isEyeProtectMode;
    private static boolean isFirebaseConfig;
    private static boolean isFirstAppOpenShow;
    private static boolean isFirsttimePssword;
    private static boolean isInsertPage;
    private static boolean isOPenThemeDialog;
    private static boolean isOrganizeChange;
    private static boolean isSwipe;
    private static boolean isUpdated;
    private static boolean isUpdatedSetUp;
    private static boolean isVertical;
    private static boolean notificationClick;
    private static boolean openExternalCheck;
    private static int pageIndex;
    private static int pageIndexNew;
    @JvmField
    public static float startPointX;
    @JvmField
    public static boolean strikeOut;
    private static int toolsIndex;
    @JvmField
    public static boolean underline;
    private static int viewpagerPosCheck;
    private static double widhCoreView;
    @NotNull
    public static final Const INSTANCE = new Const();
    @NotNull
    private static final String PURCHASE_KEY = "pdf_lifetime_pro";
    private static int PDF_FILE_TAB = 1;
    private static int WORD_FILE_TAB = 2;
    private static int EXCEL_FILE_TAB = 3;
    private static int PPT_FILE_TAB = 4;
    private static int OTHER_FILE_TAB = 5;
    private static int CurrentViewpagerPosition = 1;
    private static int colorResIdHighlite = R.color.color_picker_3;
    private static int colorResIdHighlite1 = R.color.color_picker_3;
    private static int colorResIdUnderline = R.color.color_picker_3;
    private static int colorResIdStrikeOut = R.color.color_picker_3;
    private static int colorResIdInk = R.color.color_picker_3;
    @JvmField
    @Nullable
    public static final List<PointData> insertionOrderList = new ArrayList();
    @JvmField
    public static final Object MARKUP_SESSION_LOCK = new Object();
    @JvmField
    @NotNull
    public static final List<PointData> redoStack = new ArrayList();
    @JvmField
    @NotNull
    public static final List<PointData> deletedPoints = new ArrayList();
    @JvmField
    @NotNull
    public static final Map<Integer, List<PointData>> savePoints = new LinkedHashMap();
    @NotNull
    private static String viewSelectedPathName = "";
    @NotNull
    private static String filePassword = "";
    @NotNull
    private static String toolsPassword = "";
    @NotNull
    private static String path = "";
    @NotNull
    private static String privacyPolicyUrl = "https://sites.google.com/gameadzone.com/pdf-reader-editor";

    /*
    public *//* synthetic *//* class WhenMappings {
        public static final *//* synthetic *//* int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[PageType.values().length];
            try {
                iArr[PageType.BLANK.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                iArr[PageType.LINE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                iArr[PageType.GRID.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                iArr[PageType.GRAPH.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            $EnumSwitchMapping$0 = iArr;
        }
    }*/

    
    public static final class WhenMappings {

        public static final int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[PageType.values().length];

            try {
                iArr[PageType.BLANK.ordinal()] = 1;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                iArr[PageType.LINE.ordinal()] = 2;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                iArr[PageType.GRID.ordinal()] = 3;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                iArr[PageType.GRAPH.ordinal()] = 4;
            } catch (NoSuchFieldError ignored) {
            }

            $EnumSwitchMapping$0 = iArr;
        }
    }

    private Const() {
    }

    @NotNull
    public final String getPURCHASE_KEY() {
        return PURCHASE_KEY;
    }

    public final boolean isAppOpenShowShow() {
        return isAppOpenShowShow;
    }

    public final void setAppOpenShowShow(boolean z) {
        isAppOpenShowShow = z;
    }

    public final boolean isFirstAppOpenShow() {
        return isFirstAppOpenShow;
    }

    public final void setFirstAppOpenShow(boolean z) {
        isFirstAppOpenShow = z;
    }

    public final boolean getFromSplash() {
        return fromSplash;
    }

    public final void setFromSplash(boolean z) {
        fromSplash = z;
    }

    public final boolean isFirebaseConfig() {
        return isFirebaseConfig;
    }

    public final void setFirebaseConfig(boolean z) {
        isFirebaseConfig = z;
    }

    public final boolean isOPenThemeDialog() {
        return isOPenThemeDialog;
    }

    public final void setOPenThemeDialog(boolean z) {
        isOPenThemeDialog = z;
    }

    public final boolean isInsertPage() {
        return isInsertPage;
    }

    public final void setInsertPage(boolean z) {
        isInsertPage = z;
    }

    public final long getFileDate() {
        return fileDate;
    }

    public final void setFileDate(long j) {
        fileDate = j;
    }

    public final int getALL_FILE_TAB() {
        return ALL_FILE_TAB;
    }

    public final void setALL_FILE_TAB(int i) {
        ALL_FILE_TAB = i;
    }

    public final int getPDF_FILE_TAB() {
        return PDF_FILE_TAB;
    }

    public final void setPDF_FILE_TAB(int i) {
        PDF_FILE_TAB = i;
    }

    public final int getWORD_FILE_TAB() {
        return WORD_FILE_TAB;
    }

    public final void setWORD_FILE_TAB(int i) {
        WORD_FILE_TAB = i;
    }

    public final int getEXCEL_FILE_TAB() {
        return EXCEL_FILE_TAB;
    }

    public final void setEXCEL_FILE_TAB(int i) {
        EXCEL_FILE_TAB = i;
    }

    public final int getPPT_FILE_TAB() {
        return PPT_FILE_TAB;
    }

    public final void setPPT_FILE_TAB(int i) {
        PPT_FILE_TAB = i;
    }

    public final int getOTHER_FILE_TAB() {
        return OTHER_FILE_TAB;
    }

    public final void setOTHER_FILE_TAB(int i) {
        OTHER_FILE_TAB = i;
    }

    public final int getCurrentViewpagerPosition() {
        return CurrentViewpagerPosition;
    }

    public final void setCurrentViewpagerPosition(int i) {
        CurrentViewpagerPosition = i;
    }

    public final boolean isSwipe() {
        return isSwipe;
    }

    public final void setSwipe(boolean z) {
        isSwipe = z;
    }

    public final boolean isOrganizeChange() {
        return isOrganizeChange;
    }

    public final void setOrganizeChange(boolean z) {
        isOrganizeChange = z;
    }

    public final boolean isEyeProtectMode() {
        return isEyeProtectMode;
    }

    public final void setEyeProtectMode(boolean z) {
        isEyeProtectMode = z;
    }

    public final boolean getCheckTheme() {
        return CheckTheme;
    }

    public final void setCheckTheme(boolean z) {
        CheckTheme = z;
    }

    public final boolean getNotificationClick() {
        return notificationClick;
    }

    public final void setNotificationClick(boolean z) {
        notificationClick = z;
    }

    public final boolean getOpenExternalCheck() {
        return openExternalCheck;
    }

    public final void setOpenExternalCheck(boolean z) {
        openExternalCheck = z;
    }

    public final boolean isDialogOpen() {
        return isDialogOpen;
    }

    public final void setDialogOpen(boolean z) {
        isDialogOpen = z;
    }

    public final int getToolsIndex() {
        return toolsIndex;
    }

    public final void setToolsIndex(int i) {
        toolsIndex = i;
    }

    public final int getPageIndex() {
        return pageIndex;
    }

    public final void setPageIndex(int i) {
        pageIndex = i;
    }

    public final int getPageIndexNew() {
        return pageIndexNew;
    }

    public final void setPageIndexNew(int i) {
        pageIndexNew = i;
    }

    public final int getColorResIdHighlite() {
        return colorResIdHighlite;
    }

    public final void setColorResIdHighlite(int i) {
        colorResIdHighlite = i;
    }

    public final int getColorResIdHighlite1() {
        return colorResIdHighlite1;
    }

    public final void setColorResIdHighlite1(int i) {
        colorResIdHighlite1 = i;
    }

    public final int getColorResIdUnderline() {
        return colorResIdUnderline;
    }

    public final void setColorResIdUnderline(int i) {
        colorResIdUnderline = i;
    }

    public final int getColorResIdStrikeOut() {
        return colorResIdStrikeOut;
    }

    public final void setColorResIdStrikeOut(int i) {
        colorResIdStrikeOut = i;
    }

    public final int getColorResIdInk() {
        return colorResIdInk;
    }

    public final void setColorResIdInk(int i) {
        colorResIdInk = i;
    }

    public final boolean getAddWidget4() {
        return addWidget4;
    }

    public final void setAddWidget4(boolean z) {
        addWidget4 = z;
    }

    public final boolean getCheckDeleteOrNot() {
        return checkDeleteOrNot;
    }

    public final void setCheckDeleteOrNot(boolean z) {
        checkDeleteOrNot = z;
    }

    public final int getCurrentPage() {
        return currentPage;
    }

    public final void setCurrentPage(int i) {
        currentPage = i;
    }

    public final int getViewpagerPosCheck() {
        return viewpagerPosCheck;
    }

    public final void setViewpagerPosCheck(int i) {
        viewpagerPosCheck = i;
    }

    @NotNull
    public final String getViewSelectedPathName() {
        return viewSelectedPathName;
    }

    public final void setViewSelectedPathName(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<set-?>");
        viewSelectedPathName = str;
    }

    @NotNull
    public final String getFilePassword() {
        return filePassword;
    }

    public final void setFilePassword(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<set-?>");
        filePassword = str;
    }

    @NotNull
    public final String getToolsPassword() {
        return toolsPassword;
    }

    public final void setToolsPassword(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<set-?>");
        toolsPassword = str;
    }

    public final boolean getCreatePdfOrNot() {
        return createPdfOrNot;
    }

    public final void setCreatePdfOrNot(boolean z) {
        createPdfOrNot = z;
    }

    public final boolean isFirsttimePssword() {
        return isFirsttimePssword;
    }

    public final void setFirsttimePssword(boolean z) {
        isFirsttimePssword = z;
    }

    public final double getWidhCoreView() {
        return widhCoreView;
    }

    public final void setWidhCoreView(double d) {
        widhCoreView = d;
    }

    public final boolean getCheckprogress() {
        return checkprogress;
    }

    public final void setCheckprogress(boolean z) {
        checkprogress = z;
    }

    public final boolean getChecknotification() {
        return checknotification;
    }

    public final void setChecknotification(boolean z) {
        checknotification = z;
    }

    public final boolean getChecknotificationdialog() {
        return checknotificationdialog;
    }

    public final void setChecknotificationdialog(boolean z) {
        checknotificationdialog = z;
    }

    public final boolean getCheckstatus() {
        return checkstatus;
    }

    public final void setCheckstatus(boolean z) {
        checkstatus = z;
    }

    public final boolean getCheckdiaogopn() {
        return checkdiaogopn;
    }

    public final void setCheckdiaogopn(boolean z) {
        checkdiaogopn = z;
    }

    public final int getCounter() {
        return counter;
    }

    public final void setCounter(int i) {
        counter = i;
    }

    public final int getCountersplash() {
        return countersplash;
    }

    public final void setCountersplash(int i) {
        countersplash = i;
    }

    @NotNull
    public final String getPath() {
        return path;
    }

    public final void setPath(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<set-?>");
        path = str;
    }

    public final boolean isVertical() {
        return isVertical;
    }

    public final void setVertical(boolean z) {
        isVertical = z;
    }

    public final boolean isUpdated() {
        return isUpdated;
    }

    public final void setUpdated(boolean z) {
        isUpdated = z;
    }

    public final boolean isUpdatedSetUp() {
        return isUpdatedSetUp;
    }

    public final void setUpdatedSetUp(boolean z) {
        isUpdatedSetUp = z;
    }

    @NotNull
    public final String getPrivacyPolicyUrl() {
        return privacyPolicyUrl;
    }

    public final void setPrivacyPolicyUrl(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<set-?>");
        privacyPolicyUrl = str;
    }

   /* public final boolean isConnected(@NotNull Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        Object systemService = context.getSystemService("connectivity");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.net.ConnectivityManager");
        NetworkInfo[] allNetworkInfo = ((ConnectivityManager) systemService).getAllNetworkInfo();
        Intrinsics.checkNotNullExpressionValue(allNetworkInfo, "getAllNetworkInfo(...)");
        for (NetworkInfo networkInfo : allNetworkInfo) {
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }*/

    public final boolean isPdfCorrupted(@NotNull Context context, @NotNull String pdfPath) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(pdfPath, "pdfPath");
        try {
            return new MuPDFCore(context, pdfPath).countPages() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

   /* public final boolean isManageExternalStoragePermissionGranted(@NotNull Context context) {
        boolean isExternalStorageManager;
        Intrinsics.checkNotNullParameter(context, "context");
        if (Build.VERSION.SDK_INT < 30) {
            return context.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0 && context.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0;
        }
        isExternalStorageManager = Environment.isExternalStorageManager();
        return isExternalStorageManager;
    }*/

    @NotNull
    public final String getExtensionsByFileType(@NotNull String fileType) {
        Intrinsics.checkNotNullParameter(fileType, "fileType");
        switch (fileType.hashCode()) {
            case -1828502393:
                if (fileType.equals(PPT_FILE)) {
                    return FILE_TYPE_PPTX;
                }
                break;
            case -933604828:
                if (fileType.equals(EXCEL_FILE)) {
                    return FILE_TYPE_XLSX;
                }
                break;
            case -642133493:
                if (fileType.equals(TXT_FILE)) {
                    return FILE_TYPE_TXT;
                }
                break;
            case -589120111:
                if (fileType.equals(WORD_FILE)) {
                    return FILE_TYPE_DOCX;
                }
                break;
            case 5547209:
                if (fileType.equals(PDF_FILE)) {
                    return ".pdf";
                }
                break;
        }
        return "";
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x0040, code lost:
        if (r5.equals("yellow") != false) goto L5;
     */
    /* JADX WARN: Code restructure failed: missing block: B:15:0x0049, code lost:
        if (r5.equals("orange") == false) goto L18;
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x0050, code lost:
        if (r6 == false) goto L11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x0052, code lost:
        r6 = new java.lang.StringBuilder();
        r6.append("nodata");
        r6.append(r5);
        r5 = "_blackmode";
     */
    /* JADX WARN: Code restructure failed: missing block: B:20:0x005f, code lost:
        r6.append(r5);
        r1 = r6.toString();
     */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x0067, code lost:
        r6 = new java.lang.StringBuilder();
        r6.append("nodata_");
     */
    /* JADX WARN: Code restructure failed: missing block: B:6:0x002e, code lost:
        if (r5.equals("green") == false) goto L18;
     */
    /* JADX WARN: Code restructure failed: missing block: B:9:0x0037, code lost:
        if (r5.equals("red") == false) goto L18;
    public final void noDataImage(@NotNull Context context, @NotNull android.widget.ImageView imageView, @NotNull ClientConfigPref clientConfigPref, boolean isDarkMode) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(imageView, "imageView");
        Intrinsics.checkNotNullParameter(clientConfigPref, "clientConfigPref");
        String theme = clientConfigPref.getApp_theme().toLowerCase(java.util.Locale.ROOT);
        Intrinsics.checkNotNullExpressionValue(theme, "toLowerCase(...)");
        String imageName;
        if (theme.equals("green") || theme.equals("red") || theme.equals("yellow") || theme.equals("orange")) {
            if (isDarkMode) {
                imageName = "nodata" + theme + "_blackmode";
            } else {
                imageName = "nodata_" + theme;
            }
        } else {
            if (isDarkMode) {
                imageName = "nodata_black";
            } else {
                imageName = "nodata";
            }
        }
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        imageView.setImageResource(resId);
    }

    public final void disableViewTemporarily(@NotNull final View view, long j) {
        Intrinsics.checkNotNullParameter(view, "view");
        view.setEnabled(false);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.office.allreader.utils.Const$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                Const.disableViewTemporarily$lambda$0(view);
            }
        }, j);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void disableViewTemporarily$lambda$0(View view) {
        Intrinsics.checkNotNullParameter(view, "$view");
        view.setEnabled(true);
    }

    public final int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public final boolean isLandscape() {
        return Resources.getSystem().getConfiguration().orientation == 2;
    }

    /*public final int setDialogTheme(@NotNull ClientConfigPref clientConfigPref) {
        Intrinsics.checkNotNullParameter(clientConfigPref, "clientConfigPref");
        return clientConfigPref.getApp_theme().equals("red") ? R.style.AppBottomSheetDialogTheme_red : clientConfigPref.getApp_theme().equals("green") ? R.style.AppBottomSheetDialogTheme_green : clientConfigPref.getApp_theme().equals("yellow") ? R.style.AppBottomSheetDialogTheme_yellow : clientConfigPref.getApp_theme().equals("orange") ? R.style.AppBottomSheetDialogTheme_orange : R.style.AppBottomSheetDialogTheme;
    }*/

    /*@Nullable
    public final Object generateThumbnail(@NotNull String str, @NotNull Continuation<? super Bitmap> continuation) {
        return BuildersKt.withContext(Dispatchers.getIO(), new Const$generateThumbnail$2(str, null), continuation);
    }*/

    /*public static *//* synthetic *//* Bitmap getPdfThumbnail$default(Const r0, File file, int i, int i2, int i3, int i4, Object obj) {
        if ((i4 & 2) != 0) {
            i = 0;
        }
        if ((i4 & 4) != 0) {
            i2 = 200;
        }
        if ((i4 & 8) != 0) {
            i3 = 300;
        }
        return r0.getPdfThumbnail(file, i, i2, i3);
    }

    @Nullable
    public final Bitmap getPdfThumbnail(@NotNull File pdfFile, int i, int i2, int i3) {
        Intrinsics.checkNotNullParameter(pdfFile, "pdfFile");
        try {
            ParcelFileDescriptor open = ParcelFileDescriptor.open(pdfFile, 268435456);
            PdfRenderer pdfRenderer = new PdfRenderer(open);
            PdfRenderer.Page openPage = pdfRenderer.openPage(i);
            Intrinsics.checkNotNullExpressionValue(openPage, "openPage(...)");
            Bitmap createBitmap = Bitmap.createBitmap(i2, i3, Bitmap.Config.ARGB_8888);
            openPage.render(createBitmap, null, null, 1);
            openPage.close();
            pdfRenderer.close();
            open.close();
            return createBitmap;
        } catch (Exception unused) {
            return null;
        }
    }*/

   /* @NotNull
    public final Pair<Integer, Integer> getPageSizeInPoints(@NotNull Context context, @NotNull String pageSize) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(pageSize, "pageSize");
        return Intrinsics.areEqual(pageSize, context.getString(R.string.a3)) ? new Pair<>(842, 1191) : Intrinsics.areEqual(pageSize, context.getString(R.string.a4)) ? new Pair<>(595, 842) : Intrinsics.areEqual(pageSize, context.getString(R.string.a5)) ? new Pair<>(420, 595) : Intrinsics.areEqual(pageSize, context.getString(R.string.b4)) ? new Pair<>(729, 1032) : Intrinsics.areEqual(pageSize, context.getString(R.string.b5)) ? new Pair<>(516, 729) : Intrinsics.areEqual(pageSize, context.getString(R.string.letter)) ? new Pair<>(Integer.valueOf((int) TypedValues.MotionType.TYPE_QUANTIZE_INTERPOLATOR_ID), 792) : Intrinsics.areEqual(pageSize, context.getString(R.string.legal)) ? new Pair<>(Integer.valueOf((int) TypedValues.MotionType.TYPE_QUANTIZE_INTERPOLATOR_ID), Integer.valueOf((int) PointerIconCompat.TYPE_TEXT)) : new Pair<>(595, 842);
    }*/

    @NotNull
    public final Pair<Integer, Integer> getPageSizeInPoints(
            @NotNull Context context,
            @NotNull String pageSize
    ) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(pageSize, "pageSize");

        return Intrinsics.areEqual(pageSize, context.getString(R.string.a3))
                ? new Pair<>(842, 1191)
                : Intrinsics.areEqual(pageSize, context.getString(R.string.a4))
                ? new Pair<>(595, 842)
                : Intrinsics.areEqual(pageSize, context.getString(R.string.a5))
                ? new Pair<>(420, 595)
                : Intrinsics.areEqual(pageSize, context.getString(R.string.b4))
                ? new Pair<>(729, 1032)
                : Intrinsics.areEqual(pageSize, context.getString(R.string.b5))
                ? new Pair<>(516, 729)
                : Intrinsics.areEqual(pageSize, context.getString(R.string.letter))
                ? new Pair<>(612, 792)
                : Intrinsics.areEqual(pageSize, context.getString(R.string.legal))
                ? new Pair<>(612, 1008)
                : new Pair<>(595, 842);
    }

    public final void drawPageContent(@NotNull Canvas canvas, int i, int i2, @NotNull PageType pageType, int i3) {
        float f;
        float f2;
        int i4;
        Intrinsics.checkNotNullParameter(canvas, "canvas");
        Intrinsics.checkNotNullParameter(pageType, "pageType");
        Paint paint = new Paint();
        paint.setColor(-16777216);
        paint.setStrokeWidth(1.0f);
        paint.setTextSize(32.0f);
        paint.setAntiAlias(true);
        int i5 = WhenMappings.$EnumSwitchMapping$0[pageType.ordinal()];
        if (i5 != 1) {
            if (i5 == 2) {
                for (float f3 = 150.0f; f3 < i2; f3 += 60.0f) {
                    canvas.drawLine(50.0f, f3, i - 50.0f, f3, paint);
                }
                return;
            }
            if (i5 == 3) {
                float f4 = 20 * Resources.getSystem().getDisplayMetrics().density;
                paint.setColor(-3355444);
                paint.setStrokeWidth(0.5f);
                float f5 = 0.0f;
                while (true) {
                    f = i2;
                    if (f5 > f) {
                        break;
                    }
                    canvas.drawLine(0.0f, f5, i, f5, paint);
                    f5 += f4;
                }
                for (float f6 = 0.0f; f6 <= i; f6 += f4) {
                    canvas.drawLine(f6, 0.0f, f6, f, paint);
                }
            } else if (i5 != 4) {
                throw new NoWhenBranchMatchedException();
            } else {
                float f7 = 5 * Resources.getSystem().getDisplayMetrics().density;
                float f8 = 0.0f;
                while (true) {
                    f2 = i2;
                    float f9 = 0.3f;
                    i4 = 10;
                    if (f8 > f2) {
                        break;
                    }
                    if ((f8 / f7) % 10 == 0.0f) {
                        f9 = 1.2f;
                    }
                    paint.setStrokeWidth(f9);
                    paint.setColor(-3355444);
                    canvas.drawLine(0.0f, f8, i, f8, paint);
                    f8 += f7;
                }
                float f10 = 0.0f;
                while (f10 <= i) {
                    paint.setStrokeWidth((f10 / f7) % ((float) i4) == 0.0f ? 1.2f : 0.3f);
                    paint.setColor(-3355444);
                    canvas.drawLine(f10, 0.0f, f10, f2, paint);
                    f10 += f7;
                    i4 = i4;
                }
            }
        }
    }
}
