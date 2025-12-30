package es.joshluq.encryptionkit.domain.model

/**
 * Custom exception thrown when a cryptographic operation fails.
 */
class CryptoException(
    message: String,
    cause: Throwable? = null,
    val reason: Reason = Reason.UNKNOWN
) : Exception(message, cause) {

    /**
     * Categorizes the specific reason for the failure, allowing the UI/Consumer to react appropriately.
     */
    enum class Reason {
        /** Failed to generate the cryptographic key in the Keystore. */
        KEY_GENERATION_FAILED,

        /** The key was permanently invalidated (e.g., new biometric enrollment).
         * User must re-authenticate/reset keys. */
        KEY_PERMANENTLY_INVALIDATED,

        /** The user needs to authenticate (Biometric/PIN) to unlock the key. */
        USER_NOT_AUTHENTICATED,

        /** The key is considered weak or compromised. */
        WEAK_KEY,

        /** The public key fingerprint (hash) did not match the expected pinned value. Possible MITM. */
        PUBLIC_KEY_PINNING_FAILURE,

        /** General cryptographic failure (padding, block size, etc.). */
        OPERATION_FAILED,

        /** Key not found in Keystore. */
        KEY_NOT_FOUND,

        /** Certificate file missing. */
        CERTIFICATE_NOT_FOUND,

        /** Unknown or unclassified error. */
        UNKNOWN
    }
}
