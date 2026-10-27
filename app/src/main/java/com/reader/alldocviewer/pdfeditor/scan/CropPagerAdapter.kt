package com.reader.alldocviewer.pdfeditor.scan

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Point
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.databinding.ItemMediaEditPagerBinding
import me.pqpo.smartcropperlib.SmartCropper

class CropPagerAdapter(
    private val mActivity: Activity,
    private val mediaList: MutableList<Bitmap>,
    private val onDrag: (Boolean) -> Unit
) : RecyclerView.Adapter<CropPagerAdapter.MediaViewHolder>() {

    private val autoScanEnableList: MutableList<Boolean> = ArrayList()
    private val croppedBitmaps: MutableList<Bitmap> = ArrayList()
    private val isManuallyAdjusted: MutableList<Boolean> = ArrayList()
    private val detectedPointsList: MutableList<Array<Point>?> = ArrayList()

    init {
        iniCroplist()
    }

    fun iniCroplist() {
        croppedBitmaps.clear()
        croppedBitmaps.addAll(mediaList)
        isManuallyAdjusted.clear()
        autoScanEnableList.clear()
        detectedPointsList.clear()
        for (i in mediaList.indices) {
            isManuallyAdjusted.add(false)
            autoScanEnableList.add(false)
            detectedPointsList.add(null)
        }
        Log.e("ViewPagerActivity", "adapter.iniCroplist:${croppedBitmaps.size}")
    }

    fun isAutoScanEnabledAt(position: Int): Boolean {
        if (position in autoScanEnableList.indices) {
            return autoScanEnableList[position]
        }
        return false
    }

    fun toggleAutoCropAt(position: Int) {
        if (position in autoScanEnableList.indices) {
            val currentVal = autoScanEnableList[position]
            autoScanEnableList[position] = !currentVal
            isManuallyAdjusted[position] = false
            notifyItemChanged(position)
        }
    }

    fun resetManualAdjustment() {
        for (i in isManuallyAdjusted.indices) {
            isManuallyAdjusted[i] = false
        }
    }

    inner class MediaViewHolder(val binding: ItemMediaEditPagerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaEditPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val bitmap = mediaList[position]
        val autoScanEnable = isAutoScanEnabledAt(position)
        holder.binding.cropImageView.setAutoScanEnable(autoScanEnable)
        holder.binding.cropImageView.setImageToCrop(
            bitmap,
            true,
            object : CropImageView.OnSetCropWindowChangeListener {
                override fun onCropWindowChanged(isGestureFinished: Boolean) {
                    isManuallyAdjusted[position] = true
                    if (isGestureFinished) {
                        val cropped = holder.binding.cropImageView.crop()
                        if (cropped != null) {
                            croppedBitmaps[position] = cropped
                        }
                    }
                }
            },
            object : CropImageView.OnHandleListener {
                override fun onHandle(z: Boolean) {
                    onDrag(z)
                }
            }
        )

        // Set points explicitly based on autoScanEnable toggle if the page is not manually adjusted
        if (!isManuallyAdjusted[position]) {
            if (autoScanEnable) {
                val points = detectedPointsList.getOrNull(position) ?: SmartCropper.scan(bitmap)?.also {
                    if (position < detectedPointsList.size) {
                        detectedPointsList[position] = it
                    }
                }
                if (points != null && points.size == 4) {
                    holder.binding.cropImageView.setCropPoints(points)
                } else {
                    holder.binding.cropImageView.setCropPoints(arrayOf(
                        Point(0, 0),
                        Point(bitmap.width, 0),
                        Point(bitmap.width, bitmap.height),
                        Point(0, bitmap.height)
                    ))
                }
            } else {
                holder.binding.cropImageView.setCropPoints(arrayOf(
                    Point(0, 0),
                    Point(bitmap.width, 0),
                    Point(bitmap.width, bitmap.height),
                    Point(0, bitmap.height)
                ))
            }
        }
    }

    fun getCroppedBitmapAt(position: Int): Bitmap {
        if (position in mediaList.indices) {
            if (position < croppedBitmaps.size && isManuallyAdjusted[position]) {
                return croppedBitmaps[position]
            } else {
                val bitmap = mediaList[position]
                val autoScanEnable = isAutoScanEnabledAt(position)
                if (autoScanEnable) {
                    val points = detectedPointsList.getOrNull(position) ?: SmartCropper.scan(bitmap)?.also {
                        if (position < detectedPointsList.size) {
                            detectedPointsList[position] = it
                        }
                    }
                    if (points != null && points.size == 4) {
                        val cropped = SmartCropper.crop(bitmap, points)
                        if (cropped != null) {
                            return cropped
                        }
                    }
                }
                return bitmap
            }
        }
        return mediaList[position]
    }

    fun getCroppedBitmapList(): List<Bitmap> {
        val list = ArrayList<Bitmap>()
        for (i in mediaList.indices) {
            if (i < croppedBitmaps.size && isManuallyAdjusted[i]) {
                list.add(croppedBitmaps[i])
            } else {
                val bitmap = mediaList[i]
                val autoScanEnable = isAutoScanEnabledAt(i)
                if (autoScanEnable) {
                    val points = detectedPointsList.getOrNull(i) ?: SmartCropper.scan(bitmap)?.also {
                        if (i < detectedPointsList.size) {
                            detectedPointsList[i] = it
                        }
                    }
                    if (points != null && points.size == 4) {
                        val cropped = SmartCropper.crop(bitmap, points)
                        if (cropped != null) {
                            list.add(cropped)
                            continue
                        }
                    }
                }
                list.add(bitmap)
            }
        }
        return list
    }

    override fun getItemCount(): Int = mediaList.size

    fun applyFilterItem(position: Int, newItem: Bitmap) {
        Log.e("ViewPagerActivity", "adapter.applyFilterItem:$position")
        Log.e("ViewPagerActivity", "adapter.mediaList:${mediaList.size}")
        Log.e("ViewPagerActivity", "adapter.croppedBitmaps:${croppedBitmaps.size}")
        mediaList[position] = newItem
        croppedBitmaps[position] = newItem
        if (position in detectedPointsList.indices) {
            detectedPointsList[position] = null
        }
        notifyItemChanged(position)
    }

    fun removeItem(position: Int) {
        if (position in croppedBitmaps.indices) {
            croppedBitmaps.removeAt(position)
        }
        if (position in isManuallyAdjusted.indices) {
            isManuallyAdjusted.removeAt(position)
        }
        if (position in autoScanEnableList.indices) {
            autoScanEnableList.removeAt(position)
        }
        if (position in detectedPointsList.indices) {
            detectedPointsList.removeAt(position)
        }
        notifyItemRemoved(position)
    }
}
