package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.repository.EncryptionRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class ManagementUseCaseTest {

    private val repository: EncryptionRepository = mockk()
    
    @Test
    fun `InitializeLibraryUseCase should call repository initializeKey`() {
        val useCase = InitializeLibraryUseCase(repository)
        val config = EncryptionConfig("alias", false, false)
        every { repository.initializeKey(config) } just runs

        useCase(config)

        verify { repository.initializeKey(config) }
    }

    @Test
    fun `DeleteKeyUseCase should call repository deleteKey`() {
        val useCase = DeleteKeyUseCase(repository)
        val alias = "test_alias"
        every { repository.deleteKey(alias) } just runs

        useCase(alias)

        verify { repository.deleteKey(alias) }
    }

    @Test
    fun `GetSecurityLevelUseCase should return level from repository`() {
        val useCase = GetSecurityLevelUseCase(repository)
        val alias = "test_alias"
        every { repository.getSecurityLevel(alias) } returns SecurityLevel.STRONGBOX

        val result = useCase(alias)

        assertEquals(SecurityLevel.STRONGBOX, result)
        verify { repository.getSecurityLevel(alias) }
    }
}
