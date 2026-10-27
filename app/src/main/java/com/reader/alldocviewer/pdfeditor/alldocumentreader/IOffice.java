package com.reader.alldocviewer.pdfeditor.alldocumentreader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.common.IOfficeToPicture;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.res.ResKit;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.MainControl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public abstract class IOffice implements IMainFrame {
    private MainControl control;
    private String tempFilePath;
    private boolean writeLog = true;
    private final Object bg = -3355444;

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void changePage() {
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void changeZoom() {
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void completeLayout() {
    }

    public void destroyEngine() {
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void error(int i) {
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public abstract Activity getActivity();

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public abstract String getAppName();

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public int getBottomBarHeight() {
        return 0;
    }

    public abstract int getMovingOrientation();

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public abstract File getTemporaryDirectory();

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public int getTopBarHeight() {
        return 0;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public byte getWordDefaultView() {
        return (byte) 0;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isChangePage() {
        return true;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isDrawPageNumber() {
        return true;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isIgnoreOriginalSize() {
        return false;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isPopUpErrorDlg() {
        return true;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isShowFindDlg() {
        return true;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isShowPasswordDlg() {
        return true;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isShowProgressBar() {
        return true;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isShowTXTEncodeDlg() {
        return true;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isShowZoomingMsg() {
        return true;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isThumbnail() {
        return false;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isTouchZoom() {
        return true;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isZoomAfterLayoutForWord() {
        return true;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean onEventMethod(View view, MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2, byte b) {
        return false;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void setFindBackForwardState(boolean z) {
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void setIgnoreOriginalSize(boolean z) {
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void setThumbnail(boolean z) {
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void showProgressBar(boolean z) {
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void updateToolsbarStatus() {
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void updateViewImages(List<Integer> list) {
    }

    @Override
    public boolean useBackgroundPageLayout() {
        return true;
    }

    @Override
    public void onWordInitComplete() {
    }

    public IOffice() {
        initControl();
    }

    public void changeModeView() {
        if (this.control != null) {
            this.control.actionEvent(20, null);
        }
    }

    public void findCheckMove(String str) {
        this.control.getFind().find(str);
    }

    public Boolean findCheck(String str) {
        MainControl mainControl = this.control;
        if (mainControl != null && mainControl.getFind() != null) {
            return Boolean.valueOf(this.control.getFind().find(str));
        }
        return Boolean.FALSE;
    }

    public void findBackward() {
        this.control.getFind().findBackward();
    }

    public void findForward() {
        this.control.getFind().findForward();
    }

    private void initControl() {
        MainControl mainControl = new MainControl(this);
        this.control = mainControl;
        mainControl.setOffictToPicture(new IOfficeToPicture() { // from class: com.reader.alldocviewer.pdfeditor.alldocumentreader.IOffice.1
            private Bitmap bitmap;

            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.common.IOfficeToPicture
            public void dispose() {
            }

            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.common.IOfficeToPicture
            public byte getModeType() {
                return (byte) 1;
            }

            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.common.IOfficeToPicture
            public boolean isZoom() {
                return false;
            }

            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.common.IOfficeToPicture
            public void setModeType(byte b) {
            }

            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.common.IOfficeToPicture
            public Bitmap getBitmap(int i, int i2) {
                if (i == 0 || i2 == 0) {
                    return null;
                }
                Bitmap bitmap = this.bitmap;
                if (bitmap == null || bitmap.getWidth() != i || this.bitmap.getHeight() != i2) {
                    Bitmap bitmap2 = this.bitmap;
                    if (bitmap2 != null) {
                        bitmap2.recycle();
                    }
                    this.bitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
                }
                return this.bitmap;
            }

            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.common.IOfficeToPicture
            public void callBack(Bitmap bitmap) {
                IOffice.this.saveBitmapToFile(bitmap);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void saveBitmapToFile(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        if (this.tempFilePath == null) {
            this.tempFilePath = getActivity().getCacheDir().getAbsolutePath();
            File file = new File(this.tempFilePath + File.separatorChar + "tempPic");
            if (!file.exists()) {
                file.mkdir();
            }
            this.tempFilePath = file.getAbsolutePath();
        }
        File file2 = new File(this.tempFilePath + File.separatorChar + "export_image.jpg");
        try {
            if (file2.exists()) {
                file2.delete();
            }
            file2.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public View getView() {
        return this.control.getView();
    }

    public void openFile(String str) {
        getControl().openFile(str);
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean doActionEvent(int i, Object obj) {
        try {
        } catch (Exception e) {
            this.control.getSysKit().getErrorKit().writerLog(e);
        }
        if (i == 20) {
            updateToolsbarStatus();
        } else if (i != 788529152) {
            return i == 1073741828;
        } else {
            String trim = ((String) obj).trim();
            if (trim.length() > 0 && this.control.getFind().find(trim)) {
                setFindBackForwardState(true);
            }
        }
        return false;
    }

    public MainControl getControl() {
        return this.control;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public String getLocalString(String str) {
        return ResKit.instance().getLocalString(str);
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public String getTXTDefaultEncode() {
        return "GBK";
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void setWriteLog(boolean z) {
        this.writeLog = z;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public boolean isWriteLog() {
        return this.writeLog;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public Object getViewBackground() {
        return this.bg;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public byte getPageListViewMovingPosition() {
        return (byte) getMovingOrientation();
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IMainFrame
    public void dispose() {
        MainControl mainControl = this.control;
        if (mainControl != null) {
            mainControl.dispose();
            this.control = null;
        }
    }
}
