package es.joshluq.encryptionkit.data.datasource

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import es.joshluq.foundationkit.log.LoggerKit
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import java.security.GeneralSecurityException

class TinkDataSourceTest {
    private val context: Context = mockk(relaxed = true)
    private val logger: LoggerKit = mockk(relaxed = true)
    private lateinit var dataSource: TinkDataSource

    @Before
    fun setUp() {
        dataSource = TinkDataSource(context, logger)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getAead should cache Aead instance for same alias`() {
        mockkConstructor(AndroidKeysetManager.Builder::class)
        val mockManager = mockk<AndroidKeysetManager>(relaxed = true)
        val mockKeysetHandle = mockk<KeysetHandle>(relaxed = true)
        val mockAead = mockk<Aead>()

        every { anyConstructed<AndroidKeysetManager.Builder>().withSharedPref(any(), any(), any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withKeyTemplate(any<KeyTemplate>()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withMasterKeyUri(any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().build() } returns mockManager

        every { mockManager.keysetHandle } returns mockKeysetHandle
        every { mockKeysetHandle.getPrimitive(any(), Aead::class.java) } returns mockAead

        val alias = "test_alias"
        val aead1 = dataSource.getAead(alias)
        val aead2 = dataSource.getAead(alias)

        assertSame(aead1, aead2)
    }

    @Test
    fun `getAead should recover when first initialization fails`() {
        mockkConstructor(AndroidKeysetManager.Builder::class)
        val mockManager = mockk<AndroidKeysetManager>(relaxed = true)
        val mockKeysetHandle = mockk<KeysetHandle>(relaxed = true)
        val mockAead = mockk<Aead>()

        every { anyConstructed<AndroidKeysetManager.Builder>().withSharedPref(any(), any(), any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withKeyTemplate(any<KeyTemplate>()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withMasterKeyUri(any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        // Fail on first build(), succeed on subsequent
        every { anyConstructed<AndroidKeysetManager.Builder>().build() } throws GeneralSecurityException("Keystore error") andThen
            mockManager

        every { mockManager.keysetHandle } returns mockKeysetHandle
        every { mockKeysetHandle.getPrimitive(any(), Aead::class.java) } returns mockAead

        val alias = "test_alias"
        val aead = dataSource.getAead(alias)

        assertSame(mockAead, aead)
    }

    @Test
    fun `deleteAead should remove cached Aead and clear storage`() {
        mockkConstructor(AndroidKeysetManager.Builder::class)
        val mockManager = mockk<AndroidKeysetManager>(relaxed = true)
        val mockKeysetHandle = mockk<KeysetHandle>(relaxed = true)
        val mockAead1 = mockk<Aead>()
        val mockAead2 = mockk<Aead>()

        every { anyConstructed<AndroidKeysetManager.Builder>().withSharedPref(any(), any(), any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withKeyTemplate(any<KeyTemplate>()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withMasterKeyUri(any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().build() } returns mockManager

        every { mockManager.keysetHandle } returns mockKeysetHandle
        every { mockKeysetHandle.getPrimitive(any(), Aead::class.java) } returns mockAead1 andThen mockAead2

        val alias = "test_alias"
        val aead1 = dataSource.getAead(alias)
        assertSame(mockAead1, aead1)

        dataSource.deleteAead(alias)

        val aead2 = dataSource.getAead(alias)
        assertSame(mockAead2, aead2)
    }

    @Test
    fun `getStreamingAead should cache StreamingAead instance for same alias`() {
        mockkConstructor(AndroidKeysetManager.Builder::class)
        val mockManager = mockk<AndroidKeysetManager>(relaxed = true)
        val mockKeysetHandle = mockk<KeysetHandle>(relaxed = true)
        val mockStreamingAead = mockk<com.google.crypto.tink.StreamingAead>()

        every { anyConstructed<AndroidKeysetManager.Builder>().withSharedPref(any(), any(), any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withKeyTemplate(any<KeyTemplate>()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withMasterKeyUri(any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().build() } returns mockManager

        every { mockManager.keysetHandle } returns mockKeysetHandle
        every { mockKeysetHandle.getPrimitive(any(), com.google.crypto.tink.StreamingAead::class.java) } returns mockStreamingAead

        val alias = "test_stream_alias"
        val saead1 = dataSource.getStreamingAead(alias)
        val saead2 = dataSource.getStreamingAead(alias)

        assertSame(saead1, saead2)
    }

    @Test
    fun `getDeterministicAead should cache DeterministicAead instance for same alias`() {
        mockkConstructor(AndroidKeysetManager.Builder::class)
        val mockManager = mockk<AndroidKeysetManager>(relaxed = true)
        val mockKeysetHandle = mockk<KeysetHandle>(relaxed = true)
        val mockDaead = mockk<com.google.crypto.tink.DeterministicAead>()

        every { anyConstructed<AndroidKeysetManager.Builder>().withSharedPref(any(), any(), any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withKeyTemplate(any<KeyTemplate>()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withMasterKeyUri(any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().build() } returns mockManager

        every { mockManager.keysetHandle } returns mockKeysetHandle
        every { mockKeysetHandle.getPrimitive(any(), com.google.crypto.tink.DeterministicAead::class.java) } returns mockDaead

        val alias = "test_daead_alias"
        val daead1 = dataSource.getDeterministicAead(alias)
        val daead2 = dataSource.getDeterministicAead(alias)

        assertSame(daead1, daead2)
    }

    @Test
    fun `rotateAead should add new key, set it as primary and invalidate cache`() {
        mockkConstructor(AndroidKeysetManager.Builder::class)
        val mockManager = mockk<AndroidKeysetManager>(relaxed = true)
        val mockKeysetHandle = mockk<KeysetHandle>(relaxed = true)
        val mockEntry = mockk<com.google.crypto.tink.KeysetHandle.Entry>(relaxed = true)
        val mockAead1 = mockk<Aead>()
        val mockAead2 = mockk<Aead>()

        every { anyConstructed<AndroidKeysetManager.Builder>().withSharedPref(any(), any(), any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withKeyTemplate(any<KeyTemplate>()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withMasterKeyUri(any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().build() } returns mockManager

        every { mockManager.keysetHandle } returns mockKeysetHandle
        every { mockKeysetHandle.size() } returns 2
        every { mockKeysetHandle.getAt(1) } returns mockEntry
        every { mockEntry.id } returns 42
        every { mockManager.add(any<KeyTemplate>()) } returns mockManager
        every { mockManager.setPrimary(42) } returns mockManager
        every { mockKeysetHandle.getPrimitive(any(), Aead::class.java) } returns mockAead1 andThen mockAead2

        val alias = "rotate_alias"
        val aead1 = dataSource.getAead(alias)
        assertSame(mockAead1, aead1)

        dataSource.rotateAead(alias)

        io.mockk.verify { mockManager.add(any<KeyTemplate>()) }
        io.mockk.verify { mockManager.setPrimary(42) }

        val aead2 = dataSource.getAead(alias)
        assertSame(mockAead2, aead2)
    }

    @Test
    fun `getPublicKeySign should cache PublicKeySign instance for same alias`() {
        mockkConstructor(AndroidKeysetManager.Builder::class)
        val mockManager = mockk<AndroidKeysetManager>(relaxed = true)
        val mockKeysetHandle = mockk<KeysetHandle>(relaxed = true)
        val mockSigner = mockk<com.google.crypto.tink.PublicKeySign>()

        every { anyConstructed<AndroidKeysetManager.Builder>().withSharedPref(any(), any(), any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withKeyTemplate(any<KeyTemplate>()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withMasterKeyUri(any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().build() } returns mockManager

        every { mockManager.keysetHandle } returns mockKeysetHandle
        every { mockKeysetHandle.getPrimitive(any(), com.google.crypto.tink.PublicKeySign::class.java) } returns mockSigner

        val alias = "test_sign_alias"
        val sign1 = dataSource.getPublicKeySign(alias)
        val sign2 = dataSource.getPublicKeySign(alias)

        assertSame(sign1, sign2)
    }

    @Test
    fun `getPublicKeyVerify should cache PublicKeyVerify instance for same alias`() {
        mockkConstructor(AndroidKeysetManager.Builder::class)
        val mockManager = mockk<AndroidKeysetManager>(relaxed = true)
        val mockKeysetHandle = mockk<KeysetHandle>(relaxed = true)
        val mockPublicKeysetHandle = mockk<KeysetHandle>(relaxed = true)
        val mockVerifier = mockk<com.google.crypto.tink.PublicKeyVerify>()

        every { anyConstructed<AndroidKeysetManager.Builder>().withSharedPref(any(), any(), any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withKeyTemplate(any<KeyTemplate>()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withMasterKeyUri(any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().build() } returns mockManager

        every { mockManager.keysetHandle } returns mockKeysetHandle
        every { mockKeysetHandle.publicKeysetHandle } returns mockPublicKeysetHandle
        every { mockPublicKeysetHandle.getPrimitive(any(), com.google.crypto.tink.PublicKeyVerify::class.java) } returns mockVerifier

        val alias = "test_verify_alias"
        val verify1 = dataSource.getPublicKeyVerify(alias)
        val verify2 = dataSource.getPublicKeyVerify(alias)

        assertSame(verify1, verify2)
    }

    @Test
    fun `getMac should cache Mac instance for same alias`() {
        mockkConstructor(AndroidKeysetManager.Builder::class)
        val mockManager = mockk<AndroidKeysetManager>(relaxed = true)
        val mockKeysetHandle = mockk<KeysetHandle>(relaxed = true)
        val mockMac = mockk<com.google.crypto.tink.Mac>()

        every { anyConstructed<AndroidKeysetManager.Builder>().withSharedPref(any(), any(), any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withKeyTemplate(any<KeyTemplate>()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withMasterKeyUri(any()) } answers
            { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().build() } returns mockManager

        every { mockManager.keysetHandle } returns mockKeysetHandle
        every { mockKeysetHandle.getPrimitive(any(), com.google.crypto.tink.Mac::class.java) } returns mockMac

        val alias = "test_mac_alias"
        val mac1 = dataSource.getMac(alias)
        val mac2 = dataSource.getMac(alias)

        assertSame(mac1, mac2)
    }
}
