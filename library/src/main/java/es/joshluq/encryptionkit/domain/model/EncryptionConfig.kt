package es.joshluq.encryptionkit.domain.model

/**
 * Configuration data model.
 */
data class EncryptionConfig(
    val alias: String,
    val useStrongBox: Boolean,
    val requireUserAuth: Boolean
)
