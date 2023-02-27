package br.com.frazo.janac.domain.models

import java.time.OffsetDateTime

data class Note(
    val title: String,
    val text: String,
    val createdAt: OffsetDateTime? = null,
    val binnedAt: OffsetDateTime? = null
)

