package es.joshluq.encryptionkit.domain.model

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HexUtilsTest {
    @Test
    fun `encode should return empty string for empty byte array`() {
        val result = HexUtils.encode(byteArrayOf())
        assertEquals("", result)
    }

    @Test
    fun `encode should return valid lowercase hex string`() {
        val bytes = byteArrayOf(0x00, 0x0f, 0x10, 0xaf.toByte(), 0xff.toByte())
        val result = HexUtils.encode(bytes)
        assertEquals("000f10afff", result)
    }

    @Test
    fun `decode should return empty byte array for empty string`() {
        val result = HexUtils.decode("")
        assertArrayEquals(byteArrayOf(), result)
    }

    @Test
    fun `decode should parse valid hex string correctly`() {
        val hex = "000f10afff"
        val expected = byteArrayOf(0x00, 0x0f, 0x10, 0xaf.toByte(), 0xff.toByte())
        val result = HexUtils.decode(hex)
        assertArrayEquals(expected, result)
    }

    @Test
    fun `decode should handle uppercase and mixed case`() {
        val hex = "000F10AfFF"
        val expected = byteArrayOf(0x00, 0x0f, 0x10, 0xaf.toByte(), 0xff.toByte())
        val result = HexUtils.decode(hex)
        assertArrayEquals(expected, result)
    }

    @Test
    fun `roundtrip encode and decode preserves original bytes`() {
        val original = byteArrayOf(-128, -1, 0, 1, 42, 127)
        val encoded = HexUtils.encode(original)
        val decoded = HexUtils.decode(encoded)
        assertArrayEquals(original, decoded)
    }

    @Test
    fun `decode with odd length should throw IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            HexUtils.decode("abc")
        }
    }

    @Test
    fun `decode with invalid hex character should throw IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            HexUtils.decode("001g")
        }
    }
}
