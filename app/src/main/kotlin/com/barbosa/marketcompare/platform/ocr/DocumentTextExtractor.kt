package com.barbosa.marketcompare.platform.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DocumentTextExtractor(private val context: Context) {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun extractTextFromUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            if (mimeType.contains("pdf", ignoreCase = true) || uri.toString().endsWith(".pdf", ignoreCase = true)) {
                extractFromPdf(uri)
            } else {
                extractFromImage(uri)
            }
        }
    }

    private suspend fun extractFromImage(uri: Uri): String {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("No se pudo abrir el stream de la imagen")
        val bitmap = inputStream.use { BitmapFactory.decodeStream(it) }
            ?: throw IllegalArgumentException("No se pudo decodificar la imagen")

        return processBitmapWithMlKit(bitmap)
    }

    private suspend fun extractFromPdf(uri: Uri): String {
        val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("No se pudo abrir el archivo PDF")

        val extractedText = StringBuilder()

        pfd.use { descriptor ->
            val pdfRenderer = PdfRenderer(descriptor)
            try {
                val pageCount = pdfRenderer.pageCount.coerceAtMost(3) // Procesar hasta las primeras 3 páginas
                for (pageIndex in 0 until pageCount) {
                    val page = pdfRenderer.openPage(pageIndex)
                    val width = page.width * 2
                    val height = page.height * 2
                    val bitmap = createBitmap(width, height)

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val pageText = processBitmapWithMlKit(bitmap)
                    if (pageText.isNotBlank()) {
                        extractedText.append(pageText).append("\n")
                    }
                    bitmap.recycle()
                }
            } finally {
                pdfRenderer.close()
            }
        }

        return extractedText.toString()
    }

    private suspend fun processBitmapWithMlKit(bitmap: Bitmap): String =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
}

