package es.joshluq.encryptionkit.data.provider.biometric

import androidx.biometric.BiometricPrompt
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.foundationkit.log.LoggerKit
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class BiometricCryptoHelperTest {
    private val logger: LoggerKit = mockk(relaxed = true)
    private lateinit var helper: BiometricCryptoHelper

    @Before
    fun setUp() {
        helper = BiometricCryptoHelper(logger)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `encrypt should encrypt data with cipher and return BiometricCryptoResult`() {
        val mockCipher = mockk<Cipher>()
        val mockCryptoObject = mockk<BiometricPrompt.CryptoObject>()
        val plaintext = byteArrayOf(1, 2, 3)
        val ciphertext = byteArrayOf(4, 5, 6)
        val iv = byteArrayOf(7, 8, 9)
        val associatedData = "ad".toByteArray()

        every { mockCryptoObject.cipher } returns mockCipher
        every { mockCipher.updateAAD(associatedData) } returns Unit
        every { mockCipher.doFinal(plaintext) } returns ciphertext
        every { mockCipher.iv } returns iv

        val secureBytes = SecureBytes(plaintext.copyOf())
        val result = helper.encrypt(mockCryptoObject, secureBytes, associatedData)

        assertArrayEquals(ciphertext, result.ciphertext)
        assertArrayEquals(iv, result.iv)
        assertTrue(secureBytes.isWiped())
    }

    @Test
    fun `decrypt should decrypt ciphertext with cipher and return SecureBytes`() {
        val mockCipher = mockk<Cipher>()
        val mockCryptoObject = mockk<BiometricPrompt.CryptoObject>()
        val ciphertext = byteArrayOf(4, 5, 6)
        val plaintext = byteArrayOf(1, 2, 3)
        val associatedData = "ad".toByteArray()

        every { mockCryptoObject.cipher } returns mockCipher
        every { mockCipher.updateAAD(associatedData) } returns Unit
        every { mockCipher.doFinal(ciphertext) } returns plaintext

        val result = helper.decrypt(mockCryptoObject, ciphertext, associatedData)

        assertArrayEquals(plaintext, result.data)
    }

    @Test(expected = CryptoException::class)
    fun `encrypt should throw CryptoException if cipher is null`() {
        val mockCryptoObject = mockk<BiometricPrompt.CryptoObject>()
        every { mockCryptoObject.cipher } returns null

        helper.encrypt(mockCryptoObject, SecureBytes(byteArrayOf(1)))
    }

    @Test(expected = CryptoException::class)
    fun `decrypt should throw CryptoException if cipher is null`() {
        val mockCryptoObject = mockk<BiometricPrompt.CryptoObject>()
        every { mockCryptoObject.cipher } returns null

        helper.decrypt(mockCryptoObject, byteArrayOf(1))
    }

    @Test
    fun `createEncryptCryptoObject should initialize Cipher in ENCRYPT_MODE`() {
        mockkStatic(KeyStore::class, Cipher::class)
        val mockKeyStore = mockk<KeyStore>(relaxed = true)
        val mockSecretKeyEntry = mockk<KeyStore.SecretKeyEntry>()
        val mockSecretKey = mockk<SecretKey>()
        val mockCipher = mockk<Cipher>(relaxed = true)

        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeyStore
        every { mockKeyStore.containsAlias("bio_alias") } returns true
        every { mockKeyStore.getEntry("bio_alias", null) } returns mockSecretKeyEntry
        every { mockSecretKeyEntry.secretKey } returns mockSecretKey

        every { Cipher.getInstance("AES/GCM/NoPadding") } returns mockCipher
        every { mockCipher.init(Cipher.ENCRYPT_MODE, mockSecretKey) } returns Unit

        val cryptoObject = helper.createEncryptCryptoObject("bio_alias")

        assertEquals(mockCipher, cryptoObject.cipher)
        verify { mockCipher.init(Cipher.ENCRYPT_MODE, mockSecretKey) }
    }

    @Test
    fun `createDecryptCryptoObject should initialize Cipher in DECRYPT_MODE with GCMParameterSpec`() {
        mockkStatic(KeyStore::class, Cipher::class)
        val mockKeyStore = mockk<KeyStore>(relaxed = true)
        val mockSecretKeyEntry = mockk<KeyStore.SecretKeyEntry>()
        val mockSecretKey = mockk<SecretKey>()
        val mockCipher = mockk<Cipher>(relaxed = true)
        val iv = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeyStore
        every { mockKeyStore.containsAlias("bio_alias") } returns true
        every { mockKeyStore.getEntry("bio_alias", null) } returns mockSecretKeyEntry
        every { mockSecretKeyEntry.secretKey } returns mockSecretKey

        every { Cipher.getInstance("AES/GCM/NoPadding") } returns mockCipher
        every { mockCipher.init(Cipher.DECRYPT_MODE, mockSecretKey, any<GCMParameterSpec>()) } returns Unit

        val cryptoObject = helper.createDecryptCryptoObject("bio_alias", iv)

        assertEquals(mockCipher, cryptoObject.cipher)
        verify { mockCipher.init(Cipher.DECRYPT_MODE, mockSecretKey, any<GCMParameterSpec>()) }
    }

    @Test
    fun `deleteKey should remove entry from AndroidKeyStore`() {
        mockkStatic(KeyStore::class)
        val mockKeyStore = mockk<KeyStore>(relaxed = true)

        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeyStore
        every { mockKeyStore.containsAlias("bio_alias") } returns true
        every { mockKeyStore.deleteEntry("bio_alias") } returns Unit

        helper.deleteKey("bio_alias")

        verify { mockKeyStore.deleteEntry("bio_alias") }
    }
}
