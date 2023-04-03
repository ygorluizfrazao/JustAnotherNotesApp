package br.com.frazo.janac.domain.models

import java.io.File
import java.time.OffsetDateTime

data class Note(
    val title: String,
    val text: String,
    val audioNote: File? = null,
    val createdAt: OffsetDateTime? = null,
    val binnedAt: OffsetDateTime? = null
)

