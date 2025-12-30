package es.joshluq.encryptionkit.domain

/**
 * Represents the security level of the cryptographic key.
 */
enum class SecurityLevel {
    /**
     * Key is stored in the Android Keystore but strictly in software.
     * Vulnerable to root attacks if the OS is compromised.
     */
    SOFTWARE,

    /**
     * Key is stored in the Trusted Execution Environment (TEE).
     * Isolated from the main OS.
     */
    TRUSTED_ENVIRONMENT,

    /**
     * Key is stored in a dedicated Secure Element (StrongBox).
     * Highest level of security, resistant to physical attacks.
     */
    STRONGBOX
}
