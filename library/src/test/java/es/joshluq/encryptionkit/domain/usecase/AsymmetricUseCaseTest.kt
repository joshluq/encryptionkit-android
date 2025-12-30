package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class AsymmetricUseCaseTest {

    private val repository: EncryptionRepository = mockk()
    private val useCase = EncryptAsymmetricUseCase(repository)
    
    private val config = EncryptionConfig("alias", false, false)
    private val data = "secret".toByteArray()
    private val encrypted = "rsa_encrypted".toByteArray()

    @Test
    fun `invoke should call repository encryptAsymmetric`() = runBlocking {
        // Given
        coEvery { repository.encryptAsymmetric(data, config) } returns encrypted

        // When
        val result = useCase(data, config)

        // Then
        assertArrayEquals(encrypted, result)
        coVerify { repository.encryptAsymmetric(data, config) }
    }

    @Test
    fun `invoke with SecureBytes should use raw data`() = runBlocking {
        // Given
        val secure = SecureBytes(data.copyOf())
        coEvery { repository.encryptAsymmetric(any(), config) } returns encrypted

        // When
        val result = useCase(secure, config)

        // Then
        assertArrayEquals(encrypted, result)
        coVerify { repository.encryptAsymmetric(match { it.contentEquals(data) }, config) }
    }
}
