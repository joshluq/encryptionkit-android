package es.joshluq.encryptionkit.domain

/**
 * Interface defining core cryptographic operations.
 */
interface CryptoEngine {

    /**
     * Encrypts the given data using the configured key.
     *
     * @param data The plaintext data to encrypt.
     * @return A [CryptoResult] containing the ciphertext and IV.
     * @throws CryptoException If encryption fails.
     */
    @Throws(CryptoException::class)
    fun encrypt(data: ByteArray): CryptoResult

    /**
     * Decrypts the given data.
     *
     * @param ciphertext The encrypted data.
     * @param iv The initialization vector used during encryption.
     * @return The decrypted plaintext.
     * @throws CryptoException If decryption fails (e.g., integrity check failure).
     */
    @Throws(CryptoException::class)
    fun decrypt(ciphertext: ByteArray, iv: ByteArray): ByteArray
}
