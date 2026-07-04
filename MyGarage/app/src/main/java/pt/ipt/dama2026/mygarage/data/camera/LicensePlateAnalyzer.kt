package pt.ipt.dama2026.mygarage.data.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.Closeable
import java.util.Locale

/**
 * Analisa frames da câmara via ML Kit para detetar matrículas portuguesas.
 * Requer 3 leituras consecutivas iguais antes de notificar o callback para reduzir falsos positivos.
 * O limite de 3 está definido na constante REQUIRED_CONSECUTIVE_MATCHES no companion object
 */
class LicensePlateAnalyzer(
    private val onPlateFound: (String) -> Unit
) : ImageAnalysis.Analyzer, Closeable {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastDetectedPlate: String? = null
    private var consecutiveMatches = 0

    /** Processa cada frame: extrai texto, aplica regex de matrícula e contabiliza matches consecutivos. */
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

    /** Liberta o recognizer ML Kit. */
    override fun close() {
        textRecognizer.close()
    }

    /** Aplica os padrões de matrícula portuguesa ao texto normalizado e devolve o primeiro match. */
    private fun extractLicensePlate(rawText: String): String? {
        val normalizedText = rawText.uppercase(Locale.ROOT)

        return PLATE_PATTERNS
            .asSequence()
            .mapNotNull { regex -> regex.find(normalizedText)?.value }
            .firstOrNull()
            ?.normalizePlate()
    }

    /** Normaliza separadores: espaços/pontos → hífen, remove duplos hífenes e hífenes nas extremidades. */
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
