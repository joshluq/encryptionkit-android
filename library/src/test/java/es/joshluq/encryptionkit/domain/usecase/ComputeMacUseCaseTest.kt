package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ComputeMacUseCaseTest {
    private val repository: EncryptionRepository = mockk()
    private lateinit var useCase: ComputeMacUseCase

    @Before
    fun setUp() {
        useCase = ComputeMacUseCase(repository)
    }

    @Test
    fun `invoke should return MAC tag on success`() =
        runTest {
            val data = "payload".toByteArray()
            val alias = "mac_alias"
            val expectedTag = "mac_tag".toByteArray()

            every { repository.computeMac(data, alias) } returns expectedTag

            val result = useCase(ComputeMacUseCase.Input(data, alias))

            assertTrue(result.isSuccess)
            assertArrayEquals(expectedTag, result.getOrNull()?.tag)
        }

    @Test
    fun `invoke should return failure on repository exception`() =
        runTest {
            val data = "payload".toByteArray()
            val alias = "mac_alias"

            every { repository.computeMac(data, alias) } throws RuntimeException("Mac computation failed")

            val result = useCase(ComputeMacUseCase.Input(data, alias))

            assertTrue(result.isFailure)
        }
}
