package br.com.frazo.janac.domain.usecases.notes.read


interface GetNotesUseCase<T> {

    operator fun invoke(): T
}