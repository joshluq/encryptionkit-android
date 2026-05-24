package es.joshluq.encryptionkit.data.provider

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.sdk.EncryptionKit
import es.joshluq.foundationkit.provider.SerializerProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SecureDataStoreProviderTest {

    private val dataStore: DataStore<Preferences> = mockk()
    private val serializerProvider: SerializerProvider = mockk()
    private val encryptionKit: EncryptionKit = mockk()

    private lateinit var provider: SecureDataStoreProvider

    @Before
    fun setUp() {
        mockkStatic(Base64::class)
        provider = SecureDataStoreProvider(
            dataStore,
            serializerProvider,
            encryptionKit
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `save should serialize, encrypt with associated data and store in dataStore`() = runTest {
        val key = "key"
        val value = "value"
        val serialized = "serialized"
        val encryptedBytes = "encrypted".toByteArray()
        val base64 = "base64"

        every { serializerProvider.serialize(value, String::class.java) } returns serialized
        coEvery { encryptionKit.encrypt(any(), any()) } returns Result.success(
            CryptoResult(encryptedBytes)
        )
        every { Base64.encodeToString(encryptedBytes, Base64.NO_WRAP) } returns base64

        val mutablePreferences = mockk<MutablePreferences>(relaxed = true)
        coEvery { dataStore.edit(any()) } coAnswers {
            val transform = it.invocation.args[0] as suspend (MutablePreferences) -> Unit
            transform(mutablePreferences)
            mutablePreferences
        }

        provider.save(key, value, String::class.java)

        verify { serializerProvider.serialize(value, String::class.java) }
        coVerify { encryptionKit.encrypt(match { it.data.contentEquals(serialized.toByteArray()) }, match { it.contentEquals(key.toByteArray()) }) }
        verify { Base64.encodeToString(encryptedBytes, Base64.NO_WRAP) }
        verify { mutablePreferences[any<Preferences.Key<*>>()] = base64 }
    }

    @Test
    fun `read should fetch from dataStore, decrypt with associated data and deserialize`() = runTest {
        val key = "key"
        val base64 = "base64"
        val encryptedBytes = "encrypted".toByteArray()
        val decryptedBytes = "decrypted".toByteArray()
        val expectedValue = "value"

        val preferences = mockk<Preferences>()
        every { preferences[any<Preferences.Key<*>>()] } returns base64
        every { dataStore.data } returns flowOf(preferences)

        every { Base64.decode(base64, Base64.NO_WRAP) } returns encryptedBytes
        coEvery { encryptionKit.decrypt(encryptedBytes, match { it.contentEquals(key.toByteArray()) }) } returns Result.success(
            SecureBytes(decryptedBytes)
        )
        every { serializerProvider.deserialize(any(), String::class.java) } returns expectedValue

        val result = provider.read(key, String::class.java)

        assertEquals(expectedValue, result)
        verify { Base64.decode(base64, Base64.NO_WRAP) }
        coVerify { encryptionKit.decrypt(encryptedBytes, match { it.contentEquals(key.toByteArray()) }) }
        verify { serializerProvider.deserialize(any(), String::class.java) }
    }

    @Test
    fun `delete should remove key from dataStore`() = runTest {
        val key = "key"
        val mutablePreferences = mockk<MutablePreferences>(relaxed = true)
        coEvery { dataStore.edit(any()) } coAnswers {
            val transform = it.invocation.args[0] as suspend (MutablePreferences) -> Unit
            transform(mutablePreferences)
            mutablePreferences
        }

        provider.delete(key)

        verify { mutablePreferences.remove(any<Preferences.Key<*>>()) }
    }

    @Test
    fun `clear should clear dataStore`() = runTest {
        val mutablePreferences = mockk<MutablePreferences>(relaxed = true)
        coEvery { dataStore.edit(any()) } coAnswers {
            val transform = it.invocation.args[0] as suspend (MutablePreferences) -> Unit
            transform(mutablePreferences)
            mutablePreferences
        }

        provider.clear()

        verify { mutablePreferences.clear() }
    }
}
