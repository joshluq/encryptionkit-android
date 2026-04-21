package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration model for the encryption library.
 *
 * @property alias Key alias in Android Keystore.
 * @property useStrongBox Prefer StrongBox (Secure Element).
 * @property requireUserAuth Require Biometric/PIN authentication.
 * @property publicKeyHash Optional SHA-256 hash (Hex) of the expected public key for pinning validation.
 * @property certificatePathProvider Provider for the X.509 certificate path.
 */
data class EncryptionConfig(
    val alias: String,
    val useStrongBox: Boolean,
    val requireUserAuth: Boolean,
    val publicKeyHash: String? = null,
    val certificatePathProvider: CertificatePathProvider? = null
) : ManagerConfig {

    companion object {
        /**
         * DSL entry point for creating an [EncryptionConfig] instance.
         */
        inline fun build(block: Builder.() -> Unit): EncryptionConfig =
            Builder().apply(block).build()
    }

    /**
     * Builder class for creating [EncryptionConfig] instances with Kotlin DSL support.
     */
    class Builder {
        var alias: String = "encryption_kit_default_key"
        var useStrongBox: Boolean = false
        var requireUserAuth: Boolean = false
        var publicKeyHash: String? = null
        var certificatePathProvider: CertificatePathProvider? = null

        fun setAlias(alias: String) = apply { this.alias = alias }
        fun useStrongBox(useStrongBox: Boolean) = apply { this.useStrongBox = useStrongBox }
        fun setRequireUserAuthentication(require: Boolean) = apply { this.requireUserAuth = require }
        fun setCertificatePathProvider(provider: CertificatePathProvider) = apply {
            this.certificatePathProvider = provider
        }
        fun setPublicKeyPinning(sha256Hash: String) = apply { this.publicKeyHash = sha256Hash }

        fun build() = EncryptionConfig(
            alias = alias,
            useStrongBox = useStrongBox,
            requireUserAuth = requireUserAuth,
            publicKeyHash = publicKeyHash,
            certificatePathProvider = certificatePathProvider
        )
    }
}
