package br.com.frazo.jana_c.domain.models

import java.time.OffsetDateTime

data class Note(
    val title: String,
    val text: String,
    val createdAt: OffsetDateTime? = null,
    val binnedAt: OffsetDateTime?
)

fun Note.clone(
    title: String = this.title,
    text: String = this.text,
    createdAt: OffsetDateTime? = this.createdAt,
    binnedAt: OffsetDateTime? = this.binnedAt
): Note = Note(title, text, createdAt, binnedAt)

