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
import kotlin.math.min

/**
 * On-device OCR using Tesseract4Android with the Hebrew trained data bundled in assets.
 * Supported language: "heb" (Hebrew). Falls back gracefully if the data is missing.
 */
object TesseractOcrHelper {

    private const val LANG = "heb"
    private const val DATA_DIR_NAME = "tessdata"
    private const val MODEL_VERSION = 2
    private const val MAX_DIM = 2000

    suspend fun extractText(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        ensureTrainedData(context)
        val tessDataDir = context.filesDir.absolutePath

        val raw = decodeBitmap(context, uri) ?: return@withContext ""
        val scaled = scaleBitmap(raw)
        if (raw !== scaled) raw.recycle()
        val processed = toHighContrastGrayscale(scaled)
        if (scaled !== processed) scaled.recycle()

        val api = TessBaseAPI()
        return@withContext try {
            if (!api.init(tessDataDir, LANG, TessBaseAPI.OEM_LSTM_ONLY)) {
                return@withContext ""
            }
            api.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
            api.setVariable("user_defined_dpi", "300")
            api.setImage(processed)
            api.utF8Text?.trim() ?: ""
        } finally {
            api.recycle()
            processed.recycle()
        }
    }

    /** Scale down to MAX_DIM on the longest side for optimal Tesseract performance. */
    private fun scaleBitmap(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val maxSide = maxOf(w, h)
        if (maxSide <= MAX_DIM) return src
        val scale = MAX_DIM.toFloat() / maxSide
        return Bitmap.createScaledBitmap(src, (w * scale).toInt(), (h * scale).toInt(), true)
    }

    /** Grayscale + contrast boost — higher contrast text recognized far more reliably. */
    private fun toHighContrastGrayscale(src: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = ColorMatrix()
        // Desaturate
        cm.setSaturation(0f)
        // Boost contrast: scale=1.5, translate=-38 (shifts midpoint down, increases separation)
        val contrast = 1.5f
        val translate = (-0.5f * 255 * (contrast - 1)).toInt().toFloat()
        val contrastMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        contrastMatrix.preConcat(cm)
        paint.colorFilter = ColorMatrixColorFilter(contrastMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    private fun ensureTrainedData(context: Context) {
        val destDir = File(context.filesDir, DATA_DIR_NAME)
        val destFile = File(destDir, "$LANG.traineddata")
        val versionFile = File(context.filesDir, "tessdata_version")
        val installedVersion = if (versionFile.exists()) versionFile.readText().trim().toIntOrNull() ?: 0 else 0
        if (destFile.exists() && installedVersion >= MODEL_VERSION) return
        destDir.mkdirs()
        context.assets.open("$DATA_DIR_NAME/$LANG.traineddata").use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        versionFile.writeText(MODEL_VERSION.toString())
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