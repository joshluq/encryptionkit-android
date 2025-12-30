package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class GetSecurityLevelUseCase(
    private val repository: EncryptionRepository
) : UseCase<GetSecurityLevelUseCase.Input, GetSecurityLevelUseCase.Output> {

    override fun invoke(input: Input): Flow<Output> = flow {
        val level = repository.getSecurityLevel(input.alias)
        emit(Output(level))
    }

    data class Input(val alias: String) : UseCaseInput
    data class Output(val level: SecurityLevel) : UseCaseOutput
}
