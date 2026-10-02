package es.joshluq.encryptionkit.sdk

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import es.joshluq.encryptionkit.data.provider.SecureDataStoreProvider
import es.joshluq.encryptionkit.di.EncryptionKitComponent
import es.joshluq.encryptionkit.di.e
import es.joshluq.encryptionkit.di.i
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.model.SecurityLevel
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
import es.joshluq.foundationkit.manager.ContextManagerFactory
import es.joshluq.foundationkit.manager.Manager
import es.joshluq.foundationkit.manager.ManagerBuilder
import es.joshluq.foundationkit.provider.SerializerProvider
import es.joshluq.foundationkit.provider.StorageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Main entry point for the EncryptionKit SDK.
 * This class acts as a **Facade**, providing a simplified interface to complex cryptographic
 * operations like symmetric encryption (AES-GCM), asymmetric encryption (RSA-OAEP), and hashing.
 */
class EncryptionKit internal constructor(
    private val componentFactory: (EncryptionKitConfig) -> EncryptionKitComponent = {
        EncryptionKitComponent(
            it,
        )
    },
) : Manager<EncryptionKitConfig>() {
    companion object : ContextManagerFactory<EncryptionKit, EncryptionKitConfig, EncryptionKitBuilder> {
        private const val TAG = "EncryptionKitManager"

        override val builder: ManagerBuilder<EncryptionKitConfig, EncryptionKit> = Builder()

        override fun createBuilder(context: android.content.Context): EncryptionKitBuilder = EncryptionKitBuilder(context)
    }

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var component: EncryptionKitComponent

    /**
     * Supported hashing algorithms for the SDK.
     */
    enum class HashAlgorithm(
        val value: String,
    ) {
        SHA_256("SHA-256"),
        MD5("MD5"),
    }

    internal fun initialize(config: EncryptionKitConfig) {
        val appContext = config.context.applicationContext
        this.config = config.copy(context = appContext)
        this.component = componentFactory(this.config)
        component.logger.i(TAG) { "Initializing EncryptionKit SDK with alias: ${config.alias}" }
        managerScope.launch {
            val input = InitializeLibraryUseCase.Input(config.alias)
            component.initializeLibraryUseCase(input)
        }
    }

    /**
     * Encrypts the provided secure data wrapper.
     */
    suspend fun encrypt(
        secureData: SecureBytes,
        associatedData: ByteArray = ByteArray(0),
    ): Result<CryptoResult> {
        check(isConfigInitialized()) {
            "EncryptionKitManager is not initialized"
        }
        val input = EncryptSymmetricUseCase.Input(secureData.data, config.alias, associatedData)
        return component
            .encryptSymmetricUseCase(input)
            .map { it.result }
            .mapFailure()
    }

    /**
     * Decrypts the provided ciphertext using the configured symmetric key (AES-GCM).
     */
    suspend fun decrypt(
        ciphertext: ByteArray,
        associatedData: ByteArray = ByteArray(0),
    ): Result<SecureBytes> {
        check(isConfigInitialized()) {
            "EncryptionKitManager is not initialized"
        }
        val input = DecryptSymmetricUseCase.Input(ciphertext, config.alias, associatedData)
        return component
            .decryptSymmetricUseCase(input)
            .map { SecureBytes(it.data) }
            .mapFailure()
    }

    /**
     * Encrypts the provided data using RSA-OAEP with a public key.
     */
    suspend fun encryptWithPublicKey(data: ByteArray): Result<ByteArray> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val publicKeyHash =
            config.publicKeyHash ?: return Result.failure(Exception("Public key hash not set"))

        val input = EncryptAsymmetricUseCase.Input(data = data, publicKeyHash = publicKeyHash)
        return component
            .encryptAsymmetricUseCase(input)
            .map { it.data }
            .mapFailure()
    }

    /**
     * Retrieves the current hardware security level of the symmetric key.
     */
    suspend fun getSecurityLevel(): Result<SecurityLevel> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val input = GetSecurityLevelUseCase.Input(config.alias)
        return component
            .getSecurityLevelUseCase(input)
            .map { it.level }
            .mapFailure()
    }

    /**
     * Deletes the symmetric key associated with this instance.
     */
    suspend fun deleteKey(): Result<Unit> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val input = DeleteKeyUseCase.Input(config.alias)
        return component
            .deleteKeyUseCase(input)
            .map { }
            .mapFailure()
    }

    /**
     * Rotates the primary encryption key for the Keyset.
     * Generates a fresh AES-GCM key and sets it as the primary key.
     * Previous keys are retained in the Keyset to ensure seamless decryption of historic data.
     *
     * @param alias Optional alias to rotate. If null, the configured [EncryptionKitConfig.alias] is used.
     */
    suspend fun rotateKey(alias: String? = null): Result<Unit> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val targetAlias = alias ?: config.alias
        component.logger.i(TAG) { "Rotating primary key for alias: $targetAlias" }
        val input = RotateKeyUseCase.Input(targetAlias)
        return component
            .rotateKeyUseCase(input)
            .map { }
            .mapFailure()
    }

    /**
     * Generates a cryptographic hash of the provided data using a safe [HashAlgorithm].
     */
    suspend fun hash(
        data: ByteArray,
        algorithm: HashAlgorithm = HashAlgorithm.SHA_256,
    ): Result<ByteArray> =
        component
            .hashDataUseCase(HashDataUseCase.Input(data, algorithm.value))
            .map { it.data }
            .mapFailure()

    /**
     * Generates a cryptographic hash of the provided text and returns it as a Hex string.
     */
    suspend fun hashToHex(
        text: String,
        algorithm: HashAlgorithm = HashAlgorithm.SHA_256,
    ): Result<String> =
        component
            .hashDataUseCase(HashDataUseCase.Input(text.toByteArray(), algorithm.value))
            .map { output -> output.data.joinToString("") { "%02x".format(it) } }
            .mapFailure()

    /**
     * Signs the provided data using an asymmetric digital signature (ECDSA NIST P-256 with SHA-256).
     *
     * @param data Payload to sign.
     * @param alias Optional key alias. If null, the configured [EncryptionKitConfig.alias] is used.
     * @return Result containing the DER-encoded digital signature bytes.
     */
    suspend fun sign(
        data: ByteArray,
        alias: String? = null,
    ): Result<ByteArray> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val targetAlias = alias ?: config.alias
        val input = SignDataUseCase.Input(data, targetAlias)
        return component
            .signDataUseCase(input)
            .map { it.signature }
            .mapFailure()
    }

    /**
     * Verifies an asymmetric digital signature against the provided data (ECDSA NIST P-256 with SHA-256).
     *
     * @param data Original payload.
     * @param signature DER-encoded digital signature to verify.
     * @param alias Optional key alias. If null, the configured [EncryptionKitConfig.alias] is used.
     * @return Result containing true if signature is valid, false otherwise.
     */
    suspend fun verifySignature(
        data: ByteArray,
        signature: ByteArray,
        alias: String? = null,
    ): Result<Boolean> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val targetAlias = alias ?: config.alias
        val input = VerifySignatureUseCase.Input(data, signature, targetAlias)
        return component
            .verifySignatureUseCase(input)
            .map { it.isValid }
            .mapFailure()
    }

    /**
     * Computes a Message Authentication Code (MAC) using HMAC-SHA256 (256-bit tag)
     * to guarantee message authenticity and integrity.
     *
     * @param data Payload to authenticate.
     * @param alias Optional key alias. If null, the configured [EncryptionKitConfig.alias] is used.
     * @return Result containing the 256-bit MAC tag.
     */
    suspend fun computeMac(
        data: ByteArray,
        alias: String? = null,
    ): Result<ByteArray> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val targetAlias = alias ?: config.alias
        val input = ComputeMacUseCase.Input(data, targetAlias)
        return component
            .computeMacUseCase(input)
            .map { it.tag }
            .mapFailure()
    }

    /**
     * Verifies a Message Authentication Code (MAC) in constant time to prevent timing attacks.
     *
     * @param data Original payload.
     * @param mac MAC tag to verify.
     * @param alias Optional key alias. If null, the configured [EncryptionKitConfig.alias] is used.
     * @return Result containing true if MAC is valid, false otherwise.
     */
    suspend fun verifyMac(
        data: ByteArray,
        mac: ByteArray,
        alias: String? = null,
    ): Result<Boolean> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val targetAlias = alias ?: config.alias
        val input = VerifyMacUseCase.Input(data, mac, targetAlias)
        return component
            .verifyMacUseCase(input)
            .map { it.isValid }
            .mapFailure()
    }

    /**
     * Encrypts data from an [InputStream] to an [OutputStream] using Streaming AEAD (AES256-GCM-HKDF-4KB).
     * Ideal for large files (videos, photos, databases) to prevent OutOfMemoryError.
     */
    suspend fun encryptStream(
        inputStream: java.io.InputStream,
        outputStream: java.io.OutputStream,
        associatedData: ByteArray = ByteArray(0),
    ): Result<Unit> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val input =
            EncryptStreamUseCase.Input(
                inputStream = inputStream,
                outputStream = outputStream,
                alias = config.alias,
                associatedData = associatedData,
            )
        return component
            .encryptStreamUseCase(input)
            .map { }
            .mapFailure()
    }

    /**
     * Decrypts ciphertext from an [InputStream] to an [OutputStream] using Streaming AEAD (AES256-GCM-HKDF-4KB).
     */
    suspend fun decryptStream(
        inputStream: java.io.InputStream,
        outputStream: java.io.OutputStream,
        associatedData: ByteArray = ByteArray(0),
    ): Result<Unit> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val input =
            DecryptStreamUseCase.Input(
                inputStream = inputStream,
                outputStream = outputStream,
                alias = config.alias,
                associatedData = associatedData,
            )
        return component
            .decryptStreamUseCase(input)
            .map { }
            .mapFailure()
    }

    /**
     * Encrypts the provided secure data deterministically using AES256-SIV (RFC 5297).
     * Ideal for searchable fields or database indexing where identical plaintext must produce identical ciphertext.
     */
    suspend fun encryptDeterministic(
        secureData: SecureBytes,
        associatedData: ByteArray = ByteArray(0),
    ): Result<CryptoResult> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val input = EncryptDeterministicUseCase.Input(secureData.data, config.alias, associatedData)
        return component
            .encryptDeterministicUseCase(input)
            .map { it.result }
            .mapFailure()
    }

    /**
     * Decrypts the provided deterministic ciphertext using AES256-SIV.
     */
    suspend fun decryptDeterministic(
        ciphertext: ByteArray,
        associatedData: ByteArray = ByteArray(0),
    ): Result<SecureBytes> {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        val input = DecryptDeterministicUseCase.Input(ciphertext, config.alias, associatedData)
        return component
            .decryptDeterministicUseCase(input)
            .map { SecureBytes(it.data) }
            .mapFailure()
    }

    /**
     * Creates a secure storage provider that encrypts data using Tink and saves it to DataStore.
     */
    fun createSecureStorage(
        dataStore: DataStore<Preferences>,
        serializerProvider: SerializerProvider,
    ): StorageProvider {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        return SecureDataStoreProvider(
            dataStore = dataStore,
            serializerProvider = serializerProvider,
            encryptionKit = this,
        )
    }

    /**
     * Creates a Room [es.joshluq.encryptionkit.data.provider.room.EncryptedStringConverter] instance configured with this [EncryptionKit].
     *
     * @param associatedData Optional associated data for AEAD binding.
     * @param deterministic If true, uses AES256-SIV for searchable database columns.
     */
    fun createEncryptedStringConverter(
        associatedData: ByteArray = ByteArray(0),
        deterministic: Boolean = false,
    ): es.joshluq.encryptionkit.data.provider.room.EncryptedStringConverter {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        return es.joshluq.encryptionkit.data.provider.room.EncryptedStringConverter(
            encryptionKit = this,
            associatedData = associatedData,
            deterministic = deterministic,
        )
    }

    /**
     * Creates a Room [es.joshluq.encryptionkit.data.provider.room.EncryptedByteArrayConverter] instance configured with this [EncryptionKit].
     *
     * @param associatedData Optional associated data for AEAD binding.
     * @param deterministic If true, uses AES256-SIV for searchable database columns.
     */
    fun createEncryptedByteArrayConverter(
        associatedData: ByteArray = ByteArray(0),
        deterministic: Boolean = false,
    ): es.joshluq.encryptionkit.data.provider.room.EncryptedByteArrayConverter {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        return es.joshluq.encryptionkit.data.provider.room.EncryptedByteArrayConverter(
            encryptionKit = this,
            associatedData = associatedData,
            deterministic = deterministic,
        )
    }

    /**
     * Creates a [es.joshluq.encryptionkit.data.provider.biometric.BiometricCryptoHelper] instance
     * for hardware-backed, biometric-authenticated cryptographic operations.
     */
    fun createBiometricCryptoHelper(): es.joshluq.encryptionkit.data.provider.biometric.BiometricCryptoHelper {
        check(isConfigInitialized()) {
            "EncryptionKit is not initialized"
        }
        return es.joshluq.encryptionkit.data.provider.biometric.BiometricCryptoHelper(
            logger = component.logger,
        )
    }

    private fun <T> Result<T>.mapFailure(): Result<T> =
        fold(
            onSuccess = { Result.success(it) },
            onFailure = {
                val exception = mapToCryptoException(it)
                component.logger.e(TAG, exception) { "Operation failed: ${exception.message}" }
                Result.failure(exception)
            },
        )

    private fun mapToCryptoException(e: Throwable): CryptoException =
        e as? CryptoException
            ?: CryptoException(e.message ?: "Unknown error", e, CryptoException.Reason.UNKNOWN)

    /**
     * Builder class for creating [EncryptionKit] instances.
     */
    class Builder : ManagerBuilder<EncryptionKitConfig, EncryptionKit> {
        override fun build(config: EncryptionKitConfig): EncryptionKit =
            EncryptionKit().apply {
                initialize(config)
            }
    }
}
