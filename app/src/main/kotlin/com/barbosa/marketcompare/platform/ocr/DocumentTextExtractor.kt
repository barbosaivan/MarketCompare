package com.barbosa.marketcompare.platform.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import com.barbosa.marketcompare.domain.model.RawOcrElement
import com.barbosa.marketcompare.domain.model.RawOcrLine
import com.barbosa.marketcompare.domain.model.RawOcrPayload
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
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

    suspend fun extractOcrFromUri(uri: Uri): Result<RawOcrPayload> = withContext(Dispatchers.IO) {
        runCatching {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            if (mimeType.contains("pdf", ignoreCase = true) || uri.toString().endsWith(".pdf", ignoreCase = true)) {
                extractFromPdf(uri)
            } else {
                extractFromImage(uri)
            }
        }
    }

    suspend fun extractTextFromUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        extractOcrFromUri(uri).map { it.fullText }
    }

    private suspend fun extractFromImage(uri: Uri): RawOcrPayload {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("No se pudo abrir el stream de la imagen")
        val bitmap = inputStream.use { BitmapFactory.decodeStream(it) }
            ?: throw IllegalArgumentException("No se pudo decodificar la imagen")

        return try {
            processBitmapWithMlKit(bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    private suspend fun extractFromPdf(uri: Uri): RawOcrPayload {
        val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("No se pudo abrir el archivo PDF")

        val fullTextBuilder = StringBuilder()
        val allLines = mutableListOf<RawOcrLine>()
        var maxWidth = 0
        var totalHeight = 0

        pfd.use { descriptor ->
            val pdfRenderer = PdfRenderer(descriptor)
            try {
                val pageCount = pdfRenderer.pageCount.coerceAtMost(3)
                for (pageIndex in 0 until pageCount) {
                    val page = pdfRenderer.openPage(pageIndex)
                    val width = page.width * 2
                    val height = page.height * 2
                    val bitmap = createBitmap(width, height)

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    try {
                        val pagePayload = processBitmapWithMlKit(bitmap)
                        if (pagePayload.fullText.isNotBlank()) {
                            fullTextBuilder.append(pagePayload.fullText).append("\n")
                        }
                        maxWidth = maxOf(maxWidth, width)
                        val yOffset = totalHeight
                        val adjustedLines = pagePayload.lines.map { line ->
                            line.copy(
                                top = line.top + yOffset,
                                bottom = line.bottom + yOffset,
                                elements = line.elements.map { elem ->
                                    elem.copy(top = elem.top + yOffset, bottom = elem.bottom + yOffset)
                                }
                            )
                        }
                        allLines.addAll(adjustedLines)
                        totalHeight += height
                    } finally {
                        bitmap.recycle()
                    }
                }
            } finally {
                pdfRenderer.close()
            }
        }

        return RawOcrPayload(
            fullText = fullTextBuilder.toString().trim(),
            imageWidth = maxWidth,
            imageHeight = totalHeight,
            lines = allLines
        )
    }

    private suspend fun processBitmapWithMlKit(bitmap: Bitmap): RawOcrPayload =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val payload = mapVisionTextToPayload(visionText, bitmap.width, bitmap.height)
                    continuation.resume(payload)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }

    private fun mapVisionTextToPayload(visionText: Text, width: Int, height: Int): RawOcrPayload {
        val lines = mutableListOf<RawOcrLine>()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val lineBox = line.boundingBox
                val elements = line.elements.map { elem ->
                    val elemBox = elem.boundingBox
                    RawOcrElement(
                        text = elem.text,
                        left = elemBox?.left ?: 0,
                        top = elemBox?.top ?: 0,
                        right = elemBox?.right ?: 0,
                        bottom = elemBox?.bottom ?: 0
                    )
                }
                lines.add(
                    RawOcrLine(
                        text = line.text,
                        left = lineBox?.left ?: 0,
                        top = lineBox?.top ?: 0,
                        right = lineBox?.right ?: 0,
                        bottom = lineBox?.bottom ?: 0,
                        elements = elements
                    )
                )
            }
        }
        return RawOcrPayload(
            fullText = visionText.text,
            imageWidth = width,
            imageHeight = height,
            lines = lines
        )
    }
}
