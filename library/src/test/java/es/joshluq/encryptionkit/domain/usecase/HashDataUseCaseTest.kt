package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import es.joshluq.foundationkit.testing.coroutines.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HashDataUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: EncryptionRepository = mockk()
    private val useCase = HashDataUseCase(repository)

    @Test
    fun `invoke should call repository hash with correct algorithm`() =
        runTest {
            // Given
            val data = "test".toByteArray()
            val expectedHash = "hash".toByteArray()
            val input = HashDataUseCase.Input(data, "SHA-256")
            every { repository.hash(data, "SHA-256") } returns expectedHash

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(expectedHash, result.getOrNull()?.data)
            verify { repository.hash(data, "SHA-256") }
        }
}
