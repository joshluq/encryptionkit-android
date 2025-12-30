package es.joshluq.encryptionkit.domain

import java.security.PublicKey

/**
 * Provider interface for retrieving a public key for asymmetric encryption.
 */
interface PublicKeyProvider {
    /**
     * Retrieves the public key.
     * @return The [PublicKey] instance or null if not available.
     */
    suspend fun getPublicKey(): PublicKey?
}
