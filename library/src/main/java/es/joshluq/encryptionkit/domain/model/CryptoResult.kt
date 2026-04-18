package es.joshluq.encryptionkit.domain.model

/**
 * Encapsulates the result of a symmetric encryption operation.
 *
 * @property ciphertext The encrypted data as a byte array.
 * @property iv The Initialization Vector (IV) used during the encryption process.
 *              This value is required to decrypt the ciphertext and should be stored alongside it.
 */
data class CryptoResult(
    val ciphertext: ByteArray,
    val iv: ByteArray
) {
    /**
     * Converts the ciphertext to a Hexadecimal string.
     */
    fun toHexString(): String = ciphertext.joinToString("") { "%02x".format(it) }

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
