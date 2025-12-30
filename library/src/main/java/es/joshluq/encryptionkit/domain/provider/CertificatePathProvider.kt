package es.joshluq.encryptionkit.domain.provider

/**
 * Interface to provide the physical path to a certificate file.
 * The implementation resides in the consumer application.
 */
interface CertificatePathProvider {
    /**
     * Returns the absolute path to the certificate file (.crt, .pem, .der).
     * Returns null if no certificate is provided.
     */
    fun getCertificatePath(): String?
}
