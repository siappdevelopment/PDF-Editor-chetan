package com.reader.alldocviewer.pdfeditor.pdfviewer.util;

import com.tom_roush.fontbox.cmap.CMapParser;

import java.util.ArrayList;


public class ArrayUtils {
    public static int[] deleteDuplicatedPages(int[] pages) {
        ArrayList arrayList = new ArrayList();
        int length = pages.length;
        int i = -1;
        int i2 = 0;
        while (i2 < length) {
            int i3 = pages[i2];
            Integer valueOf = Integer.valueOf(i3);
            if (i != i3) {
                arrayList.add(valueOf);
            }
            i2++;
            i = i3;
        }
        int[] iArr = new int[arrayList.size()];
        for (int i4 = 0; i4 < arrayList.size(); i4++) {
            iArr[i4] = ((Integer) arrayList.get(i4)).intValue();
        }
        return iArr;
    }

    public static int[] calculateIndexesInDuplicateArray(int[] originalUserPages) {
        int[] iArr = new int[originalUserPages.length];
        if (originalUserPages.length == 0) {
            return iArr;
        }
        int i = 0;
        iArr[0] = 0;
        for (int i2 = 1; i2 < originalUserPages.length; i2++) {
            if (originalUserPages[i2] != originalUserPages[i2 - 1]) {
                i++;
            }
            iArr[i2] = i;
        }
        return iArr;
    }

    public static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i != array.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
