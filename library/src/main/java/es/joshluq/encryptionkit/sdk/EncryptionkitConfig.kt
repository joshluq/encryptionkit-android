package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.di.EncryptionkitDefaults
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration model for the encryption library.
 *
 * @property alias Key alias in Android Keystore.
 * @property context Application context.
 * @property publicKeyHash Optional SHA-256 hash (Hex) of the expected public key for pinning validation.
 * @property certificatePathProvider Provider for the X.509 certificate path.
 * @property logger The logger instance for the SDK.
 */
data class EncryptionkitConfig(
    val alias: String,
    val context: android.content.Context,
    val publicKeyHash: String? = null,
    val certificatePathProvider: CertificatePathProvider,
    val logger: LoggerKit,
) : ManagerConfig {

    companion object {
        /**
         * DSL entry point for creating an [EncryptionkitConfig] instance.
         */
        inline fun build(context: android.content.Context, block: Builder.() -> Unit): EncryptionkitConfig =
            Builder(context).apply(block).build()
    }

    /**
     * Builder class for creating [EncryptionkitConfig] instances with Kotlin DSL support.
     */
    class Builder(val context: android.content.Context) {
        var alias: String = "encryption_kit_default_key"
        var publicKeyHash: String? = null
        var certificatePathProvider: CertificatePathProvider = EncryptionkitDefaults.emptyPathProvider
        var logger: LoggerKit = EncryptionkitDefaults.logger

        fun build() = EncryptionkitConfig(
            alias = alias,
            context = context,
            publicKeyHash = publicKeyHash,
            certificatePathProvider = certificatePathProvider,
            logger = logger
        )
    }
}
