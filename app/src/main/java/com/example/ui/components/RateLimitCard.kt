package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.UsageLog
import com.example.ui.theme.DesignTokens

/**
 * Rate-limit card with two gauge sections (primary 3h + weekly 7d).
 *
 * Uses feedback.error when percentage >= 100, otherwise action.primary or
 * action.secondary. Each gauge value carries a contentDescription for
 * screen readers. Reset times displayed below the gauges.
 *
 * Gauge sections render via [RateLimitGaugeSection] and the reset-times row
 * via [ResetTimesRow], both extracted to keep this composable under 250
 * lines.
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
      ResetTimesRow(
        primaryResetAfterSeconds = log.primaryResetAfterSeconds,
        primaryResetAt = log.primaryResetAt,
        secondaryResetAfterSeconds = log.secondaryResetAfterSeconds,
        secondaryResetAt = log.secondaryResetAt,
        seRestableceLabel = seRestableceLabel,
        naLabel = naLabel,
      )
    }
  }
}