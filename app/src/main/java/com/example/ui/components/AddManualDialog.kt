package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManualDialog(
    onDismiss: () -> Unit,
    onSave: (provider: String, email: String, token: String, cookies: String, userAgent: String, userId: String) -> Unit
) {
    var provider by remember { mutableStateOf("OpenAI") }
    var email by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
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

    val titleText = stringResource(R.string.configuracion_manual_title)
    val introText = stringResource(R.string.manual_dialog_intro)
    val emailLabel = stringResource(R.string.nombre_correo_opcional)
    val orgIdLabel = stringResource(R.string.id_organizacion_requerido)
    val tokenBearerLabel = stringResource(R.string.token_auth_bearer)
    val sessionKeyLabel = stringResource(R.string.session_key_label)
    val cookiesSemicolonLabel = stringResource(R.string.cookies_semicolon)
    val cookiesSessionLabel = stringResource(R.string.cookies_session)
    val cookiesOpcionalLabel = stringResource(R.string.cookies_opcional)
    val userAgentLabel = stringResource(R.string.user_agent_label)
    val validarGuardar = stringResource(R.string.validar_y_guardar)
    val cancelar = stringResource(R.string.cancelar)
    val placeholderEmailOpenai = stringResource(R.string.placeholder_email_openai)
    val placeholderEmailAnthropic = stringResource(R.string.placeholder_email_anthropic)
    val placeholderEmailOllama = stringResource(R.string.placeholder_email_ollama)
    val placeholderOrgId = stringResource(R.string.placeholder_org_id)
    val placeholderTokenOpenai = stringResource(R.string.placeholder_token_openai)
    val placeholderTokenAnthropic = stringResource(R.string.placeholder_token_anthropic)
    val placeholderCookiesOpenai = stringResource(R.string.placeholder_cookies_openai)
    val placeholderCookiesOllama = stringResource(R.string.placeholder_cookies_ollama)
    val placeholderCookiesAnthropic = stringResource(R.string.placeholder_cookies_anthropic)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titleText,
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
                    text = introText,
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
                        Text(stringResource(R.string.provider_openai), fontSize = MaterialTheme.typography.labelSmall.fontSize)
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
                        Text(stringResource(R.string.provider_anthropic), fontSize = MaterialTheme.typography.labelSmall.fontSize)
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
                        Text(stringResource(R.string.provider_ollama), fontSize = MaterialTheme.typography.labelSmall.fontSize)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Label / Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(emailLabel) },
                    placeholder = {
                        Text(
                            when (provider) {
                                "OpenAI" -> placeholderEmailOpenai
                                "Anthropic" -> placeholderEmailAnthropic
                                else -> placeholderEmailOllama
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
                        label = { Text(orgIdLabel) },
                        placeholder = { Text(placeholderOrgId) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (provider != "Ollama") {
                    // Authorization Token (or Session Key)
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text(if (provider == "OpenAI") tokenBearerLabel else sessionKeyLabel) },
                        placeholder = { Text(if (provider == "OpenAI") placeholderTokenOpenai else placeholderTokenAnthropic) },
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
                                "OpenAI" -> cookiesSemicolonLabel
                                "Ollama" -> cookiesSessionLabel
                                else -> cookiesOpcionalLabel
                            }
                        )
                    },
                    placeholder = {
                        Text(
                            when (provider) {
                                "OpenAI" -> placeholderCookiesOpenai
                                "Ollama" -> placeholderCookiesOllama
                                else -> placeholderCookiesAnthropic
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
                    label = { Text(userAgentLabel) },
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
                Text(validarGuardar)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelar)
            }
        }
    )
}