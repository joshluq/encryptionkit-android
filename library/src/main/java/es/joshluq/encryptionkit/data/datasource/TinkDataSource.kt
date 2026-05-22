package es.joshluq.encryptionkit.data.datasource

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import es.joshluq.foundationkit.log.LoggerKit
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.concurrent.ConcurrentHashMap

/**
 * DataSource responsible for managing Google Tink KeysetHandles and Aead primitives.
 * It provides a cache for Aead primitives to improve performance.
 */
internal class TinkDataSource(
    private val context: Context,
    private val logger: LoggerKit
) {
    companion object {
        private const val TAG = "TinkDataSource"
    }

    private val aeadCache = ConcurrentHashMap<String, Aead>()

    init {
        try {
            AeadConfig.register()
            logger.d(TAG, "Tink AeadConfig registered successfully.")
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, "Failed to register Tink AeadConfig", e)
        }
    }

    fun getAead(alias: String): Aead {
        return aeadCache.getOrPut(alias) {
            createAead(alias)
        }
    }

    private fun createAead(alias: String): Aead {
        try {
            val keysetHandle = AndroidKeysetManager.Builder()
                .withSharedPref(context, "keyset_$alias", "tink_prefs_$alias")
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri("android-keystore://$alias")
                .build()
                .keysetHandle
            return keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, "Failed to initialize AndroidKeysetManager for alias $alias", e)
            throw e
        } catch (e: IOException) {
            logger.e(TAG, "IO error initializing AndroidKeysetManager for alias $alias", e)
            throw e
        }
    }
}
