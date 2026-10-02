package es.joshluq.encryptionkit.data.provider

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.sdk.EncryptionKit
import es.joshluq.foundationkit.provider.SerializerProvider
import es.joshluq.foundationkit.provider.StorageProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class SecureDataStoreProvider(
    private val dataStore: DataStore<Preferences>,
    private val serializerProvider: SerializerProvider,
    private val encryptionKit: EncryptionKit,
) : StorageProvider {
    override suspend fun <T : Any> save(
        key: String,
        value: T,
        type: Class<T>,
    ) {
        val serializedValue = serializerProvider.serialize(value, type)
        // Use the preference key as associated data for extra security
        val associatedData = key.toByteArray(Charsets.UTF_8)

        val base64String =
            SecureBytes(serializedValue.toByteArray(Charsets.UTF_8)).use { secureBytes ->
                val encryptionResult = encryptionKit.encrypt(secureBytes, associatedData).getOrThrow()
                Base64.encodeToString(encryptionResult.ciphertext, Base64.NO_WRAP)
            }
        val prefKey = stringPreferencesKey(key)

        dataStore.edit { preferences ->
            preferences[prefKey] = base64String
        }
    }

    override suspend fun <T : Any> read(
        key: String,
        type: Class<T>,
    ): T? {
        val prefKey = stringPreferencesKey(key)
        val base64String = dataStore.data.map { preferences -> preferences[prefKey] }.first() ?: return null

        val encryptedBytes = Base64.decode(base64String, Base64.NO_WRAP)

        // Use the same preference key as associated data to verify integrity
        val associatedData = key.toByteArray(Charsets.UTF_8)

        val decryptionResult = encryptionKit.decrypt(encryptedBytes, associatedData)

        if (decryptionResult.isFailure) {
            // Self-healing: delete the undecryptable entry from DataStore
            delete(key)
            return null
        }

        val decryptedString =
            decryptionResult.getOrThrow().use { decryptedSecureBytes ->
                String(decryptedSecureBytes.data, Charsets.UTF_8)
            }
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
