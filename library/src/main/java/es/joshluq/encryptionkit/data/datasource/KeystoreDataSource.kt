package es.joshluq.encryptionkit.data.datasource

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import java.security.Key
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

/**
 * DataSource responsible for low-level interactions with the Android Keystore.
 */
class KeystoreDataSource {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    fun ensureKeyExists(config: EncryptionConfig) {
        if (!keyStore.containsAlias(config.alias)) {
            generateKey(config)
        }
    }

    private fun generateKey(config: EncryptionConfig) {
        try {
            generateKeyInternal(config, config.useStrongBox)
        } catch (e: Exception) {
            if (config.useStrongBox) {
                // Fallback attempt
                try {
                    generateKeyInternal(config, false)
                } catch (fallbackEx: Exception) {
                    throw CryptoException("Key gen fallback failed", fallbackEx, CryptoException.ErrorType.KEY_GENERATION_FAILED)
                }
            } else {
                throw CryptoException("Key gen failed", e, CryptoException.ErrorType.KEY_GENERATION_FAILED)
            }
        }
    }

    private fun generateKeyInternal(config: EncryptionConfig, useStrongBox: Boolean) {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val builder = KeyGenParameterSpec.Builder(
            config.alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)

        if (config.requireUserAuth) {
            builder.setUserAuthenticationRequired(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            }
        }

        if (useStrongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setIsStrongBoxBacked(true)
        }

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    fun getKey(alias: String): Key? {
        return keyStore.getKey(alias, null)
    }

    fun deleteKey(alias: String) {
        keyStore.deleteEntry(alias)
    }

    fun getSecurityLevel(alias: String): SecurityLevel {
        val key = getKey(alias) as? SecretKey ?: return SecurityLevel.SOFTWARE
        return try {
            val factory = SecretKeyFactory.getInstance(key.algorithm, "AndroidKeyStore")
            val keyInfo = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                when (keyInfo.securityLevel) {
                    KeyProperties.SECURITY_LEVEL_STRONGBOX -> SecurityLevel.STRONGBOX
                    KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> SecurityLevel.TRUSTED_ENVIRONMENT
                    else -> SecurityLevel.SOFTWARE
                }
            } else {
                if (keyInfo.isInsideSecureHardware) SecurityLevel.TRUSTED_ENVIRONMENT else SecurityLevel.SOFTWARE
            }
        } catch (e: Exception) {
            SecurityLevel.SOFTWARE
        }
    }
}
