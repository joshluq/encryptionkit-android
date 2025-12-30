package es.joshluq.encryptionkit.data.datasource

import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class FileDataSourceTest {

    private val provider: CertificatePathProvider = mockk()
    private val dataSource = FileDataSource(provider)

    @Test(expected = CryptoException::class)
    fun `getPublicKeyFromCertificate should throw if path is null`() {
        every { provider.getCertificatePath() } returns null
        dataSource.getPublicKeyFromCertificate()
    }

    @Test(expected = CryptoException::class)
    fun `getPublicKeyFromCertificate should throw if file does not exist`() {
        every { provider.getCertificatePath() } returns "/invalid/path/cert.crt"
        dataSource.getPublicKeyFromCertificate()
    }
}
