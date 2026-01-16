package es.joshluq.encryptionkit.domain.model

/**
 * Configuration model for the encryption library.
 *
 * @property alias Key alias in Android Keystore.
 * @property useStrongBox Prefer StrongBox (Secure Element).
 * @property requireUserAuth Require Biometric/PIN authentication.
 * @property publicKeyHash Optional SHA-256 hash (Hex) of the expected public key for pinning validation.
 */
data class EncryptionConfig(
    val alias: String,
    val useStrongBox: Boolean,
    val requireUserAuth: Boolean,
    val publicKeyHash: String? = null
)
