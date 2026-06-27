package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.ProviderBadge
import com.example.ui.theme.DesignTokens

/**
 * Single provider card on the onboarding screen.
 *
 * Extracted from OnboardingScreen.kt to keep the main composable under 250
 * lines. Provider brand background, border, and title colors are resolved in
 * the caller and passed in; the button color is a provider identity token
 * resolved via [com.example.ui.theme.ProviderColor].
 */
@Composable
internal fun ProviderOnboardingCard(
  provider: String,
  title: String,
  description: String,
  cardBg: Color,
  cardBorder: Color,
  titleColor: Color,
  buttonColor: Color,
  buttonLabel: String,
  contentDescription: String,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(DesignTokens.Radius.xl),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    border = BorderStroke(1.dp, cardBorder),
  ) {
    Column(
      modifier = Modifier.padding(DesignTokens.Spacing.lg),
      verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm + DesignTokens.Spacing.xs),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm + DesignTokens.Spacing.xs),
      ) {
        ProviderBadge(provider = provider)
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = titleColor,
          )
          Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = titleColor.copy(alpha = 0.8f),
          )
        }
      }
      Button(
        onClick = onClick,
        modifier = Modifier
          .fillMaxWidth()
          .semantics { this.contentDescription = contentDescription },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        shape = RoundedCornerShape(DesignTokens.Radius.md),
      ) {
        Icon(imageVector = Icons.Default.Web, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.padding(horizontal = DesignTokens.Spacing.xs))
        Text(buttonLabel, fontSize = DesignTokens.Sizing.small13, fontWeight = FontWeight.Bold)
      }
    }
  }
}