package pt.ipt.dama2026.mygarage.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Insere vírgulas a cada 3 dígitos só para mostrar (não altera o texto real).
 * VisualTransformation é uma interface do Jetpack Compose que permite alterar a forma como o texto é exibido, sem modificar o valor real do texto.
 * Isso é útil para formatação de entrada, como números de telefone, códigos postais ou, neste caso, quilometragem de veículos.
 * A implementação de [OffsetMapping] garante que o cursor e a seleção de texto funcionem corretamente, mapeando as posições do texto original para o texto transformado e vice-versa.
 * A função [filter] é chamada sempre que o texto muda, e ela retorna um [TransformedText] que contém o texto transformado e o mapeamento de deslocamento.
 * A função [addCommas] é usada para construir a string transformada de forma eficiente, inserindo vírgulas a cada três dígitos.
 * O mapeamento de deslocamento é calculado com base na quantidade de vírgulas inseridas, garantindo que o cursor se comporte de maneira intuitiva para o usuário.
 * A transformação é puramente visual; o valor real do texto permanece inalterado, permitindo que a lógica de negócios e a validação de entrada funcionem com o valor original.
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
