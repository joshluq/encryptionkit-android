package es.joshluq.encryptionkit.data.provider.room

import android.util.Base64
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.sdk.EncryptionKit
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class EncryptedStringConverterTest {
    private val encryptionKit: EncryptionKit = mockk()

    @Before
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), Base64.NO_WRAP) } answers {
            java.util.Base64
                .getEncoder()
                .encodeToString(it.invocation.args[0] as ByteArray)
        }
        every { Base64.decode(any<String>(), Base64.NO_WRAP) } answers {
            java.util.Base64
                .getDecoder()
                .decode(it.invocation.args[0] as String)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `null values should return null`() {
        val converter = EncryptedStringConverter(encryptionKit)
        assertNull(converter.fromPlaintext(null))
        assertNull(converter.toPlaintext(null))
    }

    @Test
    fun `fromPlaintext standard AEAD should encrypt and return base64 string`() {
        val converter = EncryptedStringConverter(encryptionKit)
        val plaintext = "super_secret_value"
        val encryptedBytes = "encrypted_bytes".toByteArray()

        coEvery { encryptionKit.encrypt(any(), any()) } returns Result.success(CryptoResult(encryptedBytes))

        val result = converter.fromPlaintext(plaintext)

        val expectedBase64 =
            java.util.Base64
                .getEncoder()
                .encodeToString(encryptedBytes)
        assertEquals(expectedBase64, result)
    }

    @Test
    fun `toPlaintext standard AEAD should decode base64 and decrypt`() {
        val converter = EncryptedStringConverter(encryptionKit)
        val plaintext = "super_secret_value"
        val encryptedBytes = "encrypted_bytes".toByteArray()
        val base64 =
            java.util.Base64
                .getEncoder()
                .encodeToString(encryptedBytes)

        coEvery { encryptionKit.decrypt(encryptedBytes, any()) } returns Result.success(SecureBytes(plaintext.toByteArray()))

        val result = converter.toPlaintext(base64)

        assertEquals(plaintext, result)
    }

    @Test
    fun `deterministic converter should use encryptDeterministic and decryptDeterministic`() {
        val converter = EncryptedStringConverter(encryptionKit, deterministic = true)
        val plaintext = "searchable_field"
        val encryptedBytes = "det_encrypted_bytes".toByteArray()
        val base64 =
            java.util.Base64
                .getEncoder()
                .encodeToString(encryptedBytes)

        coEvery { encryptionKit.encryptDeterministic(any(), any()) } returns Result.success(CryptoResult(encryptedBytes))
        coEvery { encryptionKit.decryptDeterministic(encryptedBytes, any()) } returns Result.success(SecureBytes(plaintext.toByteArray()))

        val encryptedResult = converter.fromPlaintext(plaintext)
        assertEquals(base64, encryptedResult)

        val decryptedResult = converter.toPlaintext(base64)
        assertEquals(plaintext, decryptedResult)
    }
}
