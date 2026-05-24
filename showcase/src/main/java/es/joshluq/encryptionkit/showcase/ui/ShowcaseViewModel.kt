package es.joshluq.encryptionkit.showcase.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import es.joshluq.encryptionkit.domain.model.*
import es.joshluq.encryptionkit.sdk.EncryptionKitManager
import es.joshluq.foundationkit.provider.StorageProvider
import es.joshluq.foundationkit.provider.read
import es.joshluq.foundationkit.provider.save
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowcaseViewModel @Inject constructor(
    private val encryptionKitManager: EncryptionKitManager,
    private val secureStorage: StorageProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShowcaseUiState>(ShowcaseUiState.Idle)
    val uiState: StateFlow<ShowcaseUiState> = _uiState.asStateFlow()

    private var lastResult: CryptoResult? = null

    fun encrypt(text: String) {
        val secureData = SecureBytes(text.toByteArray())
        viewModelScope.launch {
            encryptionKitManager.encrypt(secureData = secureData)
                .onSuccess { result ->
                    lastResult = result
                    _uiState.value = ShowcaseUiState.Success(
                        message = "Encrypted (via SecureBytes): ${result.toHexString()}",
                        ciphertext = result.toHexString(),
                    )
                }
                .onFailure { e ->
                    _uiState.value = ShowcaseUiState.Error("Encryption failed: ${e.message}")
                }
            secureData.close()
        }
    }

    fun decrypt(ciphertextHex: String) {
        viewModelScope.launch {
            try {
                val ciphertext = ciphertextHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                encryptionKitManager.decrypt(ciphertext)
                    .onSuccess { decryptedBytes ->
                        _uiState.value = ShowcaseUiState.Success("Decrypted: ${String(decryptedBytes.data)}")
                    }
                    .onFailure { e ->
                        _uiState.value = ShowcaseUiState.Error("Decryption failed: ${e.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = ShowcaseUiState.Error("Invalid hex input: ${e.message}")
            }
        }
    }

    fun encryptAsymmetric(text: String) {
        viewModelScope.launch {
            encryptionKitManager.encryptWithPublicKey(data = text.toByteArray())
                .onSuccess { encrypted ->
                    val hexString = encrypted.joinToString("") { "%02x".format(it) }
                    _uiState.value = ShowcaseUiState.Success("Asymmetric Encrypted: $hexString")
                }
                .onFailure { e ->
                    _uiState.value = ShowcaseUiState.Error("Asymmetric Encryption failed: ${e.message}")
                }
        }
    }

    fun hashSHA256(text: String) {
        viewModelScope.launch {
            encryptionKitManager.hashToHex(text = text, algorithm = EncryptionKitManager.HashAlgorithm.SHA_256)
                .onSuccess { hash ->
                    _uiState.value = ShowcaseUiState.Success("SHA-256 Hash: $hash")
                }
                .onFailure { e ->
                    _uiState.value = ShowcaseUiState.Error("Hashing failed: ${e.message}")
                }
        }
    }

    fun hashMD5(text: String) {
        viewModelScope.launch {
            encryptionKitManager.hashToHex(text = text, algorithm = EncryptionKitManager.HashAlgorithm.MD5)
                .onSuccess { hash ->
                    _uiState.value = ShowcaseUiState.Success("MD5 Hash: $hash")
                }
                .onFailure { e ->
                    _uiState.value = ShowcaseUiState.Error("MD5 Hashing failed: ${e.message}")
                }
        }
    }

    fun checkSecurity() {
        viewModelScope.launch {
            encryptionKitManager.getSecurityLevel()
                .onSuccess { level ->
                    _uiState.value = ShowcaseUiState.Success("Hardware Security Level: $level")
                }
                .onFailure { e ->
                    _uiState.value = ShowcaseUiState.Error("Failed to get security level: ${e.message}")
                }
        }
    }

    fun saveSecurely(key: String, value: String) {
        viewModelScope.launch {
            try {
                secureStorage.save(key, value)
                _uiState.value = ShowcaseUiState.Success("Successfully saved '$key' securely!")
            } catch (e: Exception) {
                _uiState.value = ShowcaseUiState.Error("Failed to save securely: ${e.message}")
            }
        }
    }

    fun readSecurely(key: String) {
        viewModelScope.launch {
            try {
                val value: String? = secureStorage.read(key)
                if (value != null) {
                    _uiState.value = ShowcaseUiState.Success("Securely read '$key': $value")
                } else {
                    _uiState.value = ShowcaseUiState.Error("Key '$key' not found in secure storage")
                }
            } catch (e: Exception) {
                _uiState.value = ShowcaseUiState.Error("Failed to read securely: ${e.message}")
            }
        }
    }
}

sealed class ShowcaseUiState {
    object Idle : ShowcaseUiState()
    data class Success(
        val message: String,
        val ciphertext: String? = null,
        val iv: String? = null
    ) : ShowcaseUiState()
    data class Error(val message: String) : ShowcaseUiState()
}
