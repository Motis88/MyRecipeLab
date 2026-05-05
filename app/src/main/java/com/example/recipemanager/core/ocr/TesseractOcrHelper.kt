package com.example.recipemanager.core.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * On-device OCR using Tesseract4Android with the Hebrew trained data bundled in assets.
 * Supported language: "heb" (Hebrew). Falls back gracefully if the data is missing.
 */
object TesseractOcrHelper {

    private const val LANG = "heb"
    private const val DATA_DIR_NAME = "tessdata"

    /**
     * Extracts text from [uri] using Tesseract Hebrew model.
     * Must be called from a coroutine; runs on [Dispatchers.IO].
     */
    suspend fun extractText(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        ensureTrainedData(context)
        val tessDataDir = File(context.filesDir, "").absolutePath

        val bitmap = decodeBitmap(context, uri)
            ?: return@withContext ""

        val api = TessBaseAPI()
        return@withContext try {
            if (!api.init(tessDataDir, LANG)) {
                return@withContext ""
            }
            api.setImage(bitmap)
            val text = api.utF8Text ?: ""
            text
        } finally {
            api.recycle()
            bitmap.recycle()
        }
    }

    /**
     * Copies heb.traineddata from assets to the app's files directory if not already present.
     */
    private fun ensureTrainedData(context: Context) {
        val destDir = File(context.filesDir, DATA_DIR_NAME)
        val destFile = File(destDir, "$LANG.traineddata")
        if (destFile.exists()) return
        destDir.mkdirs()
        context.assets.open("$DATA_DIR_NAME/$LANG.traineddata").use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun decodeBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }
}
