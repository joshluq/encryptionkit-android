package es.joshluq.encryptionkit.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import es.joshluq.encryptionkit.showcase.ui.ShowcaseUiState
import es.joshluq.encryptionkit.showcase.ui.ShowcaseViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ShowcaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ShowcaseScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun ShowcaseScreen(viewModel: ShowcaseViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var textToEncrypt by remember { mutableStateOf("Military-grade data") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
    ) {
        Text(
            text = "Encryptionkit Showcase",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        PlaintextInputSection(
            text = textToEncrypt,
            onTextChange = { textToEncrypt = it },
        )

        Spacer(modifier = Modifier.height(24.dp))

        SymmetricEncryptionSection(
            uiState = uiState,
            textToEncrypt = textToEncrypt,
            onEncrypt = viewModel::encrypt,
            onDecrypt = viewModel::decrypt,
        )

        Spacer(modifier = Modifier.height(24.dp))

        SecureStorageSection(
            textToEncrypt = textToEncrypt,
            onSave = viewModel::saveSecurely,
            onRead = viewModel::readSecurely,
        )

        Spacer(modifier = Modifier.height(24.dp))

        AsymmetricEncryptionSection(
            textToEncrypt = textToEncrypt,
            onEncryptAsymmetric = viewModel::encryptAsymmetric,
        )

        Spacer(modifier = Modifier.height(24.dp))

        HashingSection(
            textToEncrypt = textToEncrypt,
            onHashSHA256 = viewModel::hashSHA256,
            onHashMD5 = viewModel::hashMD5,
        )

        Spacer(modifier = Modifier.height(24.dp))

        DiagnosticsSection(
            onCheckSecurity = viewModel::checkSecurity,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ConsoleOutputSection(
            uiState = uiState,
        )
    }
}

@Composable
private fun PlaintextInputSection(
    text: String,
    onTextChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        label = { Text("Text to Encrypt/Hash") },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SymmetricEncryptionSection(
    uiState: ShowcaseUiState,
    textToEncrypt: String,
    onEncrypt: (String) -> Unit,
    onDecrypt: (String) -> Unit,
) {
    var ciphertextInput by remember { mutableStateOf("") }
    var ivInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is ShowcaseUiState.Success) {
            uiState.ciphertext?.let { ciphertextInput = it }
            uiState.iv?.let { ivInput = it }
        }
    }

    Text(
        text = "Symmetric (AES-GCM)",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        text = "Local encryption/decryption using Android Keystore.",
        style = MaterialTheme.typography.bodySmall,
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = ciphertextInput,
        onValueChange = { ciphertextInput = it },
        label = { Text("Ciphertext (Hex)") },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodySmall,
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = ivInput,
        onValueChange = { ivInput = it },
        label = { Text("IV (Hex, Managed by Tink / Optional)") },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodySmall,
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { onEncrypt(textToEncrypt) },
            modifier = Modifier.weight(1f),
        ) {
            Text("Encrypt")
        }
        Button(
            onClick = { onDecrypt(ciphertextInput) },
            modifier = Modifier.weight(1f),
            enabled = ciphertextInput.isNotEmpty(),
        ) {
            Text("Decrypt")
        }
    }
}

@Composable
private fun SecureStorageSection(
    textToEncrypt: String,
    onSave: (String, String) -> Unit,
    onRead: (String) -> Unit,
) {
    var secureKeyInput by remember { mutableStateOf("session_token") }

    Text(
        text = "Secure Storage (DataStore + Tink)",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        text = "Persist data securely using SecureDataStoreProvider.",
        style = MaterialTheme.typography.bodySmall,
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = secureKeyInput,
        onValueChange = { secureKeyInput = it },
        label = { Text("Storage Key") },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { onSave(secureKeyInput, textToEncrypt) },
            modifier = Modifier.weight(1f),
        ) {
            Text("Save Securely")
        }
        Button(
            onClick = { onRead(secureKeyInput) },
            modifier = Modifier.weight(1f),
        ) {
            Text("Read Securely")
        }
    }
}

@Composable
private fun AsymmetricEncryptionSection(
    textToEncrypt: String,
    onEncryptAsymmetric: (String) -> Unit,
) {
    Text(
        text = "Asymmetric (RSA-OAEP)",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        text = "Encrypt using a Certificate/Public Key (for server).",
        style = MaterialTheme.typography.bodySmall,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = { onEncryptAsymmetric(textToEncrypt) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("RSA Encrypt (Public Key)")
    }
}

@Composable
private fun HashingSection(
    textToEncrypt: String,
    onHashSHA256: (String) -> Unit,
    onHashMD5: (String) -> Unit,
) {
    Text(
        text = "Integrity & Hashing",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(
        text = "One-way secure fingerprinting.",
        style = MaterialTheme.typography.bodySmall,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { onHashSHA256(textToEncrypt) },
            modifier = Modifier.weight(1f),
        ) {
            Text("SHA-256")
        }
        Button(
            onClick = { onHashMD5(textToEncrypt) },
            modifier = Modifier.weight(1f),
        ) {
            Text("MD5")
        }
    }
}

@Composable
private fun DiagnosticsSection(
    onCheckSecurity: () -> Unit,
) {
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onCheckSecurity,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
    ) {
        Text("Check Hardware Security Level")
    }
}

@Composable
private fun ConsoleOutputSection(
    uiState: ShowcaseUiState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Console Output:",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (uiState) {
                is ShowcaseUiState.Success -> {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                is ShowcaseUiState.Error -> {
                    Text(
                        text = "Error: ${uiState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                else -> {
                    Text(
                        text = "Ready to secure your data.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
