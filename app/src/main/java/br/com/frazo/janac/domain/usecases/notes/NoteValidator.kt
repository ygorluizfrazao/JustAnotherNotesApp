package br.com.frazo.janac.domain.usecases.notes

import br.com.frazo.janac.domain.models.Note

interface NoteValidator {

    fun validate(note: Note): NoteValidatorResult

    sealed class NoteValidatorResult {

        object Valid : NoteValidatorResult()
        data class InvalidLength(val field: String, val minLength: Int) : NoteValidatorResult()
        data class Error(val throwable: Throwable) : NoteValidatorResult()

    }
}