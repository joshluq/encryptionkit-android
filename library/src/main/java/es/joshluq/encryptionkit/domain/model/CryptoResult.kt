package es.joshluq.encryptionkit.domain.model

/**
 * Encapsulates the result of a symmetric encryption operation.
 *
 * @property ciphertext The encrypted data as a byte array.
 */
data class CryptoResult(
    val ciphertext: ByteArray
) {
    /**
     * Converts the ciphertext to a Hexadecimal string.
     */
    fun toHexString(): String = ciphertext.joinToString("") { "%02x".format(it) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CryptoResult
        return ciphertext.contentEquals(other.ciphertext)
    }

    override fun hashCode(): Int {
        return ciphertext.contentHashCode()
    }
}
