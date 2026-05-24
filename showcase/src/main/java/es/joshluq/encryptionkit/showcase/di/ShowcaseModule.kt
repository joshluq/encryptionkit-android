package es.joshluq.encryptionkit.showcase.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.encryptionkit.sdk.EncryptionKitManager
import es.joshluq.foundationkit.provider.SerializerProvider
import es.joshluq.foundationkit.provider.StorageProvider
import java.io.File
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "showcase_prefs")

@Module
@InstallIn(SingletonComponent::class)
object ShowcaseModule {

    @Provides
    @Singleton
    fun provideSerializerProvider(): SerializerProvider {
        return object : SerializerProvider {
            override fun <T : Any> serialize(value: T, type: Class<T>): String {
                return value.toString()
            }

            @Suppress("UNCHECKED_CAST")
            override fun <T : Any> deserialize(value: String, type: Class<T>): T {
                return when (type) {
                    String::class.java -> value as T
                    else -> throw IllegalArgumentException("Unsupported type in showcase serializer")
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideSecureStorage(
        @ApplicationContext context: Context,
        encryptionkitManager: EncryptionKitManager,
        serializerProvider: SerializerProvider
    ): StorageProvider {
        return encryptionkitManager.createSecureStorage(
            dataStore = context.dataStore,
            serializerProvider = serializerProvider
        )
    }

    @Provides
    @Singleton
    fun provideCertificatePathProvider(@ApplicationContext context: Context): CertificatePathProvider {
        return object : CertificatePathProvider {
            override fun getCertificatePath(): String? {
                // For showcase purposes, we can simulate a certificate file.
                val fakeCertFile = File(context.cacheDir, "fake_cert.crt")
                if (!fakeCertFile.exists()) {
                    return null
                }
                return fakeCertFile.absolutePath
            }
        }
    }

    @Provides
    @Singleton
    fun provideEncryptionKitManager(
        @ApplicationContext context: Context,
        certificatePathProvider: CertificatePathProvider
    ): EncryptionKitManager {
        return EncryptionKitManager.build(context) {
            alias = "showcase_secure_key"
            this.certificatePathProvider = certificatePathProvider
        }
    }
}
