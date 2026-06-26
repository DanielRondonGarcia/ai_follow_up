package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Account
import com.example.data.UsageLog
import com.example.ui.theme.DesignTokens
import java.util.Locale

/**
 * Sub-sections for [AccountDetailScreen] (part 1): limit-reached banner
 * and the plan/credits mini-cards row.
 */

@Composable
internal fun LimitReachedBanner(provider: String?) {
  val providerName = when (provider) {
    "Anthropic" -> stringResource(R.string.provider_anthropic)
    "Ollama" -> stringResource(R.string.provider_ollama)
    else -> stringResource(R.string.provider_openai)
  }
  val subtitle = stringResource(R.string.limite_alcanzado_subtitulo, providerName)
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(DesignTokens.Radius.xxl),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(DesignTokens.Spacing.lg),
      horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
      verticalAlignment = Alignment.Top,
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.VpnKey,
          contentDescription = stringResource(R.string.alerta),
          tint = MaterialTheme.colorScheme.onError,
          modifier = Modifier.size(20.dp),
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.limite_alcanzado),
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
        )
      }
    }
  }
}

@Composable
internal fun PlanTypeCard(activeAccount: Account?, activeLog: UsageLog, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier.height(110.dp),
    shape = RoundedCornerShape(DesignTokens.Radius.xxl),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = stringResource(R.string.tipo_de_plan),
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          letterSpacing = 1.sp,
          fontSize = 9.sp,
        ),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
      ) {
        Text(
          text = (activeAccount?.planType ?: activeLog.planType).uppercase(),
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
          ),
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(DesignTokens.Radius.md))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
          Text(
            text = stringResource(R.string.activo),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontSize = 8.sp,
            ),
          )
        }
      }
    }
  }
}

@Composable
internal fun CreditsMiniCard(activeLog: UsageLog, modifier: Modifier = Modifier) {
  val creditosLabel = stringResource(R.string.creditos)
  val displayBalance = if (activeLog.unlimited) {
    "\u221E"
  } else if (activeLog.balance.contains("/")) {
    activeLog.balance
  } else {
    String.format(Locale.US, "%.2f", activeLog.balance.toDoubleOrNull() ?: 0.0)
  }
  Card(
    modifier = modifier.height(110.dp),
    shape = RoundedCornerShape(DesignTokens.Radius.xxl),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = creditosLabel,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          letterSpacing = 1.sp,
          fontSize = 9.sp,
        ),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
      ) {
        Text(
          text = displayBalance,
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
          ),
        )
        Text(
          text = stringResource(R.string.usd),
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
          ),
          modifier = Modifier.padding(bottom = 2.dp),
        )
      }
    }
  }
}