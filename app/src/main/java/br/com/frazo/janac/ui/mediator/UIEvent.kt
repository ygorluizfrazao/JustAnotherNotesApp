package br.com.frazo.janac.ui.mediator

import br.com.frazo.janac.domain.models.Note

sealed class UIEvent{

    object NoteAdded: UIEvent()
    data class NotBinnedNotesFetched(val notes: List<Note>): UIEvent()
    data class BinnedNotesFetched(val notes: List<Note>): UIEvent()

}
