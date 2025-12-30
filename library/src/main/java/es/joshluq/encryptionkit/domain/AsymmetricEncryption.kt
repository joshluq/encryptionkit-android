package es.joshluq.encryptionkit.domain

import java.security.PublicKey

/**
 * Interface for asymmetric encryption operations.
 */
interface AsymmetricEncryption {

    /**
     * Encrypts data using a public key.
     *
     * @param data The plaintext data to encrypt.
     * @param publicKey The [PublicKey] to use for encryption.
     * @return The encrypted data.
     * @throws CryptoException If encryption fails.
     */
    @Throws(CryptoException::class)
    fun encrypt(data: ByteArray, publicKey: PublicKey): ByteArray

    /**
     * Encrypts a string using a public key and returns the result as a Base64 string.
     *
     * @param plaintext The string to encrypt.
     * @param publicKey The [PublicKey] to use for encryption.
     * @return The encrypted data as a Base64 encoded string.
     * @throws CryptoException If encryption fails.
     */
    @Throws(CryptoException::class)
    fun encryptToBase64(plaintext: String, publicKey: PublicKey): String
}
