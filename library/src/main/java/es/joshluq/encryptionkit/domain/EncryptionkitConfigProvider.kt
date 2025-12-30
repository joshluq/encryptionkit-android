package es.joshluq.encryptionkit.domain

/**
 * Interface to provide the configuration for the Encryptionkit library.
 * Implement this interface to supply custom configuration (alias, security level, etc.).
 */
interface EncryptionkitConfigProvider {
    val config: EncryptionkitConfig
}
