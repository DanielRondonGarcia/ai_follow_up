package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import com.example.data.UsageLog
import com.example.ui.theme.DesignTokens
import java.util.Locale

/**
 * Credits / balance card showing the account balance and unlimited flag.
 *
 * Uses surfaceVariant + primary semantic tokens. Numeric balance carries
 * a contentDescription for screen readers.
 *
 * @param log the usage log containing credits data.
 * @param modifier optional layout modifier.
 */
@Composable
fun CreditsCard(
  log: UsageLog,
  modifier: Modifier = Modifier,
) {
  val creditosLabel = stringResource(R.string.creditos)
  val balanceLabel = stringResource(R.string.balance_creditos)
  val ilimitadoLabel = stringResource(R.string.ilimitado)
  val unlimitedDescLabel = stringResource(R.string.uso_ilimitado_activado)
  val limitesDescLabel = stringResource(R.string.limites_controlados)
  val usdLabel = stringResource(R.string.usd)

  // Build display balance string
  val displayBalance = if (log.unlimited) {
    "\u221E" // infinity symbol
  } else if (log.balance.contains("/")) {
    log.balance
  } else {
    String.format(Locale.US, "%.2f", log.balance.toDoubleOrNull() ?: 0.0)
  }

  val cdBalance = stringResource(R.string.cd_credits_balance, displayBalance)
  val subtitleText = if (log.unlimited) unlimitedDescLabel else limitesDescLabel

  Card(
    modifier = modifier
      .fillMaxWidth()
      .semantics {
        this.contentDescription = cdBalance
      },
    shape = RoundedCornerShape(DesignTokens.Radius.xl),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(DesignTokens.Spacing.md + DesignTokens.Spacing.xs),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = creditosLabel,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
        ) {
          Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(DesignTokens.Spacing.lg),
          )
          Column {
            Text(
              text = balanceLabel,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = subtitleText,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = displayBalance,
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface,
            ),
          )
          if (!log.unlimited) {
            Text(
              text = usdLabel,
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
            )
          }
        }
      }
    }
  }
}