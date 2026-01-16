package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

internal class GetSecurityLevelUseCase(
    private val repository: EncryptionRepository
) : UseCase<GetSecurityLevelUseCase.Input, GetSecurityLevelUseCase.Output> {

    override suspend fun invoke(input: Input): Result<Output> = runCatching {
        val level = repository.getSecurityLevel(input.alias)
        Output(level)
    }

    data class Input(val alias: String) : UseCaseInput
    data class Output(val level: SecurityLevel) : UseCaseOutput
}
