package es.joshluq.encryptionkit.data.provider

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.sdk.EncryptionkitManager
import es.joshluq.foundationkit.provider.SerializerProvider
import es.joshluq.foundationkit.provider.StorageProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class SecureDataStoreProvider(
    private val dataStore: DataStore<Preferences>,
    private val serializerProvider: SerializerProvider,
    private val encryptionkitManager: EncryptionkitManager
) : StorageProvider {

    override suspend fun <T : Any> save(key: String, value: T, type: Class<T>) {
        val serializedValue = serializerProvider.serialize(value, type)
        val secureBytes = SecureBytes(serializedValue.toByteArray(Charsets.UTF_8))
        
        // Use the preference key as associated data for extra security
        val associatedData = key.toByteArray(Charsets.UTF_8)
        
        val encryptionResult = encryptionkitManager.encrypt(secureBytes, associatedData).getOrThrow()
        val encryptedBytes = encryptionResult.ciphertext
        val base64String = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        val prefKey = stringPreferencesKey(key)
        secureBytes.close()

        dataStore.edit { preferences ->
            preferences[prefKey] = base64String
        }
    }

    override suspend fun <T : Any> read(key: String, type: Class<T>): T? {
        val prefKey = stringPreferencesKey(key)
        val base64String = dataStore.data.map { preferences -> preferences[prefKey] }.first() ?: return null

        val encryptedBytes = Base64.decode(base64String, Base64.NO_WRAP)
        
        // Use the same preference key as associated data to verify integrity
        val associatedData = key.toByteArray(Charsets.UTF_8)
        
        val decryptedSecureBytes = encryptionkitManager.decrypt(encryptedBytes, associatedData).getOrThrow()
        val decryptedString = String(decryptedSecureBytes.data, Charsets.UTF_8)
        decryptedSecureBytes.close()
        return serializerProvider.deserialize(decryptedString, type)
    }

    override suspend fun delete(key: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences.remove(prefKey)
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
