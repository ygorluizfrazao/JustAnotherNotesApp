package br.com.frazo.janac.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File
import java.time.OffsetDateTime
@Parcelize
data class Note(
    val title: String,
    val text: String,
    val audioNote: File? = null,
    val createdAt: OffsetDateTime? = null,
    val binnedAt: OffsetDateTime? = null
) : Parcelable

