package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
@Composable
private fun tokenScheme(isDark: Boolean): ColorScheme = (if (isDark) darkColorScheme() else lightColorScheme()).copy(
  primary = DesignTokens.Colors.Semantic.actionPrimary, onPrimary = DesignTokens.Colors.Semantic.textOnAction, primaryContainer = DesignTokens.Colors.Semantic.primaryContainer, onPrimaryContainer = DesignTokens.Colors.Semantic.onPrimaryContainer,
  secondary = DesignTokens.Colors.Semantic.actionSecondary, onSecondary = DesignTokens.Colors.Semantic.textOnAction, secondaryContainer = DesignTokens.Colors.Semantic.secondaryContainer, onSecondaryContainer = DesignTokens.Colors.Semantic.onSecondaryContainer,
  tertiary = DesignTokens.Colors.Semantic.tertiary, onTertiary = DesignTokens.Colors.Semantic.onTertiary, tertiaryContainer = DesignTokens.Colors.Semantic.tertiaryContainer, onTertiaryContainer = DesignTokens.Colors.Semantic.onTertiaryContainer,
  background = DesignTokens.Colors.Semantic.surfacePage, onBackground = DesignTokens.Colors.Semantic.textPrimary, surface = DesignTokens.Colors.Semantic.surfaceCard, onSurface = DesignTokens.Colors.Semantic.textPrimary,
  surfaceVariant = DesignTokens.Colors.Semantic.surfaceVariant, onSurfaceVariant = DesignTokens.Colors.Semantic.textSecondary, outline = DesignTokens.Colors.Semantic.outline, outlineVariant = DesignTokens.Colors.Semantic.outlineVariant,
  error = DesignTokens.Colors.Semantic.error, onError = DesignTokens.Colors.Semantic.onError, errorContainer = DesignTokens.Colors.Semantic.errorContainer, onErrorContainer = DesignTokens.Colors.Semantic.onErrorContainer,
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val scheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
      if (darkTheme) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current)
    else -> tokenScheme(darkTheme)
  }
  MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
}