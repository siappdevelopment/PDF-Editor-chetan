package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.preference;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/* loaded from: classes11.dex */
public class Preferences implements Parcelable {
    @NonNull
    public static final Parcelable.Creator<Preferences> CREATOR = new Parcelable.Creator<Preferences>() { // from class: com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.preference.Preferences.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        @NonNull
        public Preferences createFromParcel(Parcel parcel) {
            return new Preferences(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        @NonNull
        public Preferences[] newArray(int i) {
            return new Preferences[i];
        }
    };
    public int columnPosition;
    public int columnPositionOffset;
    public int rowPosition;
    public int rowPositionOffset;
    public int selectedColumnPosition;
    public int selectedRowPosition;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Preferences() {
    }

    public Preferences(Parcel parcel) {
        this.rowPosition = parcel.readInt();
        this.rowPositionOffset = parcel.readInt();
        this.columnPosition = parcel.readInt();
        this.columnPositionOffset = parcel.readInt();
        this.selectedRowPosition = parcel.readInt();
        this.selectedColumnPosition = parcel.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.rowPosition);
        parcel.writeInt(this.rowPositionOffset);
        parcel.writeInt(this.columnPosition);
        parcel.writeInt(this.columnPositionOffset);
        parcel.writeInt(this.selectedRowPosition);
        parcel.writeInt(this.selectedColumnPosition);
    }
}
