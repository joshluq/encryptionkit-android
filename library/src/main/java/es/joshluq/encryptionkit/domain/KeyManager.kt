package es.joshluq.encryptionkit.domain

import java.security.Key
import java.security.PublicKey

/**
 * Interface for managing cryptographic keys in the Android Keystore.
 */
interface KeyManager {

    /**
     * Generates a new cryptographic key.
     *
     * @param alias The alias to identify the key.
     * @param requireUserAuth Whether the key requires user authentication (biometrics) to be used.
     * @param useStrongBox Whether to prefer storing the key in StrongBox (Secure Element).
     * @throws CryptoException If key generation fails.
     */
    @Throws(CryptoException::class)
    fun generateKey(alias: String, requireUserAuth: Boolean, useStrongBox: Boolean)

    /**
     * Retrieves a key from the Keystore.
     *
     * @param alias The alias of the key.
     * @return The key instance or null if not found.
     */
    fun getKey(alias: String): Key?

    /**
     * Checks if a key exists in the Keystore.
     *
     * @param alias The alias of the key.
     */
    fun hasKey(alias: String): Boolean

    /**
     * Deletes a key from the Keystore.
     *
     * @param alias The alias of the key.
     */
    fun deleteKey(alias: String)

    /**
     * Returns the security level of the key (Software, TEE, or StrongBox).
     *
     * @param alias The alias of the key.
     */
    fun getSecurityLevel(alias: String): SecurityLevel

    /**
     * Converts a Base64 encoded X.509 public key string into a [PublicKey] object.
     *
     * @param base64PublicKey The Base64 encoded public key string.
     * @return The [PublicKey] instance.
     * @throws CryptoException If the string is not a valid public key.
     */
    @Throws(CryptoException::class)
    fun getPublicKeyFromBase64(base64PublicKey: String): PublicKey
}
