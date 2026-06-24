package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Account
import com.example.data.UsageLog
import com.example.ui.theme.DesignTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Overview card for a single agent account on the dashboard list.
 *
 * Role = button (clickable to navigate to detail). Focus ring comes from
 * MaterialTheme.colorScheme.outline (>=3:1). Shows primary and secondary
 * usage progress bars when [latestLog] is non-null; shows a loading
 * placeholder when null.
 *
 * @param account the account to display.
 * @param latestLog the most recent UsageLog for this account, or null (loading).
 * @param onClick callback when the card is tapped.
 * @param modifier optional layout modifier.
 */
@Composable
fun AgentOverviewCard(
  account: Account,
  latestLog: UsageLog?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val providerName = when (account.provider) {
    "Anthropic" -> context.getString(R.string.provider_anthropic)
    "Ollama" -> context.getString(R.string.provider_ollama)
    else -> context.getString(R.string.provider_openai)
  }
  val cdCard = stringResource(R.string.cd_agent_card, providerName)
  val cdDetails = stringResource(R.string.ver_detalles)
  val syncPending = stringResource(R.string.sync_pending)
  val usageSessionLabel = stringResource(R.string.uso_de_sesion)
  val usageWeeklyLabel = stringResource(R.string.uso_semanal)
  val noDataText = stringResource(R.string.sin_datos_de_uso)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(
        role = Role.Button,
        onClick = onClick,
      )
      .semantics {
        this.contentDescription = cdCard
      },
    shape = RoundedCornerShape(DesignTokens.Radius.xl),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface,
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(
      DesignTokens.Spacing.none,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
  ) {
    Column(
      modifier = Modifier.padding(DesignTokens.Spacing.lg),
      verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
    ) {
      // Header: Badge + Email + Chevron
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
        ) {
          ProviderBadge(provider = account.provider)
          Text(
            text = account.email,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = cdDetails,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      if (latestLog != null) {
        // Primary usage gauge
        val primaryPercent = latestLog.primaryUsedPercent
        val isPrimaryHigh = primaryPercent > 80.0
        val primaryColor = if (isPrimaryHigh) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

        Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)) {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = usageSessionLabel,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = stringResource(R.string.por_ciento_usado, primaryPercent.toInt()),
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
              color = primaryColor,
            )
          }
          LinearProgressIndicator(
            progress = {
              (primaryPercent / 100.0).coerceIn(0.0, 1.0).toFloat()
            },
            color = primaryColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(DesignTokens.Spacing.xs)),
          )
        }

        // Secondary usage gauge (if applicable)
        if (latestLog.secondaryUsedPercent > 0.0 || account.provider != "Ollama") {
          val secondaryPercent = latestLog.secondaryUsedPercent
          val isSecondaryHigh = secondaryPercent >= 90.0
          val secondaryColor = if (isSecondaryHigh) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary

          Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)) {
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = usageWeeklyLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = stringResource(R.string.por_ciento_usado, secondaryPercent.toInt()),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = secondaryColor,
              )
            }
            LinearProgressIndicator(
              progress = {
                (secondaryPercent / 100.0).coerceIn(0.0, 1.0).toFloat()
              },
              color = secondaryColor,
              trackColor = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(DesignTokens.Spacing.xs)),
            )
          }
        }

        // Footer: plan + balance + last updated
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = stringResource(R.string.plan_label, latestLog.planType.uppercase()),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (latestLog.hasCredits) {
              Text(
                text = stringResource(R.string.saldo_label, latestLog.balance),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
          val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
          val updatedText = sdf.format(Date(account.lastUpdated))
          Text(
            text = updatedText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          )
        }
      } else {
        // Loading / sync pending state
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DesignTokens.Spacing.md),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = syncPending,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}