package com.barbosa.marketcompare.platform.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Cargador seguro y optimizado de imágenes de recibos y facturas en alta resolución (MC-055).
 */
object ReceiptImageLoader {

    private const val MAX_IMAGE_DIMENSION = 2048

    suspend fun loadBitmap(context: Context, uriString: String?): ImageBitmap? = withContext(Dispatchers.IO) {
        if (uriString.isNullOrBlank()) return@withContext null
        runCatching {
            val uri = uriString.toUri()
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val isPdf = mimeType.contains("pdf", ignoreCase = true) || uriString.endsWith(".pdf", ignoreCase = true)

            if (isPdf) {
                renderPdfPage(context, uri)
            } else {
                decodeImageUri(context, uri)
            }?.asImageBitmap()
        }.getOrNull()
    }

    private fun decodeImageUri(context: Context, uri: Uri): Bitmap? {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        } ?: return null

        val sampleSize = calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight)
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
        return inputStream.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        }
    }

    private fun renderPdfPage(context: Context, uri: Uri): Bitmap? {
        val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
        return pfd.use { descriptor ->
            val pdfRenderer = PdfRenderer(descriptor)
            try {
                if (pdfRenderer.pageCount == 0) return null
                val page = pdfRenderer.openPage(0)
                val width = (page.width * 2).coerceAtMost(MAX_IMAGE_DIMENSION)
                val height = (page.height * 2).coerceAtMost(MAX_IMAGE_DIMENSION)
                val bitmap = createBitmap(width, height)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                bitmap
            } finally {
                pdfRenderer.close()
            }
        }
    }

    private fun calculateInSampleSize(rawWidth: Int, rawHeight: Int): Int {
        var inSampleSize = 1
        if (rawHeight > MAX_IMAGE_DIMENSION || rawWidth > MAX_IMAGE_DIMENSION) {
            val halfHeight = rawHeight / 2
            val halfWidth = rawWidth / 2
            while ((halfHeight / inSampleSize) >= MAX_IMAGE_DIMENSION || (halfWidth / inSampleSize) >= MAX_IMAGE_DIMENSION) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }
}
