package es.joshluq.encryptionkit.showcase.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.joshluq.encryptionkit.domain.model.CryptoResult
import es.joshluq.encryptionkit.domain.model.HexUtils
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.sdk.EncryptionKit
import es.joshluq.foundationkit.coroutines.throttleFirst
import es.joshluq.foundationkit.provider.StorageProvider
import es.joshluq.foundationkit.provider.read
import es.joshluq.foundationkit.provider.save
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ShowcaseAction {
    data class Encrypt(val text: String) : ShowcaseAction
    data class Decrypt(val ciphertextHex: String) : ShowcaseAction
    data class EncryptAsymmetric(val text: String) : ShowcaseAction
    data class HashSHA256(val text: String) : ShowcaseAction
    data class HashMD5(val text: String) : ShowcaseAction
    data object CheckSecurity : ShowcaseAction
    data class SaveSecurely(val key: String, val value: String) : ShowcaseAction
    data class ReadSecurely(val key: String) : ShowcaseAction
}

@HiltViewModel
class ShowcaseViewModel @Inject constructor(
    private val encryptionKit: EncryptionKit,
    private val secureStorage: StorageProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShowcaseUiState>(ShowcaseUiState.Idle)
    val uiState: StateFlow<ShowcaseUiState> = _uiState.asStateFlow()

    private var lastResult: CryptoResult? = null

    private val actionFlow = MutableSharedFlow<ShowcaseAction>(extraBufferCapacity = 64)

    init {
        viewModelScope.launch {
            actionFlow
                .throttleFirst(350L)
                .collect { action ->
                    processAction(action)
                }
        }
    }

    fun onAction(action: ShowcaseAction) {
        actionFlow.tryEmit(action)
    }

    fun encrypt(text: String) = onAction(ShowcaseAction.Encrypt(text))
    fun decrypt(ciphertextHex: String) = onAction(ShowcaseAction.Decrypt(ciphertextHex))
    fun encryptAsymmetric(text: String) = onAction(ShowcaseAction.EncryptAsymmetric(text))
    fun hashSHA256(text: String) = onAction(ShowcaseAction.HashSHA256(text))
    fun hashMD5(text: String) = onAction(ShowcaseAction.HashMD5(text))
    fun checkSecurity() = onAction(ShowcaseAction.CheckSecurity)
    fun saveSecurely(key: String, value: String) = onAction(ShowcaseAction.SaveSecurely(key, value))
    fun readSecurely(key: String) = onAction(ShowcaseAction.ReadSecurely(key))

    private suspend fun processAction(action: ShowcaseAction) {
        when (action) {
            is ShowcaseAction.Encrypt -> executeEncrypt(action.text)
            is ShowcaseAction.Decrypt -> executeDecrypt(action.ciphertextHex)
            is ShowcaseAction.EncryptAsymmetric -> executeEncryptAsymmetric(action.text)
            is ShowcaseAction.HashSHA256 -> executeHashSHA256(action.text)
            is ShowcaseAction.HashMD5 -> executeHashMD5(action.text)
            is ShowcaseAction.CheckSecurity -> executeCheckSecurity()
            is ShowcaseAction.SaveSecurely -> executeSaveSecurely(action.key, action.value)
            is ShowcaseAction.ReadSecurely -> executeReadSecurely(action.key)
        }
    }

    private suspend fun executeEncrypt(text: String) {
        val secureData = SecureBytes(text.toByteArray())
        encryptionKit.encrypt(secureData = secureData)
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

    private suspend fun executeDecrypt(ciphertextHex: String) {
        try {
            val ciphertext = HexUtils.decode(ciphertextHex)
            encryptionKit.decrypt(ciphertext)
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

    private suspend fun executeEncryptAsymmetric(text: String) {
        encryptionKit.encryptWithPublicKey(data = text.toByteArray())
            .onSuccess { encrypted ->
                val hexString = HexUtils.encode(encrypted)
                _uiState.value = ShowcaseUiState.Success("Asymmetric Encrypted: $hexString")
            }
            .onFailure { e ->
                _uiState.value = ShowcaseUiState.Error("Asymmetric Encryption failed: ${e.message}")
            }
    }

    private suspend fun executeHashSHA256(text: String) {
        encryptionKit.hashToHex(text = text, algorithm = EncryptionKit.HashAlgorithm.SHA_256)
            .onSuccess { hash ->
                _uiState.value = ShowcaseUiState.Success("SHA-256 Hash: $hash")
            }
            .onFailure { e ->
                _uiState.value = ShowcaseUiState.Error("Hashing failed: ${e.message}")
            }
    }

    private suspend fun executeHashMD5(text: String) {
        encryptionKit.hashToHex(text = text, algorithm = EncryptionKit.HashAlgorithm.MD5)
            .onSuccess { hash ->
                _uiState.value = ShowcaseUiState.Success("MD5 Hash: $hash")
            }
            .onFailure { e ->
                _uiState.value = ShowcaseUiState.Error("MD5 Hashing failed: ${e.message}")
            }
    }

    private suspend fun executeCheckSecurity() {
        encryptionKit.getSecurityLevel()
            .onSuccess { level ->
                _uiState.value = ShowcaseUiState.Success("Hardware Security Level: $level")
            }
            .onFailure { e ->
                _uiState.value = ShowcaseUiState.Error("Failed to get security level: ${e.message}")
            }
    }

    private suspend fun executeSaveSecurely(key: String, value: String) {
        try {
            secureStorage.save(key, value)
            _uiState.value = ShowcaseUiState.Success("Successfully saved '$key' securely!")
        } catch (e: Exception) {
            _uiState.value = ShowcaseUiState.Error("Failed to save securely: ${e.message}")
        }
    }

    private suspend fun executeReadSecurely(key: String) {
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

sealed class ShowcaseUiState {
    object Idle : ShowcaseUiState()
    data class Success(
        val message: String,
        val ciphertext: String? = null,
        val iv: String? = null,
    ) : ShowcaseUiState()
    data class Error(val message: String) : ShowcaseUiState()
}
