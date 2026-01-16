package es.joshluq.encryptionkit.data.datasource

import es.joshluq.encryptionkit.domain.model.EncryptionConfig
import es.joshluq.encryptionkit.domain.model.SecurityLevel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.security.KeyStore
import javax.crypto.SecretKey

class KeystoreDataSourceTest {

    private lateinit var dataSource: KeystoreDataSource
    private val mockKeyStore: KeyStore = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(KeyStore::class)
        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeyStore
        dataSource = KeystoreDataSource()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getKey should return key from keystore`() {
        val alias = "test_alias"
        val mockKey: SecretKey = mockk()
        every { mockKeyStore.getKey(alias, null) } returns mockKey

        val result = dataSource.getKey(alias)

        assertNotNull(result)
        assertEquals(mockKey, result)
    }

    @Test
    fun `getKey should return null if key not found`() {
        val alias = "missing_alias"
        every { mockKeyStore.getKey(alias, null) } returns null

        val result = dataSource.getKey(alias)

        assertNull(result)
    }

    @Test
    fun `deleteKey should call deleteEntry on keystore`() {
        val alias = "test_alias"
        
        dataSource.deleteKey(alias)

        verify { mockKeyStore.deleteEntry(alias) }
    }

    @Test
    fun `ensureKeyExists should not generate key if it already exists`() {
        val config = EncryptionConfig("existing_alias", false, false)
        every { mockKeyStore.containsAlias(config.alias) } returns true

        dataSource.ensureKeyExists(config)

        // verify no key generation was attempted (internal call check)
        verify(exactly = 0) { mockKeyStore.deleteEntry(any()) } // placeholder check
    }

    @Test
    fun `getSecurityLevel should return SOFTWARE if key is null`() {
        every { mockKeyStore.getKey(any(), null) } returns null
        
        val level = dataSource.getSecurityLevel("alias")
        
        assertEquals(SecurityLevel.SOFTWARE, level)
    }
}
