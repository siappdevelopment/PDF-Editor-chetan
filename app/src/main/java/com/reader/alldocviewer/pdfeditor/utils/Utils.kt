package com.reader.alldocviewer.pdfeditor.utils

import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.reader.alldocviewer.pdfeditor.databinding.AlertDialogDesignBinding
import com.reader.alldocviewer.pdfeditor.databinding.DialogGotoPageDesignBinding
import com.reader.alldocviewer.pdfeditor.databinding.PdfSaveDialogDesignBinding
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import android.media.MediaScannerConnection
import androidx.core.view.ViewCompat
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ui.dialogs.UnlockAskBehaviorBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.data.local.dao.FileDao
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.ui.activities.officeviewer.ViewFilesNewActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.PdfViewerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Utils {

    private var cachedNavigationBarInset: Int = -1

    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface UtilsEntryPoint {
        fun fileDao(): FileDao
    }

    fun createShortcutIcon(context: Context, styleId: Int, extension: String): Bitmap {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (styleId) {
            0 -> { // Classic Red
                paint.color = Color.parseColor("#E53935")
                canvas.drawRoundRect(
                    0f,
                    0f,
                    size.toFloat(),
                    size.toFloat(),
                    size * 0.2f,
                    size * 0.2f,
                    paint
                )
            }

            1 -> { // Dark Premium
                paint.color = Color.parseColor("#212121")
                canvas.drawRoundRect(
                    0f,
                    0f,
                    size.toFloat(),
                    size.toFloat(),
                    size * 0.2f,
                    size * 0.2f,
                    paint
                )
                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#FFD700")
                paint.strokeWidth = size * 0.05f
                canvas.drawRoundRect(
                    0f,
                    0f,
                    size.toFloat(),
                    size.toFloat(),
                    size * 0.2f,
                    size * 0.2f,
                    paint
                )
                paint.style = Paint.Style.FILL
            }

            2 -> { // Sleek Blue
                paint.color = Color.parseColor("#1E88E5")
                canvas.drawRoundRect(
                    0f,
                    0f,
                    size.toFloat(),
                    size.toFloat(),
                    size * 0.2f,
                    size * 0.2f,
                    paint
                )
            }

            3 -> { // Vibrant Gradient
                val gradient = LinearGradient(
                    0f, 0f, size.toFloat(), size.toFloat(),
                    Color.parseColor("#8E24AA"), Color.parseColor("#F06292"),
                    Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                canvas.drawRoundRect(
                    0f,
                    0f,
                    size.toFloat(),
                    size.toFloat(),
                    size * 0.2f,
                    size * 0.2f,
                    paint
                )
                paint.shader = null
            }
        }

        val drawableId = when (extension.lowercase()) {
            "pdf" -> R.drawable.ic_pdf
            "doc", "docx" -> R.drawable.ic_word_type
            "xls", "xlsx" -> R.drawable.ic_excel_type
            "ppt", "pptx" -> R.drawable.ic_ppt_type
            else -> R.drawable.ic_other_type
        }

        val iconDrawable = ContextCompat.getDrawable(context, drawableId)
        if (iconDrawable != null) {
            val iconSize = (size * 0.5).toInt()
            val left = (size - iconSize) / 2
            val top = (size - iconSize) / 2
            val tintColor = if (styleId == 1) Color.parseColor("#FFD700") else Color.WHITE
            iconDrawable.mutate()
            androidx.core.graphics.drawable.DrawableCompat.setTint(iconDrawable, tintColor)
            iconDrawable.setBounds(left, top, left + iconSize, top + iconSize)
            iconDrawable.draw(canvas)
        } else {
            val label = extension.uppercase().take(4)
            paint.color = if (styleId == 1) Color.parseColor("#FFD700") else Color.WHITE
            paint.textSize = size * 0.35f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            val textBounds = Rect()
            paint.getTextBounds(label, 0, label.length, textBounds)
            val y = (size / 2) + (textBounds.height() / 2)
            canvas.drawText(label, size / 2f, y.toFloat(), paint)
        }
        return bitmap
    }

    fun pinShortcutForFile(
        context: Activity,
        file: ModelFile,
        styleId: Int
    ) {

        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Toast.makeText(
                context,
                context.getString(R.string.launcher_does_not_support_pinned_shortcuts),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val extension = file.path.orEmpty().substringAfterLast('.', "").lowercase()
        val isPdf = extension == "pdf"

        val shortcutIntent = if (isPdf) {
            Intent(
                context,
                PdfViewerActivity::class.java
            ).apply {
                action = Intent.ACTION_VIEW
                addCategory(Intent.CATEGORY_DEFAULT)
                putExtra("path", file.path)
                putExtra("file_name", file.name)
                putExtra("filePassword", "")
            }
        } else {
            Intent(
                context,
                ViewFilesNewActivity::class.java
            ).apply {
                action = Intent.ACTION_VIEW
                addCategory(Intent.CATEGORY_DEFAULT)
                putExtra("path", file.path)
                putExtra("name", file.name)
            }
        }

        val iconBitmap = createShortcutIcon(context, styleId, extension)
        val iconCompat = IconCompat.createWithBitmap(iconBitmap)

        val shortcutInfo = ShortcutInfoCompat.Builder(
            context,
            file.path ?: file.name ?: System.currentTimeMillis().toString()
        )
            .setShortLabel(file.name ?: "Document")
            .setIcon(iconCompat)
            .setIntent(shortcutIntent)
            .build()

        val successIntent = Intent(
            context.packageName + ".SHORTCUT_PINNED"
        )

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            successIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val requestSent = ShortcutManagerCompat.requestPinShortcut(
            context,
            shortcutInfo,
            pendingIntent.intentSender
        )

        if (!requestSent) {
            Toast.makeText(
                context,
                context.getString(R.string.failed_to_request_shortcut),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        Toast.makeText(
            context,
            context.getString(R.string.shortcut_request_sent),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun openPdfReader(path: String, password: String = ""): PdfReader {
        val pwdBytes = password.takeIf { it.isNotEmpty() }?.toByteArray()
        val file = File(path)
        if (!file.exists() || !file.isFile) {
            throw java.io.FileNotFoundException("File not found: $path")
        }

        openPdfReaderFromPath(path, pwdBytes)?.let { return it }
        openPdfReaderFromPath(path, pwdBytes, partial = true)?.let { return it }
        openPdfReaderFromBytes(file.readBytes(), pwdBytes)?.let { return it }

        throw java.io.IOException("Cannot open PDF: ${file.name}")
    }

    fun isValidPdfForMerge(path: String, password: String = ""): Boolean {
        return canOpenPdfForMerge(path, password)
    }

    /** Fast merge pre-check — partial PdfReader only, no full-file byte reads. */
    fun canOpenPdfForMerge(path: String, password: String = ""): Boolean {
        if (path.isBlank() || path.startsWith("content://")) return false
        val file = File(path)
        if (!file.exists() || !file.isFile || file.length() < 64L) return false
        if (!hasPdfHeader(file)) return false
        val pwdBytes = password.takeIf { it.isNotEmpty() }?.toByteArray()
        return try {
            val reader = openPdfReaderFromPath(path, pwdBytes, partial = true)
                ?: openPdfReaderFromPath(path, pwdBytes, partial = false)
            if (reader == null) return false
            val valid = reader.numberOfPages > 0
            reader.close()
            valid
        } catch (_: Exception) {
            false
        }
    }

    private fun hasPdfHeader(file: File): Boolean {
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(5)
                input.read(header) == 5 && String(header, Charsets.US_ASCII).startsWith("%PDF-")
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun openPdfReaderFromPath(
        path: String,
        pwdBytes: ByteArray?,
        partial: Boolean = false,
    ): PdfReader? {
        return try {
            when {
                pwdBytes != null -> PdfReader(path, pwdBytes, partial)
                partial -> PdfReader(path, null as ByteArray?, true)
                else -> PdfReader(path)
            }
        } catch (e: com.itextpdf.text.exceptions.BadPasswordException) {
            if (pwdBytes == null) {
                try {
                    PdfReader(path, ByteArray(0), partial)
                } catch (_: Exception) {
                    null
                }
            } else {
                throw e
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun openPdfReaderFromBytes(bytes: ByteArray, pwdBytes: ByteArray?): PdfReader? {
        return try {
            if (pwdBytes != null) PdfReader(bytes, pwdBytes) else PdfReader(bytes)
        } catch (e: com.itextpdf.text.exceptions.BadPasswordException) {
            if (pwdBytes == null) {
                try {
                    PdfReader(bytes, ByteArray(0))
                } catch (_: Exception) {
                    null
                }
            } else {
                throw e
            }
        } catch (_: Exception) {
            null
        }
    }

    fun verifyPdfPassword(path: String, password: String): Boolean {
        if (path.isEmpty()) return false
        return try {
            val reader = if (password.isNotEmpty()) {
                PdfReader(path, password.toByteArray())
            } else {
                openPdfReader(path)
            }
            reader.close()
            true
        } catch (_: Exception) {
            false
        }
    }

    /** True only when the PDF cannot be opened without a user password. */
    fun requiresPdfOpenPassword(path: String): Boolean {
        if (path.isBlank() || path.startsWith("content://")) return false
        val file = File(path)
        if (!file.exists() || !file.isFile || file.length() == 0L) return false
        return try {
            val reader = PdfReader(path)
            reader.close()
            false
        } catch (e: com.itextpdf.text.exceptions.BadPasswordException) {
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isPdfPasswordProtected(path: String): Boolean {
        if (isPdfPasswordProtectedFast(path)) return true
        if (path.isEmpty()) return false
        val file = File(path)
        if (!file.exists() || !file.isFile || file.length() == 0L) return false
        return try {
            val reader = PdfReader(path)
            val isEnc = reader.isEncrypted
            reader.close()
            isEnc
        } catch (e: com.itextpdf.text.exceptions.BadPasswordException) {
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Lightweight scan-time check — avoids opening every PDF with PdfReader. */
    fun isPdfPasswordProtectedFast(path: String): Boolean {
        if (path.isEmpty()) return false
        val file = File(path)
        if (!file.isFile || file.length() == 0L) return false
        return try {
            file.inputStream().buffered().use { input ->
                val buffer = ByteArray(16_384)
                var offset = 0
                while (offset < buffer.size) {
                    val read = input.read(buffer, offset, minOf(4_096, buffer.size - offset))
                    if (read <= 0) break
                    offset += read
                    if (String(buffer, 0, offset, Charsets.ISO_8859_1).contains("/Encrypt")) {
                        return true
                    }
                }
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun encryptPdf(file: File, password: String): Boolean {
        val tempFile = File(file.parent, "temp_${System.currentTimeMillis()}.pdf")
        var reader: PdfReader? = null
        var stamper: PdfStamper? = null
        var fos: FileOutputStream? = null
        return try {
            reader = PdfReader(file.absolutePath)
            fos = FileOutputStream(tempFile)
            stamper = PdfStamper(reader, fos)
            stamper.setEncryption(
                password.toByteArray(),
                password.toByteArray(),
                PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY,
                PdfWriter.STANDARD_ENCRYPTION_128
            )
            stamper.close()
            reader.close()
            fos.close()

            if (file.delete()) {
                tempFile.renameTo(file)
                true
            } else {
                tempFile.delete()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempFile.exists()) tempFile.delete()
            false
        } finally {
            try {
                reader?.close()
            } catch (ex: Exception) {
            }
            try {
                stamper?.close()
            } catch (ex: Exception) {
            }
            try {
                fos?.close()
            } catch (ex: Exception) {
            }
        }
    }

    fun decryptPdf(file: File, password: String): Boolean {
        val tempFile = File(file.parent, "temp_${System.currentTimeMillis()}.pdf")
        var reader: PdfReader? = null
        var stamper: PdfStamper? = null
        var fos: FileOutputStream? = null
        return try {
            reader = PdfReader(file.absolutePath, password.toByteArray())
            fos = FileOutputStream(tempFile)
            stamper = PdfStamper(reader, fos)
            stamper.close()
            reader.close()
            fos.close()

            if (file.delete()) {
                tempFile.renameTo(file)
                true
            } else {
                tempFile.delete()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempFile.exists()) tempFile.delete()
            false
        } finally {
            try {
                reader?.close()
            } catch (ex: Exception) {
            }
            try {
                stamper?.close()
            } catch (ex: Exception) {
            }
            try {
                fos?.close()
            } catch (ex: Exception) {
            }
        }
    }

    fun decryptPdfToCopy(file: File, password: String): File? {
        val baseName = file.nameWithoutExtension
        val ext = file.extension
        var targetFile = File(file.parent, "${baseName}_unlocked.$ext")
        var counter = 1
        while (targetFile.exists()) {
            targetFile = File(file.parent, "${baseName}_unlocked_$counter.$ext")
            counter++
        }

        return try {
            file.copyTo(targetFile, overwrite = true)
            val success = decryptPdf(targetFile, password)
            if (success) {
                targetFile
            } else {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (targetFile.exists()) {
                targetFile.delete()
            }
            null
        }
    }

    /** Creates a temporary decrypted copy for one-time use (e.g. printing). Caller must delete. */
    fun decryptPdfToTempFile(file: File, password: String): File? {
        val tempFile = File.createTempFile("print_", ".pdf")
        var reader: PdfReader? = null
        var stamper: PdfStamper? = null
        var fos: FileOutputStream? = null
        return try {
            reader = PdfReader(file.absolutePath, password.toByteArray())
            fos = FileOutputStream(tempFile)
            stamper = PdfStamper(reader, fos)
            stamper.close()
            reader.close()
            fos.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempFile.exists()) {
                tempFile.delete()
            }
            null
        } finally {
            try {
                stamper?.close()
            } catch (_: Exception) {
            }
            try {
                reader?.close()
            } catch (_: Exception) {
            }
            try {
                fos?.close()
            } catch (_: Exception) {
            }
        }
    }

    fun handleDecryptPdf(
        context: Context,
        fragmentManager: androidx.fragment.app.FragmentManager,
        file: File,
        password: String,
        onSuccess: (File) -> Unit,
        onFailure: () -> Unit
    ) {
        val behavior = AppPreferences.getUnlockSavingBehavior(context)
        if (behavior == "always_ask") {
            val askDialog = UnlockAskBehaviorBottomSheetDialog { choice ->
                performDecryption(context, file, password, choice, onSuccess, onFailure)
            }
            askDialog.show(fragmentManager, askDialog.tag)
        } else {
            performDecryption(context, file, password, behavior, onSuccess, onFailure)
        }
    }

    private fun performDecryption(
        context: Context,
        file: File,
        password: String,
        behavior: String,
        onSuccess: (File) -> Unit,
        onFailure: () -> Unit
    ) {
        if (behavior == "save_copy") {
            val decryptedFile = decryptPdfToCopy(file, password)
            if (decryptedFile != null) {
                val fileKey = com.reader.alldocviewer.pdfeditor.utils.FileDedupe.createKey(
                    decryptedFile.absolutePath,
                    decryptedFile.name,
                    decryptedFile.length(),
                    decryptedFile.parent
                )
                val modelFile = ModelFile(
                    fileKey = fileKey,
                    name = decryptedFile.name,
                    path = decryptedFile.absolutePath,
                    time = decryptedFile.lastModified(),
                    fileType = FileConst.PDF_FILE,
                    size = decryptedFile.length(),
                    filePathName = decryptedFile.name,
                    originalFolderPath = decryptedFile.parent,
                    isDeleted = 0,
                    isEncrypted = false
                )

                try {
                    val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        UtilsEntryPoint::class.java
                    )
                    val fileDao = entryPoint.fileDao()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            fileDao.insertFile(modelFile)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                MediaScannerConnection.scanFile(
                    context.applicationContext,
                    arrayOf(decryptedFile.absolutePath),
                    arrayOf("application/pdf")
                ) { _, _ ->
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onSuccess(decryptedFile)
                    }
                }
            } else {
                onFailure()
            }
        } else {
            val success = decryptPdf(file, password)
            if (success) {
                MediaScannerConnection.scanFile(
                    context.applicationContext,
                    arrayOf(file.absolutePath),
                    arrayOf("application/pdf")
                ) { _, _ ->
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onSuccess(file)
                    }
                }
            } else {
                onFailure()
            }
        }
    }

    val positiveButton = "positiveButton"
    val negativeButton = "negativeButton"

    @JvmStatic
    fun applyToolActivitySystemBars(activity: Activity) {
        val theme = AppPreferences.getAppTheme(activity)
        val isNightMode = when (theme) {
            1 -> false
            2 -> true
            else -> (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        }
        val resolvedColor = ContextCompat.getColor(
            activity,
            if (isNightMode) R.color.bg_color else R.color.white,
        )
        applyToolActivitySystemBars(activity, resolvedColor, !isNightMode)
    }

    @JvmStatic
    fun applyToolActivitySystemBars(
        activity: Activity,
        statusBarColor: Int,
        lightStatusBarIcons: Boolean,
    ) {
        val window = activity.window
        window.statusBarColor = statusBarColor
        window.navigationBarColor = statusBarColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = lightStatusBarIcons
            isAppearanceLightNavigationBars = lightStatusBarIcons
            show(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        captureNavigationBarInset(window.decorView)
    }

    @JvmStatic
    fun applyFullScreenContentStatusBar(activity: Activity) {
        applyGoogleFullScreenAdWindow(activity)
    }

    /**
     * Prepares the host activity window so Google App Open / Interstitial ads render
     * edge-to-edge without a bottom navigation-bar gap.
     */
    @JvmStatic
    fun applyGoogleFullScreenAdWindow(activity: Activity) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            show(WindowInsetsCompat.Type.systemBars())
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    /** Restores the app's normal immersive system bar configuration after a Google fullscreen ad. */
    @JvmStatic
    fun restoreSystemBarsAfterGoogleFullScreenAd(activity: Activity) {
        if (activity.isFinishing) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed) {
            return
        }
        hideSystemNavigationBar(activity)
        applyToolActivitySystemBars(activity)
    }

    @JvmStatic
    fun hideSystemNavigationBar(activity: Activity) {
        hideSystemNavigationBar(activity.window)
    }

    fun showSystemNavigationBar(activity: Activity) {
        showSystemNavigationBar(activity.window)
    }

    fun hideSystemNavigationBar(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        captureNavigationBarInset(window.decorView)
    }

    @JvmStatic
    fun rememberNavigationBarInset(inset: Int) {
        if (inset > 0) {
            cachedNavigationBarInset = inset
        }
    }

    @JvmStatic
    fun resetNavigationBarInsetCache() {
        cachedNavigationBarInset = -1
    }

    private fun captureNavigationBarInset(anchor: View) {
        ViewCompat.getRootWindowInsets(anchor)?.let { insets ->
            rememberNavigationBarInset(
                insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars()).bottom,
            )
        }
    }

    private fun readNavigationBarHeightFromResources(context: Context): Int {
        val resourceId = context.resources.getIdentifier(
            "navigation_bar_height",
            "dimen",
            "android",
        )
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    private fun resolveStableNavigationBarInset(context: Context): Int {
        if (cachedNavigationBarInset >= 0) {
            return cachedNavigationBarInset
        }

        val decorViews = linkedSetOf<View>()
        context.findActivity()?.window?.decorView?.let(decorViews::add)
        (context as? Activity)?.window?.decorView?.let(decorViews::add)

        for (decorView in decorViews) {
            captureNavigationBarInset(decorView)
            if (cachedNavigationBarInset >= 0) {
                return cachedNavigationBarInset
            }
        }

        val resourceInset = readNavigationBarHeightFromResources(context)
        if (resourceInset > 0) {
            cachedNavigationBarInset = resourceInset
        }
        return maxOf(cachedNavigationBarInset, 0)
    }

    @JvmStatic
    fun applyBottomSheetImmersiveNavigation(dialog: Dialog) {
        val window = dialog.window ?: return
        applyDialogImmersiveNavigation(window)

        val coordinator = dialog.findViewById<ViewGroup>(
            com.google.android.material.R.id.coordinator,
        )
        coordinator?.fitsSystemWindows = false
        coordinator?.setPadding(0, 0, 0, 0)

        val sheet = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet,
        ) ?: return
        sheet.fitsSystemWindows = false

        captureNavigationBarInset(window.decorView)

        fun applyNegativeBottomMargin() {
            val stableInset = resolveStableNavigationBarInset(dialog.context)
            coordinator?.setPadding(0, 0, 0, 0)
            (sheet.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                val targetMargin = -stableInset
                if (params.bottomMargin != targetMargin) {
                    params.bottomMargin = targetMargin
                    sheet.layoutParams = params
                }
            }
        }

        applyNegativeBottomMargin()

        ViewCompat.setOnApplyWindowInsetsListener(coordinator ?: sheet) { _, insets ->
            rememberNavigationBarInset(
                insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars()).bottom,
            )
            applyNegativeBottomMargin()
            insets
        }

        sheet.viewTreeObserver.addOnPreDrawListener(object : android.view.ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                applyNegativeBottomMargin()
                sheet.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    @JvmStatic
    fun resetBottomSheetImmersiveNavigation(dialog: Dialog) {
        val coordinator = dialog.findViewById<ViewGroup>(
            com.google.android.material.R.id.coordinator,
        )
        val sheet = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet,
        )

        coordinator?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it, null)
            it.setPadding(0, 0, 0, 0)
        }
        sheet?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it, null)
            (it.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                if (params.bottomMargin != 0) {
                    params.bottomMargin = 0
                    it.layoutParams = params
                }
            }
        }
    }

    @JvmStatic
    fun applyFullScreenDialogImmersiveNavigation(dialog: Dialog) {
        val window = dialog.window ?: return
        applyDialogImmersiveNavigation(window)
    }

    private fun applyDialogImmersiveNavigation(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun restoreHostSystemBarsAfterDialog(activity: Activity) {
        hideSystemNavigationBar(activity)
        // Post so we win over dialog window teardown, which can reset status-bar icon colors
        // to the activity theme default after the dismiss listener returns.
        activity.window.decorView.post {
            if (activity.isFinishing) return@post
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed) {
                return@post
            }
            hideSystemNavigationBar(activity)
            restoreHostStatusBarAppearance(activity)
        }
    }

    /**
     * Bottom-sheet themes can leave [WindowInsetsControllerCompat.isAppearanceLightStatusBars]
     * stuck on the theme default; re-apply the host screen's intended status-bar icon color.
     */
    private fun restoreHostStatusBarAppearance(activity: Activity) {
        when (activity) {
            is com.reader.alldocviewer.pdfeditor.ui.activities.main.MainActivity -> {
                activity.restoreStatusBarAppearance()
            }
            is androidx.fragment.app.FragmentActivity -> {
                restoreStatusBarFromFragments(activity.supportFragmentManager.fragments)
            }
        }
    }

    private fun restoreStatusBarFromFragments(fragments: List<androidx.fragment.app.Fragment>) {
        for (fragment in fragments) {
            val homeLauncher =
                fragment as? com.reader.alldocviewer.pdfeditor.launcher.HomeLauncherFragment
            if (homeLauncher != null && homeLauncher.isAdded) {
                homeLauncher.restoreStatusBarAppearance()
                return
            }
            if (fragment.isAdded && fragment.childFragmentManager.fragments.isNotEmpty()) {
                restoreStatusBarFromFragments(fragment.childFragmentManager.fragments)
            }
        }
    }

    fun showSystemNavigationBar(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    fun setDialogSystemBars(dialog: Dialog, isDarkTheme: Boolean, context: Context) {
        val window = dialog.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val decorView = window.decorView
        val windowInsetsController = WindowCompat.getInsetsController(window, decorView)
        windowInsetsController.isAppearanceLightStatusBars = !isDarkTheme
        windowInsetsController.isAppearanceLightNavigationBars = !isDarkTheme
        val navBarColor = if (isDarkTheme) {
            ContextCompat.getColor(context, R.color.black)
        } else {
            ContextCompat.getColor(context, R.color.white)
        }
        val statusBarColor = if (isDarkTheme) {
            ContextCompat.getColor(context, R.color.black)
        } else {
            ContextCompat.getColor(context, R.color.white)
        }
        window.navigationBarColor = navBarColor
        window.statusBarColor = statusBarColor
    }

    fun alertDialog(
        context: Context,
        title: String,
        description: String,
        btnOkay: String,
        btnCancel: String,
        isImageVisible: Boolean = false,
        image: Int = 0,
        ivClose: Boolean = false,
        onItemClick: (String) -> Unit
    ): Dialog {
        val dialog = Dialog(context)
        val alertBinding = AlertDialogDesignBinding.inflate(LayoutInflater.from(context))

        dialog.setContentView(alertBinding.root)

        // Optional: transparent background (important)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setCancelable(false)

        val margin = (20 * context.resources.displayMetrics.density).toInt()

        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        dialog.window?.setLayout(
            screenWidth - (margin * 3),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        alertBinding.ivImage.isVisible = isImageVisible
        alertBinding.ivClose.isVisible = ivClose
        alertBinding.tvTitle.text = title
        alertBinding.tvDescription.text = description
        alertBinding.tvOkay.text = btnOkay
        alertBinding.tvCancel.text = btnCancel

        if (image != 0) {
            alertBinding.ivImage.setImageResource(image)
        }


        alertBinding.tvOkay.setOnClickListener {
            onItemClick(positiveButton)
            dialog.dismiss()
        }

        alertBinding.tvCancel.setOnClickListener {
            onItemClick(negativeButton)
            dialog.dismiss()
        }

        alertBinding.ivClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        return dialog
    }


    fun pdfSaveDialog(
        context: Context,
        defaultName: String = "",
        onItemClick: (action: String, name: String) -> Unit
    ) {
        val dialog = Dialog(context)
        val alertBinding = PdfSaveDialogDesignBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(alertBinding.root)

        // Optional: transparent background (important)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setCancelable(false)

        val margin = (20 * context.resources.displayMetrics.density).toInt()

        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        dialog.window?.setLayout(
            screenWidth - (margin * 3),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        if (defaultName.isNotEmpty()) {
            alertBinding.tvSaveName.setText(defaultName)
            alertBinding.tvSaveName.setSelectAllOnFocus(true)
        }

        alertBinding.tvOkay.setOnClickListener {
            val enteredName = alertBinding.tvSaveName.text.toString().trim()
            onItemClick(positiveButton, enteredName)
            dialog.dismiss()
        }

        alertBinding.tvCancel.setOnClickListener {
            onItemClick(negativeButton, "")
            dialog.dismiss()
        }

        dialog.show()
    }

    fun gotoPageDialog(
        context: Context,
        pageCount: Int,
        onItemClick: (action: String, pageText: String) -> Unit
    ) {
        val dialog = Dialog(context)
        val binding = DialogGotoPageDesignBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setCancelable(false)

        val margin = (20 * context.resources.displayMetrics.density).toInt()
        val screenWidth = context.resources.displayMetrics.widthPixels
        dialog.window?.setLayout(
            screenWidth - (margin * 3),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.tvDescription.text = context.getString(R.string.enter_page_number, pageCount)

        binding.tvOkay.setOnClickListener {
            onItemClick(positiveButton, binding.etPageNumber.text.toString().trim())
            dialog.dismiss()
        }

        binding.tvCancel.setOnClickListener {
            onItemClick(negativeButton, "")
            dialog.dismiss()
        }

        dialog.show()
    }

    fun compressPdf(
        src: File,
        dest: File,
        qualityPercent: Int,
        password: String = "",
        onProgress: (Int) -> Unit = {}
    ): Boolean {
        var reader: PdfReader? = null
        var stamper: PdfStamper? = null
        return try {
            val passwordBytes = if (password.isNotEmpty()) password.toByteArray() else null
            reader = if (passwordBytes != null) {
                PdfReader(src.absolutePath, passwordBytes)
            } else {
                PdfReader(src.absolutePath)
            }
            val n = reader.xrefSize
            for (i in 0 until n) {
                val progress = (i * 100) / n
                onProgress(progress)
                val obj = reader.getPdfObject(i)
                if (obj != null && obj.isStream) {
                    val stream = obj as com.itextpdf.text.pdf.PRStream
                    val subtype = stream.getAsName(com.itextpdf.text.pdf.PdfName.SUBTYPE)
                    if (com.itextpdf.text.pdf.PdfName.IMAGE == subtype) {
                        try {
                            val filter = stream.get(com.itextpdf.text.pdf.PdfName.FILTER)
                            var imageBytes: ByteArray? = null
                            if (com.itextpdf.text.pdf.PdfName.DCTDECODE.equals(filter)) {
                                try {
                                    imageBytes = PdfReader.getStreamBytesRaw(stream)
                                } catch (e: Exception) {
                                    // ignore
                                }
                            }
                            if (imageBytes == null) {
                                val image = com.itextpdf.text.pdf.parser.PdfImageObject(stream)
                                imageBytes = image.imageAsBytes
                            }
                            if (imageBytes != null) {
                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                                    imageBytes,
                                    0,
                                    imageBytes.size
                                )
                                if (bitmap != null) {
                                    val baos = java.io.ByteArrayOutputStream()
                                    bitmap.compress(
                                        android.graphics.Bitmap.CompressFormat.JPEG,
                                        qualityPercent,
                                        baos
                                    )
                                    val compressedBytes = baos.toByteArray()
                                    stream.setData(compressedBytes, false)
                                    stream.put(
                                        com.itextpdf.text.pdf.PdfName.TYPE,
                                        com.itextpdf.text.pdf.PdfName.XOBJECT
                                    )
                                    stream.put(
                                        com.itextpdf.text.pdf.PdfName.SUBTYPE,
                                        com.itextpdf.text.pdf.PdfName.IMAGE
                                    )
                                    stream.put(
                                        com.itextpdf.text.pdf.PdfName.FILTER,
                                        com.itextpdf.text.pdf.PdfName.DCTDECODE
                                    )
                                    stream.put(
                                        com.itextpdf.text.pdf.PdfName.WIDTH,
                                        com.itextpdf.text.pdf.PdfNumber(bitmap.width)
                                    )
                                    stream.put(
                                        com.itextpdf.text.pdf.PdfName.HEIGHT,
                                        com.itextpdf.text.pdf.PdfNumber(bitmap.height)
                                    )
                                    stream.put(
                                        com.itextpdf.text.pdf.PdfName.BITSPERCOMPONENT,
                                        com.itextpdf.text.pdf.PdfNumber(8)
                                    )
                                    bitmap.recycle()
                                }
                            }
                        } catch (t: Throwable) {
                            android.util.Log.e(
                                "Utils",
                                "Error compressing image at index $i: ${t.message}"
                            )
                            if (t is OutOfMemoryError) {
                                System.gc()
                            }
                        }
                    }
                }
            }
            val fos = FileOutputStream(dest)
            stamper = PdfStamper(reader, fos)
            stamper.setFullCompression()
            stamper.writer.compressionLevel = com.itextpdf.text.pdf.PdfStream.BEST_COMPRESSION
            stamper.close()
            fos.close()
            reader.close()
            onProgress(100)
            true
        } catch (e: Exception) {
            android.util.Log.e("Utils", "Error compressing PDF: ${e.message}")
            try {
                stamper?.close()
            } catch (ex: Exception) {
            }
            try {
                reader?.close()
            } catch (ex: Exception) {
            }
            false
        }
    }
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

//fun Context.createImmersiveBottomSheetDialog(
//    theme: Int = com.reader.alldocviewer.pdfeditor.R.style.ImmersiveBottomSheetDialogTheme,
//): ImmersiveBottomSheetDialog {
//    return com.reader.alldocviewer.pdfeditor.ui.dialogs.ImmersiveBottomSheetDialog(this, theme).also {
//        it.setupImmersiveBottomSheetNavigation()
//    }
//}

fun BottomSheetDialog.setupImmersiveBottomSheetNavigation(
    onShow: ((BottomSheetDialog) -> Unit)? = null,
) {
    setOnShowListener { dialogInterface ->
        val sheetDialog = dialogInterface as BottomSheetDialog
        onShow?.invoke(sheetDialog)
        Utils.applyBottomSheetImmersiveNavigation(sheetDialog)
    }
}

fun BottomSheetDialog.showWithImmersiveBottomSheetNavigation(
    onShow: ((BottomSheetDialog) -> Unit)? = null,
) {
    setupImmersiveBottomSheetNavigation(onShow)
    show()
}
