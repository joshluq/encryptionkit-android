package es.joshluq.encryptionkit.domain.provider

/**
 * Interface that the consumer application must implement to provide access to a physical certificate file.
 * This is used for operations requiring a Public Key loaded from an X.509 certificate.
 */
interface CertificatePathProvider {
    /**
     * Returns the absolute file system path to the certificate file (e.g., .crt, .pem, .der).
     *
     * @return The absolute path as a String, or null if no certificate is configured.
     */
    fun getCertificatePath(): String?
}
