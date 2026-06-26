package pt.ipt.dama2026.mygarage.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Injects a hyphen ("-") after every 2 characters for visual display only.
 *
 * Example: raw "AA11BB" is rendered as "AA-11-BB" without modifying the
 * underlying text buffer, so the cursor tracks the raw index correctly.
 *
 * OffsetMapping:
 * - [originalToTransformed]: every 2 chars add 1 for the injected hyphen.
 * - [transformedToOriginal]: every 3 chars subtract 1 (the hyphen at pos 2,5,...).
 */
object LicensePlateVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val transformed = buildString {
            raw.forEachIndexed { index, c ->
                if (index > 0 && index % 2 == 0) append('-')
                append(c)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val hyphensBefore = (offset / 2).coerceAtMost(2)
                return (offset + hyphensBefore).coerceAtMost(transformed.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val hyphensBefore = (offset / 3).coerceAtMost(2)
                return (offset - hyphensBefore).coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}
