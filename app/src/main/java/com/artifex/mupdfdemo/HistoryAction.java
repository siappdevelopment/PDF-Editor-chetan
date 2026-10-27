package com.artifex.mupdfdemo;

import java.util.Stack;

/* loaded from: classes7.dex */
public class HistoryAction {
    static final int TYPE_DELETE = 2;
    static final int TYPE_MARKUP = 1;
    Object data;
    int type;
    public static Stack<HistoryAction> undoStack = new Stack<>();
    public static Stack<HistoryAction> redoStack = new Stack<>();

    public HistoryAction(int type, Object data) {
        this.type = type;
        this.data = data;
    }
}
