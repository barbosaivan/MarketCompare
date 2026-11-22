package com.barbosa.marketcompare.platform.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.media.ExifInterface
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * Cargador seguro y optimizado de imágenes de recibos y facturas en alta resolución (MC-055).
 */
object ReceiptImageLoader {

    private const val TAG = "ReceiptImageLoader"
    private const val MAX_IMAGE_DIMENSION = 2048

    suspend fun loadBitmap(context: Context, uriString: String?): ImageBitmap? = withContext(Dispatchers.IO) {
        if (uriString.isNullOrBlank()) return@withContext null
        runCatching {
            val uri = uriString.toUri()
            Log.d(TAG, "Cargando imagen o PDF desde: $uriString")
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val isPdf = mimeType.contains("pdf", ignoreCase = true) || uriString.endsWith(".pdf", ignoreCase = true)

            val bitmap = if (isPdf) {
                renderPdfPage(context, uri)
            } else {
                decodeImageUri(context, uri)
            }
            bitmap?.asImageBitmap()
        }.onFailure { e ->
            Log.e(TAG, "Error al cargar la imagen desde $uriString", e)
        }.getOrNull()
    }

    private fun decodeImageUri(context: Context, uri: Uri): Bitmap? {
        val rotationDegrees = getExifRotation(context, uri)
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openInputStream(context, uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        } ?: run {
            Log.w(TAG, "No se pudo abrir el stream de lectura de límites para: $uri")
            return null
        }

        val sampleSize = calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight)
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decodedBitmap = openInputStream(context, uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: run {
            Log.w(TAG, "No se pudo decodificar el bitmap para: $uri")
            return null
        }

        return rotateBitmap(decodedBitmap, rotationDegrees)
    }

    private fun renderPdfPage(context: Context, uri: Uri): Bitmap? {
        val pfd = openFileDescriptor(context, uri) ?: run {
            Log.w(TAG, "No se pudo obtener el ParcelFileDescriptor para el PDF: $uri")
            return null
        }
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

    private fun openInputStream(context: Context, uri: Uri): InputStream? {
        return runCatching {
            if (uri.scheme == "file") {
                uri.path?.let { File(it).inputStream() }
            } else {
                context.contentResolver.openInputStream(uri)
            }
        }.getOrNull()
    }

    private fun openFileDescriptor(context: Context, uri: Uri): ParcelFileDescriptor? {
        return runCatching {
            if (uri.scheme == "file") {
                uri.path?.let { ParcelFileDescriptor.open(File(it), ParcelFileDescriptor.MODE_READ_ONLY) }
            } else {
                context.contentResolver.openFileDescriptor(uri, "r")
            }
        }.getOrNull()
    }

    private fun getExifRotation(context: Context, uri: Uri): Int {
        return runCatching {
            openInputStream(context, uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        }.getOrDefault(0)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
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
