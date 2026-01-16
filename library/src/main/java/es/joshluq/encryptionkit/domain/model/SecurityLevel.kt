package es.joshluq.encryptionkit.domain.model

/**
 * Represents the hardware security level where the cryptographic key is stored.
 */
enum class SecurityLevel {
    /**
     * The key is stored in the Android Keystore but strictly in the Android OS software layer.
     * This is the least secure level and may be vulnerable if the OS is compromised (e.g. root access).
     */
    SOFTWARE,

    /**
     * The key is stored in the Trusted Execution Environment (TEE).
     * The key material is isolated from the main OS memory, providing good protection against software attacks.
     */
    TRUSTED_ENVIRONMENT,

    /**
     * The key is stored in a dedicated Secure Element (StrongBox).
     * This provides the highest level of security, resistant to physical tampering and side-channel attacks.
     */
    STRONGBOX
}
