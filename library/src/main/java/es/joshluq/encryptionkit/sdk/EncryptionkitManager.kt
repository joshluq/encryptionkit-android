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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Encrypts the provided byte array using the configured symmetric key (AES-GCM).
     */
    fun encrypt(
        data: ByteArray,
        onSuccess: (CryptoResult) -> Unit,
        onError: (CryptoException) -> Unit = {}
    ) {
        encryptSymmetricUseCase(EncryptSymmetricUseCase.Input(data, config))
            .onEach { onSuccess(it.result) }
            .catch { e -> onError(mapToCryptoException(e)) }
            .launchIn(sdkScope)
    }

    /**
     * Encrypts the provided secure data wrapper.
     */
    fun encrypt(
        secureData: SecureBytes,
        onSuccess: (CryptoResult) -> Unit,
        onError: (CryptoException) -> Unit = {}
    ) {
        encryptSymmetricUseCase(EncryptSymmetricUseCase.Input(secureData.data, config))
            .onEach { onSuccess(it.result) }
            .catch { e -> onError(mapToCryptoException(e)) }
            .launchIn(sdkScope)
    }

    /**
     * Decrypts the provided ciphertext using the configured symmetric key (AES-GCM).
     */
    fun decrypt(
        ciphertext: ByteArray,
        iv: ByteArray,
        onSuccess: (ByteArray) -> Unit,
        onError: (CryptoException) -> Unit = {}
    ) {
        decryptSymmetricUseCase(DecryptSymmetricUseCase.Input(ciphertext, iv, config))
            .onEach { onSuccess(it.data) }
            .catch { e -> onError(mapToCryptoException(e)) }
            .launchIn(sdkScope)
    }

    /**
     * Convenience method to decrypt data from a [CryptoResult].
     */
    fun decrypt(
        result: CryptoResult,
        onSuccess: (ByteArray) -> Unit,
        onError: (CryptoException) -> Unit = {}
    ) {
        decrypt(result.ciphertext, result.iv, onSuccess, onError)
    }

    /**
     * Encrypts the provided data using RSA-OAEP with a public key.
     */
    fun encryptWithPublicKey(
        data: ByteArray,
        onSuccess: (ByteArray) -> Unit,
        onError: (CryptoException) -> Unit = {}
    ) {
        encryptAsymmetricUseCase(EncryptAsymmetricUseCase.Input(data = data, config = config))
            .onEach { onSuccess(it.data) }
            .catch { e -> onError(mapToCryptoException(e)) }
            .launchIn(sdkScope)
    }

    /**
     * Retrieves the current hardware security level of the symmetric key.
     */
    fun getSecurityLevel(
        onSuccess: (SecurityLevel) -> Unit,
        onError: (CryptoException) -> Unit = {}
    ) {
        getSecurityLevelUseCase(GetSecurityLevelUseCase.Input(config.alias))
            .onEach { onSuccess(it.level) }
            .catch { e -> onError(mapToCryptoException(e)) }
            .launchIn(sdkScope)
    }

    /**
     * Deletes the symmetric key associated with this instance.
     */
    fun deleteKey(onComplete: () -> Unit = {}, onError: (CryptoException) -> Unit = {}) {
        deleteKeyUseCase(DeleteKeyUseCase.Input(config.alias))
            .onEach { onComplete() }
            .catch { e -> onError(mapToCryptoException(e)) }
            .launchIn(sdkScope)
    }

    /**
     * Generates a cryptographic hash of the provided data.
     */
    fun hash(
        data: ByteArray,
        algorithm: String = "SHA-256",
        onSuccess: (ByteArray) -> Unit,
        onError: (CryptoException) -> Unit = {}
    ) {
        hashDataUseCase(HashDataUseCase.Input(data, algorithm))
            .onEach { onSuccess(it.data) }
            .catch { e -> onError(mapToCryptoException(e)) }
            .launchIn(sdkScope)
    }

    /**
     * Generates a cryptographic hash of the provided text and returns it as a Hex string.
     */
    fun hashToHex(
        text: String,
        algorithm: String = "SHA-256",
        onSuccess: (String) -> Unit,
        onError: (CryptoException) -> Unit = {}
    ) {
        hashDataUseCase(HashDataUseCase.Input(text.toByteArray(), algorithm))
            .onEach { output ->
                val hexString = output.data.joinToString("") { "%02x".format(it) }
                onSuccess(hexString)
            }
            .catch { e -> onError(mapToCryptoException(e)) }
            .launchIn(sdkScope)
    }

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
                initializeLibraryUseCase(InitializeLibraryUseCase.Input(config)).collect()
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
