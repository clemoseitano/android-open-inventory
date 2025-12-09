package com.coptimize.openinventory.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File

object ImageUtils {
    fun getCompressedImageBytes(context: Context, imageUri: Uri, maxBytes: Int = 100_000): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            var quality = 100
            var stream = ByteArrayOutputStream()
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

            // Loop to compress until under limit
            while (stream.toByteArray().size > maxBytes && quality > 5) {
                stream = ByteArrayOutputStream()
                quality -= 5 // Reduce quality by 5% each step
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            }

            stream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}