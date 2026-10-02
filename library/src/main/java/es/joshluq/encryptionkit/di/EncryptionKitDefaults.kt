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

/**
 * Extension functions for [LoggerKit] to provide efficient logging with a tag and lambda message.
 * Message lambda avoids string allocation if logging is disabled.
 */
internal inline fun LoggerKit.v(
    tag: String = EncryptionKitDefaults.TAG,
    message: () -> String,
) = v(tag, message())

internal inline fun LoggerKit.d(
    tag: String = EncryptionKitDefaults.TAG,
    message: () -> String,
) = d(tag, message())

internal inline fun LoggerKit.i(
    tag: String = EncryptionKitDefaults.TAG,
    message: () -> String,
) = i(tag, message())

internal inline fun LoggerKit.w(
    tag: String = EncryptionKitDefaults.TAG,
    throwable: Throwable? = null,
    message: () -> String,
) = w(tag, message(), throwable)

internal inline fun LoggerKit.w(
    throwable: Throwable,
    message: () -> String,
) = w(EncryptionKitDefaults.TAG, message(), throwable)

internal inline fun LoggerKit.e(
    tag: String = EncryptionKitDefaults.TAG,
    throwable: Throwable? = null,
    message: () -> String,
) = e(tag, message(), throwable)

internal inline fun LoggerKit.e(
    throwable: Throwable,
    message: () -> String,
) = e(EncryptionKitDefaults.TAG, message(), throwable)
