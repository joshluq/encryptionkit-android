package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.data.datasource.FileDataSource
import es.joshluq.encryptionkit.data.datasource.KeystoreDataSource
import es.joshluq.encryptionkit.data.repository.EncryptionRepositoryImpl
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.encryptionkit.domain.usecase.DecryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.DeleteKeyUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptAsymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.GetSecurityLevelUseCase
import es.joshluq.encryptionkit.domain.usecase.HashDataUseCase
import es.joshluq.encryptionkit.domain.usecase.InitializeLibraryUseCase
import kotlinx.coroutines.runBlocking

/**
 * Main entry point for the EncryptionKit SDK.
 * This class provides a high-level facade for all cryptographic operations, including
 * symmetric encryption (AES-GCM), asymmetric encryption (RSA-OAEP), and hashing.
 *
 * Instances of this class are obtained via the [EncryptionkitManager.Builder].
 */
class EncryptionkitManager internal constructor(
    private val encryptSymmetricUseCase: EncryptSymmetricUseCase,
    private val decryptSymmetricUseCase: DecryptSymmetricUseCase,
    private val encryptAsymmetricUseCase: EncryptAsymmetricUseCase,
    private val getSecurityLevelUseCase: GetSecurityLevelUseCase,
    private val deleteKeyUseCase: DeleteKeyUseCase,
    private val hashDataUseCase: HashDataUseCase,
    private val config: EncryptionConfig
) {

    /**
     * Encrypts the provided byte array using the configured symmetric key (AES-GCM).
     */
    suspend fun encrypt(data: ByteArray): Result<CryptoResult> =
        encryptSymmetricUseCase(EncryptSymmetricUseCase.Input(data, config))
            .map { it.result }
            .mapFailure()

    /**
     * Encrypts the provided secure data wrapper.
     */
    suspend fun encrypt(secureData: SecureBytes): Result<CryptoResult> =
        encryptSymmetricUseCase(EncryptSymmetricUseCase.Input(secureData.data, config))
            .map { it.result }
            .mapFailure()

    /**
     * Decrypts the provided ciphertext using the configured symmetric key (AES-GCM).
     */
    suspend fun decrypt(ciphertext: ByteArray, iv: ByteArray): Result<ByteArray> =
        decryptSymmetricUseCase(DecryptSymmetricUseCase.Input(ciphertext, iv, config))
            .map { it.data }
            .mapFailure()

    /**
     * Convenience method to decrypt data from a [CryptoResult].
     */
    suspend fun decrypt(result: CryptoResult): Result<ByteArray> =
        decrypt(result.ciphertext, result.iv)

    /**
     * Encrypts the provided data using RSA-OAEP with a public key.
     */
    suspend fun encryptWithPublicKey(data: ByteArray): Result<ByteArray> =
        encryptAsymmetricUseCase(EncryptAsymmetricUseCase.Input(data = data, config = config))
            .map { it.data }
            .mapFailure()

    /**
     * Retrieves the current hardware security level of the symmetric key.
     */
    suspend fun getSecurityLevel(): Result<SecurityLevel> =
        getSecurityLevelUseCase(GetSecurityLevelUseCase.Input(config.alias))
            .map { it.level }
            .mapFailure()

    /**
     * Deletes the symmetric key associated with this instance.
     */
    suspend fun deleteKey(): Result<Unit> =
        deleteKeyUseCase(DeleteKeyUseCase.Input(config.alias))
            .map { }
            .mapFailure()

    /**
     * Generates a cryptographic hash of the provided data.
     */
    suspend fun hash(data: ByteArray, algorithm: String = "SHA-256"): Result<ByteArray> =
        hashDataUseCase(HashDataUseCase.Input(data, algorithm))
            .map { it.data }
            .mapFailure()

    /**
     * Generates a cryptographic hash of the provided text and returns it as a Hex string.
     */
    suspend fun hashToHex(text: String, algorithm: String = "SHA-256"): Result<String> =
        hashDataUseCase(HashDataUseCase.Input(text.toByteArray(), algorithm))
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
    class Builder {
        private var alias: String = "encryption_kit_default_key"
        private var useStrongBox: Boolean = false
        private var requireUserAuth: Boolean = false
        private var certificatePathProvider: CertificatePathProvider? = null
        private var publicKeyHash: String? = null

        fun setAlias(alias: String) = apply { this.alias = alias }
        fun useStrongBox(useStrongBox: Boolean) = apply { this.useStrongBox = useStrongBox }
        fun setRequireUserAuthentication(require: Boolean) = apply { this.requireUserAuth = require }
        fun setCertificatePathProvider(
            provider: CertificatePathProvider
        ) = apply { this.certificatePathProvider = provider }
        fun setPublicKeyPinning(sha256Hash: String) = apply { this.publicKeyHash = sha256Hash }

        fun build(): EncryptionkitManager {
            val config = EncryptionConfig(alias, useStrongBox, requireUserAuth, publicKeyHash)
            val keystoreDataSource = KeystoreDataSource()
            val certProvider = certificatePathProvider ?: object : CertificatePathProvider {
                override fun getCertificatePath(): String? = null
            }
            val fileDataSource = FileDataSource(certProvider)
            val repository = EncryptionRepositoryImpl(keystoreDataSource, fileDataSource)

            val initializeLibraryUseCase = InitializeLibraryUseCase(repository)

            runBlocking {
                initializeLibraryUseCase(InitializeLibraryUseCase.Input(config))
            }

            return EncryptionkitManager(
                encryptSymmetricUseCase = EncryptSymmetricUseCase(repository),
                decryptSymmetricUseCase = DecryptSymmetricUseCase(repository),
                encryptAsymmetricUseCase = EncryptAsymmetricUseCase(repository),
                getSecurityLevelUseCase = GetSecurityLevelUseCase(repository),
                deleteKeyUseCase = DeleteKeyUseCase(repository),
                hashDataUseCase = HashDataUseCase(repository),
                config = config
            )
        }
    }
}
