package es.joshluq.encryptionkit.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShowcaseScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowcaseScreen(viewModel: ShowcaseViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var textToEncrypt by remember { mutableStateOf("Military-grade data") }
    var secureKeyInput by remember { mutableStateOf("session_token") }
    var ciphertextInput by remember { mutableStateOf("") }
    var ivInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Update inputs when encryption succeeds
    LaunchedEffect(uiState) {
        if (uiState is ShowcaseUiState.Success) {
            val success = uiState as ShowcaseUiState.Success
            success.ciphertext?.let { ciphertextInput = it }
            success.iv?.let { ivInput = it }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Encryptionkit Showcase",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = textToEncrypt,
            onValueChange = { textToEncrypt = it },
            label = { Text("Text to Encrypt/Hash") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Symmetric Encryption
        Text(text = "Symmetric (AES-GCM)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = "Local encryption/decryption using Android Keystore.", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = ciphertextInput,
            onValueChange = { ciphertextInput = it },
            label = { Text("Ciphertext (Hex)") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = ivInput,
            onValueChange = { ivInput = it },
            label = { Text("IV (Hex, Managed by Tink / Optional)") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.encrypt(textToEncrypt) }, 
                modifier = Modifier.weight(1f)
            ) {
                Text("Encrypt")
            }
            Button(
                onClick = { viewModel.decrypt(ciphertextInput) },
                modifier = Modifier.weight(1f),
                enabled = ciphertextInput.isNotEmpty()
            ) {
                Text("Decrypt")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Secure Storage
        Text(text = "Secure Storage (DataStore + Tink)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = "Persist data securely using SecureDataStoreProvider.", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = secureKeyInput,
            onValueChange = { secureKeyInput = it },
            label = { Text("Storage Key") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.saveSecurely(secureKeyInput, textToEncrypt) }, 
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Securely")
            }
            Button(
                onClick = { viewModel.readSecurely(secureKeyInput) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Read Securely")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Asymmetric Encryption
        Text(text = "Asymmetric (RSA-OAEP)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = "Encrypt using a Certificate/Public Key (for server).", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { viewModel.encryptAsymmetric(textToEncrypt) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("RSA Encrypt (Public Key)")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Hashing
        Text(text = "Integrity & Hashing", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = "One-way secure fingerprinting.", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.hashSHA256(textToEncrypt) }, modifier = Modifier.weight(1f)) {
                Text("SHA-256")
            }
            Button(onClick = { viewModel.hashMD5(textToEncrypt) }, modifier = Modifier.weight(1f)) {
                Text("MD5")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Diagnostics
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.checkSecurity() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Check Hardware Security Level")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Console Output:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                when (val state = uiState) {
                    is ShowcaseUiState.Success -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is ShowcaseUiState.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        Text(
                            text = "Ready to secure your data.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
