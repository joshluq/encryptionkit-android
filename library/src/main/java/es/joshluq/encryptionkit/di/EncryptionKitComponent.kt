package es.joshluq.encryptionkit.di

import es.joshluq.encryptionkit.data.datasource.TinkDataSource
import es.joshluq.encryptionkit.data.repository.EncryptionRepositoryImpl
import es.joshluq.encryptionkit.domain.usecase.ComputeMacUseCase
import es.joshluq.encryptionkit.domain.usecase.DecryptDeterministicUseCase
import es.joshluq.encryptionkit.domain.usecase.DecryptStreamUseCase
import es.joshluq.encryptionkit.domain.usecase.DecryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.DeleteKeyUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptAsymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptDeterministicUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptStreamUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.GetSecurityLevelUseCase
import es.joshluq.encryptionkit.domain.usecase.HashDataUseCase
import es.joshluq.encryptionkit.domain.usecase.InitializeLibraryUseCase
import es.joshluq.encryptionkit.domain.usecase.RotateKeyUseCase
import es.joshluq.encryptionkit.domain.usecase.SignDataUseCase
import es.joshluq.encryptionkit.domain.usecase.VerifyMacUseCase
import es.joshluq.encryptionkit.domain.usecase.VerifySignatureUseCase
import es.joshluq.encryptionkit.sdk.EncryptionKitConfig
import es.joshluq.foundationkit.log.LoggerKit

/**
 * Internal Dependency Injection component
 * Following the Internal Dependency Graph pattern.
 */
internal class EncryptionKitComponent(
    val config: EncryptionKitConfig,
) {
    val logger: LoggerKit by lazy { config.logger }

    private val tinkDataSource: TinkDataSource by lazy {
        TinkDataSource(config.context, logger)
    }

    private val repository: EncryptionRepositoryImpl by lazy {
        EncryptionRepositoryImpl(tinkDataSource, config.certificatePathProvider, logger)
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

    val encryptStreamUseCase: EncryptStreamUseCase by lazy {
        EncryptStreamUseCase(repository)
    }

    val decryptStreamUseCase: DecryptStreamUseCase by lazy {
        DecryptStreamUseCase(repository)
    }

    val encryptDeterministicUseCase: EncryptDeterministicUseCase by lazy {
        EncryptDeterministicUseCase(repository)
    }

    val decryptDeterministicUseCase: DecryptDeterministicUseCase by lazy {
        DecryptDeterministicUseCase(repository)
    }

    val rotateKeyUseCase: RotateKeyUseCase by lazy {
        RotateKeyUseCase(repository)
    }

    val signDataUseCase: SignDataUseCase by lazy {
        SignDataUseCase(repository)
    }

    val verifySignatureUseCase: VerifySignatureUseCase by lazy {
        VerifySignatureUseCase(repository)
    }

    val computeMacUseCase: ComputeMacUseCase by lazy {
        ComputeMacUseCase(repository)
    }

    val verifyMacUseCase: VerifyMacUseCase by lazy {
        VerifyMacUseCase(repository)
    }
}
