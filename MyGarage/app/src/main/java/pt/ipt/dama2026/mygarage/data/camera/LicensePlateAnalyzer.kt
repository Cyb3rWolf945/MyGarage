package pt.ipt.dama2026.mygarage.data.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.Closeable
import java.util.Locale

class LicensePlateAnalyzer(
    private val onPlateFound: (String) -> Unit
) : ImageAnalysis.Analyzer, Closeable {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastDetectedPlate: String? = null
    private var consecutiveMatches = 0

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val detectedPlate = extractLicensePlate(visionText.text)

                if (detectedPlate != null) {
                    if (detectedPlate == lastDetectedPlate) {
                        consecutiveMatches++
                        if (consecutiveMatches == REQUIRED_CONSECUTIVE_MATCHES) {
                            onPlateFound(detectedPlate)
                        }
                    } else {
                        lastDetectedPlate = detectedPlate
                        consecutiveMatches = 1
                    }
                }
            }
            .addOnFailureListener { throwable ->
                Log.e(TAG, "License plate text recognition failed", throwable)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun close() {
        textRecognizer.close()
    }

    private fun extractLicensePlate(rawText: String): String? {
        val normalizedText = rawText.uppercase(Locale.ROOT)

        return PLATE_PATTERNS
            .asSequence()
            .mapNotNull { regex -> regex.find(normalizedText)?.value }
            .firstOrNull()
            ?.normalizePlate()
    }

    private fun String.normalizePlate(): String {
        return replace(SEPARATOR_REGEX, "-")
            .replace("--", "-")
            .trim('-')
    }
    companion object {
        private const val TAG = "LicensePlateAnalyzer"
        private const val REQUIRED_CONSECUTIVE_MATCHES = 3
        private val SEPARATOR_REGEX = """[\s\.\•]+""".toRegex()
        private const val SEP = """[-\s\.\•]?"""

        private val PLATE_PATTERNS = listOf(
            Regex("""\b[A-Z]{2}$SEP\d{2}$SEP\d{2}\b"""), // AA-00-00
            Regex("""\b\d{2}$SEP[A-Z]{2}$SEP\d{2}\b"""), // 00-AA-00
            Regex("""\b\d{2}$SEP\d{2}$SEP[A-Z]{2}\b"""), // 00-00-AA
            Regex("""\b[A-Z]{2}$SEP\d{2}$SEP[A-Z]{2}\b""")  // AA-00-AA (New 2020+ format)
        )
    }
}
