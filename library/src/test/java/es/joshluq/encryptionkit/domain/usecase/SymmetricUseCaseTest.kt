package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class SymmetricUseCaseTest {

    private val repository: EncryptionRepository = mockk()
    private val encryptUseCase = EncryptSymmetricUseCase(repository)
    private val decryptUseCase = DecryptSymmetricUseCase(repository)
    
    private val config = EncryptionConfig("test_alias", useStrongBox = false, requireUserAuth = false)
    private val data = "hello".toByteArray()
    private val ciphertext = "encrypted".toByteArray()
    private val iv = "iv123".toByteArray()
    private val cryptoResult = CryptoResult(ciphertext, iv)

    @Test
    fun `EncryptSymmetricUseCase should delegate to repository`() {
        // Given
        every { repository.encryptSymmetric(data, config) } returns cryptoResult

        // When
        val result = encryptUseCase(data, config)

        // Then
        assertEquals(cryptoResult, result)
        verify { repository.encryptSymmetric(data, config) }
    }

    @Test
    fun `EncryptSymmetricUseCase should support SecureBytes`() {
        // Given
        val secureBytes = SecureBytes(data.copyOf())
        every { repository.encryptSymmetric(any(), config) } returns cryptoResult

        // When
        val result = encryptUseCase(secureBytes, config)

        // Then
        assertEquals(cryptoResult, result)
        // verify called with the underlying array
        verify { repository.encryptSymmetric(match { it.contentEquals(data) }, config) }
    }

    @Test
    fun `DecryptSymmetricUseCase should delegate to repository`() {
        // Given
        every { repository.decryptSymmetric(ciphertext, iv, config) } returns data

        // When
        val result = decryptUseCase(ciphertext, iv, config)

        // Then
        assertArrayEquals(data, result)
        verify { repository.decryptSymmetric(ciphertext, iv, config) }
    }
}
