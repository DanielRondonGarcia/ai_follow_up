package com.example.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.Account
import com.example.ui.components.AddManualDialog
import com.example.ui.components.ProfileSelector
import com.example.ui.components.WebViewLogin

/**
 * Modal overlays for MainScreen, extracted to keep MainScreen.kt under 200 lines.
 */

@Composable
internal fun ProfileSelectorSheet(
  accounts: List<Account>,
  activeAccount: Account?,
  onDismiss: () -> Unit,
  onSelect: (Int) -> Unit,
  onDelete: (Account) -> Unit,
  onAddManualClick: () -> Unit,
  onAddAutoClick: (String) -> Unit,
) {
  ProfileSelector(
    accounts = accounts,
    activeAccount = activeAccount,
    onDismiss = onDismiss,
    onSelect = onSelect,
    onDelete = onDelete,
    onAddManualClick = onAddManualClick,
    onAddAutoClick = onAddAutoClick,
  )
}

@Composable
internal fun ManualDialogOverlay(
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, String, String) -> Unit,
) {
  AddManualDialog(
    onDismiss = onDismiss,
    onSave = onSave,
  )
}

@Composable
internal fun WebViewLoginOverlay(
  provider: String,
  onDismiss: () -> Unit,
  onTokenCaptured: (String, String, String, String, String) -> Unit,
) {
  WebViewLogin(
    provider = provider,
    onDismiss = onDismiss,
    onTokenCaptured = onTokenCaptured,
  )
}

@Composable
internal fun MigrationNoticeDialog(
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = { /* non-dismissible on outside click */ },
    title = { Text("Security Upgrade") },
    text = {
      Text(
        "Your accounts were cleared due to a new encryption-at-rest security improvement. " +
          "Please re-add your accounts."
      )
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Got it") }
    },
  )
}

@Composable
internal fun ClearHistoryConfirmDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.limpiar_historial_titulo)) },
    text = { Text(stringResource(R.string.limpiar_historial_confirmacion)) },
    confirmButton = {
      TextButton(
        onClick = onConfirm,
        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
          contentColor = MaterialTheme.colorScheme.error,
        ),
      ) { Text(stringResource(R.string.borrar)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.cancelar))
      }
    },
  )
}