package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Account
import com.example.ui.theme.ProviderAnthropic
import com.example.ui.theme.ProviderOllama
import com.example.ui.theme.ProviderOpenAi

@Composable
fun ProfileSelector(
    accounts: List<Account>,
    activeAccount: Account?,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onDelete: (Account) -> Unit,
    onAddManualClick: () -> Unit,
    onAddAutoClick: (provider: String) -> Unit
) {
    val titleText = stringResource(R.string.cuentas_de_monitoreo_title)
    val emptyText = stringResource(R.string.no_hay_cuentas)
    val cdPerfil = stringResource(R.string.cd_perfil)
    val cdEliminar = stringResource(R.string.cd_eliminar)
    val planLabelFormat = stringResource(R.string.plan_label)
    val badgeClaude = stringResource(R.string.badge_claude)
    val badgeOllama = stringResource(R.string.badge_ollama)
    val badgeGpt = stringResource(R.string.badge_gpt)
    val vincularChatgpt = stringResource(R.string.vincular_chatgpt_auto)
    val vincularClaude = stringResource(R.string.vincular_claude_auto)
    val vincularOllama = stringResource(R.string.vincular_ollama_auto)
    val manualAvanzado = stringResource(R.string.configuracion_manual_avanzado)
    val listoLabel = stringResource(R.string.listo)

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
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (accounts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(accounts) { account ->
                            val isActive = account.id == activeAccount?.id
                            val borderColor = if (isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            }
                            val bgColor = if (isActive) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            } else {
                                Color.Transparent
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .border(
                                        width = if (isActive) 2.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelect(account.id) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = cdPerfil,
                                        tint = if (isActive) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val badgeColor = when (account.provider) {
                                                "Anthropic" -> ProviderAnthropic
                                                "Ollama" -> ProviderOllama
                                                else -> ProviderOpenAi
                                            }
                                            Surface(
                                                color = badgeColor,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    text = when (account.provider) {
                                                        "Anthropic" -> badgeClaude
                                                        "Ollama" -> badgeOllama
                                                        else -> badgeGpt
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = account.email,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                        }
                                        Text(
                                            text = planLabelFormat.format(account.planType.uppercase()),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDelete(account) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = cdEliminar,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ChatGPT Auto button
                    Button(
                        onClick = { onAddAutoClick("OpenAI") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ProviderOpenAi)
                    ) {
                        Icon(imageVector = Icons.Default.Web, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(vincularChatgpt)
                    }

                    // Claude Auto button
                    Button(
                        onClick = { onAddAutoClick("Anthropic") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ProviderAnthropic)
                    ) {
                        Icon(imageVector = Icons.Default.Web, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(vincularClaude)
                    }

                    // Ollama Auto button
                    Button(
                        onClick = { onAddAutoClick("Ollama") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ProviderOllama)
                    ) {
                        Icon(imageVector = Icons.Default.Web, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(vincularOllama)
                    }

                    // Manual Config button
                    OutlinedButton(
                        onClick = onAddManualClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(manualAvanzado)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(listoLabel)
            }
        }
    )
}