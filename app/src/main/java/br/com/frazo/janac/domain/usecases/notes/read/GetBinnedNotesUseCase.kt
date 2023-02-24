package br.com.frazo.janac.domain.usecases.notes.read

interface GetBinnedNotesUseCase<T> {

    operator fun invoke(): T

}