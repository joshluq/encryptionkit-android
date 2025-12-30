package es.joshluq.encryptionkit.data

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Base64
import es.joshluq.encryptionkit.domain.CryptoException
import es.joshluq.encryptionkit.domain.KeyManager
import es.joshluq.encryptionkit.domain.SecurityLevel
import java.security.Key
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [KeyManager] using the Android Keystore System.
 */
@Singleton
class AndroidKeyManager @Inject constructor() : KeyManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    override fun generateKey(alias: String, requireUserAuth: Boolean, useStrongBox: Boolean) {
        try {
            generateKeyInternal(alias, requireUserAuth, useStrongBox)
        } catch (e: Exception) {
            // If StrongBox was requested and failed, try falling back to TEE (Standard)
            // This handles cases where StrongBox is not available on the device.
            if (useStrongBox) {
                try {
                    generateKeyInternal(alias, requireUserAuth, false)
                } catch (fallbackException: Exception) {
                    throw CryptoException(
                        "Failed to generate key (fallback failed)",
                        fallbackException,
                        CryptoException.ErrorType.KEY_GENERATION_FAILED
                    )
                }
            } else {
                throw CryptoException(
                    "Failed to generate key",
                    e,
                    CryptoException.ErrorType.KEY_GENERATION_FAILED
                )
            }
        }
    }

    private fun generateKeyInternal(alias: String, requireUserAuth: Boolean, useStrongBox: Boolean) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)

        if (requireUserAuth) {
            builder.setUserAuthenticationRequired(true)
            // By default, validity duration is -1, requiring auth for every use.
            // This enables integration with BiometricPrompt.
            
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

    override fun getKey(alias: String): Key? {
        return try {
            keyStore.getKey(alias, null)
        } catch (e: Exception) {
            null
        }
    }

    override fun hasKey(alias: String): Boolean {
        return try {
            keyStore.containsAlias(alias)
        } catch (e: Exception) {
            false
        }
    }

    override fun deleteKey(alias: String) {
        try {
            keyStore.deleteEntry(alias)
        } catch (e: Exception) {
            // Ignore if already deleted or error
        }
    }

    override fun getSecurityLevel(alias: String): SecurityLevel {
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
                // Fallback for older APIs: we can at least detect if it's hardware backed
                if (keyInfo.isInsideSecureHardware) {
                    SecurityLevel.TRUSTED_ENVIRONMENT
                } else {
                    SecurityLevel.SOFTWARE
                }
            }
        } catch (e: Exception) {
            SecurityLevel.SOFTWARE
        }
    }

    override fun getPublicKeyFromBase64(base64PublicKey: String): PublicKey {
        return try {
            val keyBytes = Base64.decode(base64PublicKey, Base64.DEFAULT)
            val spec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(spec)
        } catch (e: Exception) {
            throw CryptoException(
                "Invalid public key format",
                e,
                CryptoException.ErrorType.INVALID_ALGORITHM
            )
        }
    }
}
