package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.handler;

import androidx.annotation.NonNull;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.TableView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.preference.Preferences;


public class PreferencesHandler {
    @NonNull
    private final ScrollHandler scrollHandler;
    @NonNull
    private final SelectionHandler selectionHandler;

    public PreferencesHandler(@NonNull TableView tableView) {
        this.scrollHandler = tableView.getScrollHandler();
        this.selectionHandler = tableView.getSelectionHandler();
    }

    @NonNull
    public Preferences savePreferences() {
        Preferences preferences = new Preferences();
        preferences.columnPosition = this.scrollHandler.getColumnPosition();
        preferences.columnPositionOffset = this.scrollHandler.getColumnPositionOffset();
        preferences.rowPosition = this.scrollHandler.getRowPosition();
        preferences.rowPositionOffset = this.scrollHandler.getRowPositionOffset();
        preferences.selectedColumnPosition = this.selectionHandler.getSelectedColumnPosition();
        preferences.selectedRowPosition = this.selectionHandler.getSelectedRowPosition();
        return preferences;
    }

    public void loadPreferences(@NonNull Preferences preferences) {
        this.scrollHandler.scrollToColumnPosition(preferences.columnPosition, preferences.columnPositionOffset);
        this.scrollHandler.scrollToRowPosition(preferences.rowPosition, preferences.rowPositionOffset);
        this.selectionHandler.setSelectedColumnPosition(preferences.selectedColumnPosition);
        this.selectionHandler.setSelectedRowPosition(preferences.selectedRowPosition);
    }
}
