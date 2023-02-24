package br.com.frazo.janac.domain.usecases.notes

import br.com.frazo.janac.domain.usecases.notes.create.AddNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.delete.RemoveNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetNotBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.update.BinNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.update.EditNoteUseCase


data class NotesUseCaseGroup<ReadReturnType, ManipulationReturnType>(
    val addNoteUseCase: AddNoteUseCase<ManipulationReturnType>,
    val removeNoteUseCase: RemoveNoteUseCase<ManipulationReturnType>,
    val getBinnedNotesUseCase: GetBinnedNotesUseCase<ReadReturnType>,
    val getNotBinnedNotesUseCase: GetNotBinnedNotesUseCase<ReadReturnType>,
    val getNotesUseCase: GetNotesUseCase<ReadReturnType>,
    val binNoteUseCase: BinNoteUseCase<ManipulationReturnType>,
    val editNoteUseCase: EditNoteUseCase<ManipulationReturnType>
)