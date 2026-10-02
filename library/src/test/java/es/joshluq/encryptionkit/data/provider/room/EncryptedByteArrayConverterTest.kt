package es.joshluq.encryptionkit.data.provider.room

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.sdk.EncryptionKit
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EncryptedByteArrayConverterTest {
    private val encryptionKit: EncryptionKit = mockk()

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `null values should return null`() {
        val converter = EncryptedByteArrayConverter(encryptionKit)
        assertNull(converter.fromPlaintext(null))
        assertNull(converter.toPlaintext(null))
    }

    @Test
    fun `fromPlaintext standard AEAD should encrypt and return ciphertext bytes`() {
        val converter = EncryptedByteArrayConverter(encryptionKit)
        val plaintext = byteArrayOf(1, 2, 3, 4)
        val encryptedBytes = byteArrayOf(9, 8, 7, 6)

        coEvery { encryptionKit.encrypt(any(), any()) } returns Result.success(CryptoResult(encryptedBytes))

        val result = converter.fromPlaintext(plaintext)

        assertArrayEquals(encryptedBytes, result)
    }

    @Test
    fun `toPlaintext standard AEAD should decrypt ciphertext bytes`() {
        val converter = EncryptedByteArrayConverter(encryptionKit)
        val expectedPlaintext = byteArrayOf(1, 2, 3, 4)
        val encryptedBytes = byteArrayOf(9, 8, 7, 6)

        coEvery { encryptionKit.decrypt(encryptedBytes, any()) } answers {
            Result.success(SecureBytes(expectedPlaintext.copyOf()))
        }

        val result = converter.toPlaintext(encryptedBytes)

        assertArrayEquals(expectedPlaintext, result)
    }

    @Test
    fun `deterministic converter should use encryptDeterministic and decryptDeterministic`() {
        val converter = EncryptedByteArrayConverter(encryptionKit, deterministic = true)
        val expectedPlaintext = byteArrayOf(10, 20, 30)
        val encryptedBytes = byteArrayOf(90, 80, 70)

        coEvery { encryptionKit.encryptDeterministic(any(), any()) } returns Result.success(CryptoResult(encryptedBytes))
        coEvery { encryptionKit.decryptDeterministic(encryptedBytes, any()) } answers {
            Result.success(SecureBytes(expectedPlaintext.copyOf()))
        }

        val encryptedResult = converter.fromPlaintext(expectedPlaintext)
        assertArrayEquals(encryptedBytes, encryptedResult)

        val decryptedResult = converter.toPlaintext(encryptedBytes)
        assertArrayEquals(expectedPlaintext, decryptedResult)
    }
}
