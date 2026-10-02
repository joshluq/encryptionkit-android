package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicUseCaseTest {
    private val repository: EncryptionRepository = mockk()
    private val encryptDeterministicUseCase = EncryptDeterministicUseCase(repository)
    private val decryptDeterministicUseCase = DecryptDeterministicUseCase(repository)

    @Test
    fun `encryptDeterministicUseCase should return CryptoResult from repository`() =
        runTest {
            val data = "deterministic text".toByteArray()
            val alias = "daead_alias"
            val ad = "context_key".toByteArray()
            val expectedCiphertext = "deterministic_cipher".toByteArray()

            every {
                repository.encryptDeterministic(data, alias, ad)
            } returns CryptoResult(expectedCiphertext)

            val result =
                encryptDeterministicUseCase(
                    EncryptDeterministicUseCase.Input(
                        data = data,
                        alias = alias,
                        associatedData = ad,
                    ),
                )

            assertTrue(result.isSuccess)
            assertArrayEquals(expectedCiphertext, result.getOrThrow().result.ciphertext)
            verify { repository.encryptDeterministic(data, alias, ad) }
        }

    @Test
    fun `decryptDeterministicUseCase should return plaintext ByteArray from repository`() =
        runTest {
            val ciphertext = "deterministic_cipher".toByteArray()
            val alias = "daead_alias"
            val ad = "context_key".toByteArray()
            val expectedPlaintext = "deterministic text".toByteArray()

            every {
                repository.decryptDeterministic(ciphertext, alias, ad)
            } returns expectedPlaintext

            val result =
                decryptDeterministicUseCase(
                    DecryptDeterministicUseCase.Input(
                        ciphertext = ciphertext,
                        alias = alias,
                        associatedData = ad,
                    ),
                )

            assertTrue(result.isSuccess)
            assertArrayEquals(expectedPlaintext, result.getOrThrow().data)
            verify { repository.decryptDeterministic(ciphertext, alias, ad) }
        }

    @Test
    fun `encryptDeterministicUseCase should return failure when repository throws`() =
        runTest {
            every {
                repository.encryptDeterministic(any(), any(), any())
            } throws RuntimeException("Deterministic failed")

            val result =
                encryptDeterministicUseCase(
                    EncryptDeterministicUseCase.Input(
                        data = "data".toByteArray(),
                        alias = "alias",
                    ),
                )

            assertTrue(result.isFailure)
            assertEquals("Deterministic failed", result.exceptionOrNull()?.message)
        }
}
