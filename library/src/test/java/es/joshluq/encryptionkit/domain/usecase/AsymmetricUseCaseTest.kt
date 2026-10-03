package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.testing.coroutines.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AsymmetricUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: EncryptionRepository = mockk()
    private val useCase = EncryptAsymmetricUseCase(repository)

    private val publicKeyHash = "test_hash"
    private val data = "secret".toByteArray()
    private val encrypted = "rsa_encrypted".toByteArray()

    @Test
    fun `invoke should call repository encryptAsymmetric`() =
        runTest {
            // Given
            coEvery { repository.encryptAsymmetric(data, publicKeyHash) } returns encrypted
            val input = EncryptAsymmetricUseCase.Input(data = data, publicKeyHash = publicKeyHash)

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isSuccess)
            assertArrayEquals(encrypted, result.getOrNull()?.data)
            coVerify { repository.encryptAsymmetric(data, publicKeyHash) }
        }

    @Test
    fun `invoke with SecureBytes should use raw data`() =
        runTest {
            // Given
            val secure = SecureBytes(data.copyOf())
            coEvery { repository.encryptAsymmetric(any(), publicKeyHash) } returns encrypted
            val input = EncryptAsymmetricUseCase.Input(secureData = secure, publicKeyHash = publicKeyHash)

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isSuccess)
            assertArrayEquals(encrypted, result.getOrNull()?.data)
            coVerify { repository.encryptAsymmetric(match { it.contentEquals(data) }, publicKeyHash) }
        }
}
