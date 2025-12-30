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
    val scrollState = rememberScrollState()

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

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Symmetric (AES-GCM)", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.encrypt(textToEncrypt) }, modifier = Modifier.weight(1f)) {
                Text("Encrypt")
            }
            Button(onClick = { viewModel.decrypt() }, modifier = Modifier.weight(1f)) {
                Text("Decrypt")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Asymmetric (RSA-OAEP)", style = MaterialTheme.typography.titleMedium)
        Button(
            onClick = { viewModel.encryptAsymmetric(textToEncrypt) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("RSA Encrypt with Fake Public Key")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Integrity & Hashing", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.hashSHA256(textToEncrypt) }, modifier = Modifier.weight(1f)) {
                Text("SHA-256")
            }
            Button(onClick = { viewModel.hashMD5(textToEncrypt) }, modifier = Modifier.weight(1f)) {
                Text("MD5 (Legacy)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.checkSecurity() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Verify Hardware Security (TEE/StrongBox)")
        }

        Spacer(modifier = Modifier.height(24.dp))

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
