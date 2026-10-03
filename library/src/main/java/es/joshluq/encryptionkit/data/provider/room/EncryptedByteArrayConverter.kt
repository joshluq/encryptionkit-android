package es.joshluq.encryptionkit.data.provider.room

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.sdk.EncryptionKit
import kotlinx.coroutines.runBlocking

/**
 * A Room [TypeConverter] that transparently encrypts and decrypts [ByteArray] columns (BLOB in SQLite).
 * Supports standard AEAD (AES-GCM) or deterministic encryption (AES-SIV) for searchable fields.
 */
@ProvidedTypeConverter
class EncryptedByteArrayConverter(
    private val encryptionKit: EncryptionKit,
    private val associatedData: ByteArray = ByteArray(0),
    private val deterministic: Boolean = false,
) {
    @TypeConverter
    fun fromPlaintext(plaintext: ByteArray?): ByteArray? {
        if (plaintext == null) return null
        return runBlocking {
            SecureBytes(plaintext).use { secureBytes ->
                val result =
                    if (deterministic) {
                        encryptionKit.encryptDeterministic(secureBytes, associatedData)
                    } else {
                        encryptionKit.encrypt(secureBytes, associatedData)
                    }
                result.getOrThrow().ciphertext
            }
        }
    }

    @TypeConverter
    fun toPlaintext(ciphertext: ByteArray?): ByteArray? {
        if (ciphertext == null) return null
        return runBlocking {
            val result =
                if (deterministic) {
                    encryptionKit.decryptDeterministic(ciphertext, associatedData)
                } else {
                    encryptionKit.decrypt(ciphertext, associatedData)
                }
            result.getOrThrow().data
        }
    }
}
