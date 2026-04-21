package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.data.datasource.FileDataSource
import es.joshluq.encryptionkit.data.datasource.KeystoreDataSource
import es.joshluq.encryptionkit.data.repository.EncryptionRepositoryImpl
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.encryptionkit.domain.usecase.DecryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.DeleteKeyUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptAsymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.GetSecurityLevelUseCase
import es.joshluq.encryptionkit.domain.usecase.HashDataUseCase
import es.joshluq.encryptionkit.domain.usecase.InitializeLibraryUseCase

/**
 * Internal Dependency Injection container for the Encryption module.
 * Following the Internal Dependency Graph pattern.
 */
internal open class EncryptionkitComponent(val config: EncryptionkitConfig) {

    private val keystoreDataSource: KeystoreDataSource by lazy {
        KeystoreDataSource()
    }

    private val fileDataSource: FileDataSource by lazy {
        val certProvider = config.certificatePathProvider ?: object : CertificatePathProvider {
            override fun getCertificatePath(): String? = null
        }
        FileDataSource(certProvider)
    }

    private val repository: EncryptionRepositoryImpl by lazy {
        EncryptionRepositoryImpl(keystoreDataSource, fileDataSource)
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
