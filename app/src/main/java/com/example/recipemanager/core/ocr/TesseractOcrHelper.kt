package com.example.recipemanager.core.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
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
        val tessDataDir = context.filesDir.absolutePath

        val raw = decodeBitmap(context, uri)
            ?: return@withContext ""
        val bitmap = toGrayscale(raw)
        if (raw !== bitmap) raw.recycle()

        val api = TessBaseAPI()
        return@withContext try {
            if (!api.init(tessDataDir, LANG, TessBaseAPI.OEM_LSTM_ONLY)) {
                return@withContext ""
            }
            api.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
            api.setImage(bitmap)
            api.utF8Text?.trim() ?: ""
        } finally {
            api.recycle()
            bitmap.recycle()
        }
    }

    /** Convert bitmap to grayscale - Tesseract accuracy improves significantly. */
    private fun toGrayscale(src: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
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
