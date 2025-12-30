package es.joshluq.encryptionkit.data

import es.joshluq.encryptionkit.domain.KeyManager
import es.joshluq.encryptionkit.domain.KeyRepository
import es.joshluq.encryptionkit.domain.PublicKeyProvider
import es.joshluq.encryptionkit.domain.SecurityLevel
import java.security.PublicKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [KeyRepository] delegating to [KeyManager] and [PublicKeyProvider].
 */
@Singleton
class RealKeyRepository @Inject constructor(
    private val keyManager: KeyManager,
    private val publicKeyProvider: PublicKeyProvider
) : KeyRepository {

    override suspend fun getPublicKey(): PublicKey? {
        return publicKeyProvider.getPublicKey()
    }

    override fun isKeyReady(alias: String): Boolean {
        return keyManager.hasKey(alias)
    }

    override fun generateSymmetricKey(alias: String, requireUserAuth: Boolean, useStrongBox: Boolean) {
        keyManager.generateKey(alias, requireUserAuth, useStrongBox)
    }

    override fun deleteSymmetricKey(alias: String) {
        keyManager.deleteKey(alias)
    }

    override fun getSecurityLevel(alias: String): SecurityLevel {
        return keyManager.getSecurityLevel(alias)
    }
}
