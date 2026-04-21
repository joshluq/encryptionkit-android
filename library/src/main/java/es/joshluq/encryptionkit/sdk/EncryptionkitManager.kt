package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.usecase.DecryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.DeleteKeyUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptAsymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.GetSecurityLevelUseCase
import es.joshluq.encryptionkit.domain.usecase.HashDataUseCase
import es.joshluq.encryptionkit.domain.usecase.InitializeLibraryUseCase
import es.joshluq.foundationkit.manager.Manager
import es.joshluq.foundationkit.manager.ManagerBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Main entry point for the EncryptionKit SDK.
 * This class acts as a **Facade**, providing a simplified interface to complex cryptographic
 * operations like symmetric encryption (AES-GCM), asymmetric encryption (RSA-OAEP), and hashing.
 */
class EncryptionkitManager internal constructor(
    private val componentFactory: (EncryptionkitConfig) -> EncryptionkitComponent = { EncryptionkitComponent(it) }
) : Manager<EncryptionkitConfig>() {

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var component: EncryptionkitComponent

    /**
     * Supported hashing algorithms for the SDK.
     */
    enum class HashAlgorithm(val value: String) {
        SHA_256("SHA-256"),
        MD5("MD5")
    }

    fun initialize(config: EncryptionkitConfig) {
        this.config = config
        this.component = componentFactory(config)
        managerScope.launch {
            component.initializeLibraryUseCase(InitializeLibraryUseCase.Input(config))
        }
    }

    /**
     * Encrypts the provided secure data wrapper.
     */
    suspend fun encrypt(secureData: SecureBytes): Result<CryptoResult> =
        component.encryptSymmetricUseCase(EncryptSymmetricUseCase.Input(secureData.data, config))
            .map { it.result }
            .mapFailure()

    /**
     * Decrypts the provided ciphertext using the configured symmetric key (AES-GCM).
     */
    suspend fun decrypt(ciphertext: ByteArray, iv: ByteArray): Result<ByteArray> =
        component.decryptSymmetricUseCase(DecryptSymmetricUseCase.Input(ciphertext, iv, config))
            .map { it.data }
            .mapFailure()

    /**
     * Encrypts the provided data using RSA-OAEP with a public key.
     */
    suspend fun encryptWithPublicKey(data: ByteArray): Result<ByteArray> =
        component.encryptAsymmetricUseCase(EncryptAsymmetricUseCase.Input(data = data, config = config))
            .map { it.data }
            .mapFailure()

    /**
     * Retrieves the current hardware security level of the symmetric key.
     */
    suspend fun getSecurityLevel(): Result<SecurityLevel> =
        component.getSecurityLevelUseCase(GetSecurityLevelUseCase.Input(config.alias))
            .map { it.level }
            .mapFailure()

    /**
     * Deletes the symmetric key associated with this instance.
     */
    suspend fun deleteKey(): Result<Unit> =
        component.deleteKeyUseCase(DeleteKeyUseCase.Input(config.alias))
            .map { }
            .mapFailure()

    /**
     * Generates a cryptographic hash of the provided data using a safe [HashAlgorithm].
     */
    suspend fun hash(
        data: ByteArray,
        algorithm: HashAlgorithm = HashAlgorithm.SHA_256
    ): Result<ByteArray> =
        component.hashDataUseCase(HashDataUseCase.Input(data, algorithm.value))
            .map { it.data }
            .mapFailure()

    /**
     * Generates a cryptographic hash of the provided text and returns it as a Hex string.
     */
    suspend fun hashToHex(
        text: String,
        algorithm: HashAlgorithm = HashAlgorithm.SHA_256
    ): Result<String> =
        component.hashDataUseCase(HashDataUseCase.Input(text.toByteArray(), algorithm.value))
            .map { output -> output.data.joinToString("") { "%02x".format(it) } }
            .mapFailure()

    private fun <T> Result<T>.mapFailure(): Result<T> =
        fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(mapToCryptoException(it)) }
        )

    private fun mapToCryptoException(e: Throwable): CryptoException {
        return e as? CryptoException
            ?: CryptoException(e.message ?: "Unknown error", e, CryptoException.Reason.UNKNOWN)
    }

    /**
     * Builder class for creating [EncryptionkitManager] instances.
     */
    class Builder : ManagerBuilder<EncryptionkitConfig> {
        override fun build(config: EncryptionkitConfig): EncryptionkitManager {
            return EncryptionkitManager().apply {
                initialize(config)
            }
        }
    }
}
