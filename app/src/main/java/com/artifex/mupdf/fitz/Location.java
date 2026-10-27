package com.artifex.mupdf.fitz;

import java.util.Objects;

/* loaded from: classes7.dex */
public class Location {
    public final int chapter;
    public final int page;

    public Location(int i, int i2) {
        this.chapter = i;
        this.page = i2;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Location) {
            Location location = (Location) obj;
            return this.chapter == location.chapter && this.page == location.page;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.chapter), Integer.valueOf(this.page));
    }

    public String toString() {
        return "Location(chapter=" + this.chapter + ", page=" + this.page + ")";
    }
}
