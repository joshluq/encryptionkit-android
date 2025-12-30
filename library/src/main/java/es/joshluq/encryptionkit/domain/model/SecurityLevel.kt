package es.joshluq.encryptionkit.domain.model

/**
 * Represents the security level of the cryptographic key.
 */
enum class SecurityLevel {
    SOFTWARE,
    TRUSTED_ENVIRONMENT,
    STRONGBOX
}
