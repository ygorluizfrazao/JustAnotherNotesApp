package br.com.frazo.janac.domain.extensions

import br.com.frazo.janac.domain.models.Note

fun Note.isNewNote(): Boolean {
    return createdAt == null
}