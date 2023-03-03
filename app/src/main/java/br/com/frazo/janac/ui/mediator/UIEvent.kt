package br.com.frazo.janac.ui.mediator

import br.com.frazo.janac.domain.models.Note

sealed class UIEvent{

    data class NoteCreated(val newNote: Note): UIEvent()
    data class NoteEdited(val oldNote: Note, val newNote: Note): UIEvent()
    data class NotBinnedNotesFetched(val notes: List<Note>): UIEvent()
    data class BinnedNotesFetched(val notes: List<Note>): UIEvent()

}
