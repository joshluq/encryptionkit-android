package es.joshluq.encryptionkit.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.joshluq.encryptionkit.data.AesCryptoEngine
import es.joshluq.encryptionkit.data.AndroidKeyManager
import es.joshluq.encryptionkit.data.RealAsymmetricEncryption
import es.joshluq.encryptionkit.data.RealKeyRepository
import es.joshluq.encryptionkit.domain.AsymmetricEncryption
import es.joshluq.encryptionkit.domain.CryptoEngine
import es.joshluq.encryptionkit.domain.KeyManager
import es.joshluq.encryptionkit.domain.KeyRepository
import javax.inject.Singleton

/**
 * Hilt module for providing EncryptionKit dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class EncryptionKitModule {

    @Binds
    @Singleton
    abstract fun bindKeyManager(
        androidKeyManager: AndroidKeyManager
    ): KeyManager

    @Binds
    @Singleton
    abstract fun bindCryptoEngine(
        aesCryptoEngine: AesCryptoEngine
    ): CryptoEngine

    @Binds
    @Singleton
    abstract fun bindAsymmetricEncryption(
        realAsymmetricEncryption: RealAsymmetricEncryption
    ): AsymmetricEncryption

    @Binds
    @Singleton
    abstract fun bindKeyRepository(
        realKeyRepository: RealKeyRepository
    ): KeyRepository
}
