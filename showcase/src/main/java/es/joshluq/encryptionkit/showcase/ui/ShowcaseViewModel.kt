package es.joshluq.encryptionkit.showcase.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import es.joshluq.encryptionkit.domain.model.*
import es.joshluq.encryptionkit.sdk.EncryptionkitManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ShowcaseViewModel @Inject constructor(
    private val encryptionKitManager: EncryptionkitManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShowcaseUiState>(ShowcaseUiState.Idle)
    val uiState: StateFlow<ShowcaseUiState> = _uiState.asStateFlow()

    private var lastResult: CryptoResult? = null

    fun encrypt(text: String) {
        val secureData = SecureBytes(text.toByteArray())
        
        encryptionKitManager.encrypt(
            secureData = secureData,
            onSuccess = { result ->
                lastResult = result
                _uiState.value = ShowcaseUiState.Success("Encrypted (via SecureBytes): ${result.ciphertext.joinToString("") { "%02x".format(it) }}")
                secureData.close() 
            },
            onError = { e ->
                _uiState.value = ShowcaseUiState.Error("Encryption failed: ${e.message} [Reason: ${e.reason}]")
                secureData.close()
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
        encryptionKitManager.hashToHex(
            text = text,
            algorithm = "SHA-256",
            onSuccess = { hash ->
                _uiState.value = ShowcaseUiState.Success("SHA-256 Hash: $hash")
            },
            onError = { e ->
                _uiState.value = ShowcaseUiState.Error("Hashing failed: ${e.message}")
            }
        )
    }

    fun hashMD5(text: String) {
        encryptionKitManager.hashToHex(
            text = text,
            algorithm = "MD5",
            onSuccess = { hash ->
                _uiState.value = ShowcaseUiState.Success("MD5 Hash: $hash")
            },
            onError = { e ->
                _uiState.value = ShowcaseUiState.Error("MD5 Hashing failed: ${e.message}")
            }
        )
    }

    fun checkSecurity() {
        encryptionKitManager.getSecurityLevel(
            onSuccess = { level ->
                _uiState.value = ShowcaseUiState.Success("Hardware Security Level: $level")
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
