package br.com.frazo.janac.domain.usecases

class TrimmedUppercaseDataTransformerUseCase: DataTransformerUseCase<String> {

    override operator fun invoke(data: String): String {
        return data.trim().uppercase()
    }

}