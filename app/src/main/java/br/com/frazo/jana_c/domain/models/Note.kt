package br.com.frazo.jana_c.domain.models

import java.time.OffsetDateTime

data class Note(val title: String, val text: String, val createdAt: OffsetDateTime? = null)
