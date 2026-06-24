package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.DesignTokens

/**
 * Dismissible error banner with animate-out.
 *
 * Uses errorContainer / error semantic tokens (auto-swap light/dark).
 * The dismiss button carries a contentDescription for screen readers.
 *
 * @param message the error text to display.
 * @param onDismiss callback invoked when the user taps the dismiss button.
 * @param modifier optional layout modifier.
 */
@Composable
fun ErrorBanner(
  message: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = message.isNotEmpty(),
    exit = fadeOut(animationSpec = tween(DesignTokens.Motion.fast)),
    modifier = modifier,
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(DesignTokens.Radius.lg),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
      ),
      border = BorderStroke(
        DesignTokens.Spacing.none,
        MaterialTheme.colorScheme.error,
      ),
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
          horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm + DesignTokens.Spacing.xs),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              tint = Color.White,
              contentDescription = null,
              modifier = Modifier.size(DesignTokens.Spacing.lg),
            )
          }
          Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
        val cdDismiss = stringResource(R.string.cd_dismiss_error)
        IconButton(
          onClick = onDismiss,
            modifier = Modifier.size(DesignTokens.Spacing.xxxl),
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            contentDescription = cdDismiss,
            modifier = Modifier.size(DesignTokens.Spacing.lg),
          )
        }
      }
    }
  }
}