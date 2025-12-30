package es.joshluq.encryptionkit.showcase.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.joshluq.encryptionkit.domain.EncryptionkitConfig
import es.joshluq.encryptionkit.domain.EncryptionkitConfigProvider
import es.joshluq.encryptionkit.domain.PublicKeyProvider
import java.security.KeyPairGenerator
import java.security.PublicKey
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ShowcaseModule {

    @Provides
    @Singleton
    fun provideEncryptionkitConfigProvider(): EncryptionkitConfigProvider {
        return object : EncryptionkitConfigProvider {
            override val config: EncryptionkitConfig = EncryptionkitConfig(
                alias = "showcase_secure_key",
                useStrongBox = true,
                requireUserAuth = false
            )
        }
    }

    @Provides
    @Singleton
    fun providePublicKeyProvider(fakePublicKey: PublicKey): PublicKeyProvider {
        return object : PublicKeyProvider {
            override suspend fun getPublicKey(): PublicKey {
                return fakePublicKey
            }
        }
    }

    @Provides
    @Singleton
    fun provideFakePublicKey(): PublicKey {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair().public
    }
}
