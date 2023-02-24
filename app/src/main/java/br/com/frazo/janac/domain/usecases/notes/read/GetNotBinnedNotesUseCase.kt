package br.com.frazo.janac.domain.usecases.notes.read


interface GetNotBinnedNotesUseCase<T>{

    operator fun invoke(): T
}