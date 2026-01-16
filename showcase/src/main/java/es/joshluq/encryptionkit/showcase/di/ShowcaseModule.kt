package es.joshluq.encryptionkit.showcase.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.encryptionkit.sdk.EncryptionkitManager
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ShowcaseModule {

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
        certificatePathProvider: CertificatePathProvider
    ): EncryptionkitManager {
        return EncryptionkitManager.Builder()
            .setAlias("showcase_secure_key")
            .useStrongBox(true) 
            .setRequireUserAuthentication(false)
            .setCertificatePathProvider(certificatePathProvider)
            .build()
    }
}
