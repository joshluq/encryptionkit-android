package es.joshluq.encryptionkit.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.crypto.tink.Aead
import es.joshluq.encryptionkit.data.datasource.TinkDataSource
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.provider.CertificatePathProvider
import es.joshluq.foundationkit.log.LoggerKit
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec

class EncryptionRepositoryImplTest {

    private val context: Context = mockk(relaxed = true)
    private val tinkDataSource: TinkDataSource = mockk()
    private val certificatePathProvider: CertificatePathProvider = mockk()
    private val logger: LoggerKit = mockk(relaxed = true)
    private lateinit var repository: EncryptionRepositoryImpl

    @Before
    fun setUp() {
        repository = EncryptionRepositoryImpl(tinkDataSource, certificatePathProvider, logger, context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initializeKey should call tinkDataSource`() {
        val alias = "alias"
        every { tinkDataSource.getAead(alias) } returns mockk()

        repository.initializeKey(alias)

        verify { tinkDataSource.getAead(alias) }
    }

    @Test
    fun `encryptSymmetric should return CryptoResult when successful`() {
        val mockAead: Aead = mockk()
        val alias = "alias"
        val data = "data".toByteArray()
        val associatedData = "ad".toByteArray()
        val encrypted = "encrypted".toByteArray()

        every { tinkDataSource.getAead(alias) } returns mockAead
        every { mockAead.encrypt(data, associatedData) } returns encrypted

        val result = repository.encryptSymmetric(data, alias, associatedData)

        assertEquals(encrypted, result.ciphertext)
    }

    @Test
    fun `decryptSymmetric should return decrypted data`() {
        val mockAead: Aead = mockk()
        val alias = "alias"
        val ciphertext = "encrypted".toByteArray()
        val associatedData = "ad".toByteArray()
        val decrypted = "data".toByteArray()

        every { tinkDataSource.getAead(alias) } returns mockAead
        every { mockAead.decrypt(ciphertext, associatedData) } returns decrypted

        val result = repository.decryptSymmetric(ciphertext, alias, associatedData)

        assertArrayEquals(decrypted, result)
    }

    @Test
    fun `deleteKey should clear shared prefs and keystore entry`() {
        mockkStatic(KeyStore::class)
        val mockPrefs = mockk<SharedPreferences>(relaxed = true)
        val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)
        val mockKeystore = mockk<KeyStore>(relaxed = true)
        
        every { context.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        
        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeystore
        every { mockKeystore.containsAlias("alias") } returns true

        repository.deleteKey("alias")

        verify { mockEditor.clear() }
        verify { mockKeystore.deleteEntry("alias") }
    }

    @Test
    fun `hash should return digest from MessageDigest`() {
        mockkStatic(MessageDigest::class)
        val mockDigest: MessageDigest = mockk()
        val data = "test".toByteArray()
        val expectedHash = "hash".toByteArray()

        every { MessageDigest.getInstance("SHA-256") } returns mockDigest
        every { mockDigest.digest(data) } returns expectedHash

        val result = repository.hash(data, "SHA-256")

        assertArrayEquals(expectedHash, result)
    }

    @Test
    fun `getPublicKey should read certificate and return public key`() = runTest {
        val tempFile = File.createTempFile("test_cert", ".crt")
        tempFile.writeText("dummy cert")
        every { certificatePathProvider.getCertificatePath() } returns tempFile.absolutePath
        
        mockkStatic(CertificateFactory::class)
        val mockCertFactory = mockk<CertificateFactory>()
        val mockCertificate = mockk<Certificate>()
        val mockPublicKey = mockk<PublicKey>()
        
        every { CertificateFactory.getInstance("X.509") } returns mockCertFactory
        every { mockCertFactory.generateCertificate(any()) } returns mockCertificate
        every { mockCertificate.publicKey } returns mockPublicKey

        val result = repository.getPublicKey()

        assertEquals(mockPublicKey, result)
        tempFile.delete()
    }

    @Test(expected = CryptoException::class)
    fun `getPublicKey should throw if file does not exist`() = runTest {
        every { certificatePathProvider.getCertificatePath() } returns "non_existent_file_path_12345"
        repository.getPublicKey()
    }

    @Test
    fun `encryptAsymmetric should use Cipher with OAEP`() = runTest {
        val mockPublicKey: PublicKey = mockk()
        val data = "secret".toByteArray()
        val encrypted = "encrypted_secret".toByteArray()

        mockkStatic(Cipher::class, MessageDigest::class)
        val mockCipher = mockk<Cipher>()
        val mockDigest = mockk<MessageDigest>()

        // Mock public key and hashing
        every { mockPublicKey.encoded } returns "key".toByteArray()
        every { MessageDigest.getInstance("SHA-256") } returns mockDigest
        every { mockDigest.digest(any()) } returns byteArrayOf(0x68, 0x61, 0x73, 0x68) // "hash" in hex is different but let's say it matches

        // Mock Repository.getPublicKey (it's internal, so we mock the certificate provider instead)
        val tempFile = File.createTempFile("test_cert_2", ".crt")
        every { certificatePathProvider.getCertificatePath() } returns tempFile.absolutePath
        mockkStatic(CertificateFactory::class)
        val mockCertFactory = mockk<CertificateFactory>()
        val mockCertificate = mockk<Certificate>()
        every { CertificateFactory.getInstance("X.509") } returns mockCertFactory
        every { mockCertFactory.generateCertificate(any()) } returns mockCertificate
        every { mockCertificate.publicKey } returns mockPublicKey

        every { Cipher.getInstance("RSA/ECB/OAEPPadding") } returns mockCipher
        every { mockCipher.init(Cipher.ENCRYPT_MODE, mockPublicKey, any<OAEPParameterSpec>()) } returns Unit
        every { mockCipher.doFinal(data) } returns encrypted

        // Use a hash that will match our mocked digest output "68617368"
        val result = repository.encryptAsymmetric(data, "68617368")

        assertArrayEquals(encrypted, result)
        tempFile.delete()
    }
}
