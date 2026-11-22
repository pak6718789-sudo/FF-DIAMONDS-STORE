package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ScreenshotStorage(private val context: Context) {
    private val screenshotsDir: File
        get() = File(context.filesDir, "screenshots").apply {
            if (!exists()) mkdirs()
        }

    suspend fun saveReceiptFromUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileName = "receipt_${UUID.randomUUID().toString().take(12)}.jpg"
            val destFile = File(screenshotsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                val rawBytes = input.readBytes()
                if (rawBytes.isEmpty()) {
                    return@withContext Result.failure(IllegalArgumentException("Selected image file is empty."))
                }
                val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
                if (bitmap != null) {
                    val scaled = scaleDownIfNeeded(bitmap, maxDim = 1280)
                    FileOutputStream(destFile).use { out ->
                        scaled.compress(Bitmap.CompressFormat.JPEG, 82, out)
                    }
                    if (scaled !== bitmap) scaled.recycle()
                    bitmap.recycle()
                } else {
                    FileOutputStream(destFile).use { out ->
                        out.write(rawBytes)
                    }
                }
            } ?: return@withContext Result.failure(IllegalStateException("Could not open selected image."))

            Result.success(destFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDim && height <= maxDim) return bitmap
        val ratio = minOf(maxDim.toFloat() / width, maxDim.toFloat() / height)
        val newW = (width * ratio).toInt().coerceAtLeast(1)
        val newH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }
}
