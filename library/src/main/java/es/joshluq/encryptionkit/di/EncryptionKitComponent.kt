package es.joshluq.encryptionkit.di

import es.joshluq.encryptionkit.data.datasource.TinkDataSource
import es.joshluq.encryptionkit.data.repository.EncryptionRepositoryImpl
import es.joshluq.encryptionkit.domain.usecase.DecryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.DeleteKeyUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptAsymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.GetSecurityLevelUseCase
import es.joshluq.encryptionkit.domain.usecase.HashDataUseCase
import es.joshluq.encryptionkit.domain.usecase.InitializeLibraryUseCase
import es.joshluq.encryptionkit.sdk.EncryptionKitConfig
import es.joshluq.foundationkit.log.LoggerKit

/**
 * Internal Dependency Injection component
 * Following the Internal Dependency Graph pattern.
 */
internal class EncryptionKitComponent(val config: EncryptionKitConfig) {

    val logger: LoggerKit by lazy { config.logger }

    private val tinkDataSource: TinkDataSource by lazy {
        TinkDataSource(config.context, logger)
    }

    private val repository: EncryptionRepositoryImpl by lazy {
        EncryptionRepositoryImpl(tinkDataSource, config.certificatePathProvider, logger, config.context)
    }

    val initializeLibraryUseCase: InitializeLibraryUseCase by lazy {
        InitializeLibraryUseCase(repository)
    }

    val encryptSymmetricUseCase: EncryptSymmetricUseCase by lazy {
        EncryptSymmetricUseCase(repository)
    }

    val decryptSymmetricUseCase: DecryptSymmetricUseCase by lazy {
        DecryptSymmetricUseCase(repository)
    }

    val encryptAsymmetricUseCase: EncryptAsymmetricUseCase by lazy {
        EncryptAsymmetricUseCase(repository)
    }

    val getSecurityLevelUseCase: GetSecurityLevelUseCase by lazy {
        GetSecurityLevelUseCase(repository)
    }

    val deleteKeyUseCase: DeleteKeyUseCase by lazy {
        DeleteKeyUseCase(repository)
    }

    val hashDataUseCase: HashDataUseCase by lazy {
        HashDataUseCase(repository)
    }
}
