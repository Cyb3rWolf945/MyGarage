package ipt.pt.mygarage.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Formats numeric input with thousand separators (commas) for visual display only.
 *
 * Example: raw "15500" is rendered as "15,500" without modifying the underlying
 * text buffer, so the cursor tracks the raw index correctly.
 */
object MileageVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val transformed = addCommas(raw)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (raw.isEmpty() || offset == 0) return 0
                var transformedPos = 0
                var rawPos = 0
                while (rawPos < offset && rawPos < raw.length) {
                    if (transformedPos < transformed.length && transformed[transformedPos] == ',') {
                        transformedPos++
                    }
                    transformedPos++
                    rawPos++
                }
                return transformedPos.coerceAtMost(transformed.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var rawPos = 0
                var i = 0
                while (i < offset && i < transformed.length) {
                    if (transformed[i] != ',') rawPos++
                    i++
                }
                return rawPos.coerceAtMost(raw.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }

    private fun addCommas(text: String): String {
        if (text.length <= 3) return text
        val result = StringBuilder()
        var count = 0
        for (i in text.length - 1 downTo 0) {
            if (count > 0 && count % 3 == 0) {
                result.insert(0, ',')
            }
            result.insert(0, text[i])
            count++
        }
        return result.toString()
    }
}
