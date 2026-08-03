package com.reader.alldocviewer.pdfeditor.pdfviewer.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/* loaded from: classes11.dex */
public class FileUtils {
    public static File fileFromAsset(Context context, String assetName) throws IOException {
        File cacheDir = context.getCacheDir();
        File file = new File(cacheDir, assetName + "-pdfview.pdf");
        if (assetName.contains("/")) {
            file.getParentFile().mkdirs();
        }
        copy(context.getAssets().open(assetName), file);
        return file;
    }

    public static void copy(InputStream inputStream, File output) throws IOException {
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(output);
            try {
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = inputStream.read(bArr);
                    if (read != -1) {
                        fileOutputStream2.write(bArr, 0, read);
                    } else {
                        try {
                            inputStream.close();
                            return;
                        } finally {
                            fileOutputStream2.close();
                        }
                    }
                }
            } catch (Throwable th) {
                th = th;
                fileOutputStream = fileOutputStream2;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } finally {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    }
                }
                throw th;
            }
        } catch (Throwable th2) {
//            th = th2;
        }
    }
}
