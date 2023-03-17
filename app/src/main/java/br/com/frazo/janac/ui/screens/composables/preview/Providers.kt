package br.com.frazo.janac.ui.screens.composables.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import br.com.frazo.janac.domain.models.Note
import kotlin.random.Random

class SampleNoteProvider : PreviewParameterProvider<Note> {
    override val values: Sequence<Note> = sequenceOf(
            Note(
                title = LoremIpsum(Random.nextInt(4)).values.shuffled()
                    .reduce { acc, s -> acc + s },
                text = LoremIpsum(Random.nextInt(50)).values.shuffled()
                    .reduce { acc, s -> acc + s },
                createdAt = null,
                binnedAt = null
            ),
            Note(
                title = LoremIpsum(Random.nextInt(4)).values.shuffled()
                    .reduce { acc, s -> acc + s },
                text = LoremIpsum(Random.nextInt(50)).values.shuffled()
                    .reduce { acc, s -> acc + s },
                createdAt = null,
                binnedAt = null
            )
        )

}