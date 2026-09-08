package com.reader.alldocviewer.pdfeditor.pdfviewer.util;


public class MathUtils {
    public static final double BIG_ENOUGH_CEIL = 16384.999999999996d;
    public static final double BIG_ENOUGH_FLOOR = 16384.0d;
    public static final int BIG_ENOUGH_INT = 16384;

    public static int ceil(float value) {
        return ((int) (value + 16384.999999999996d)) - 16384;
    }

    public static int floor(float value) {
        return ((int) (value + 16384.0d)) - 16384;
    }

    public static float limit(float number, float between, float and) {
        return number <= between ? between : number >= and ? and : number;
    }

    public static int limit(int number, int between, int and) {
        return number <= between ? between : number >= and ? and : number;
    }

    public static float max(float number, float max) {
        return number > max ? max : number;
    }

    public static int max(int number, int max) {
        return number > max ? max : number;
    }

    public static float min(float number, float min) {
        return number < min ? min : number;
    }

    public static int min(int number, int min) {
        return number < min ? min : number;
    }
}
