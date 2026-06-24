package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.UsageLog
import com.example.ui.theme.DesignTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Rate-limit card with two gauge sections (primary 3h + weekly 7d).
 *
 * Uses feedback.error when percentage >= 100, otherwise action.primary or
 * action.secondary. Each gauge value carries a contentDescription for
 * screen readers. Reset times displayed below the gauges.
 *
 * @param log the usage log containing rate-limit data.
 * @param modifier optional layout modifier.
 */
@Composable
fun RateLimitCard(
  log: UsageLog,
  modifier: Modifier = Modifier,
) {
  val rateLimitsLabel = stringResource(R.string.rate_limits)
  val updatedLabel = stringResource(R.string.actualizado_recientemente)
  val ventanaPrincipalLabel = stringResource(R.string.ventana_principal)
  val ventanaSemanalLabel = stringResource(R.string.ventana_semanal)
  val seRestableceLabel = stringResource(R.string.se_restablece_en_label)
  val naLabel = stringResource(R.string.na)
  val usadoLabel = stringResource(R.string.usado)
  val cdGaugePrimary = stringResource(R.string.cd_gauge_primary, log.primaryUsedPercent.toInt())
  val cdGaugeWeekly = stringResource(R.string.cd_gauge_weekly, log.secondaryUsedPercent.toInt())

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(DesignTokens.Radius.xl),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface,
    ),
    border = BorderStroke(
      DesignTokens.Spacing.none,
      MaterialTheme.colorScheme.outline,
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(DesignTokens.Spacing.lg),
      verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md + DesignTokens.Spacing.xs),
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = rateLimitsLabel,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
        )
        Text(
          text = updatedLabel,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
          ),
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md)) {
        // Primary window (3h)
        RateLimitGaugeSection(
          label = ventanaPrincipalLabel,
          percentage = log.primaryUsedPercent,
          resetSeconds = log.primaryResetAfterSeconds,
          resetAt = log.primaryResetAt,
          naLabel = naLabel,
          usadoLabel = usadoLabel,
          seRestableceLabel = seRestableceLabel,
          contentDescription = cdGaugePrimary,
        )

        // Weekly window (7d)
        RateLimitGaugeSection(
          label = ventanaSemanalLabel,
          percentage = log.secondaryUsedPercent,
          resetSeconds = log.secondaryResetAfterSeconds,
          resetAt = log.secondaryResetAt,
          naLabel = naLabel,
          usadoLabel = usadoLabel,
          seRestableceLabel = seRestableceLabel,
          contentDescription = cdGaugeWeekly,
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

      // Reset times row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val primaryResetLabel = remember(log.primaryResetAfterSeconds, log.primaryResetAt) {
          formatResetTime(log.primaryResetAfterSeconds, log.primaryResetAt, naLabel)
        }

        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = seRestableceLabel,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
          )
          Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
          Text(
            text = primaryResetLabel,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            ),
          )
        }

        Box(
          modifier = Modifier
            .width(1.dp)
            .height(DesignTokens.Spacing.xxl),
        )

        val secondaryResetLabel = remember(log.secondaryResetAfterSeconds, log.secondaryResetAt) {
          formatResetTimeWeekly(log.secondaryResetAfterSeconds, log.secondaryResetAt, naLabel)
        }

        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = seRestableceLabel,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
          )
          Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
          Text(
            text = secondaryResetLabel,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            ),
          )
        }
      }
    }
  }
}

/**
 * Single gauge section with label, percentage text, linear progress bar.
 * Color switches to error when percentage >= 100.
 */
@Composable
private fun RateLimitGaugeSection(
  label: String,
  percentage: Double,
  resetSeconds: Long,
  resetAt: Long,
  naLabel: String,
  usadoLabel: String,
  seRestableceLabel: String,
  contentDescription: String,
) {
  val isLimitReached = percentage >= 100.0
  val isHigh = percentage >= 90.0
  val barColor = when {
    isLimitReached -> MaterialTheme.colorScheme.error
    isHigh -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.primary
  }

  Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)) {
    Row(
      modifier = Modifier.fillMaxWidth().semantics {
        this.contentDescription = contentDescription
      },
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(
          color = MaterialTheme.colorScheme.onSurface,
        ),
      )
      Text(
        text = stringResource(R.string.por_ciento_usado, percentage.toInt()),
        style = MaterialTheme.typography.bodySmall.copy(
          fontWeight = FontWeight.Bold,
          color = barColor,
        ),
      )
    }
    LinearProgressIndicator(
      progress = {
        (percentage / 100.0).coerceIn(0.0, 1.0).toFloat()
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(DesignTokens.Spacing.sm)
        .clip(RoundedCornerShape(DesignTokens.Spacing.xs)),
      color = barColor,
      trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
  }
}

/**
 * Formats the reset time for the primary (hours) window.
 */
private fun formatResetTime(
  resetSeconds: Long,
  resetAt: Long,
  naLabel: String,
): String {
  return if (resetSeconds > 0) {
    val hrs = resetSeconds / 3600
    val mins = (resetSeconds % 3600) / 60
    if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
  } else if (resetAt > 0) {
    val date = Date(resetAt * 1000)
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    sdf.format(date)
  } else {
    naLabel
  }
}

/**
 * Formats the reset time for the weekly (days) window.
 */
private fun formatResetTimeWeekly(
  resetSeconds: Long,
  resetAt: Long,
  naLabel: String,
): String {
  return if (resetSeconds > 0) {
    val days = resetSeconds / 86400
    val hrs = (resetSeconds % 86400) / 3600
    if (days > 0) "${days}d ${hrs}h" else "${hrs}h"
  } else if (resetAt > 0) {
    val date = Date(resetAt * 1000)
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    sdf.format(date)
  } else {
    naLabel
  }
}