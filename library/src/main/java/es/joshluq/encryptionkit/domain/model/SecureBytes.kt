package es.joshluq.encryptionkit.domain.model

import java.util.Arrays

/**
 * A wrapper for sensitive data (ByteArray) that implements [AutoCloseable].
 * It allows explicit clearing (zeroing) of the memory to prevent sensitive data leaks
 * in heap dumps.
 *
 * @property data The raw sensitive bytes.
 */
class SecureBytes(val data: ByteArray) : AutoCloseable {

    /**
     * Wipes the data by overwriting the array with zeros.
     * This makes the data unrecoverable from memory.
     */
    override fun close() {
        Arrays.fill(data, 0.toByte())
    }

    /**
     * Checks if the data has been wiped (all zeros).
     * Note: This is a best-effort check, as valid data could technically be all zeros.
     */
    fun isWiped(): Boolean {
        return data.all { it == 0.toByte() }
    }
}
