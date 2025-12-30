package es.joshluq.encryptionkit.domain

/**
 * Custom exception for cryptographic errors.
 */
class CryptoException(
    message: String,
    cause: Throwable? = null,
    val errorType: ErrorType
) : Exception(message, cause) {

    enum class ErrorType {
        KEY_GENERATION_FAILED,
        ENCRYPTION_FAILED,
        DECRYPTION_FAILED,
        KEY_NOT_FOUND,
        USER_NOT_AUTHENTICATED,
        STRONG_BOX_UNAVAILABLE,
        INVALID_ALGORITHM,
        UNKNOWN
    }
}
