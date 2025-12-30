package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.domain.CryptoResult
import es.joshluq.encryptionkit.domain.SecurityLevel
import es.joshluq.encryptionkit.domain.usecase.DecryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.DeleteKeyUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptAsymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.GetSecurityLevelUseCase
import es.joshluq.encryptionkit.domain.usecase.HashDataUseCase
import es.joshluq.encryptionkit.domain.usecase.InitializeLibraryUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main entry point for the Encryptionkit library.
 * Facade that orchestrates security operations using Use Cases.
 */
@Singleton
class EncryptionkitManager @Inject constructor(
    private val initializeLibraryUseCase: InitializeLibraryUseCase,
    private val encryptSymmetricUseCase: EncryptSymmetricUseCase,
    private val decryptSymmetricUseCase: DecryptSymmetricUseCase,
    private val encryptAsymmetricUseCase: EncryptAsymmetricUseCase,
    private val getSecurityLevelUseCase: GetSecurityLevelUseCase,
    private val deleteKeyUseCase: DeleteKeyUseCase,
    private val hashDataUseCase: HashDataUseCase
) {

    init {
        initializeLibraryUseCase()
    }

    /**
     * Encrypts the given data using AES-GCM (Symmetric).
     */
    fun encrypt(data: ByteArray): CryptoResult {
        return encryptSymmetricUseCase(data)
    }

    /**
     * Decrypts the given ciphertext (Symmetric).
     */
    fun decrypt(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        return decryptSymmetricUseCase(ciphertext, iv)
    }

    /**
     * Convenience method to decrypt a [CryptoResult] directly.
     */
    fun decrypt(result: CryptoResult): ByteArray {
        return decryptSymmetricUseCase(result.ciphertext, result.iv)
    }

    /**
     * Encrypts data using Asymmetric Encryption (RSA-OAEP) with a public key.
     */
    suspend fun encryptWithPublicKey(data: ByteArray): ByteArray {
        return encryptAsymmetricUseCase(data)
    }

    /**
     * Returns the security level of the current symmetric key.
     */
    fun getSecurityLevel(): SecurityLevel {
        return getSecurityLevelUseCase()
    }

    /**
     * Deletes the symmetric key associated with this instance.
     */
    fun deleteKey() {
        deleteKeyUseCase()
    }

    /**
     * Hashes data using SHA-256.
     */
    fun hash(data: ByteArray, algorithm: String = "SHA-256"): ByteArray {
        return hashDataUseCase(data, algorithm)
    }

    /**
     * Convenience method to hash a string to a hex string.
     */
    fun hashToHex(text: String, algorithm: String = "SHA-256"): String {
        return hashDataUseCase.toHexString(text.toByteArray(), algorithm)
    }
}
