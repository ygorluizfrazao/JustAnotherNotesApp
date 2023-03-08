package br.com.frazo.janac.domain.usecases

interface DataTransformerUseCase<D> {

    operator fun invoke(data: D): D

}