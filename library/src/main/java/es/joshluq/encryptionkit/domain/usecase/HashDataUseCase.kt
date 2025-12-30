package es.joshluq.encryptionkit.domain.usecase

import es.joshluq.encryptionkit.domain.repository.EncryptionRepository

class HashDataUseCase(
    private val repository: EncryptionRepository
) {
    operator fun invoke(data: ByteArray, algorithm: String = "SHA-256"): ByteArray {
        return repository.hash(data, algorithm)
    }

    fun toHexString(data: ByteArray, algorithm: String = "SHA-256"): String {
        return invoke(data, algorithm).joinToString("") { "%02x".format(it) }
    }
}
