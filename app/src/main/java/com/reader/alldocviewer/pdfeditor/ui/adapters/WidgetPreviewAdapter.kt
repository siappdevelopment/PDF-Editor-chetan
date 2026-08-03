package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.R

class WidgetPreviewAdapter(private val images: List<Int>) :
    RecyclerView.Adapter<WidgetPreviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivWidgetPreview: ImageView = view.findViewById(R.id.ivWidgetPreview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_widget_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.ivWidgetPreview.setImageResource(images[position])
    }

    override fun getItemCount(): Int = images.size
}
