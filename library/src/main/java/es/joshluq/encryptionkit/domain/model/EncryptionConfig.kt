package es.joshluq.encryptionkit.domain.model

/**
 * Internal configuration model for the encryption library.
 *
 * @property alias The alias (name) used to identify the key in the Android Keystore.
 * @property useStrongBox Whether the key should be stored in the Secure Element (StrongBox).
 * @property requireUserAuth Whether the key requires user authentication (biometrics) to be used.
 */
data class EncryptionConfig(
    val alias: String,
    val useStrongBox: Boolean,
    val requireUserAuth: Boolean
)
