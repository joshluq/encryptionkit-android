package es.joshluq.encryptionkit.sdk

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import es.joshluq.encryptionkit.data.provider.SecureDataStoreProvider
import es.joshluq.encryptionkit.di.EncryptionKitComponent
import es.joshluq.encryptionkit.domain.model.CryptoException
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import es.joshluq.encryptionkit.domain.usecase.ComputeMacUseCase
import es.joshluq.encryptionkit.domain.usecase.DecryptDeterministicUseCase
import es.joshluq.encryptionkit.domain.usecase.DecryptStreamUseCase
import es.joshluq.encryptionkit.domain.usecase.DecryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.DeleteKeyUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptAsymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptDeterministicUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptStreamUseCase
import es.joshluq.encryptionkit.domain.usecase.EncryptSymmetricUseCase
import es.joshluq.encryptionkit.domain.usecase.GetSecurityLevelUseCase
import es.joshluq.encryptionkit.domain.usecase.HashDataUseCase
import es.joshluq.encryptionkit.domain.usecase.InitializeLibraryUseCase
import es.joshluq.encryptionkit.domain.usecase.RotateKeyUseCase
import es.joshluq.encryptionkit.domain.usecase.SignDataUseCase
import es.joshluq.encryptionkit.domain.usecase.VerifyMacUseCase
import es.joshluq.encryptionkit.domain.usecase.VerifySignatureUseCase
import es.joshluq.foundationkit.provider.SerializerProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class EncryptionKitTest {
    private val component: EncryptionKitComponent = mockk()
    private val initializeLibraryUseCase: InitializeLibraryUseCase = mockk(relaxed = true)
    private val encryptSymmetricUseCase: EncryptSymmetricUseCase = mockk()
    private val decryptSymmetricUseCase: DecryptSymmetricUseCase = mockk()
    private val encryptAsymmetricUseCase: EncryptAsymmetricUseCase = mockk()
    private val getSecurityLevelUseCase: GetSecurityLevelUseCase = mockk()
    private val deleteKeyUseCase: DeleteKeyUseCase = mockk()
    private val hashDataUseCase: HashDataUseCase = mockk()
    private val encryptStreamUseCase: EncryptStreamUseCase = mockk()
    private val decryptStreamUseCase: DecryptStreamUseCase = mockk()
    private val encryptDeterministicUseCase: EncryptDeterministicUseCase = mockk()
    private val decryptDeterministicUseCase: DecryptDeterministicUseCase = mockk()
    private val rotateKeyUseCase: RotateKeyUseCase = mockk()
    private val signDataUseCase: SignDataUseCase = mockk()
    private val verifySignatureUseCase: VerifySignatureUseCase = mockk()
    private val computeMacUseCase: ComputeMacUseCase = mockk()
    private val verifyMacUseCase: VerifyMacUseCase = mockk()

    private val context: android.content.Context = mockk(relaxed = true)
    private val config =
        EncryptionKitBuilder(context)
            .apply {
                alias = "test_alias"
            }.build()

    private lateinit var manager: EncryptionKit

    @Before
    fun setUp() {
        every { component.initializeLibraryUseCase } returns initializeLibraryUseCase
        every { component.logger } returns mockk(relaxed = true)
        every { component.encryptSymmetricUseCase } returns encryptSymmetricUseCase
        every { component.decryptSymmetricUseCase } returns decryptSymmetricUseCase
        every { component.encryptAsymmetricUseCase } returns encryptAsymmetricUseCase
        every { component.getSecurityLevelUseCase } returns getSecurityLevelUseCase
        every { component.deleteKeyUseCase } returns deleteKeyUseCase
        every { component.hashDataUseCase } returns hashDataUseCase
        every { component.encryptStreamUseCase } returns encryptStreamUseCase
        every { component.decryptStreamUseCase } returns decryptStreamUseCase
        every { component.encryptDeterministicUseCase } returns encryptDeterministicUseCase
        every { component.decryptDeterministicUseCase } returns decryptDeterministicUseCase
        every { component.rotateKeyUseCase } returns rotateKeyUseCase
        every { component.signDataUseCase } returns signDataUseCase
        every { component.verifySignatureUseCase } returns verifySignatureUseCase
        every { component.computeMacUseCase } returns computeMacUseCase
        every { component.verifyMacUseCase } returns verifyMacUseCase

        manager = EncryptionKit { component }

        manager.initialize(config)
    }

    @Test
    fun `encrypt should return success result when successful`() =
        runBlocking {
            val data = byteArrayOf(1, 2, 3)
            val associatedData = "ad".toByteArray()
            val secureBytes = SecureBytes(data)
            val expectedResult = CryptoResult("cipher".toByteArray())

            coEvery { encryptSymmetricUseCase(match { it.associatedData.contentEquals(associatedData) }) } returns
                Result.success(EncryptSymmetricUseCase.Output(expectedResult))

            val result = manager.encrypt(secureBytes, associatedData)

            assertTrue(result.isSuccess)
            assertEquals(expectedResult, result.getOrNull())
        }

    @Test
    fun `decrypt should return success result when successful`() =
        runBlocking {
            val ciphertext = "cipher".toByteArray()
            val associatedData = "ad".toByteArray()
            val expectedPlaintext = "plain".toByteArray()

            coEvery { decryptSymmetricUseCase(match { it.associatedData.contentEquals(associatedData) }) } returns
                Result.success(DecryptSymmetricUseCase.Output(expectedPlaintext))

            val result = manager.decrypt(ciphertext, associatedData)

            assertTrue(result.isSuccess)
            assertArrayEquals(expectedPlaintext, result.getOrNull()?.data)
        }

    @Test
    fun `encryptWithPublicKey should return success result when successful`() =
        runBlocking {
            val data = "data".toByteArray()
            val expectedCiphertext = "cipher_asym".toByteArray()

            // Ensure config has public key hash
            val configWithHash = config.copy(publicKeyHash = "some_hash")
            manager.initialize(configWithHash)

            coEvery { encryptAsymmetricUseCase(any()) } returns Result.success(EncryptAsymmetricUseCase.Output(expectedCiphertext))

            val result = manager.encryptWithPublicKey(data)

            assertTrue(result.isSuccess)
            assertArrayEquals(expectedCiphertext, result.getOrNull())
        }

    @Test
    fun `getSecurityLevel should return success result when successful`() =
        runBlocking {
            val expectedLevel = SecurityLevel.STRONGBOX

            coEvery { getSecurityLevelUseCase(any()) } returns Result.success(GetSecurityLevelUseCase.Output(expectedLevel))

            val result = manager.getSecurityLevel()

            assertTrue(result.isSuccess)
            assertEquals(expectedLevel, result.getOrNull())
        }

    @Test
    fun `deleteKey should return success when successful`() =
        runBlocking {
            coEvery { deleteKeyUseCase(any()) } returns Result.success(es.joshluq.foundationkit.usecase.NoneOutput)

            val result = manager.deleteKey()

            assertTrue(result.isSuccess)
        }

    @Test
    fun `hash should return success result when successful`() =
        runBlocking {
            val data = byteArrayOf(1, 2, 3)
            val expectedHash = byteArrayOf(4, 5, 6)

            coEvery { hashDataUseCase(any()) } returns Result.success(HashDataUseCase.Output(expectedHash))

            val result = manager.hash(data)

            assertTrue(result.isSuccess)
            assertArrayEquals(expectedHash, result.getOrNull())
        }

    @Test
    fun `hashToHex should return hex string result`() =
        runBlocking {
            val text = "test"
            val mockHash = byteArrayOf(0x00, 0xff.toByte())

            coEvery { hashDataUseCase(any()) } returns Result.success(HashDataUseCase.Output(mockHash))

            val result = manager.hashToHex(text)

            assertTrue(result.isSuccess)
            assertEquals("00ff", result.getOrNull())
        }

    @Test
    fun `any function should return failure when use case fails`() =
        runBlocking {
            val secureBytes = SecureBytes("data".toByteArray())
            val exception = Exception("Encryption failed")

            coEvery { encryptSymmetricUseCase(any()) } returns Result.failure(exception)

            val result = manager.encrypt(secureBytes)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is CryptoException)
            assertEquals("Encryption failed", result.exceptionOrNull()?.message)
        }

    @Test
    fun `createSecureStorage should return SecureDataStoreProvider instance`() {
        val dataStore: DataStore<Preferences> = mockk()
        val serializerProvider: SerializerProvider = mockk()

        val storageProvider = manager.createSecureStorage(dataStore, serializerProvider)

        assertTrue(storageProvider is SecureDataStoreProvider)
    }

    @Test
    fun `encryptStream should return success result when successful`() =
        runBlocking {
            val inStream = ByteArrayInputStream("test".toByteArray())
            val outStream = ByteArrayOutputStream()
            val associatedData = "ad".toByteArray()

            coEvery { encryptStreamUseCase(any()) } returns Result.success(EncryptStreamUseCase.Output)

            val result = manager.encryptStream(inStream, outStream, associatedData)

            assertTrue(result.isSuccess)
        }

    @Test
    fun `decryptStream should return success result when successful`() =
        runBlocking {
            val inStream = ByteArrayInputStream("test".toByteArray())
            val outStream = ByteArrayOutputStream()
            val associatedData = "ad".toByteArray()

            coEvery { decryptStreamUseCase(any()) } returns Result.success(DecryptStreamUseCase.Output)

            val result = manager.decryptStream(inStream, outStream, associatedData)

            assertTrue(result.isSuccess)
        }

    @Test
    fun `encryptDeterministic should return success result when successful`() =
        runBlocking {
            val data = byteArrayOf(1, 2, 3)
            val associatedData = "ad".toByteArray()
            val secureBytes = SecureBytes(data)
            val expectedResult = CryptoResult("cipher".toByteArray())

            coEvery { encryptDeterministicUseCase(any()) } returns
                Result.success(EncryptDeterministicUseCase.Output(expectedResult))

            val result = manager.encryptDeterministic(secureBytes, associatedData)

            assertTrue(result.isSuccess)
            assertEquals(expectedResult, result.getOrNull())
        }

    @Test
    fun `decryptDeterministic should return success result when successful`() =
        runBlocking {
            val ciphertext = "cipher".toByteArray()
            val associatedData = "ad".toByteArray()
            val expectedPlaintext = "plain".toByteArray()

            coEvery { decryptDeterministicUseCase(any()) } returns
                Result.success(DecryptDeterministicUseCase.Output(expectedPlaintext))

            val result = manager.decryptDeterministic(ciphertext, associatedData)

            assertTrue(result.isSuccess)
            assertArrayEquals(expectedPlaintext, result.getOrNull()?.data)
        }

    @Test
    fun `rotateKey should return success result when successful`() =
        runBlocking {
            coEvery { rotateKeyUseCase(any()) } returns Result.success(es.joshluq.foundationkit.usecase.NoneOutput)

            val result = manager.rotateKey()

            assertTrue(result.isSuccess)
        }

    @Test
    fun `rotateKey with custom alias should pass alias to usecase`() =
        runBlocking {
            val customAlias = "custom_key_alias"
            coEvery { rotateKeyUseCase(RotateKeyUseCase.Input(customAlias)) } returns
                Result.success(es.joshluq.foundationkit.usecase.NoneOutput)

            val result = manager.rotateKey(customAlias)

            assertTrue(result.isSuccess)
        }

    @Test
    fun `createEncryptedStringConverter should return EncryptedStringConverter instance`() {
        val converter = manager.createEncryptedStringConverter()
        org.junit.Assert.assertNotNull(converter)
    }

    @Test
    fun `createEncryptedByteArrayConverter should return EncryptedByteArrayConverter instance`() {
        val converter = manager.createEncryptedByteArrayConverter()
        org.junit.Assert.assertNotNull(converter)
    }

    @Test
    fun `sign should return signature bytes on success`() =
        runBlocking {
            val data = "payload".toByteArray()
            val expectedSignature = "signature".toByteArray()

            coEvery { signDataUseCase(any()) } returns Result.success(SignDataUseCase.Output(expectedSignature))

            val result = manager.sign(data)

            assertTrue(result.isSuccess)
            assertArrayEquals(expectedSignature, result.getOrNull())
        }

    @Test
    fun `verifySignature should return boolean on success`() =
        runBlocking {
            val data = "payload".toByteArray()
            val signature = "signature".toByteArray()

            coEvery { verifySignatureUseCase(any()) } returns Result.success(VerifySignatureUseCase.Output(true))

            val result = manager.verifySignature(data, signature)

            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull() == true)
        }

    @Test
    fun `computeMac should return MAC tag bytes on success`() =
        runBlocking {
            val data = "payload".toByteArray()
            val expectedTag = "mac_tag".toByteArray()

            coEvery { computeMacUseCase(any()) } returns Result.success(ComputeMacUseCase.Output(expectedTag))

            val result = manager.computeMac(data)

            assertTrue(result.isSuccess)
            assertArrayEquals(expectedTag, result.getOrNull())
        }

    @Test
    fun `verifyMac should return boolean on success`() =
        runBlocking {
            val data = "payload".toByteArray()
            val mac = "mac_tag".toByteArray()

            coEvery { verifyMacUseCase(any()) } returns Result.success(VerifyMacUseCase.Output(true))

            val result = manager.verifyMac(data, mac)

            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull() == true)
        }

    @Test
    fun `createBiometricCryptoHelper should return BiometricCryptoHelper instance`() {
        val helper = manager.createBiometricCryptoHelper()
        org.junit.Assert.assertNotNull(helper)
    }
}
