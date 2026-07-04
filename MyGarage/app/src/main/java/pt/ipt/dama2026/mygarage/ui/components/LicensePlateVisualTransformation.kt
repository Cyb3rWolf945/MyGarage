package pt.ipt.dama2026.mygarage.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Insere hífen a cada 2 caracteres só para mostrar (não altera o texto real).
 * Ex.: "AA11BB" aparece como "AA-11-BB". O cursor segue o texto original.
 * VisualTransformation é uma interface do Jetpack Compose que permite alterar a forma como o texto é exibido, sem modificar o valor real do texto.
 * Isso é útil para formatação de entrada, como números de telefone, códigos postais ou, neste caso, matrículas de veículos.
 * A implementação de [OffsetMapping] garante que o cursor e a seleção de texto funcionem corretamente, mapeando as posições do texto original para o texto transformado e vice-versa.
 * A função [filter] é chamada sempre que o texto muda, e ela retorna um [TransformedText] que contém o texto transformado e o mapeamento de deslocamento.
 * A função [buildString] é usada para construir a string transformada de forma eficiente, inserindo hífens a cada dois caracteres.
 * O mapeamento de deslocamento é calculado com base na quantidade de hífens inseridos, garantindo que o cursor se comporte de maneira intuitiva para o usuário.
 * A transformação é puramente visual; o valor real do texto permanece inalterado, permitindo que a lógica de negócios e a validação de entrada funcionem com o valor original.
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
