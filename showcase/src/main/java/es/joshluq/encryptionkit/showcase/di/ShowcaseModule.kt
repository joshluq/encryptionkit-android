package es.joshluq.encryptionkit.showcase.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.encryptionkit.sdk.Encryptionkit
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
                // In a real scenario, this file would exist in the app's private storage or assets.
                val fakeCertFile = File(context.cacheDir, "fake_cert.crt")
                if (!fakeCertFile.exists()) {
                    // We could write a dummy cert here if needed, or return null to test fallback
                    return null
                }
                return fakeCertFile.absolutePath
            }
        }
    }

    @Provides
    @Singleton
    fun provideEncryptionKit(
        certificatePathProvider: CertificatePathProvider
    ): Encryptionkit {
        return Encryptionkit.Builder()
            .setAlias("showcase_secure_key")
            .useStrongBox(true) // Try to use Secure Element
            .setRequireUserAuthentication(false) // Set to true to test Biometrics
            .setCertificatePathProvider(certificatePathProvider)
            .build()
    }
}
