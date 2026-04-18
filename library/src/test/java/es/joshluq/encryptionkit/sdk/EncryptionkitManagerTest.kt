package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.domain.model.*
import es.joshluq.encryptionkit.domain.usecase.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun `encrypt should return success result when successful`() = runBlocking {
        val data = "data".toByteArray()
        val expectedResult = CryptoResult("cipher".toByteArray(), "iv".toByteArray())
        
        coEvery { encryptSymmetricUseCase(any()) } returns Result.success(EncryptSymmetricUseCase.Output(expectedResult))

        val result = manager.encrypt(data)

        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }

    @Test
    fun `decrypt should return success result when successful`() = runBlocking {
        val ciphertext = "cipher".toByteArray()
        val iv = "iv".toByteArray()
        val expectedPlaintext = "plain".toByteArray()

        coEvery { decryptSymmetricUseCase(any()) } returns Result.success(DecryptSymmetricUseCase.Output(expectedPlaintext))

        val result = manager.decrypt(ciphertext, iv)

        assertTrue(result.isSuccess)
        assertArrayEquals(expectedPlaintext, result.getOrNull())
    }

    @Test
    fun `hashToHex should return hex string result`() = runBlocking {
        val text = "test"
        val mockHash = byteArrayOf(0x00, 0xff.toByte())

        coEvery { hashDataUseCase(any()) } returns Result.success(HashDataUseCase.Output(mockHash))

        val result = manager.hashToHex(text)

        assertTrue(result.isSuccess)
        assertEquals("00ff", result.getOrNull())
    }

    @Test
    fun `any function should return failure when use case fails`() = runBlocking {
        val data = "data".toByteArray()
        val exception = Exception("Encryption failed")
        
        coEvery { encryptSymmetricUseCase(any()) } returns Result.failure(exception)

        val result = manager.encrypt(data)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CryptoException)
        assertEquals("Encryption failed", result.exceptionOrNull()?.message)
    }
}
