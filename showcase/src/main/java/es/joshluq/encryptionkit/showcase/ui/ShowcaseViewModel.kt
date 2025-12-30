package es.joshluq.encryptionkit.showcase.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.sdk.Encryptionkit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowcaseViewModel @Inject constructor(
    private val encryptionKit: Encryptionkit
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShowcaseUiState>(ShowcaseUiState.Idle)
    val uiState: StateFlow<ShowcaseUiState> = _uiState.asStateFlow()

    private var lastResult: CryptoResult? = null

    fun encrypt(text: String) {
        viewModelScope.launch {
            try {
                val result = encryptionKit.encrypt(text.toByteArray())
                lastResult = result
                _uiState.value = ShowcaseUiState.Success("Encrypted: ${result.ciphertext.joinToString("") { "%02x".format(it) }}")
            } catch (e: Exception) {
                _uiState.value = ShowcaseUiState.Error(e.message ?: "Encryption failed")
            }
        }
    }

    fun decrypt() {
        viewModelScope.launch {
            val result = lastResult
            if (result == null) {
                _uiState.value = ShowcaseUiState.Error("Nothing to decrypt")
                return@launch
            }

            try {
                val decryptedBytes = encryptionKit.decrypt(result)
                _uiState.value = ShowcaseUiState.Success("Decrypted: ${String(decryptedBytes)}")
            } catch (e: Exception) {
                _uiState.value = ShowcaseUiState.Error(e.message ?: "Decryption failed")
            }
        }
    }

    fun encryptAsymmetric(text: String) {
        viewModelScope.launch {
            try {
                // NOTE: This will fail if no certificate is provided via CertificatePathProvider
                // and no valid public key is found. Ideally, handle this specific exception.
                val encrypted = encryptionKit.encryptWithPublicKey(text.toByteArray())
                _uiState.value = ShowcaseUiState.Success("Asymmetric Encrypted: ${encrypted.joinToString("") { "%02x".format(it) }}")
            } catch (e: Exception) {
                _uiState.value = ShowcaseUiState.Error("Asymmetric Encryption failed: ${e.message} (Is a certificate loaded?)")
            }
        }
    }

    fun hashSHA256(text: String) {
        viewModelScope.launch {
            try {
                val hash = encryptionKit.hashToHex(text, "SHA-256")
                _uiState.value = ShowcaseUiState.Success("SHA-256 Hash: $hash")
            } catch (e: Exception) {
                _uiState.value = ShowcaseUiState.Error(e.message ?: "Hashing failed")
            }
        }
    }

    fun hashMD5(text: String) {
        viewModelScope.launch {
            try {
                val hash = encryptionKit.hashToHex(text, "MD5")
                _uiState.value = ShowcaseUiState.Success("MD5 Hash: $hash")
            } catch (e: Exception) {
                _uiState.value = ShowcaseUiState.Error(e.message ?: "MD5 Hashing failed")
            }
        }
    }

    fun checkSecurity() {
        val level = encryptionKit.getSecurityLevel()
        _uiState.value = ShowcaseUiState.Success("Security Level: $level")
    }
}

sealed class ShowcaseUiState {
    object Idle : ShowcaseUiState()
    data class Success(val message: String) : ShowcaseUiState()
    data class Error(val message: String) : ShowcaseUiState()
}
