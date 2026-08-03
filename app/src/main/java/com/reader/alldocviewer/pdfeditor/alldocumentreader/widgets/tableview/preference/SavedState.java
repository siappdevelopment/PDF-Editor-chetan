package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.preference;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import androidx.annotation.NonNull;

/* loaded from: classes11.dex */
public class SavedState extends View.BaseSavedState {
    @NonNull
    public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.preference.SavedState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        @NonNull
        public SavedState createFromParcel(Parcel parcel) {
            return new SavedState(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        @NonNull
        public SavedState[] newArray(int i) {
            return new SavedState[i];
        }
    };
    public Preferences preferences;

    public SavedState(Parcelable parcelable) {
        super(parcelable);
    }

    private SavedState(Parcel parcel) {
        super(parcel);
        this.preferences = (Preferences) parcel.readParcelable(Preferences.class.getClassLoader());
    }

    @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeParcelable(this.preferences, i);
    }
}
