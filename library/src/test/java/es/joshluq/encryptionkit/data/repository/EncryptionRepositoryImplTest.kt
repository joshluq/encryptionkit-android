package es.joshluq.encryptionkit.data.repository

import com.google.crypto.tink.Aead
import com.google.crypto.tink.DeterministicAead
import com.google.crypto.tink.Mac
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import com.google.crypto.tink.StreamingAead
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec

class EncryptionRepositoryImplTest {
    private val tinkDataSource: TinkDataSource = mockk()
    private val certificatePathProvider: CertificatePathProvider = mockk()
    private val logger: LoggerKit = mockk(relaxed = true)
    private lateinit var repository: EncryptionRepositoryImpl

    @Before
    fun setUp() {
        repository = EncryptionRepositoryImpl(tinkDataSource, certificatePathProvider, logger)
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
    fun `deleteKey should delegate to tinkDataSource deleteAead`() {
        val alias = "alias"
        every { tinkDataSource.deleteAead(alias) } returns Unit

        repository.deleteKey(alias)

        verify { tinkDataSource.deleteAead(alias) }
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
    fun `getPublicKey should read certificate and return public key`() =
        runTest {
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
    fun `getPublicKey should throw if file does not exist`() =
        runTest {
            every { certificatePathProvider.getCertificatePath() } returns "non_existent_file_path_12345"
            repository.getPublicKey()
        }

    @Test
    fun `encryptAsymmetric should use Cipher with OAEP`() =
        runTest {
            val mockPublicKey: PublicKey = mockk()
            val data = "secret".toByteArray()
            val encrypted = "encrypted_secret".toByteArray()

            mockkStatic(Cipher::class, MessageDigest::class)
            val mockCipher = mockk<Cipher>()
            val mockDigest = mockk<MessageDigest>()

            // Mock public key and hashing
            every { mockPublicKey.encoded } returns "key".toByteArray()
            every { MessageDigest.getInstance("SHA-256") } returns mockDigest
            // "hash" in hex is different but let's say it matches
            every { mockDigest.digest(any()) } returns byteArrayOf(0x68, 0x61, 0x73, 0x68)

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

    @Test
    fun `encryptStream should stream data through Tink StreamingAead`() {
        val mockStreamingAead: StreamingAead = mockk()
        val alias = "stream_alias"
        val data = "streaming data".toByteArray()
        val associatedData = "ad".toByteArray()
        val inStream = ByteArrayInputStream(data)
        val outStream = ByteArrayOutputStream()
        val encryptingStream = ByteArrayOutputStream()

        every { tinkDataSource.getStreamingAead(alias) } returns mockStreamingAead
        every { mockStreamingAead.newEncryptingStream(outStream, associatedData) } returns encryptingStream

        repository.encryptStream(inStream, outStream, alias, associatedData)

        assertArrayEquals(data, encryptingStream.toByteArray())
    }

    @Test
    fun `decryptStream should stream data through Tink StreamingAead`() {
        val mockStreamingAead: StreamingAead = mockk()
        val alias = "stream_alias"
        val data = "decrypted data".toByteArray()
        val associatedData = "ad".toByteArray()
        val inStream = ByteArrayInputStream("ciphertext".toByteArray())
        val outStream = ByteArrayOutputStream()
        val decryptingStream = ByteArrayInputStream(data)

        every { tinkDataSource.getStreamingAead(alias) } returns mockStreamingAead
        every { mockStreamingAead.newDecryptingStream(inStream, associatedData) } returns decryptingStream

        repository.decryptStream(inStream, outStream, alias, associatedData)

        assertArrayEquals(data, outStream.toByteArray())
    }

    @Test
    fun `encryptDeterministic should return CryptoResult when successful`() {
        val mockDaead: DeterministicAead = mockk()
        val alias = "daead_alias"
        val data = "data".toByteArray()
        val associatedData = "ad".toByteArray()
        val encrypted = "encrypted_deterministic".toByteArray()

        every { tinkDataSource.getDeterministicAead(alias) } returns mockDaead
        every { mockDaead.encryptDeterministically(data, associatedData) } returns encrypted

        val result = repository.encryptDeterministic(data, alias, associatedData)

        assertArrayEquals(encrypted, result.ciphertext)
    }

    @Test
    fun `decryptDeterministic should return decrypted data`() {
        val mockDaead: DeterministicAead = mockk()
        val alias = "daead_alias"
        val ciphertext = "encrypted_deterministic".toByteArray()
        val associatedData = "ad".toByteArray()
        val decrypted = "data".toByteArray()

        every { tinkDataSource.getDeterministicAead(alias) } returns mockDaead
        every { mockDaead.decryptDeterministically(ciphertext, associatedData) } returns decrypted

        val result = repository.decryptDeterministic(ciphertext, alias, associatedData)

        assertArrayEquals(decrypted, result)
    }

    @Test
    fun `rotateKey should delegate to tinkDataSource rotateAead`() {
        val alias = "alias"
        every { tinkDataSource.rotateAead(alias) } returns Unit

        repository.rotateKey(alias)

        verify { tinkDataSource.rotateAead(alias) }
    }

    @Test
    fun `sign should call tinkDataSource getPublicKeySign and return signature`() {
        val mockSigner: PublicKeySign = mockk()
        val alias = "sign_alias"
        val data = "payload".toByteArray()
        val signature = "signature".toByteArray()

        every { tinkDataSource.getPublicKeySign(alias) } returns mockSigner
        every { mockSigner.sign(data) } returns signature

        val result = repository.sign(data, alias)

        assertArrayEquals(signature, result)
    }

    @Test
    fun `verifySignature should return true when signature is valid`() {
        val mockVerifier: PublicKeyVerify = mockk()
        val alias = "sign_alias"
        val data = "payload".toByteArray()
        val signature = "signature".toByteArray()

        every { tinkDataSource.getPublicKeyVerify(alias) } returns mockVerifier
        every { mockVerifier.verify(signature, data) } returns Unit

        val result = repository.verifySignature(data, signature, alias)

        assertTrue(result)
    }

    @Test
    fun `verifySignature should return false when signature verification fails`() {
        val mockVerifier: PublicKeyVerify = mockk()
        val alias = "sign_alias"
        val data = "payload".toByteArray()
        val signature = "invalid_signature".toByteArray()

        every { tinkDataSource.getPublicKeyVerify(alias) } returns mockVerifier
        every { mockVerifier.verify(signature, data) } throws java.security.GeneralSecurityException("Invalid signature")

        val result = repository.verifySignature(data, signature, alias)

        assertFalse(result)
    }

    @Test
    fun `computeMac should call tinkDataSource getMac and return tag`() {
        val mockMac: Mac = mockk()
        val alias = "mac_alias"
        val data = "payload".toByteArray()
        val tag = "mac_tag".toByteArray()

        every { tinkDataSource.getMac(alias) } returns mockMac
        every { mockMac.computeMac(data) } returns tag

        val result = repository.computeMac(data, alias)

        assertArrayEquals(tag, result)
    }

    @Test
    fun `verifyMac should return true when MAC is valid`() {
        val mockMac: Mac = mockk()
        val alias = "mac_alias"
        val data = "payload".toByteArray()
        val tag = "mac_tag".toByteArray()

        every { tinkDataSource.getMac(alias) } returns mockMac
        every { mockMac.verifyMac(tag, data) } returns Unit

        val result = repository.verifyMac(data, tag, alias)

        assertTrue(result)
    }

    @Test
    fun `verifyMac should return false when MAC verification fails`() {
        val mockMac: Mac = mockk()
        val alias = "mac_alias"
        val data = "payload".toByteArray()
        val tag = "invalid_tag".toByteArray()

        every { tinkDataSource.getMac(alias) } returns mockMac
        every { mockMac.verifyMac(tag, data) } throws java.security.GeneralSecurityException("Tag mismatch")

        val result = repository.verifyMac(data, tag, alias)

        assertFalse(result)
    }
}
