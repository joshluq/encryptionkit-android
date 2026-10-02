package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignDataUseCaseTest {
    private val repository: EncryptionRepository = mockk()
    private lateinit var useCase: SignDataUseCase

    @Before
    fun setUp() {
        useCase = SignDataUseCase(repository)
    }

    @Test
    fun `invoke should return signature output on success`() =
        runTest {
            val data = "payload".toByteArray()
            val alias = "sign_alias"
            val expectedSignature = "signature".toByteArray()

            every { repository.sign(data, alias) } returns expectedSignature

            val result = useCase(SignDataUseCase.Input(data, alias))

            assertTrue(result.isSuccess)
            assertArrayEquals(expectedSignature, result.getOrNull()?.signature)
        }

    @Test
    fun `invoke should return failure on repository exception`() =
        runTest {
            val data = "payload".toByteArray()
            val alias = "sign_alias"

            every { repository.sign(data, alias) } throws RuntimeException("Sign failed")

            val result = useCase(SignDataUseCase.Input(data, alias))

            assertTrue(result.isFailure)
        }
}
