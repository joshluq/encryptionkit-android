package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RotateKeyUseCaseTest {
    private val repository: EncryptionRepository = mockk()
    private lateinit var useCase: RotateKeyUseCase

    @Before
    fun setUp() {
        useCase = RotateKeyUseCase(repository)
    }

    @Test
    fun `invoke should call repository rotateKey and return success`() =
        runTest {
            val alias = "test_alias"
            every { repository.rotateKey(alias) } returns Unit

            val result = useCase(RotateKeyUseCase.Input(alias))

            assertTrue(result.isSuccess)
            verify(exactly = 1) { repository.rotateKey(alias) }
        }

    @Test
    fun `invoke should return failure when repository throws exception`() =
        runTest {
            val alias = "test_alias"
            every { repository.rotateKey(alias) } throws RuntimeException("Rotation error")

            val result = useCase(RotateKeyUseCase.Input(alias))

            assertTrue(result.isFailure)
            verify(exactly = 1) { repository.rotateKey(alias) }
        }
}
