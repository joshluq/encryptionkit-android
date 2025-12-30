package es.joshluq.encryptionkit.domain

import java.security.PublicKey

/**
 * Repository interface for managing both local symmetric keys and external public keys.
 */
interface KeyRepository {
    /**
     * Retrieves an external public key for asymmetric encryption.
     */
    suspend fun getPublicKey(): PublicKey?

    /**
     * Ensures the local symmetric key exists, generating it if necessary.
     */
    fun isKeyReady(alias: String): Boolean

    /**
     * Generates a local symmetric key.
     */
    fun generateSymmetricKey(alias: String, requireUserAuth: Boolean, useStrongBox: Boolean)

    /**
     * Deletes the local symmetric key.
     */
    fun deleteSymmetricKey(alias: String)

    /**
     * Returns the security level of the local symmetric key.
     */
    fun getSecurityLevel(alias: String): SecurityLevel
}
