package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class HashDataUseCaseTest {

    private val repository: EncryptionRepository = mockk()
    private val useCase = HashDataUseCase(repository)

    @Test
    fun `invoke should call repository hash with default algorithm`() {
        // Given
        val data = "test".toByteArray()
        val expectedHash = "hash".toByteArray()
        every { repository.hash(data, "SHA-256") } returns expectedHash

        // When
        val result = useCase(data)

        // Then
        assertEquals(expectedHash, result)
        verify { repository.hash(data, "SHA-256") }
    }

    @Test
    fun `toHexString should return correct hex string`() {
        // Given
        val data = "test".toByteArray()
        // bytes for "hello" hash (mocked)
        val mockHash = byteArrayOf(0x00, 0x01, 0x02, 0xff.toByte()) 
        every { repository.hash(data, "SHA-256") } returns mockHash

        // When
        val result = useCase.toHexString(data)

        // Then
        assertEquals("000102ff", result)
    }
}
