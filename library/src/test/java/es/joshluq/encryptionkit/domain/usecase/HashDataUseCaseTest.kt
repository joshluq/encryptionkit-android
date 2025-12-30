package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class HashDataUseCaseTest {

    private val repository: EncryptionRepository = mockk()
    private val useCase = HashDataUseCase(repository)

    @Test
    fun `invoke should call repository hash with correct algorithm`() = runBlocking {
        // Given
        val data = "test".toByteArray()
        val expectedHash = "hash".toByteArray()
        val input = HashDataUseCase.Input(data, "SHA-256")
        every { repository.hash(data, "SHA-256") } returns expectedHash

        // When
        val result = useCase(input).first()

        // Then
        assertEquals(expectedHash, result.data)
        verify { repository.hash(data, "SHA-256") }
    }
}
