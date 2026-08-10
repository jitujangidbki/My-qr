package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ExportUtils {

    enum class ExportFormat(val extension: String, val mimeType: String) {
        PNG("png", "image/png"),
        JPG("jpg", "image/jpeg")
    }

    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        filename: String,
        format: ExportFormat = ExportFormat.PNG
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val compressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            val fullFilename = "$filename.${format.extension}"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fullFilename)
                    put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/QR Studio")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext false

                context.contentResolver.openOutputStream(uri)?.use { os ->
                    bitmap.compress(compressFormat, 100, os)
                }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
                true
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val qrStudioDir = File(imagesDir, "QR Studio")
                if (!qrStudioDir.exists()) qrStudioDir.mkdirs()

                val imageFile = File(qrStudioDir, fullFilename)
                FileOutputStream(imageFile).use { os ->
                    bitmap.compress(compressFormat, 100, os)
                }

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun shareBitmap(
        context: Context,
        bitmap: Bitmap,
        title: String
    ) = withContext(Dispatchers.IO) {
        try {
            val cachePath = File(context.cacheSchemeDir, "shared_qr")
            cachePath.mkdirs()
            val file = File(cachePath, "qr_share_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Scan this QR code created with QR Studio")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share QR Code via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error sharing QR image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    suspend fun decodeBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidthPx: Int = 1024,
        reqHeightPx: Int = 1024
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            var inSampleSize = 1
            val (height: Int, width: Int) = options.outHeight to options.outWidth
            if (height > reqHeightPx || width > reqWidthPx) {
                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2
                while (halfHeight / inSampleSize >= reqHeightPx && halfWidth / inSampleSize >= reqWidthPx) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }

            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private val Context.cacheSchemeDir: File
        get() = cacheDir
}
