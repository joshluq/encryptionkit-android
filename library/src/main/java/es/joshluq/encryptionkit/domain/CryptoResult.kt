package es.joshluq.encryptionkit.domain

/**
 * Encapsulates the result of an encryption operation.
 *
 * @property ciphertext The encrypted data.
 * @property iv The initialization vector used for encryption (required for decryption).
 */
data class CryptoResult(
    val ciphertext: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CryptoResult

        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}
