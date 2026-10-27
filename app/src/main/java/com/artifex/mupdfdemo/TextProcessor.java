package com.artifex.mupdfdemo;

/* loaded from: classes7.dex */
interface TextProcessor {
    void onEndLine();

    void onStartLine();

    void onWord(TextWord textWord);
}
