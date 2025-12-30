package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class InitializeLibraryUseCase(
    private val repository: EncryptionRepository
) : UseCase<InitializeLibraryUseCase.Input, NoneOutput> {

    override fun invoke(input: Input): Flow<NoneOutput> = flow {
        repository.initializeKey(input.config)
        emit(NoneOutput)
    }

    data class Input(val config: EncryptionConfig) : UseCaseInput
}
