package es.joshluq.encryptionkit.di

import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.foundationkit.log.LoggerDefaults
import es.joshluq.foundationkit.log.LoggerKit

/**
 * Default implementations and constants for EncryptionKit.
 * Strictly follows the 'Defaults' pattern for internal configuration.
 */
internal object EncryptionKitDefaults {
    /**
     * Default tag for logging.
     */
    const val TAG = "EncryptionKit"

    /**
     * Default [LoggerKit] instance for the SDK.
     */
    val logger: LoggerKit by lazy {
        LoggerKit
            .Builder()
            .addProvider(LoggerDefaults.defaultLogProvider(tagPrefix = TAG))
            .build()
    }

    /**
     * Default [CertificatePathProvider] that returns a null path.
     */
    val emptyPathProvider: CertificatePathProvider by lazy {
        object : CertificatePathProvider {
            override fun getCertificatePath(): String? = null
        }
    }
}