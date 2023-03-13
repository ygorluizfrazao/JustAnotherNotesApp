package br.com.frazo.janac.ui.mediator

import android.annotation.SuppressLint
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
    object FinishSearchQuery : UIEvent()
    data class ContentDisplayModeChanged(val newContentDisplayMode: ContentDisplayMode) : UIEvent()
    @SuppressLint
    data class DisableFeatures(val features: List<Feature>) : UIEvent()
    @SuppressLint
    data class EnableFeatures(val features: List<Feature>) : UIEvent()
    data class Error(val message: TextResource, val throwable: Throwable? = null) : UIEvent()

}


enum class Feature {
    SEARCH,
    CHANGE_DISPLAY_MODE
}

enum class ContentDisplayMode {
    AS_LIST,
    AS_STAGGERED_GRID
}


