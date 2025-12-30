package es.joshluq.encryptionkit.showcase.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.sdk.EncryptionkitManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowcaseViewModel @Inject constructor(
    private val encryptionKitManager: EncryptionkitManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShowcaseUiState>(ShowcaseUiState.Idle)
    val uiState: StateFlow<ShowcaseUiState> = _uiState.asStateFlow()

    private var lastResult: CryptoResult? = null

    fun encrypt(text: String) {
        // Create SecureBytes. Since the Manager call is now asynchronous (launchIn),
        // we manually close it in the callbacks instead of using the 'use' block.
        val secureData = SecureBytes(text.toByteArray())
        
        encryptionKitManager.encrypt(
            secureData = secureData,
            onSuccess = { result ->
                lastResult = result
                _uiState.value = ShowcaseUiState.Success("Encrypted (via SecureBytes): ${result.ciphertext.joinToString("") { "%02x".format(it) }}")
                secureData.close() // Wipe sensitive memory
            },
            onError = { e ->
                _uiState.value = ShowcaseUiState.Error("Encryption failed: ${e.message} [Reason: ${e.reason}]")
                secureData.close() // Wipe sensitive memory
            }
        )
    }

    fun decrypt() {
        val result = lastResult
        if (result == null) {
            _uiState.value = ShowcaseUiState.Error("Nothing to decrypt")
            return
        }

        encryptionKitManager.decrypt(
            result = result,
            onSuccess = { decryptedBytes ->
                _uiState.value = ShowcaseUiState.Success("Decrypted: ${String(decryptedBytes)}")
            },
            onError = { e ->
                _uiState.value = ShowcaseUiState.Error("Decryption failed: ${e.message} [Reason: ${e.reason}]")
            }
        )
    }

    fun encryptAsymmetric(text: String) {
        encryptionKitManager.encryptWithPublicKey(
            data = text.toByteArray(),
            onSuccess = { encrypted ->
                _uiState.value = ShowcaseUiState.Success("Asymmetric Encrypted: ${encrypted.joinToString("") { "%02x".format(it) }}")
            },
            onError = { e ->
                _uiState.value = ShowcaseUiState.Error("Asymmetric Encryption failed: ${e.message} [Reason: ${e.reason}]")
            }
        )
    }

    fun hashSHA256(text: String) {
        // hashToHex is a synchronous convenience method in this version
        try {
            val hash = encryptionKitManager.hashToHex(text, "SHA-256")
            _uiState.value = ShowcaseUiState.Success("SHA-256 Hash: $hash")
        } catch (e: Exception) {
            _uiState.value = ShowcaseUiState.Error("Hashing failed: ${e.message}")
        }
    }

    fun hashMD5(text: String) {
        try {
            val hash = encryptionKitManager.hashToHex(text, "MD5")
            _uiState.value = ShowcaseUiState.Success("MD5 Hash: $hash")
        } catch (e: Exception) {
            _uiState.value = ShowcaseUiState.Error("MD5 Hashing failed: ${e.message}")
        }
    }

    fun checkSecurity() {
        encryptionKitManager.getSecurityLevel(
            onSuccess = { level ->
                _uiState.value = ShowcaseUiState.Success("Security Level: $level")
            },
            onError = { e ->
                _uiState.value = ShowcaseUiState.Error("Failed to get security level: ${e.message}")
            }
        )
    }
}

sealed class ShowcaseUiState {
    object Idle : ShowcaseUiState()
    data class Success(val message: String) : ShowcaseUiState()
    data class Error(val message: String) : ShowcaseUiState()
}

