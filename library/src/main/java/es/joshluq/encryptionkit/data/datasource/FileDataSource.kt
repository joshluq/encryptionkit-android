package es.joshluq.encryptionkit.data.datasource

import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.PublicKey
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory

/**
 * DataSource responsible for reading certificates from the file system.
 */
internal class FileDataSource(
    private val certificatePathProvider: CertificatePathProvider
) {
    fun getPublicKeyFromCertificate(): PublicKey {
        val path = certificatePathProvider.getCertificatePath()
            ?: throw CryptoException(
                "Certificate path not provided by consumer",
                null,
                CryptoException.Reason.CERTIFICATE_NOT_FOUND
            )

        val file = File(path)
        if (!file.exists()) {
            throw CryptoException(
                "Certificate file not found at: $path",
                null,
                CryptoException.Reason.CERTIFICATE_NOT_FOUND
            )
        }

        return try {
            FileInputStream(file).use { inputStream ->
                val certFactory = CertificateFactory.getInstance("X.509")
                val certificate = certFactory.generateCertificate(inputStream)
                certificate.publicKey
            }
        } catch (e: CertificateException) {
            throw CryptoException(
                "Failed to parse certificate",
                e,
                CryptoException.Reason.OPERATION_FAILED
            )
        } catch (e: IOException) {
            throw CryptoException(
                "Failed to read certificate file",
                e,
                CryptoException.Reason.OPERATION_FAILED
            )
        }
    }
}
