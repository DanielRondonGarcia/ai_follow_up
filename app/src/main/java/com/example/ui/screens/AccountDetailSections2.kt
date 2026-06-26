package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UsageLog
import com.example.ui.theme.DesignTokens

/**
 * Sub-sections for [AccountDetailScreen] (part 2): auth-token card and
 * the credits + message-estimations card with its estimation metric subcards.
 */

@Composable
internal fun AuthTokenCard(isLoading: Boolean, onSync: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(DesignTokens.Radius.xxl),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(DesignTokens.Spacing.md),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.VpnKey,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
        }
        Column {
          Text(
            text = stringResource(R.string.token_valido),
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
          )
          Text(
            text = stringResource(R.string.sincronizado_correctamente),
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              fontSize = 10.sp,
            ),
          )
        }
      }

      Button(
        onClick = onSync,
        enabled = !isLoading,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.primary,
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = Modifier.height(36.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp,
          )
        } else {
          Text(
            text = stringResource(R.string.refresh),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 10.sp,
            ),
          )
        }
      }
    }
  }
}

@Composable
internal fun CreditsEstimationsCard(log: UsageLog) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(DesignTokens.Radius.xl),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
  ) {
    Column(
      modifier = Modifier.padding(DesignTokens.Spacing.lg),
      verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
    ) {
      Text(
        text = stringResource(R.string.usos_mensajes_creditos),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
      ) {
        EstimationMetricCard(
          title = stringResource(R.string.mensajes_locales_est),
          value = if (log.approxLocalLimit > 0) "${log.approxLocalUsed} / ${log.approxLocalLimit}" else stringResource(R.string.na),
          modifier = Modifier.weight(1f),
        )
        EstimationMetricCard(
          title = stringResource(R.string.mensajes_nube_est),
          value = if (log.approxCloudLimit > 0) "${log.approxCloudUsed} / ${log.approxCloudLimit}" else stringResource(R.string.na),
          modifier = Modifier.weight(1f),
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
        ) {
          Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
          Column {
            Text(
              text = stringResource(R.string.balance_creditos),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = if (log.unlimited) stringResource(R.string.usos_ilimitado) else stringResource(R.string.limites_controlados_label),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Text(
          text = if (log.unlimited) stringResource(R.string.ilimitado) else "$${log.balance}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}

@Composable
internal fun EstimationMetricCard(
  title: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
    shape = RoundedCornerShape(DesignTokens.Radius.xl),
  ) {
    Column(
      modifier = Modifier.padding(DesignTokens.Spacing.md),
      verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = Icons.Default.Forum,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp),
        )
        Text(
          text = title,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}