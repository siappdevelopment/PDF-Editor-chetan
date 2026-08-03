package com.reader.alldocviewer.pdfeditor.createpdf

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.R

class SignatureBitmapAdapter(
    private var bitmaps: MutableList<Bitmap>,
    private val onClick: (Int, Boolean) -> Unit,
) : RecyclerView.Adapter<SignatureBitmapAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageViewBitmap)
        val remove: ImageView = view.findViewById(R.id.imageViewRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bitmap, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.setImageBitmap(bitmaps[position])
        holder.itemView.setOnClickListener { onClick(position, false) }
        holder.remove.setOnClickListener { onClick(position, true) }
    }

    override fun getItemCount(): Int = bitmaps.size

    fun updateList(items: List<Bitmap>) {
        bitmaps = items.toMutableList()
        notifyDataSetChanged()
    }
}
