package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VerifyMacUseCaseTest {
    private val repository: EncryptionRepository = mockk()
    private lateinit var useCase: VerifyMacUseCase

    @Before
    fun setUp() {
        useCase = VerifyMacUseCase(repository)
    }

    @Test
    fun `invoke should return true when MAC is valid`() =
        runTest {
            val data = "payload".toByteArray()
            val mac = "valid_mac".toByteArray()
            val alias = "mac_alias"

            every { repository.verifyMac(data, mac, alias) } returns true

            val result = useCase(VerifyMacUseCase.Input(data, mac, alias))

            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()?.isValid == true)
        }

    @Test
    fun `invoke should return false when MAC is invalid`() =
        runTest {
            val data = "payload".toByteArray()
            val mac = "invalid_mac".toByteArray()
            val alias = "mac_alias"

            every { repository.verifyMac(data, mac, alias) } returns false

            val result = useCase(VerifyMacUseCase.Input(data, mac, alias))

            assertTrue(result.isSuccess)
            assertFalse(result.getOrNull()?.isValid == true)
        }
}
