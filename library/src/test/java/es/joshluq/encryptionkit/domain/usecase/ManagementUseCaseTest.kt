package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.sdk.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManagementUseCaseTest {

    private val repository: EncryptionRepository = mockk()
    
    @Test
    fun `InitializeLibraryUseCase should call repository initializeKey`() = runBlocking {
        val useCase = InitializeLibraryUseCase(repository)
        val config = EncryptionConfig("alias", false, false)
        val input = InitializeLibraryUseCase.Input(config)
        every { repository.initializeKey(config) } just runs

        val result = useCase(input)

        assertTrue(result.isSuccess)
        verify { repository.initializeKey(config) }
    }

    @Test
    fun `DeleteKeyUseCase should call repository deleteKey`() = runBlocking {
        val useCase = DeleteKeyUseCase(repository)
        val alias = "test_alias"
        val input = DeleteKeyUseCase.Input(alias)
        every { repository.deleteKey(alias) } just runs

        val result = useCase(input)

        assertTrue(result.isSuccess)
        verify { repository.deleteKey(alias) }
    }

    @Test
    fun `GetSecurityLevelUseCase should return level from repository`() = runBlocking {
        val useCase = GetSecurityLevelUseCase(repository)
        val alias = "test_alias"
        val input = GetSecurityLevelUseCase.Input(alias)
        every { repository.getSecurityLevel(alias) } returns SecurityLevel.STRONGBOX

        val result = useCase(input)

        assertTrue(result.isSuccess)
        assertEquals(SecurityLevel.STRONGBOX, result.getOrNull()?.level)
        verify { repository.getSecurityLevel(alias) }
    }
}
