package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.sdk.EncryptionkitConfig
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SymmetricUseCaseTest {

    private val repository: EncryptionRepository = mockk()
    private val encryptUseCase = EncryptSymmetricUseCase(repository)
    private val decryptUseCase = DecryptSymmetricUseCase(repository)
    
    private val config = EncryptionkitConfig("test_alias", useStrongBox = false, requireUserAuth = false)
    private val data = "hello".toByteArray()
    private val ciphertext = "encrypted".toByteArray()
    private val iv = "iv123".toByteArray()
    private val cryptoResult = CryptoResult(ciphertext, iv)

    @Test
    fun `EncryptSymmetricUseCase should delegate to repository`() = runBlocking {
        // Given
        val alias = "test_alias"
        every { repository.encryptSymmetric(data, alias) } returns cryptoResult
        val input = EncryptSymmetricUseCase.Input(data, alias)

        // When
        val result = encryptUseCase(input)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(cryptoResult, result.getOrNull()?.result)
        verify { repository.encryptSymmetric(data, alias) }
    }

    @Test
    fun `DecryptSymmetricUseCase should delegate to repository`() = runBlocking {
        // Given
        val alias = "test_alias"
        every { repository.decryptSymmetric(ciphertext, iv, alias) } returns data
        val input = DecryptSymmetricUseCase.Input(ciphertext, iv, alias)

        // When
        val result = decryptUseCase(input)

        // Then
        assertTrue(result.isSuccess)
        assertArrayEquals(data, result.getOrNull()?.data)
        verify { repository.decryptSymmetric(ciphertext, iv, alias) }
    }
}
