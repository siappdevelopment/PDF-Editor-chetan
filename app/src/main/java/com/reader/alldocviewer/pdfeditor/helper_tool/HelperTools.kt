package com.reader.alldocviewer.pdfeditor.helper_tool

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.utils.PdfFileNameGenerator
import com.reader.alldocviewer.pdfeditor.utils.UriImageHelper
import com.reader.alldocviewer.pdfeditor.utils.Utils
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.listener.OnPdfPasswordInterface
import com.reader.alldocviewer.pdfeditor.splitPdf.SplitModel
import com.reader.alldocviewer.pdfeditor.scan.organize.OrganizeImageActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfToImage.PDFToImageActivity
import com.reader.alldocviewer.pdfeditor.ui.dialogs.PasswordBottomSheetDialog
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object HelperTools {

    val selectedMedia: ArrayList<MediaFile> = ArrayList()
    val selectedMediaFile: ArrayList<MediaFile> = ArrayList()

    // Stub lists to prevent compilation errors from references in other activities
    val splitModelFile: ArrayList<SplitModel> = ArrayList()
    val imagesTools: ArrayList<Any> = ArrayList()
    val selectedItemsImage: ArrayList<Any> = ArrayList()

    val selectedLongBitmap: ArrayList<Bitmap> = ArrayList()
    val savedPathsMultipleBitmap: ArrayList<String> = ArrayList()

    val selectedBitmap: ArrayList<Bitmap> = ArrayList()
    val selectedBitmapOrganizePage: ArrayList<Bitmap> = ArrayList()
    val selectedImageSources: ArrayList<String> = ArrayList()
    var imagesWereEdited: Boolean = false

    fun printPdfFile(
        activity: Activity,
        filePath: String,
        documentName: String?,
        deleteAfterPrint: Boolean = false,
    ): Boolean {
        if (activity.isFinishing || activity.isDestroyed) {
            return false
        }
        val printManager = resolvePrintManager(activity) ?: return false
        return try {
            val jobName = documentName?.takeIf { it.isNotBlank() } ?: "Document"
            val adapter = PDFPrintDocumentAdapter(
                activity,
                filePath,
                jobName,
                deleteAfterPrint,
            )
            printManager.print(
                jobName,
                adapter,
                PrintAttributes.Builder().build(),
            )
            true
        } catch (e: Exception) {
            Log.e("printPdfFile", "Exception: $e")
            false
        }
    }

    private fun resolvePrintManager(activity: Activity): PrintManager? {
        val serviceContext = (activity as? ActivityPrintContextProvider)?.printServiceContext ?: activity
        return serviceContext.getSystemService(Context.PRINT_SERVICE) as? PrintManager
    }

    suspend fun createPdfFromBitmaps(
        bitmaps: List<Bitmap>,
        recycleSources: Boolean = false,
        pdfNameType: PdfFileNameGenerator.Type = PdfFileNameGenerator.Type.READER,
    ): String = withContext(Dispatchers.IO) {
        val externalDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "PdfReader"
        )
        val outputFile = PdfFileNameGenerator.uniqueFile(externalDir, pdfNameType)
        writeBitmapsToPdfDocument(bitmaps, pdfDocument = PdfDocument(), outputFile, recycleSources)
    }

    suspend fun createPdfFromImageSources(
        context: Context,
        sources: List<String>,
        pdfNameType: PdfFileNameGenerator.Type = PdfFileNameGenerator.Type.READER,
        outputFileName: String? = null,
    ): String = withContext(Dispatchers.IO) {
        val externalDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "PdfReader"
        )
        if (!externalDir.exists()) {
            externalDir.mkdirs()
        }
        val outputFile = if (!outputFileName.isNullOrBlank()) {
            File(externalDir, outputFileName)
        } else {
            PdfFileNameGenerator.uniqueFile(externalDir, pdfNameType)
        }
        val pdfDocument = PdfDocument()
        try {
            sources.forEachIndexed { index, source ->
                val bitmap = UriImageHelper.decodeFullBitmap(context, source) ?: return@forEachIndexed
                try {
                    addBitmapPage(pdfDocument, bitmap, index + 1)
                } finally {
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }
            }
            FileOutputStream(outputFile).use { fileOutputStream ->
                pdfDocument.writeTo(fileOutputStream)
            }
        } finally {
            pdfDocument.close()
        }
        outputFile.absolutePath
    }

    private fun addBitmapPage(pdfDocument: PdfDocument, bitmap: Bitmap, pageNumber: Int) {
        val pageInfo = PdfDocument.PageInfo.Builder(
            bitmap.width,
            bitmap.height,
            pageNumber,
        ).create()
        val page = pdfDocument.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0f, 0f, Paint())
        pdfDocument.finishPage(page)
    }

    private fun writeBitmapsToPdfDocument(
        bitmaps: List<Bitmap>,
        pdfDocument: PdfDocument,
        outputFile: File,
        recycleSources: Boolean,
    ): String {
        try {
            bitmaps.forEachIndexed { index, bitmap ->
                if (bitmap.isRecycled) return@forEachIndexed
                try {
                    addBitmapPage(pdfDocument, bitmap, index + 1)
                } finally {
                    if (recycleSources && !bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }
            }
            FileOutputStream(outputFile).use { fileOutputStream ->
                pdfDocument.writeTo(fileOutputStream)
            }
        } finally {
            pdfDocument.close()
        }
        return outputFile.absolutePath
    }

    fun convertImageToPDF(
        context: Context,
        bitmaps: List<Bitmap>,
        resultListener: (Boolean, String, String) -> Unit
    ) {
        val progressDialog = ProgressDialog(context).apply {
            setMessage(context.getString(R.string.generating_pdf_from_images))
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val outputPath = createPdfFromBitmaps(bitmaps)
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    resultListener(
                        true,
                        context.getString(R.string.pdf_created_successfully),
                        outputPath
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.successfully_converted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    resultListener(
                        false,
                        context.getString(R.string.failed_to_create_pdf, e.message), ""
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.conversion_failed), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun handlePrintPdf(
        activity: Activity,
        file: ModelFile,
        fragmentManager: FragmentManager? = null,
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        val filePath = file.path.orEmpty()
        if (filePath.isBlank()) {
            Toast.makeText(
                activity,
                activity.getString(R.string.file_path_is_empty),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        if (!filePath.startsWith("content://")) {
            val pdfFile = File(filePath)
            if (!pdfFile.exists() || !pdfFile.isFile) {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.file_not_found),
                    Toast.LENGTH_SHORT,
                ).show()
                return
            }
        }

        if (!file.isEncrypted) {
            launchPrintJob(activity, filePath, file.name)
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val needsPassword = withContext(Dispatchers.IO) {
                Utils.requiresPdfOpenPassword(filePath)
            }
            if (activity.isFinishing || activity.isDestroyed) return@launch
            if (needsPassword) {
                showPrintPasswordDialog(activity, file, filePath, fragmentManager)
            } else {
                launchPrintJob(activity, filePath, file.name)
            }
        }
    }

    private fun launchPrintJob(
        activity: Activity,
        filePath: String,
        documentName: String?,
        deleteAfterPrint: Boolean = false,
    ) {
        if (activity.isFinishing || activity.isDestroyed) return
        if (!printPdfFile(activity, filePath, documentName, deleteAfterPrint)) {
            Log.e("handlePrintPdf", "printPdfFile failed for path=$filePath")
        }
    }

    private fun showPrintPasswordDialog(
        activity: Activity,
        file: ModelFile,
        filePath: String,
        fragmentManager: FragmentManager?,
    ) {
        val dialog = PasswordBottomSheetDialog(
            checkType = "unlock",
            modelFile = file,
            viewModel = null,
            listener = object : OnPdfPasswordInterface {
                override fun onOpenPasswordClick(password: String) {
                    printUnlockedPdf(activity, filePath, file.name, password)
                }
            }
        )
        val manager = fragmentManager
            ?: (activity as? FragmentActivity)?.supportFragmentManager
        if (manager == null) {
            Log.e("handlePrintPdf", "Unable to show password dialog: FragmentManager unavailable")
            launchPrintJob(activity, filePath, file.name)
            return
        }
        val tag = "print_password_dialog"
        if (manager.findFragmentByTag(tag) != null) return
        try {
            dialog.show(manager, tag)
        } catch (e: IllegalStateException) {
            manager.beginTransaction()
                .add(dialog, tag)
                .commitAllowingStateLoss()
        }
    }

    private fun printUnlockedPdf(
        activity: Activity,
        filePath: String,
        documentName: String?,
        password: String,
    ) {
        val progressDialog = ProgressDialog(activity).apply {
            setMessage(activity.getString(R.string.please_wait))
            setCancelable(false)
            show()
        }
        CoroutineScope(Dispatchers.IO).launch {
            val decryptedFile = Utils.decryptPdfToTempFile(File(filePath), password)
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (decryptedFile != null) {
                    printPdfFile(
                        activity,
                        decryptedFile.absolutePath,
                        documentName,
                        deleteAfterPrint = true,
                    )
                } else {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.failed_to_unlock_pdf),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    fun handlePdfToImageConvert(
        activity: Activity,
        file: ModelFile,
        checkFileName: String
    ) {
        if (checkFileName == "split_pdf") {
            splitModelFile.clear()
        }
        if (file.isEncrypted) {
            val dialog = PasswordBottomSheetDialog(
                "PasswordCheckTools",
                file,
                null,
                object : OnPdfPasswordInterface {
                    override fun onOpenPasswordClick(password: String) {
                        val intent = Intent(activity, PDFToImageActivity::class.java).apply {
                            putExtra("FILE_MODEL", file)
                            putExtra("path", file.path)
                            putExtra("checkFileName", checkFileName)
                        }
                        activity.startActivity(intent)
                    }
                }
            )
            if (activity is FragmentActivity) {
                dialog.show(activity.supportFragmentManager, dialog.tag)
            }
        } else {
            val intent = Intent(activity, PDFToImageActivity::class.java).apply {
                putExtra("FILE_MODEL", file)
                putExtra("path", file.path)
                putExtra("checkFileName", checkFileName)
            }
            activity.startActivity(intent)
        }
    }

    fun convertImageToPDF(
        context: Context,
        paths: List<MediaFile>,
        needCompress: Boolean,
        maxWidth: Int,
        maxHeight: Int,
        resultListener: (Boolean, String, String) -> Unit
    ) {
        val progressDialog = ProgressDialog(context).apply {
            setMessage(context.getString(R.string.generating_pdf_from_images))
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val externalDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "PdfReader"
                )
                val outputFile = PdfFileNameGenerator.uniqueFile(
                    externalDir,
                    PdfFileNameGenerator.Type.READER,
                )
                val fileOutputStream = FileOutputStream(outputFile)
                val pdfDocument = PdfDocument()

                paths.forEachIndexed { index, mediaFile ->
                    val bitmap = if (needCompress) {
                        compressImage(mediaFile.path ?: "", maxWidth, maxHeight)
                    } else {
                        BitmapFactory.decodeFile(mediaFile.path)
                    }

                    if (bitmap != null) {
                        val pageInfo =
                            PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1)
                                .create()
                        val page = pdfDocument.startPage(pageInfo)
                        page.canvas.drawBitmap(bitmap, 0f, 0f, Paint())
                        pdfDocument.finishPage(page)
                        bitmap.recycle()
                    }
                }

                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
                fileOutputStream.close()

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    resultListener(
                        true,
                        context.getString(R.string.pdf_created_successfully),
                        outputFile.absolutePath
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.successfully_converted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    resultListener(
                        false,
                        context.getString(R.string.generating_pdf_from_images, e.message),
                        ""
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.conversion_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun compressImage(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)
        val srcWidth = options.outWidth
        val srcHeight = options.outHeight

        var inSampleSize = 1
        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false
        val decodedBitmap = BitmapFactory.decodeFile(path, options) ?: return null

        val scaledBitmap = Bitmap.createScaledBitmap(decodedBitmap, reqWidth, reqHeight, true)
        if (decodedBitmap != scaledBitmap) {
            decodedBitmap.recycle()
        }

        return try {
            val exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                scaledBitmap.width,
                scaledBitmap.height,
                matrix,
                true
            )
        } catch (e: IOException) {
            scaledBitmap
        }
    }

    fun addImageWatermarkToList(bitmaps: List<Bitmap>, watermark: Bitmap): List<Bitmap> {
        val result = ArrayList<Bitmap>()
        for (bitmap in bitmaps) {
            val w = bitmap.width
            val h = bitmap.height
            val newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(newBitmap)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            val marginX = 20f
            val marginY = 20f
            val x = w - watermark.width - marginX
            val y = h - watermark.height - marginY
            canvas.drawBitmap(watermark, x, y, null)
            result.add(newBitmap)
        }
        return result
    }

    suspend fun combineBitmapsAndSave(
        bitmaps: List<Bitmap>,
        format: String,
        progressCallback: (Int) -> Unit
    ): String? = withContext(Dispatchers.IO) {
        if (bitmaps.isEmpty()) return@withContext null
        var totalHeight = 0
        var maxWidth = 0
        for (bitmap in bitmaps) {
            totalHeight += bitmap.height
            if (bitmap.width > maxWidth) {
                maxWidth = bitmap.width
            }
        }

        val combinedBitmap = Bitmap.createBitmap(maxWidth, totalHeight, Bitmap.Config.ARGB_8888)
        combinedBitmap.eraseColor(Color.WHITE)
        val canvas = Canvas(combinedBitmap)
        var currentY = 0f
        for ((index, bitmap) in bitmaps.withIndex()) {
            val left = (maxWidth - bitmap.width) / 2f
            canvas.drawBitmap(bitmap, left, currentY, null)
            currentY += bitmap.height
            progressCallback((index + 1) * 100 / bitmaps.size)
        }

        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "PdfReader"
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "LongImage_${System.currentTimeMillis()}.${format.lowercase()}")
        try {
            FileOutputStream(file).use { out ->
                val compressFormat =
                    if (format.uppercase() == "PNG") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                combinedBitmap.compress(compressFormat, 90, out)
            }
            combinedBitmap.recycle()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapsToPublicDocumentsFolder(
        context: Context,
        bitmaps: List<Bitmap>,
        pdfName: String,
        format: String,
        progressCallback: (Int) -> Unit
    ): String? {
        if (bitmaps.isEmpty()) return null
        val cleanName = pdfName.replace("[^a-zA-Z0-9]".toRegex(), "_").takeIf { it.isNotBlank() }
            ?: "PDF_Images"
        val baseDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "PdfReader/$cleanName"
        )
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }

        val savedPaths = ArrayList<String>()
        val compressFormat =
            if (format.uppercase() == "PNG") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val ext = format.lowercase()

        for ((index, bitmap) in bitmaps.withIndex()) {
            val file = File(baseDir, "Page_${index + 1}_${System.currentTimeMillis()}.$ext")
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(compressFormat, 90, out)
                }
                savedPaths.add(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            progressCallback((index + 1) * 100 / bitmaps.size)
        }

        savedPathsMultipleBitmap.clear()
        savedPathsMultipleBitmap.addAll(savedPaths)

        return baseDir.absolutePath
    }

    fun shareMultipleFiles(context: Context, filePaths: List<String>) {
        if (filePaths.isEmpty()) return
        val uris = ArrayList<Uri>()
        for (path in filePaths) {
            val file = File(path)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                uris.add(uri)
            }
        }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Images"))
    }

    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            UriImageHelper.decodeFullBitmap(context, uri.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * ML Kit scanner URIs are only readable by the activity that received the
     * scan result. Copy them into app cache before launching OrganizeImageActivity.
     */
    fun persistScannedPageUris(
        context: Context,
        pages: List<GmsDocumentScanningResult.Page>?,
    ): ArrayList<String> {
        val persisted = ArrayList<String>()
        if (pages.isNullOrEmpty()) return persisted

        val dir = File(context.cacheDir, "scan_to_pdf")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        pages.forEachIndexed { index, page ->
            val dest = File(dir, "scan_${System.currentTimeMillis()}_$index.jpg")
            val savedPath = copyScanUriToFile(context, page.imageUri, dest)
            if (!savedPath.isNullOrBlank()) {
                persisted.add(savedPath)
            } else {
                Log.e("Scanner", "Failed to persist scanned page: ${page.imageUri}")
            }
        }
        return persisted
    }

    fun launchOrganizeFromScannedPages(
        context: Context,
        pages: List<GmsDocumentScanningResult.Page>?,
        title: String,
    ) {
        val progressDialog = ProgressDialog(context).apply {
            setMessage(context.getString(R.string.please_wait))
            setCancelable(false)
            show()
        }
        CoroutineScope(Dispatchers.IO).launch {
            val localPaths = persistScannedPageUris(context, pages)
            withContext(Dispatchers.Main) {
                try {
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                } catch (_: Exception) {
                }
                if (context is Activity && (context.isFinishing || context.isDestroyed)) {
                    return@withContext
                }
                if (localPaths.isEmpty()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_to_load_images),
                        Toast.LENGTH_SHORT,
                    ).show()
                    return@withContext
                }
                val intent = Intent(context, OrganizeImageActivity::class.java).apply {
                    putExtra("main_title", title)
                    putStringArrayListExtra("scanned_page_uris", localPaths)
                }
                if (context !is Activity) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }

    private fun copyScanUriToFile(context: Context, uri: Uri, dest: File): String? {
        try {
            val copied = context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
                dest.exists() && dest.length() > 0L
            } ?: false
            if (copied) {
                return dest.absolutePath
            }
        } catch (e: Exception) {
            Log.e("Scanner", "openInputStream failed for $uri", e)
        }

        val sourcePath = when (uri.scheme) {
            "file", null -> uri.path
            else -> null
        } ?: return null
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists() || !sourceFile.canRead()) {
            return null
        }
        return try {
            sourceFile.copyTo(dest, overwrite = true)
            dest.absolutePath
        } catch (e: Exception) {
            Log.e("Scanner", "copyTo failed for $sourcePath", e)
            if (sourceFile.canRead()) sourceFile.absolutePath else null
        }
    }

    fun getBitmapListFromSelectedPage(
        context: Context,
        pages: List<GmsDocumentScanningResult.Page>?,
        resultListener: (List<Bitmap>) -> Unit
    ) {
        val progressDialog = ProgressDialog(context).apply {
            setMessage("Please Wait...")
            setCancelable(false)
            show()
        }
        CoroutineScope(Dispatchers.IO).launch {
            val bitmapList = ArrayList<Bitmap>()
            if (!pages.isNullOrEmpty()) {
                for (page in pages) {
                    val bitmap = getBitmapFromUri(context, page.imageUri)
                    if (bitmap != null) {
                        bitmapList.add(bitmap)
                    } else {
                        Log.wtf("Scanner", "Failed to get bitmap from URI")
                    }
                }
            }
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                resultListener(bitmapList)
            }
        }
    }
}
