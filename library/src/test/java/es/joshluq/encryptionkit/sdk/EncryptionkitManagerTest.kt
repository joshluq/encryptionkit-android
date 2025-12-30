package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.domain.model.*
import es.joshluq.encryptionkit.domain.usecase.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
    fun `encrypt should call onSuccess when successful`() {
        val data = "data".toByteArray()
        val expectedResult = CryptoResult("cipher".toByteArray(), "iv".toByteArray())
        val latch = CountDownLatch(1)
        
        every { encryptSymmetricUseCase(any()) } returns flowOf(EncryptSymmetricUseCase.Output(expectedResult))

        manager.encrypt(data, onSuccess = {
            assertEquals(expectedResult, it)
            latch.countDown()
        })

        latch.await(1, TimeUnit.SECONDS)
    }

    @Test
    fun `decrypt should call onSuccess when successful`() {
        val ciphertext = "cipher".toByteArray()
        val iv = "iv".toByteArray()
        val expectedPlaintext = "plain".toByteArray()
        val latch = CountDownLatch(1)

        every { decryptSymmetricUseCase(any()) } returns flowOf(DecryptSymmetricUseCase.Output(expectedPlaintext))

        manager.decrypt(ciphertext, iv, onSuccess = {
            assertArrayEquals(expectedPlaintext, it)
            latch.countDown()
        })

        latch.await(1, TimeUnit.SECONDS)
    }

    @Test
    fun `hashToHex should return hex string via callback`() {
        val text = "test"
        val mockHash = byteArrayOf(0x00, 0xff.toByte())
        val latch = CountDownLatch(1)

        every { hashDataUseCase(any()) } returns flowOf(HashDataUseCase.Output(mockHash))

        manager.hashToHex(text, onSuccess = {
            assertEquals("00ff", it)
            latch.countDown()
        })

        latch.await(1, TimeUnit.SECONDS)
    }
}
