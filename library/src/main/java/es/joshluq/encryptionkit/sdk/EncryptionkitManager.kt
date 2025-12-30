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
import es.joshluq.encryptionkit.domain.usecase.*

/**
 * Main entry point for the EncryptionKit SDK.
 * This class provides a high-level facade for all cryptographic operations, including
 * symmetric encryption (AES-GCM), asymmetric encryption (RSA-OAEP), and hashing.
 *
 * Instances of this class are obtained via the [EncryptionkitManager.Builder].
 */
class EncryptionkitManager private constructor(
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
     *
     * @param data The plaintext data to be encrypted.
     * @return A [CryptoResult] containing the ciphertext and the Initialization Vector (IV).
     * @throws CryptoException If the encryption process fails.
     */
    fun encrypt(data: ByteArray): CryptoResult {
        return encryptSymmetricUseCase(data, config)
    }

    /**
     * Encrypts the provided secure data wrapper.
     */
    fun encrypt(secureData: SecureBytes): CryptoResult {
        return encryptSymmetricUseCase(secureData, config)
    }

    /**
     * Decrypts the provided ciphertext using the configured symmetric key (AES-GCM).
     *
     * @param ciphertext The encrypted data.
     * @param iv The Initialization Vector used during encryption.
     * @return The original plaintext data.
     * @throws CryptoException If decryption fails (e.g., integrity check failure or invalid key).
     */
    fun decrypt(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        return decryptSymmetricUseCase(ciphertext, iv, config)
    }
    
    /**
     * Convenience method to decrypt data from a [CryptoResult].
     *
     * @param result The result object containing both ciphertext and IV.
     * @return The original plaintext data.
     * @throws CryptoException If decryption fails.
     */
    fun decrypt(result: CryptoResult): ByteArray {
        return decryptSymmetricUseCase(result.ciphertext, result.iv, config)
    }

    /**
     * Encrypts the provided data using RSA-OAEP with a public key.
     * The public key is retrieved from the certificate provided via [CertificatePathProvider].
     *
     * @param data The plaintext data to be encrypted.
     * @return The encrypted data.
     * @throws CryptoException If the public key cannot be loaded or encryption fails.
     */
    suspend fun encryptWithPublicKey(data: ByteArray): ByteArray {
        return encryptAsymmetricUseCase(data, config)
    }

    suspend fun encryptWithPublicKey(secureData: SecureBytes): ByteArray {
        return encryptAsymmetricUseCase(secureData, config)
    }

    /**
     * Retrieves the current hardware security level of the symmetric key.
     *
     * @return [SecurityLevel.STRONGBOX], [SecurityLevel.TRUSTED_ENVIRONMENT], or [SecurityLevel.SOFTWARE].
     */
    fun getSecurityLevel(): SecurityLevel {
        return getSecurityLevelUseCase(config.alias)
    }

    /**
     * Deletes the symmetric key associated with this instance from the Android Keystore.
     * WARNING: Data encrypted with this key will become permanently unrecoverable.
     */
    fun deleteKey() {
        deleteKeyUseCase(config.alias)
    }

    /**
     * Generates a cryptographic hash of the provided data.
     *
     * @param data The input data to hash.
     * @param algorithm The hashing algorithm to use (default: "SHA-256"). Supports "MD5".
     * @return The hash digest as a byte array.
     */
    fun hash(data: ByteArray, algorithm: String = "SHA-256"): ByteArray {
        return hashDataUseCase(data, algorithm)
    }
    
    /**
     * Generates a cryptographic hash of the provided text and returns it as a Hex string.
     *
     * @param text The input string to hash.
     * @param algorithm The hashing algorithm to use (default: "SHA-256").
     * @return The hash digest as a hexadecimal string.
     */
    fun hashToHex(text: String, algorithm: String = "SHA-256"): String {
        return hashDataUseCase.toHexString(text.toByteArray(), algorithm)
    }

    /**
     * Builder class for creating [EncryptionkitManager] instances.
     * No Context is required for initialization.
     */
    class Builder {
        private var alias: String = "encryption_kit_default_key"
        private var useStrongBox: Boolean = false
        private var requireUserAuth: Boolean = false
        private var certificatePathProvider: CertificatePathProvider? = null
        private var publicKeyHash: String? = null

        /**
         * Sets the alias (name) for the key in the Android Keystore.
         * Using different aliases allows multiple distinct keys to coexist.
         *
         * @param alias The unique identifier for the key.
         */
        fun setAlias(alias: String) = apply { this.alias = alias }

        /**
         * Requests the use of a Secure Element (StrongBox) for key storage.
         * StrongBox offers the highest level of security but may have performance trade-offs.
         *
         * @param useStrongBox True to prefer StrongBox, false otherwise.
         */
        fun useStrongBox(useStrongBox: Boolean) = apply { this.useStrongBox = useStrongBox }

        /**
         * Enforces user authentication (Biometrics/PIN) for key usage.
         *
         * @param require True to require authentication for every cryptographic operation.
         */
        fun setRequireUserAuthentication(require: Boolean) = apply { this.requireUserAuth = require }
        
        /**
         * Sets a provider for loading X.509 certificates from the file system.
         * Required for [encryptWithPublicKey] operations.
         *
         * @param provider An implementation of [CertificatePathProvider].
         */
        fun setCertificatePathProvider(provider: CertificatePathProvider) = apply { 
            this.certificatePathProvider = provider 
        }

        /**
         * Sets the expected SHA-256 hash (Hex) of the public key for pinning validation.
         * If the loaded key does not match this hash, asymmetric encryption will fail.
         */
        fun setPublicKeyPinning(sha256Hash: String) = apply {
            this.publicKeyHash = sha256Hash
        }

        /**
         * Builds and initializes the [EncryptionkitManager] instance.
         * This ensures the cryptographic keys are generated and ready for use.
         */
        fun build(): EncryptionkitManager {
            val config = EncryptionConfig(alias, useStrongBox, requireUserAuth, publicKeyHash)
            
            // Manual Dependency Injection Wiring (Composition Root)
            val keystoreDataSource = KeystoreDataSource()
            
            val certProvider = certificatePathProvider ?: object : CertificatePathProvider {
                override fun getCertificatePath(): String? = null
            }
            val fileDataSource = FileDataSource(certProvider)
            
            val repository = EncryptionRepositoryImpl(keystoreDataSource, fileDataSource)
            
            // Initialize Key
            val initializeLibraryUseCase = InitializeLibraryUseCase(repository)
            initializeLibraryUseCase(config)

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
