package es.joshluq.encryptionkit.data.datasource

import android.content.Context
import androidx.core.content.edit
import com.google.crypto.tink.Aead
import com.google.crypto.tink.DeterministicAead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.Mac
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.daead.DeterministicAeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.mac.MacConfig
import com.google.crypto.tink.signature.SignatureConfig
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import es.joshluq.encryptionkit.di.d
import es.joshluq.encryptionkit.di.e
import es.joshluq.encryptionkit.di.i
import es.joshluq.encryptionkit.di.w
import es.joshluq.foundationkit.log.LoggerKit
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.concurrent.ConcurrentHashMap

/**
 * DataSource responsible for managing Google Tink KeysetHandles, Aead, StreamingAead, DeterministicAead,
 * PublicKeySign, PublicKeyVerify, and Mac primitives.
 * It provides thread-safe caches for primitives to maximize performance.
 */
internal class TinkDataSource(
    private val context: Context,
    private val logger: LoggerKit,
) {
    companion object {
        private const val TAG = "TinkDataSource"
    }

    private val aeadCache = ConcurrentHashMap<String, Aead>()
    private val streamingAeadCache = ConcurrentHashMap<String, StreamingAead>()
    private val daeadCache = ConcurrentHashMap<String, DeterministicAead>()
    private val signCache = ConcurrentHashMap<String, PublicKeySign>()
    private val verifyCache = ConcurrentHashMap<String, PublicKeyVerify>()
    private val macCache = ConcurrentHashMap<String, Mac>()

    init {
        try {
            AeadConfig.register()
            StreamingAeadConfig.register()
            DeterministicAeadConfig.register()
            SignatureConfig.register()
            MacConfig.register()
            logger.d(TAG) { "Tink configurations registered successfully." }
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Failed to register Tink configs" }
        }
    }

    fun getAead(alias: String): Aead =
        aeadCache.getOrPut(alias) {
            try {
                createAead(alias)
            } catch (e: Exception) {
                logger.w(TAG, e) { "Initial Aead creation failed for $alias. Attempting recovery..." }
                recover(alias)
                createAead(alias)
            }
        }

    private fun createAead(alias: String): Aead {
        try {
            val keysetHandle =
                AndroidKeysetManager
                    .Builder()
                    .withSharedPref(context, "keyset_$alias", "tink_prefs_$alias")
                    .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                    .withMasterKeyUri("android-keystore://$alias")
                    .build()
                    .keysetHandle
            return keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Failed to initialize AndroidKeysetManager for alias $alias" }
            throw e
        } catch (e: IOException) {
            logger.e(TAG, e) { "IO error initializing AndroidKeysetManager for alias $alias" }
            throw e
        }
    }

    fun getStreamingAead(alias: String): StreamingAead =
        streamingAeadCache.getOrPut(alias) {
            try {
                createStreamingAead(alias)
            } catch (e: Exception) {
                logger.w(TAG, e) { "Initial StreamingAead creation failed for $alias. Attempting recovery..." }
                recover(alias)
                createStreamingAead(alias)
            }
        }

    private fun createStreamingAead(alias: String): StreamingAead {
        try {
            val keysetHandle =
                AndroidKeysetManager
                    .Builder()
                    .withSharedPref(context, "keyset_stream_$alias", "tink_prefs_stream_$alias")
                    .withKeyTemplate(KeyTemplates.get("AES256_GCM_HKDF_4KB"))
                    .withMasterKeyUri("android-keystore://$alias")
                    .build()
                    .keysetHandle
            return keysetHandle.getPrimitive(RegistryConfiguration.get(), StreamingAead::class.java)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Failed to initialize AndroidKeysetManager for streaming alias $alias" }
            throw e
        } catch (e: IOException) {
            logger.e(TAG, e) { "IO error initializing AndroidKeysetManager for streaming alias $alias" }
            throw e
        }
    }

    fun getDeterministicAead(alias: String): DeterministicAead =
        daeadCache.getOrPut(alias) {
            try {
                createDeterministicAead(alias)
            } catch (e: Exception) {
                logger.w(TAG, e) { "Initial DeterministicAead creation failed for $alias. Attempting recovery..." }
                recover(alias)
                createDeterministicAead(alias)
            }
        }

    private fun createDeterministicAead(alias: String): DeterministicAead {
        try {
            val keysetHandle =
                AndroidKeysetManager
                    .Builder()
                    .withSharedPref(context, "keyset_daead_$alias", "tink_prefs_daead_$alias")
                    .withKeyTemplate(KeyTemplates.get("AES256_SIV"))
                    .withMasterKeyUri("android-keystore://$alias")
                    .build()
                    .keysetHandle
            return keysetHandle.getPrimitive(RegistryConfiguration.get(), DeterministicAead::class.java)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Failed to initialize AndroidKeysetManager for deterministic alias $alias" }
            throw e
        } catch (e: IOException) {
            logger.e(TAG, e) { "IO error initializing AndroidKeysetManager for deterministic alias $alias" }
            throw e
        }
    }

    fun rotateAead(alias: String) {
        logger.i(TAG) { "Rotating primary key for AEAD alias: $alias" }
        try {
            val manager =
                AndroidKeysetManager
                    .Builder()
                    .withSharedPref(context, "keyset_$alias", "tink_prefs_$alias")
                    .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                    .withMasterKeyUri("android-keystore://$alias")
                    .build()
            manager.add(KeyTemplates.get("AES256_GCM"))
            val keysetHandle = manager.keysetHandle
            val newKeyId = keysetHandle.getAt(keysetHandle.size() - 1).id
            manager.setPrimary(newKeyId)
            aeadCache.remove(alias)
            logger.i(TAG) { "Successfully rotated primary key for AEAD alias: $alias to keyId: $newKeyId" }
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Failed to rotate key for alias $alias" }
            throw e
        } catch (e: IOException) {
            logger.e(TAG, e) { "IO error during key rotation for alias $alias" }
            throw e
        }
    }

    fun getPublicKeySign(alias: String): PublicKeySign =
        signCache.getOrPut(alias) {
            try {
                createPublicKeySign(alias)
            } catch (e: Exception) {
                logger.w(TAG, e) { "Initial PublicKeySign creation failed for $alias. Attempting recovery..." }
                recover(alias)
                createPublicKeySign(alias)
            }
        }

    private fun createPublicKeySign(alias: String): PublicKeySign {
        try {
            val keysetHandle =
                AndroidKeysetManager
                    .Builder()
                    .withSharedPref(context, "keyset_sign_$alias", "tink_prefs_sign_$alias")
                    .withKeyTemplate(KeyTemplates.get("ECDSA_P256"))
                    .withMasterKeyUri("android-keystore://$alias")
                    .build()
                    .keysetHandle
            return keysetHandle.getPrimitive(RegistryConfiguration.get(), PublicKeySign::class.java)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Failed to initialize AndroidKeysetManager for sign alias $alias" }
            throw e
        } catch (e: IOException) {
            logger.e(TAG, e) { "IO error initializing AndroidKeysetManager for sign alias $alias" }
            throw e
        }
    }

    fun getPublicKeyVerify(alias: String): PublicKeyVerify =
        verifyCache.getOrPut(alias) {
            try {
                createPublicKeyVerify(alias)
            } catch (e: Exception) {
                logger.w(TAG, e) { "Initial PublicKeyVerify creation failed for $alias. Attempting recovery..." }
                recover(alias)
                createPublicKeyVerify(alias)
            }
        }

    private fun createPublicKeyVerify(alias: String): PublicKeyVerify {
        try {
            val keysetHandle =
                AndroidKeysetManager
                    .Builder()
                    .withSharedPref(context, "keyset_sign_$alias", "tink_prefs_sign_$alias")
                    .withKeyTemplate(KeyTemplates.get("ECDSA_P256"))
                    .withMasterKeyUri("android-keystore://$alias")
                    .build()
                    .keysetHandle
            return keysetHandle.publicKeysetHandle.getPrimitive(RegistryConfiguration.get(), PublicKeyVerify::class.java)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Failed to initialize AndroidKeysetManager for verify alias $alias" }
            throw e
        } catch (e: IOException) {
            logger.e(TAG, e) { "IO error initializing AndroidKeysetManager for verify alias $alias" }
            throw e
        }
    }

    fun getMac(alias: String): Mac =
        macCache.getOrPut(alias) {
            try {
                createMac(alias)
            } catch (e: Exception) {
                logger.w(TAG, e) { "Initial Mac creation failed for $alias. Attempting recovery..." }
                recover(alias)
                createMac(alias)
            }
        }

    private fun createMac(alias: String): Mac {
        try {
            val keysetHandle =
                AndroidKeysetManager
                    .Builder()
                    .withSharedPref(context, "keyset_mac_$alias", "tink_prefs_mac_$alias")
                    .withKeyTemplate(KeyTemplates.get("HMAC_SHA256_256BITTAG"))
                    .withMasterKeyUri("android-keystore://$alias")
                    .build()
                    .keysetHandle
            return keysetHandle.getPrimitive(RegistryConfiguration.get(), Mac::class.java)
        } catch (e: GeneralSecurityException) {
            logger.e(TAG, e) { "Failed to initialize AndroidKeysetManager for mac alias $alias" }
            throw e
        } catch (e: IOException) {
            logger.e(TAG, e) { "IO error initializing AndroidKeysetManager for mac alias $alias" }
            throw e
        }
    }

    fun deleteAead(alias: String) {
        logger.i(TAG) { "Deleting Tink keysets and cache for alias: $alias" }
        aeadCache.remove(alias)
        streamingAeadCache.remove(alias)
        daeadCache.remove(alias)
        signCache.remove(alias)
        verifyCache.remove(alias)
        macCache.remove(alias)
        clearStorage(alias)
    }

    private fun recover(alias: String) {
        logger.i(TAG) { "Recovering Tink state for alias: $alias" }
        aeadCache.remove(alias)
        streamingAeadCache.remove(alias)
        daeadCache.remove(alias)
        signCache.remove(alias)
        verifyCache.remove(alias)
        macCache.remove(alias)
        clearStorage(alias)
    }

    private fun clearStorage(alias: String) {
        runCatching {
            // 1. Clear SharedPreferences for all primitives
            listOf(
                "tink_prefs_$alias",
                "tink_prefs_stream_$alias",
                "tink_prefs_daead_$alias",
                "tink_prefs_sign_$alias",
                "tink_prefs_mac_$alias",
            ).forEach { prefName ->
                context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit { clear() }
            }

            // 2. Delete from Keystore
            val keyStore =
                java.security.KeyStore
                    .getInstance("AndroidKeyStore")
                    .apply { load(null) }
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
        }.onFailure { e ->
            logger.e(TAG, e) { "Failed to clear Tink storage for alias: $alias" }
        }
    }
}
