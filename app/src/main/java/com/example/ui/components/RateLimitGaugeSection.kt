package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.DesignTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gauge section, reset-times row, and reset-time formatters for
 * [RateLimitCard].
 *
 * Extracted from RateLimitCard.kt to keep the card composable under 250
 * lines. All functions are internal to the components package.
 */

/**
 * Single gauge section with label, percentage text, linear progress bar.
 * Color switches to error when percentage >= 100.
 */
@Composable
internal fun RateLimitGaugeSection(
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
 * Reset-times row: two weighted columns (primary + weekly) with a vertical
 * divider between them. Extracted from RateLimitCard to reduce line count.
 */
@Composable
internal fun ResetTimesRow(
  primaryResetAfterSeconds: Long,
  primaryResetAt: Long,
  secondaryResetAfterSeconds: Long,
  secondaryResetAt: Long,
  seRestableceLabel: String,
  naLabel: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val primaryResetLabel = remember(primaryResetAfterSeconds, primaryResetAt) {
      formatResetTime(primaryResetAfterSeconds, primaryResetAt, naLabel)
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

    val secondaryResetLabel = remember(secondaryResetAfterSeconds, secondaryResetAt) {
      formatResetTimeWeekly(secondaryResetAfterSeconds, secondaryResetAt, naLabel)
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

/**
 * Formats the reset time for the primary (hours) window.
 */
internal fun formatResetTime(
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
internal fun formatResetTimeWeekly(
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