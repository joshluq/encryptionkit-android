package es.joshluq.encryptionkit.data.provider.room

import android.util.Base64
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.sdk.EncryptionKit
import kotlinx.coroutines.runBlocking

/**
 * A Room [TypeConverter] that transparently encrypts and decrypts [String] columns.
 * Supports standard AEAD (AES-GCM) or deterministic encryption (AES-SIV) for searchable fields.
 */
@ProvidedTypeConverter
class EncryptedStringConverter(
    private val encryptionKit: EncryptionKit,
    private val associatedData: ByteArray = ByteArray(0),
    private val deterministic: Boolean = false,
) {
    @TypeConverter
    fun fromPlaintext(plaintext: String?): String? {
        if (plaintext == null) return null
        return runBlocking {
            SecureBytes(plaintext.toByteArray(Charsets.UTF_8)).use { secureBytes ->
                val result =
                    if (deterministic) {
                        encryptionKit.encryptDeterministic(secureBytes, associatedData)
                    } else {
                        encryptionKit.encrypt(secureBytes, associatedData)
                    }
                Base64.encodeToString(result.getOrThrow().ciphertext, Base64.NO_WRAP)
            }
        }
    }

    @TypeConverter
    fun toPlaintext(ciphertextBase64: String?): String? {
        if (ciphertextBase64 == null) return null
        return runBlocking {
            val ciphertext = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
            val result =
                if (deterministic) {
                    encryptionKit.decryptDeterministic(ciphertext, associatedData)
                } else {
                    encryptionKit.decrypt(ciphertext, associatedData)
                }
            result.getOrThrow().use { secureBytes ->
                String(secureBytes.data, Charsets.UTF_8)
            }
        }
    }
}
