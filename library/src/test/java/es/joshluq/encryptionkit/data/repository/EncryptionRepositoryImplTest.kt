package es.joshluq.encryptionkit.data.repository

import es.joshluq.encryptionkit.data.datasource.FileDataSource
import es.joshluq.encryptionkit.data.datasource.KeystoreDataSource
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.sdk.EncryptionkitConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec

class EncryptionRepositoryImplTest {

    private val keystoreDataSource: KeystoreDataSource = mockk()
    private val fileDataSource: FileDataSource = mockk()
    private lateinit var repository: EncryptionRepositoryImpl

    @Before
    fun setUp() {
        repository = EncryptionRepositoryImpl(keystoreDataSource, fileDataSource)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initializeKey should delegate to keystoreDataSource`() {
        val config = EncryptionkitConfig("alias", true, true)
        every { keystoreDataSource.ensureKeyExists("alias", true, true) } returns Unit

        repository.initializeKey(config)

        verify { keystoreDataSource.ensureKeyExists("alias", true, true) }
    }

    @Test
    fun `getSecurityLevel should delegate to keystoreDataSource`() {
        every { keystoreDataSource.getSecurityLevel("alias") } returns SecurityLevel.TRUSTED_ENVIRONMENT

        val result = repository.getSecurityLevel("alias")

        assertEquals(SecurityLevel.TRUSTED_ENVIRONMENT, result)
        verify { keystoreDataSource.getSecurityLevel("alias") }
    }

    @Test
    fun `deleteKey should delegate to keystoreDataSource`() {
        every { keystoreDataSource.deleteKey("alias") } returns Unit

        repository.deleteKey("alias")

        verify { keystoreDataSource.deleteKey("alias") }
    }

    @Test(expected = CryptoException::class)
    fun `encryptSymmetric should throw if key not found`() {
        every { keystoreDataSource.getKey("alias") } returns null

        repository.encryptSymmetric("data".toByteArray(), "alias")
    }

    @Test
    fun `encryptSymmetric should return CryptoResult when successful`() {
        mockkStatic(Cipher::class)
        val mockKey: SecretKey = mockk()
        val mockCipher: Cipher = mockk()
        val data = "data".toByteArray()
        val encrypted = "encrypted".toByteArray()
        val iv = "iv123".toByteArray()

        every { keystoreDataSource.getKey("alias") } returns mockKey
        every { Cipher.getInstance("AES/GCM/NoPadding") } returns mockCipher
        every { mockCipher.init(Cipher.ENCRYPT_MODE, mockKey) } returns Unit
        every { mockCipher.doFinal(data) } returns encrypted
        every { mockCipher.iv } returns iv

        val result = repository.encryptSymmetric(data, "alias")

        assertEquals(encrypted, result.ciphertext)
        assertEquals(iv, result.iv)
    }

    @Test
    fun `decryptSymmetric should return decrypted data`() {
        mockkStatic(Cipher::class)
        val mockKey: SecretKey = mockk()
        val mockCipher: Cipher = mockk()
        val ciphertext = "encrypted".toByteArray()
        val iv = "iv123".toByteArray()
        val decrypted = "data".toByteArray()

        every { keystoreDataSource.getKey("alias") } returns mockKey
        every { Cipher.getInstance("AES/GCM/NoPadding") } returns mockCipher
        every { mockCipher.init(Cipher.DECRYPT_MODE, mockKey, any<GCMParameterSpec>()) } returns Unit
        every { mockCipher.doFinal(ciphertext) } returns decrypted

        val result = repository.decryptSymmetric(ciphertext, iv, "alias")

        assertArrayEquals(decrypted, result)
    }

    @Test
    fun `getPublicKey should delegate to fileDataSource`() = runTest {
        val mockPublicKey: PublicKey = mockk()
        every { fileDataSource.getPublicKeyFromCertificate() } returns mockPublicKey

        val result = repository.getPublicKey()

        assertEquals(mockPublicKey, result)
        verify { fileDataSource.getPublicKeyFromCertificate() }
    }

    @Test
    fun `hash should return digest from MessageDigest`() {
        mockkStatic(MessageDigest::class)
        val mockDigest: MessageDigest = mockk()
        val data = "test".toByteArray()
        val expectedHash = "hash".toByteArray()

        every { MessageDigest.getInstance("SHA-256") } returns mockDigest
        every { mockDigest.digest(data) } returns expectedHash

        val result = repository.hash(data, "SHA-256")

        assertArrayEquals(expectedHash, result)
    }

    @Test
    fun `encryptAsymmetric should validate hash and encrypt`() = runTest {
        mockkStatic(Cipher::class, MessageDigest::class)
        val mockPublicKey: PublicKey = mockk()
        val mockCipher: Cipher = mockk()
        val mockDigest: MessageDigest = mockk()
        val data = "data".toByteArray()
        val encrypted = "rsa_encrypted".toByteArray()
        val encodedKey = "encoded_key".toByteArray()
        
        // Mock public key encoding
        every { mockPublicKey.encoded } returns encodedKey
        every { fileDataSource.getPublicKeyFromCertificate() } returns mockPublicKey

        // Mock hash validation (SHA-256)
        val hashBytes = byteArrayOf(0x0a, 0x0b) // simplified hash
        val hashHex = "0a0b"
        every { MessageDigest.getInstance("SHA-256") } returns mockDigest
        every { mockDigest.digest(encodedKey) } returns hashBytes

        // Mock RSA Encryption
        every { Cipher.getInstance("RSA/ECB/OAEPPadding") } returns mockCipher
        every { mockCipher.init(Cipher.ENCRYPT_MODE, mockPublicKey, any<OAEPParameterSpec>()) } returns Unit
        every { mockCipher.doFinal(data) } returns encrypted

        val result = repository.encryptAsymmetric(data, hashHex)

        assertArrayEquals(encrypted, result)
    }

    @Test(expected = CryptoException::class)
    fun `encryptAsymmetric should throw if hash does not match`() = runTest {
        mockkStatic(MessageDigest::class)
        val mockPublicKey: PublicKey = mockk()
        val mockDigest: MessageDigest = mockk()
        
        every { mockPublicKey.encoded } returns "different".toByteArray()
        every { fileDataSource.getPublicKeyFromCertificate() } returns mockPublicKey
        
        every { MessageDigest.getInstance("SHA-256") } returns mockDigest
        every { mockDigest.digest(any()) } returns byteArrayOf(0x01) // hex "01"

        repository.encryptAsymmetric("data".toByteArray(), "wrong_hash")
    }
}
