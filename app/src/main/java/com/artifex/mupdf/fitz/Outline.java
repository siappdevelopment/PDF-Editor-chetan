package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Outline {
    public Outline[] down;
    public String title;
    public String uri;

    public Outline(String str, String str2, Outline[] outlineArr) {
        this.title = str;
        this.uri = str2;
        this.down = outlineArr;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.title);
        stringBuffer.append(' ');
        stringBuffer.append(this.uri);
        stringBuffer.append('\n');
        if (this.down != null) {
            for (int i = 0; i < this.down.length; i++) {
                stringBuffer.append('\t');
                stringBuffer.append(this.down[i]);
                stringBuffer.append('\n');
            }
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        return stringBuffer.toString();
    }
}
