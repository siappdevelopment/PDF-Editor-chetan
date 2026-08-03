package com.reader.alldocviewer.pdfeditor.ui.activities.pdfToImage

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData

object PdfDataManager {
    val imageListLiveData = MutableLiveData<List<Bitmap?>>(ArrayList())
    val selectedImagesLiveData = MutableLiveData<List<Bitmap?>>(ArrayList())
}
