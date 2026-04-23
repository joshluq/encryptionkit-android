package es.joshluq.encryptionkit.di

import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.foundationkit.log.LoggerDefaults
import es.joshluq.foundationkit.log.Loggerkit

/**
 * Default implementations and constants for Encryptionkit.
 * Strictly follows the 'Defaults' pattern for internal configuration.
 */
internal object EncryptionkitDefaults {

    /**
     * Default tag for logging.
     */
    private const val TAG = "Encryptionkit"

    /**
     * Default [Loggerkit] instance for the SDK.
     */
    val logger: Loggerkit by lazy {
        Loggerkit.Builder()
            .setProvider(LoggerDefaults.defaultLogProvider(tagPrefix = TAG))
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
