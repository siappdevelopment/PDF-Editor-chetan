package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.holder;

import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public abstract class AbstractViewHolder extends RecyclerView.ViewHolder {
    @NonNull
    private SelectionState m_eState;

    
    public enum SelectionState {
        SELECTED,
        UNSELECTED,
        SHADOWED
    }

    public boolean onFailedToRecycleView() {
        return false;
    }

    public void onViewRecycled() {
    }

    public AbstractViewHolder(@NonNull View view) {
        super(view);
        this.m_eState = SelectionState.UNSELECTED;
    }

    public boolean isSelected() {
        return this.m_eState == SelectionState.SELECTED;
    }

    public void setSelected(@NonNull SelectionState selectionState) {
        this.m_eState = selectionState;
        if (selectionState == SelectionState.SELECTED) {
            this.itemView.setSelected(true);
        } else if (selectionState == SelectionState.UNSELECTED) {
            this.itemView.setSelected(false);
        }
    }

    public boolean isShadowed() {
        return this.m_eState == SelectionState.SHADOWED;
    }

    public void setBackgroundColor(@ColorInt int i) {
        this.itemView.setBackgroundColor(i);
    }
}
