package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class DeleteKeyUseCase(
    private val repository: EncryptionRepository
) : UseCase<DeleteKeyUseCase.Input, NoneOutput> {

    override fun invoke(input: Input): Flow<NoneOutput> = flow {
        repository.deleteKey(input.alias)
        emit(NoneOutput)
    }

    data class Input(val alias: String) : UseCaseInput
}
