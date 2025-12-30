package es.joshluq.encryptionkit.domain.model

/**
 * Custom exception thrown when a cryptographic operation fails.
 *
 * @property message Detailed error message.
 * @property cause The underlying cause of the exception (if any).
 * @property errorType The specific category of the error.
 */
class CryptoException(
    message: String,
    cause: Throwable? = null,
    val errorType: ErrorType
) : Exception(message, cause) {

    /**
     * Categorizes the type of cryptographic failure.
     */
    enum class ErrorType {
        /** Failed to generate the cryptographic key in the Keystore. */
        KEY_GENERATION_FAILED,
        
        /** The encryption operation failed. */
        ENCRYPTION_FAILED,
        
        /** The decryption operation failed (e.g. invalid key or corrupted data). */
        DECRYPTION_FAILED,
        
        /** The requested key alias was not found in the Keystore. */
        KEY_NOT_FOUND,
        
        /** The user failed to authenticate (Biometric/PIN) for a key requiring authentication. */
        USER_NOT_AUTHENTICATED,
        
        /** StrongBox was requested but is not available on this device. */
        STRONG_BOX_UNAVAILABLE,
        
        /** The requested algorithm is not supported by the device. */
        INVALID_ALGORITHM,
        
        /** The certificate file required for asymmetric operations could not be found. */
        CERTIFICATE_NOT_FOUND,
        
        /** An unknown or unclassified error occurred. */
        UNKNOWN
    }
}
