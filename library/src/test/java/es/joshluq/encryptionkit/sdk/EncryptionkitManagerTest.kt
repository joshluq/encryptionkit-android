package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.domain.model.*
import es.joshluq.encryptionkit.domain.usecase.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class EncryptionkitManagerTest {

    private val encryptSymmetricUseCase: EncryptSymmetricUseCase = mockk()
    private val decryptSymmetricUseCase: DecryptSymmetricUseCase = mockk()
    private val encryptAsymmetricUseCase: EncryptAsymmetricUseCase = mockk()
    private val getSecurityLevelUseCase: GetSecurityLevelUseCase = mockk()
    private val deleteKeyUseCase: DeleteKeyUseCase = mockk()
    private val hashDataUseCase: HashDataUseCase = mockk()
    
    private val config = EncryptionConfig("test_alias", false, false)

    private val manager = EncryptionkitManager(
        encryptSymmetricUseCase,
        decryptSymmetricUseCase,
        encryptAsymmetricUseCase,
        getSecurityLevelUseCase,
        deleteKeyUseCase,
        hashDataUseCase,
        config
    )

    @Test
    fun `encrypt should delegate to EncryptSymmetricUseCase`() {
        val data = "data".toByteArray()
        val expectedResult = CryptoResult("cipher".toByteArray(), "iv".toByteArray())
        every { encryptSymmetricUseCase(data, config) } returns expectedResult

        val result = manager.encrypt(data)

        assertEquals(expectedResult, result)
        verify { encryptSymmetricUseCase(data, config) }
    }

    @Test
    fun `decrypt should delegate to DecryptSymmetricUseCase`() {
        val ciphertext = "cipher".toByteArray()
        val iv = "iv".toByteArray()
        val expectedPlaintext = "plain".toByteArray()
        every { decryptSymmetricUseCase(ciphertext, iv, config) } returns expectedPlaintext

        val result = manager.decrypt(ciphertext, iv)

        assertArrayEquals(expectedPlaintext, result)
        verify { decryptSymmetricUseCase(ciphertext, iv, config) }
    }

    @Test
    fun `encryptWithPublicKey should delegate to EncryptAsymmetricUseCase`() = runBlocking {
        val data = "data".toByteArray()
        val expectedEncrypted = "rsa_cipher".toByteArray()
        coEvery { encryptAsymmetricUseCase(data, config) } returns expectedEncrypted

        val result = manager.encryptWithPublicKey(data)

        assertArrayEquals(expectedEncrypted, result)
    }

    @Test
    fun `getSecurityLevel should delegate to GetSecurityLevelUseCase`() {
        every { getSecurityLevelUseCase(config.alias) } returns SecurityLevel.STRONGBOX

        val result = manager.getSecurityLevel()

        assertEquals(SecurityLevel.STRONGBOX, result)
        verify { getSecurityLevelUseCase(config.alias) }
    }

    @Test
    fun `hash should delegate to HashDataUseCase`() {
        val data = "data".toByteArray()
        val expectedHash = "hash".toByteArray()
        every { hashDataUseCase(data, "SHA-256") } returns expectedHash

        val result = manager.hash(data)

        assertArrayEquals(expectedHash, result)
        verify { hashDataUseCase(data, "SHA-256") }
    }
}
