package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Account
import com.example.data.UsageLog
import com.example.ui.components.AgentOverviewCard
import com.example.ui.components.ErrorBanner
import com.example.ui.components.SectionHeader
import com.example.ui.theme.DesignTokens

/**
 * Parent dashboard: list of agent cards with pull-to-refresh.
 *
 * Extracted from MainScreen.kt (parent dashboard block). State is passed in
 * from the host; the only local state is [isRefreshingAll], which tracks the
 * pull-to-refresh drag lifecycle.
 *
 * @param accounts the current list of accounts.
 * @param allLogs all usage logs across accounts (filtered per-card inside).
 * @param isLoading global loading flag from the ViewModel.
 * @param errorMessage nullable error message; shown via [ErrorBanner] when non-null.
 * @param onAccountClick callback when a card is tapped (receives the account id).
 * @param onSyncAll callback to sync all accounts; receives an onComplete lambda
 *                   so the local refresh flag can be reset.
 * @param onClearError callback to dismiss the error banner.
 * @param modifier optional layout modifier.
 */
@Composable
fun DashboardScreen(
  accounts: List<Account>,
  allLogs: List<UsageLog>,
  isLoading: Boolean,
  errorMessage: String?,
  onAccountClick: (Int) -> Unit,
  onSyncAll: (() -> Unit) -> Unit,
  onClearError: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var isRefreshingAll by remember { mutableStateOf(false) }
  val pullCdLabel = stringResource(R.string.cd_actualizar)
  val updatingLabel = stringResource(R.string.actualizando)

  PullToRefreshContainer(
    isRefreshing = isRefreshingAll || isLoading,
    onRefresh = {
      isRefreshingAll = true
      onSyncAll { isRefreshingAll = false }
    },
    pullLabel = pullCdLabel,
    updatingLabel = updatingLabel,
  ) {
    LazyColumn(
      modifier = modifier
        .fillMaxSize()
        .padding(horizontal = DesignTokens.Spacing.lg),
      verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg),
      contentPadding = PaddingValues(top = DesignTokens.Spacing.lg, bottom = 88.dp),
    ) {
      // Welcome card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(DesignTokens.Radius.xxl),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
          ),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
            ) {
              Text(
                text = stringResource(R.string.panel_de_control),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
              Text(
                text = stringResource(R.string.panel_de_control_subtitulo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
              )
            }
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Troubleshoot,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp),
              )
            }
          }
        }
      }

      // Error banner
      if (errorMessage != null) {
        item {
          ErrorBanner(
            message = errorMessage,
            onDismiss = onClearError,
          )
        }
      }

      // Section header
      item {
        SectionHeader(title = stringResource(R.string.tus_agentes_conectados))
      }

      // Agent cards
      items(accounts, key = { it.id }) { account ->
        val latestLog = allLogs.filter { it.accountId == account.id }
          .maxByOrNull { it.timestamp }
        AgentOverviewCard(
          account = account,
          latestLog = latestLog,
          onClick = { onAccountClick(account.id) },
        )
      }
    }
  }
}

/**
 * Custom pull-to-refresh container (NestedScrollConnection).
 *
 * Kept verbatim from the pre-redesign MainScreen for PR 3. PR 4 replaces
 * this with Material3 PullToRefreshBox.
 */
@Composable
private fun PullToRefreshContainer(
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  pullLabel: String,
  updatingLabel: String,
  content: @Composable () -> Unit,
) {
  var dragOffsetY by remember { mutableStateOf(0f) }
  val maxDragOffset = 250f
  val triggerOffset = 150f

  val nestedScrollConnection = remember {
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (available.y < 0 && dragOffsetY > 0) {
          val prev = dragOffsetY
          dragOffsetY = (dragOffsetY + available.y).coerceAtLeast(0f)
          return Offset(0f, dragOffsetY - prev)
        }
        return Offset.Zero
      }

      override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
      ): Offset {
        if (available.y > 0) {
          val prev = dragOffsetY
          dragOffsetY = (dragOffsetY + available.y * 0.5f).coerceAtMost(maxDragOffset)
          return Offset(0f, dragOffsetY - prev)
        }
        return Offset.Zero
      }

      override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (dragOffsetY >= triggerOffset && !isRefreshing) {
          onRefresh()
        }
        dragOffsetY = 0f
        return Velocity.Zero
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .nestedScroll(nestedScrollConnection),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer { translationY = dragOffsetY },
    ) {
      content()
    }

    if (dragOffsetY > 0 || isRefreshing) {
      Box(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = DesignTokens.Spacing.lg)
          .graphicsLayer { translationY = (dragOffsetY * 0.5f).coerceAtMost(50f) },
      ) {
        Card(
          shape = CircleShape,
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.lg, vertical = DesignTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
          ) {
            if (isRefreshing) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
              Text(
                updatingLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            } else {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = pullLabel,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp),
              )
              Text(
                pullLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }
        }
      }
    }
  }
}