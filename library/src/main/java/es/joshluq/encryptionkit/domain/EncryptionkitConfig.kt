package es.joshluq.encryptionkit.domain

/**
 * Configuration class for EncryptionkitManager.
 */
data class EncryptionkitConfig(
    val alias: String = "encryption_kit_default_key",
    val useStrongBox: Boolean = false,
    val requireUserAuth: Boolean = false
)
