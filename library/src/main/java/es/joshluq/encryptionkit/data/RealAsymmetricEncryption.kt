package es.joshluq.encryptionkit.data

import android.util.Base64
import es.joshluq.encryptionkit.domain.AsymmetricEncryption
import es.joshluq.encryptionkit.domain.CryptoException
import java.security.PublicKey
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AsymmetricEncryption] using RSA-OAEP.
 */
@Singleton
class RealAsymmetricEncryption @Inject constructor() : AsymmetricEncryption {

    companion object {
        private const val TRANSFORMATION = "RSA/ECB/OAEPPadding"
    }

    override fun encrypt(data: ByteArray, publicKey: PublicKey): ByteArray {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            
            // Requisito de Seguridad: RSA/ECB/OAEPWithSHA-256AndMGF1Padding
            // Usamos OAEPParameterSpec para asegurar SHA-256 tanto en el digest principal como en MGF1
            val oaepParams = OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
            )
            
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams)
            return cipher.doFinal(data)
        } catch (e: Exception) {
            throw CryptoException(
                "RSA encryption failed",
                e,
                CryptoException.ErrorType.ENCRYPTION_FAILED
            )
        }
    }

    override fun encryptToBase64(plaintext: String, publicKey: PublicKey): String {
        val encryptedBytes = encrypt(plaintext.toByteArray(Charsets.UTF_8), publicKey)
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }
}
