package es.joshluq.encryptionkit.domain.model

/**
 * Result of biometric-authenticated encryption.
 * Contains both the authenticated [ciphertext] and the [iv] (initialization vector) required for decryption.
 */
data class BiometricCryptoResult(
    val ciphertext: ByteArray,
    val iv: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BiometricCryptoResult
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
