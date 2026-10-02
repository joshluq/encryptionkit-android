package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VerifySignatureUseCaseTest {
    private val repository: EncryptionRepository = mockk()
    private lateinit var useCase: VerifySignatureUseCase

    @Before
    fun setUp() {
        useCase = VerifySignatureUseCase(repository)
    }

    @Test
    fun `invoke should return true when signature is valid`() =
        runTest {
            val data = "payload".toByteArray()
            val signature = "signature".toByteArray()
            val alias = "sign_alias"

            every { repository.verifySignature(data, signature, alias) } returns true

            val result = useCase(VerifySignatureUseCase.Input(data, signature, alias))

            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()?.isValid == true)
        }

    @Test
    fun `invoke should return false when signature is invalid`() =
        runTest {
            val data = "payload".toByteArray()
            val signature = "invalid_signature".toByteArray()
            val alias = "sign_alias"

            every { repository.verifySignature(data, signature, alias) } returns false

            val result = useCase(VerifySignatureUseCase.Input(data, signature, alias))

            assertTrue(result.isSuccess)
            assertFalse(result.getOrNull()?.isValid == true)
        }
}
