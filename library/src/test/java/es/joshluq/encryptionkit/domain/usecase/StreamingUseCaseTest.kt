package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class StreamingUseCaseTest {
    private val repository: EncryptionRepository = mockk(relaxed = true)
    private val encryptStreamUseCase = EncryptStreamUseCase(repository)
    private val decryptStreamUseCase = DecryptStreamUseCase(repository)

    @Test
    fun `encryptStreamUseCase should invoke repository encryptStream`() =
        runTest {
            val inputData = ByteArrayInputStream("streaming content".toByteArray())
            val outputData = ByteArrayOutputStream()
            val alias = "stream_alias"
            val ad = "ad".toByteArray()

            val result =
                encryptStreamUseCase(
                    EncryptStreamUseCase.Input(
                        inputStream = inputData,
                        outputStream = outputData,
                        alias = alias,
                        associatedData = ad,
                    ),
                )

            assertTrue(result.isSuccess)
            verify { repository.encryptStream(inputData, outputData, alias, ad) }
        }

    @Test
    fun `decryptStreamUseCase should invoke repository decryptStream`() =
        runTest {
            val inputData = ByteArrayInputStream("ciphertext stream".toByteArray())
            val outputData = ByteArrayOutputStream()
            val alias = "stream_alias"
            val ad = "ad".toByteArray()

            val result =
                decryptStreamUseCase(
                    DecryptStreamUseCase.Input(
                        inputStream = inputData,
                        outputStream = outputData,
                        alias = alias,
                        associatedData = ad,
                    ),
                )

            assertTrue(result.isSuccess)
            verify { repository.decryptStream(inputData, outputData, alias, ad) }
        }

    @Test
    fun `encryptStreamUseCase should return failure on repository exception`() =
        runTest {
            val inputData = ByteArrayInputStream("content".toByteArray())
            val outputData = ByteArrayOutputStream()
            every { repository.encryptStream(any(), any(), any(), any()) } throws RuntimeException("Stream failed")

            val result =
                encryptStreamUseCase(
                    EncryptStreamUseCase.Input(
                        inputStream = inputData,
                        outputStream = outputData,
                        alias = "alias",
                    ),
                )

            assertTrue(result.isFailure)
            assertEquals("Stream failed", result.exceptionOrNull()?.message)
        }
}
