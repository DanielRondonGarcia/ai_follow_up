package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManualDialog(
    onDismiss: () -> Unit,
    onSave: (provider: String, email: String, token: String, cookies: String, userAgent: String, userId: String) -> Unit
) {
    var provider by remember { mutableStateOf("OpenAI") } // "OpenAI" or "Anthropic"
    var email by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") } // Organization ID for Anthropic
    var token by remember { mutableStateOf("") }
    var cookies by remember { mutableStateOf("") }
    
    // Default Chrome User-Agent used by ChatGPT / Claude
    var userAgent by remember { 
        mutableStateOf("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36") 
    }

    val isConfirmEnabled = if (provider == "OpenAI") {
        token.trim().isNotEmpty() && cookies.trim().isNotEmpty()
    } else if (provider == "Anthropic") {
        userId.trim().isNotEmpty() && (token.trim().isNotEmpty() || cookies.trim().isNotEmpty())
    } else {
        cookies.trim().isNotEmpty()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Configuración Manual",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Selecciona el proveedor y pega los valores del encabezado de tu curl de ChatGPT o Claude, o las cookies para Ollama. Al guardar, se validarán consultando el estado actual de uso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Provider Segmented Control (Segmented Buttons alternative)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { provider = "OpenAI" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (provider == "OpenAI") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (provider == "OpenAI") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("OpenAI", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { provider = "Anthropic" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (provider == "Anthropic") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (provider == "Anthropic") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Anthropic", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { provider = "Ollama" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (provider == "Ollama") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (provider == "Ollama") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Ollama", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Label / Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Nombre o Correo (Opcional)") },
                    placeholder = {
                        Text(
                            when (provider) {
                                "OpenAI" -> "ej. team_marmota@rondon.cloud"
                                "Anthropic" -> "ej. Marmot Claude"
                                else -> "ej. Ollama User"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (provider == "Anthropic") {
                    // Organization ID (only for Anthropic)
                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it },
                        label = { Text("ID de Organización (Required)") },
                        placeholder = { Text("e0ce05ba-61d3-4d12-af6c-c370abb91eb4") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (provider != "Ollama") {
                    // Authorization Token (or Session Key)
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text(if (provider == "OpenAI") "Token de Autorización (Bearer)" else "sessionKey (sk-ant-...)") },
                        placeholder = { Text(if (provider == "OpenAI") "eyJhbGciOiJSUzI1NiIs..." else "sk-ant-sid02-...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }

                // Cookies
                OutlinedTextField(
                    value = cookies,
                    onValueChange = { cookies = it },
                    label = {
                        Text(
                            when (provider) {
                                "OpenAI" -> "Cookies (Semicolon-separated)"
                                "Ollama" -> "Cookies (session=...)"
                                else -> "Cookies (Opcional)"
                            }
                        )
                    },
                    placeholder = {
                        Text(
                            when (provider) {
                                "OpenAI" -> "oai-did=...; __Secure-next-auth.session-token.0=..."
                                "Ollama" -> "session=..."
                                else -> "sessionKey=sk-ant-...; activeOrg=..."
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                // User Agent
                OutlinedTextField(
                    value = userAgent,
                    onValueChange = { userAgent = it },
                    label = { Text("User-Agent (Agente de Usuario)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isConfirmEnabled) {
                        onSave(provider, email, token, cookies, userAgent, userId)
                    }
                },
                enabled = isConfirmEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Validar y Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
