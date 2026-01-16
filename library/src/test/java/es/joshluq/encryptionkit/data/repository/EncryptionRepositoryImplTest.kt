package es.joshluq.encryptionkit.data.repository

import es.joshluq.encryptionkit.data.datasource.FileDataSource
import es.joshluq.encryptionkit.data.datasource.KeystoreDataSource
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.Key
import java.security.PublicKey
import javax.crypto.SecretKey

class EncryptionRepositoryImplTest {

    private val keystoreDataSource: KeystoreDataSource = mockk()
    private val fileDataSource: FileDataSource = mockk()
    private val repository = EncryptionRepositoryImpl(keystoreDataSource, fileDataSource)

    private val config = EncryptionConfig("alias", false, false)

    @Test
    fun `getSecurityLevel should delegate to keystoreDataSource`() {
        every { keystoreDataSource.getSecurityLevel("alias") } returns SecurityLevel.TRUSTED_ENVIRONMENT

        val result = repository.getSecurityLevel("alias")

        assertEquals(SecurityLevel.TRUSTED_ENVIRONMENT, result)
        verify { keystoreDataSource.getSecurityLevel("alias") }
    }

    @Test
    fun `deleteKey should delegate to keystoreDataSource`() {
        every { keystoreDataSource.deleteKey("alias") } returns Unit

        repository.deleteKey("alias")

        verify { keystoreDataSource.deleteKey("alias") }
    }

    @Test(expected = CryptoException::class)
    fun `encryptSymmetric should throw if key not found`() {
        every { keystoreDataSource.getKey("alias") } returns null

        repository.encryptSymmetric("data".toByteArray(), config)
    }

    @Test
    fun `getPublicKey should delegate to fileDataSource`() = runTest {
        val mockPublicKey: PublicKey = mockk()
        every { fileDataSource.getPublicKeyFromCertificate() } returns mockPublicKey

        val result = repository.getPublicKey()

        assertEquals(mockPublicKey, result)
        verify { fileDataSource.getPublicKeyFromCertificate() }
    }
}
