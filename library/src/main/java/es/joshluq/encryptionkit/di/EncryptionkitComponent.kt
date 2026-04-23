package es.joshluq.encryptionkit.di

import es.joshluq.encryptionkit.data.datasource.FileDataSource
import es.joshluq.encryptionkit.data.datasource.KeystoreDataSource
import es.joshluq.encryptionkit.data.repository.EncryptionRepositoryImpl
import es.joshluq.encryptionkit.domain.usecase.DecryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.DeleteKeyUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptAsymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.GetSecurityLevelUseCase
import es.joshluq.encryptionkit.domain.usecase.HashDataUseCase
import es.joshluq.encryptionkit.domain.usecase.InitializeLibraryUseCase
import es.joshluq.encryptionkit.sdk.EncryptionkitConfig
import es.joshluq.foundationkit.log.Loggerkit

/**
 * Internal Dependency Injection component
 * Following the Internal Dependency Graph pattern.
 */
internal open class EncryptionkitComponent(val config: EncryptionkitConfig) {

    open val logger: Loggerkit by lazy { config.logger }

    private val keystoreDataSource: KeystoreDataSource by lazy {
        KeystoreDataSource()
    }

    private val fileDataSource: FileDataSource by lazy {
        val certProvider = config.certificatePathProvider
        FileDataSource(certProvider)
    }

    private val repository: EncryptionRepositoryImpl by lazy {
        EncryptionRepositoryImpl(keystoreDataSource, fileDataSource, logger)
    }

    open val initializeLibraryUseCase: InitializeLibraryUseCase by lazy {
        InitializeLibraryUseCase(repository)
    }

    open val encryptSymmetricUseCase: EncryptSymmetricUseCase by lazy {
        EncryptSymmetricUseCase(repository)
    }

    open val decryptSymmetricUseCase: DecryptSymmetricUseCase by lazy {
        DecryptSymmetricUseCase(repository)
    }

    open val encryptAsymmetricUseCase: EncryptAsymmetricUseCase by lazy {
        EncryptAsymmetricUseCase(repository)
    }

    open val getSecurityLevelUseCase: GetSecurityLevelUseCase by lazy {
        GetSecurityLevelUseCase(repository)
    }

    open val deleteKeyUseCase: DeleteKeyUseCase by lazy {
        DeleteKeyUseCase(repository)
    }

    open val hashDataUseCase: HashDataUseCase by lazy {
        HashDataUseCase(repository)
    }
}
