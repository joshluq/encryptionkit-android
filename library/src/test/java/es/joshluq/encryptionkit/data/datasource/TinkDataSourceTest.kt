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

        every { anyConstructed<AndroidKeysetManager.Builder>().withSharedPref(any(), any(), any()) } answers { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withKeyTemplate(any<KeyTemplate>()) } answers { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withMasterKeyUri(any()) } answers { it.invocation.self as AndroidKeysetManager.Builder }
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

        every { anyConstructed<AndroidKeysetManager.Builder>().withSharedPref(any(), any(), any()) } answers { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withKeyTemplate(any<KeyTemplate>()) } answers { it.invocation.self as AndroidKeysetManager.Builder }
        every { anyConstructed<AndroidKeysetManager.Builder>().withMasterKeyUri(any()) } answers { it.invocation.self as AndroidKeysetManager.Builder }
        // Fail on first build(), succeed on subsequent
        every { anyConstructed<AndroidKeysetManager.Builder>().build() } throws GeneralSecurityException("Keystore error") andThen mockManager
        
        every { mockManager.keysetHandle } returns mockKeysetHandle
        every { mockKeysetHandle.getPrimitive(any(), Aead::class.java) } returns mockAead

        val alias = "test_alias"
        val aead = dataSource.getAead(alias)

        assertSame(mockAead, aead)
    }
}
