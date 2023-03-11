package br.com.frazo.janac.ui.mediator

import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.util.TextResource

sealed class UIEvent {

    data class NoteCreated(val newNote: Note) : UIEvent()
    data class NoteEdited(val oldNote: Note, val newNote: Note) : UIEvent()
    data class NotBinnedNotesFetched(val notes: List<Note>) : UIEvent()
    data class NotBinnedNotesFiltered(val filteredNotes: List<Note>) : UIEvent()
    data class BinnedNotesFetched(val notes: List<Note>) : UIEvent()
    data class BinnedNotesFiltered(val filteredNotes: List<Note>) : UIEvent()
    data class FilterQuery(val query: String) : UIEvent()
    object FinishSearchQuery: UIEvent()
    data class Error(val message: TextResource, val throwable: Throwable? = null) : UIEvent()

}
