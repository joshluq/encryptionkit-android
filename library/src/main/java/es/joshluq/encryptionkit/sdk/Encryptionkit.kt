package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.data.datasource.FileDataSource
import es.joshluq.encryptionkit.data.datasource.KeystoreDataSource
import es.joshluq.encryptionkit.data.repository.EncryptionRepositoryImpl
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.encryptionkit.domain.usecase.*

/**
 * Main entry point for the EncryptionKit SDK.
 * Constructed via [Encryptionkit.Builder].
 */
class Encryptionkit private constructor(
    private val encryptSymmetricUseCase: EncryptSymmetricUseCase,
    private val decryptSymmetricUseCase: DecryptSymmetricUseCase,
    private val encryptAsymmetricUseCase: EncryptAsymmetricUseCase,
    private val getSecurityLevelUseCase: GetSecurityLevelUseCase,
    private val hashDataUseCase: HashDataUseCase,
    private val config: EncryptionConfig
) {

    fun encrypt(data: ByteArray): CryptoResult {
        return encryptSymmetricUseCase(data, config)
    }

    fun decrypt(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        return decryptSymmetricUseCase(ciphertext, iv, config)
    }
    
    fun decrypt(result: CryptoResult): ByteArray {
        return decryptSymmetricUseCase(result.ciphertext, result.iv, config)
    }

    suspend fun encryptWithPublicKey(data: ByteArray): ByteArray {
        return encryptAsymmetricUseCase(data)
    }

    fun getSecurityLevel(): SecurityLevel {
        return getSecurityLevelUseCase(config.alias)
    }

    fun hash(data: ByteArray, algorithm: String = "SHA-256"): ByteArray {
        return hashDataUseCase(data, algorithm)
    }
    
    fun hashToHex(text: String, algorithm: String = "SHA-256"): String {
        return hashDataUseCase.toHexString(text.toByteArray(), algorithm)
    }

    class Builder() {
        private var alias: String = "encryption_kit_default_key"
        private var useStrongBox: Boolean = false
        private var requireUserAuth: Boolean = false
        private var certificatePathProvider: CertificatePathProvider? = null

        fun setAlias(alias: String) = apply { this.alias = alias }
        fun useStrongBox(useStrongBox: Boolean) = apply { this.useStrongBox = useStrongBox }
        fun setRequireUserAuthentication(require: Boolean) = apply { this.requireUserAuth = require }
        
        fun setCertificatePathProvider(provider: CertificatePathProvider) = apply { 
            this.certificatePathProvider = provider 
        }

        fun build(): Encryptionkit {
            val config = EncryptionConfig(alias, useStrongBox, requireUserAuth)
            
            // Manual Dependency Injection Wiring (Composition Root)
            val keystoreDataSource = KeystoreDataSource()
            
            val certProvider = certificatePathProvider ?: object : CertificatePathProvider {
                override fun getCertificatePath(): String? = null
            }
            val fileDataSource = FileDataSource(certProvider)
            
            val repository = EncryptionRepositoryImpl(keystoreDataSource, fileDataSource)
            
            // Initialize Key
            val initializeLibraryUseCase = InitializeLibraryUseCase(repository)
            initializeLibraryUseCase(config)

            return Encryptionkit(
                encryptSymmetricUseCase = EncryptSymmetricUseCase(repository),
                decryptSymmetricUseCase = DecryptSymmetricUseCase(repository),
                encryptAsymmetricUseCase = EncryptAsymmetricUseCase(repository),
                getSecurityLevelUseCase = GetSecurityLevelUseCase(repository),
                hashDataUseCase = HashDataUseCase(repository),
                config = config
            )
        }
    }
}
