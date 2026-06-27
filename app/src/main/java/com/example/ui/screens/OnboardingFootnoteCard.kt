package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.DesignTokens

/**
 * Informative footnote card on the onboarding screen.
 *
 * Extracted from OnboardingScreen.kt to keep the main composable under 250
 * lines. Renders the help icon + onboarding note with micro font sizing from
 * [DesignTokens.Sizing].
 */
@Composable
internal fun OnboardingFootnoteCard() {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    ),
    shape = RoundedCornerShape(DesignTokens.Radius.sm + DesignTokens.Spacing.xs),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(DesignTokens.Spacing.md),
      horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm + DesignTokens.Spacing.xs),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = stringResource(R.string.onboarding_nota),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = DesignTokens.Sizing.micro11,
        lineHeight = DesignTokens.Sizing.micro15,
      )
    }
  }
}