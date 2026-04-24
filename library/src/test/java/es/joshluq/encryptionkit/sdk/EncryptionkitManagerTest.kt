package es.joshluq.encryptionkit.sdk

import es.joshluq.encryptionkit.di.EncryptionkitComponent
import es.joshluq.encryptionkit.domain.model.*
import es.joshluq.encryptionkit.domain.usecase.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EncryptionkitManagerTest {

    private val component: EncryptionkitComponent = mockk()
    private val initializeLibraryUseCase: InitializeLibraryUseCase = mockk(relaxed = true)
    private val encryptSymmetricUseCase: EncryptSymmetricUseCase = mockk()
    private val decryptSymmetricUseCase: DecryptSymmetricUseCase = mockk()
    private val encryptAsymmetricUseCase: EncryptAsymmetricUseCase = mockk()
    private val getSecurityLevelUseCase: GetSecurityLevelUseCase = mockk()
    private val deleteKeyUseCase: DeleteKeyUseCase = mockk()
    private val hashDataUseCase: HashDataUseCase = mockk()

    private val config = EncryptionkitConfig.build {
        alias = "test_alias"
        useStrongBox = false
        requireUserAuth = false
    }

    private lateinit var manager: EncryptionkitManager

    @Before
    fun setUp() {
        coEvery { component.initializeLibraryUseCase } returns initializeLibraryUseCase
        coEvery { component.logger } returns mockk(relaxed = true)
        coEvery { component.encryptSymmetricUseCase } returns encryptSymmetricUseCase
        coEvery { component.decryptSymmetricUseCase } returns decryptSymmetricUseCase
        coEvery { component.encryptAsymmetricUseCase } returns encryptAsymmetricUseCase
        coEvery { component.getSecurityLevelUseCase } returns getSecurityLevelUseCase
        coEvery { component.deleteKeyUseCase } returns deleteKeyUseCase
        coEvery { component.hashDataUseCase } returns hashDataUseCase

        manager = EncryptionkitManager{ component }

        manager.initialize(config)
    }

    @Test
    fun `encrypt should return success result when successful`() = runBlocking {
        val data = byteArrayOf(1, 2, 3)
        val secureBytes = SecureBytes(data)
        val expectedResult = CryptoResult("cipher".toByteArray(), "iv".toByteArray())

        coEvery { encryptSymmetricUseCase(any()) } returns Result.success(EncryptSymmetricUseCase.Output(expectedResult))

        val result = manager.encrypt(secureBytes)

        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }

    @Test
    fun `decrypt should return success result when successful`() = runBlocking {
        val ciphertext = "cipher".toByteArray()
        val iv = "iv".toByteArray()
        val expectedPlaintext = "plain".toByteArray()

        coEvery { decryptSymmetricUseCase(any()) } returns Result.success(DecryptSymmetricUseCase.Output(expectedPlaintext))

        val result = manager.decrypt(ciphertext, iv)

        assertTrue(result.isSuccess)
        assertArrayEquals(expectedPlaintext, result.getOrNull())
    }

    @Test
    fun `encryptWithPublicKey should return success result when successful`() = runBlocking {
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
    fun `getSecurityLevel should return success result when successful`() = runBlocking {
        val expectedLevel = SecurityLevel.STRONGBOX

        coEvery { getSecurityLevelUseCase(any()) } returns Result.success(GetSecurityLevelUseCase.Output(expectedLevel))

        val result = manager.getSecurityLevel()

        assertTrue(result.isSuccess)
        assertEquals(expectedLevel, result.getOrNull())
    }

    @Test
    fun `deleteKey should return success when successful`() = runBlocking {
        coEvery { deleteKeyUseCase(any()) } returns Result.success(es.joshluq.foundationkit.usecase.NoneOutput)

        val result = manager.deleteKey()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `hash should return success result when successful`() = runBlocking {
        val data = byteArrayOf(1, 2, 3)
        val expectedHash = byteArrayOf(4, 5, 6)

        coEvery { hashDataUseCase(any()) } returns Result.success(HashDataUseCase.Output(expectedHash))

        val result = manager.hash(data)

        assertTrue(result.isSuccess)
        assertArrayEquals(expectedHash, result.getOrNull())
    }

    @Test
    fun `hashToHex should return hex string result`() = runBlocking {
        val text = "test"
        val mockHash = byteArrayOf(0x00, 0xff.toByte())

        coEvery { hashDataUseCase(any()) } returns Result.success(HashDataUseCase.Output(mockHash))

        val result = manager.hashToHex(text)

        assertTrue(result.isSuccess)
        assertEquals("00ff", result.getOrNull())
    }

    @Test
    fun `any function should return failure when use case fails`() = runBlocking {
        val secureBytes = SecureBytes("data".toByteArray())
        val exception = Exception("Encryption failed")

        coEvery { encryptSymmetricUseCase(any()) } returns Result.failure(exception)

        val result = manager.encrypt(secureBytes)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CryptoException)
        assertEquals("Encryption failed", result.exceptionOrNull()?.message)
    }
}
