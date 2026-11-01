package com.example.warrantly.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

data class OcrResult(
    val merchant: String? = null,
    val date: Long? = null,
    val totalAmount: String? = null,
    val productName: String? = null
)

object ReceiptParser {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun parseDocument(context: Context, uri: Uri, mimeType: String?): OcrResult? {
        return try {
            val inputImage = if (mimeType == "application/pdf") {
                getBitmapFromPdf(context, uri)?.let { InputImage.fromBitmap(it, 0) }
            } else {
                InputImage.fromFilePath(context, uri)
            }

            if (inputImage == null) return null

            val visionText = recognizer.process(inputImage).await()
            extractData(visionText.text)
        } catch (e: Exception) {
            null
        }
    }

    private fun getBitmapFromPdf(context: Context, uri: Uri): Bitmap? {
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        var currentPage: PdfRenderer.Page? = null

        try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
            pdfRenderer = PdfRenderer(parcelFileDescriptor)

            if (pdfRenderer.pageCount <= 0) return null

            // Open the first page of the PDF
            currentPage = pdfRenderer.openPage(0)

            // Scale up 2x for better OCR text clarity
            val width = currentPage.width * 2
            val height = currentPage.height * 2
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            // PDFs have transparent backgrounds; fill it with white so text is readable
            bitmap.eraseColor(Color.WHITE)

            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            return bitmap
        } catch (e: Exception) {
            return null
        } finally {
            currentPage?.close()
            pdfRenderer?.close()
            parcelFileDescriptor?.close()
        }
    }

    private fun extractData(text: String): OcrResult {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return OcrResult()

        // 1. Guess Merchant (Clean up "Sold By:" prefixes)
        var merchant = lines.firstOrNull { it.length > 2 && !it.matches(Regex(".*\\d.*")) }
        if (merchant?.lowercase()?.startsWith("sold by") == true) {
            merchant = merchant.substringAfter(":").trim().removeSuffix(",")
        }

        // 2. Extract Date (Force UTC to prevent timezone day-shifting)
        val dateRegex = Regex("""(\d{1,2}[-/]\d{1,2}[-/]\d{2,4})""")
        var parsedDate: Long? = null
        dateRegex.find(text)?.value?.let { match ->
            val formats = listOf("dd-MM-yyyy", "dd/MM/yyyy", "MM-dd-yyyy", "MM/dd/yyyy", "MM-dd-yy", "MM/dd/yy")
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.US)
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    parsedDate = sdf.parse(match)?.time
                    if (parsedDate != null) break
                } catch (e: Exception) { /* ignore and try next */ }
            }
        }

        // 3. Extract Total Price
        val priceRegex = Regex("""\b(\d{1,5}\.\d{2})\b""")
        val prices = priceRegex.findAll(text).mapNotNull { it.groupValues[1].toDoubleOrNull() }.toList()
        val totalAmount = if (prices.isNotEmpty()) String.format("%.2f", prices.maxOrNull()!!) else null

        // 4. Guess Product Name
        // Aggressive blacklist for fine print, headers, and invoice boilerplate
        val boilerplate = listOf(
            "invoice", "tax", "order", "date", "total", "amount", "discount", "ship", "bill",
            "address", "phone", "gstin", "pan", "retail", "private limited", "returns", "policy",
            "contact", "www.", "value", "goods", "intended", "end user", "consumption", "re-sale",
            "signatory", "authorized", "terms", "conditions", "registered", "office", "thank you",
            "seller", "sold by", "hsn", "sac", "cgst", "sgst", "igst", "qty", "rate", "declaration",
            "jurisdiction", "subject to", "dispatch", "awb", "courier", "delivery", "payment", "item"
        )

        val possibleProducts = lines.filter { line ->
            val lower = line.lowercase()
            line.length in 10..100 &&
                    !boilerplate.any { term -> lower.contains(term) } &&
                    line.any { it.isLetter() } &&
                    line.split(" ").size > 1
        }

        // Take the first valid line instead of the absolute longest line to avoid missed fine print
        val productName = possibleProducts.firstOrNull { it.length > 15 } ?: possibleProducts.firstOrNull()

        return OcrResult(merchant = merchant, date = parsedDate, totalAmount = totalAmount, productName = productName)
    }
}