package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.di.EncryptionkitDefaults
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration model for the encryption library.
 *
 * @property alias Key alias in Android Keystore.
 * @property useStrongBox Prefer StrongBox (Secure Element).
 * @property requireUserAuth Require Biometric/PIN authentication.
 * @property publicKeyHash Optional SHA-256 hash (Hex) of the expected public key for pinning validation.
 * @property certificatePathProvider Provider for the X.509 certificate path.
 * @property logger The logger instance for the SDK.
 */
data class EncryptionkitConfig(
    val alias: String,
    val useStrongBox: Boolean,
    val requireUserAuth: Boolean,
    val publicKeyHash: String? = null,
    val certificatePathProvider: CertificatePathProvider,
    val logger: Loggerkit,
) : ManagerConfig {

    companion object {
        /**
         * DSL entry point for creating an [EncryptionkitConfig] instance.
         */
        inline fun build(block: Builder.() -> Unit): EncryptionkitConfig =
            Builder().apply(block).build()
    }

    /**
     * Builder class for creating [EncryptionkitConfig] instances with Kotlin DSL support.
     */
    class Builder {
        var alias: String = "encryption_kit_default_key"
        var useStrongBox: Boolean = false
        var requireUserAuth: Boolean = false
        var publicKeyHash: String? = null
        var certificatePathProvider: CertificatePathProvider = EncryptionkitDefaults.emptyPathProvider
        var logger: Loggerkit = EncryptionkitDefaults.logger

        fun build() = EncryptionkitConfig(
            alias = alias,
            useStrongBox = useStrongBox,
            requireUserAuth = requireUserAuth,
            publicKeyHash = publicKeyHash,
            certificatePathProvider = certificatePathProvider,
            logger = logger
        )
    }
}
