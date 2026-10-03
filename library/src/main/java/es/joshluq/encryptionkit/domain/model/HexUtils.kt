package es.joshluq.encryptionkit.domain.model

/**
 * High-performance hexadecimal encoder and decoder.
 * Eliminates String.format allocations and intermediate collections using bitwise operations
 * and a static character lookup table.
 */
object HexUtils {
    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    /**
     * Converts a [ByteArray] to a lowercase hexadecimal string with a single [CharArray] allocation.
     * Time Complexity: O(N)
     * Space Complexity: O(N) single String allocation
     */
    fun encode(bytes: ByteArray): String {
        val result = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            result[i * 2] = HEX_CHARS[v ushr 4]
            result[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return String(result)
    }

    /**
     * Decodes a hexadecimal string back to a [ByteArray] without creating intermediate strings or lists.
     * Time Complexity: O(N)
     * Space Complexity: O(N) single ByteArray allocation
     */
    fun decode(hex: String): ByteArray {
        val cleanHex = hex.trim()
        val len = cleanHex.length
        require(len % 2 == 0) { "Hex string length must be even: $len" }

        val result = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            val high = Character.digit(cleanHex[i], 16)
            val low = Character.digit(cleanHex[i + 1], 16)
            require(high != -1 && low != -1) { "Invalid hex character at index $i: '${cleanHex[i]}${cleanHex[i + 1]}'" }
            result[i / 2] = ((high shl 4) + low).toByte()
            i += 2
        }
        return result
    }
}
