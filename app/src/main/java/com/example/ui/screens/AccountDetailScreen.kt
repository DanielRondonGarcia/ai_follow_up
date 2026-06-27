package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Account
import com.example.data.UsageLog
import com.example.ui.components.RateLimitCard
import com.example.ui.components.UsageChart
import com.example.ui.components.ErrorBanner
import com.example.ui.theme.DesignTokens

/**
 * Account detail screen: rate limits, credits, auth token, usage chart.
 *
 * Extracted from MainScreen.kt (detail dashboard block). The full detail
 * layout is preserved: plan/credits mini-cards, rate limits card, auth
 * token card, credits estimations, usage chart, and clear-history action.
 * Sub-sections live in [AccountDetailSections.kt].
 *
 * @param activeAccount the currently selected account, or null (loading).
 * @param logs usage logs for the active account.
 * @param isLoading global loading flag.
 * @param errorMessage nullable error message; shown via [ErrorBanner].
 * @param isExpired true when the active account is in the expired set.
 * @param onSync callback to sync the active account.
 * @param onReauth callback to re-authenticate the expired account.
 * @param onDeleteLogs callback to clear the usage history.
 * @param onClearError callback to dismiss the error banner.
 * @param onShowProfileSelector callback to open the profile selector.
 * @param modifier optional layout modifier.
 */
@Composable
fun AccountDetailScreen(
  activeAccount: Account?,
  logs: List<UsageLog>,
  isLoading: Boolean,
  errorMessage: String?,
  isExpired: Boolean,
  onSync: () -> Unit,
  onReauth: () -> Unit,
  onDeleteLogs: () -> Unit,
  onClearError: () -> Unit,
  onShowProfileSelector: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val activeLog = logs.firstOrNull()
  val expiredLabel = stringResource(R.string.sesion_expirada)
  val expiredDetail = stringResource(R.string.sesion_expirada_detalle)
  val reauthLabel = stringResource(R.string.volver_a_autenticar)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = DesignTokens.Spacing.lg),
    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg),
  ) {
    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))

    if (errorMessage != null) {
      ErrorBanner(
        message = errorMessage,
        onDismiss = onClearError,
      )
    }

    if (isExpired) {
      ExpiredBanner(
        title = expiredLabel,
        description = expiredDetail,
        ctaLabel = reauthLabel,
        onReauth = onReauth,
      )
    }

    if (activeLog != null && (activeLog.primaryUsedPercent >= 100 || activeLog.secondaryUsedPercent >= 100)) {
      LimitReachedBanner(activeAccount?.provider)
    }

    if (activeLog != null) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
      ) {
        PlanTypeCard(activeAccount, activeLog, modifier = Modifier.weight(1f))
        CreditsMiniCard(activeLog, modifier = Modifier.weight(1f))
      }

      RateLimitCard(log = activeLog)

      AuthTokenCard(isLoading = isLoading, onSync = onSync)

      CreditsEstimationsCard(log = activeLog)

      UsageChart(logs = logs)

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = DesignTokens.Spacing.xs),
        horizontalArrangement = Arrangement.End,
      ) {
        TextButton(
          onClick = onDeleteLogs,
          colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
          ),
        ) {
          Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null)
          Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
          Text(stringResource(R.string.limpiar_historial))
        }
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
        ) {
          CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
          Text(
            text = stringResource(R.string.sincronizando_primera_vez),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xxl))
  }
}

/**
 * Expired-session banner shown on the account detail screen when the active
 * account is in the expired set. Distinct from the generic [ErrorBanner]: uses
 * an error-colored card with a title, description, and a re-auth CTA button.
 */
@Composable
private fun ExpiredBanner(
  title: String,
  description: String,
  ctaLabel: String,
  onReauth: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignTokens.Radius.lg),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
    ),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
  ) {
    Column(
      modifier = Modifier.padding(DesignTokens.Spacing.lg),
      verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.error,
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Button(
        onClick = onReauth,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
      ) {
        Text(ctaLabel)
      }
    }
  }
}