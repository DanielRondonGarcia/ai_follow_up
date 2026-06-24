package com.example.ui.theme

import androidx.compose.material3.Typography

/**
 * Material3 [Typography] built from [DesignTokens.Typography] (Major Third 1.25 scale).
 *
 * Every text style traces to a token in tokens/typography.json. No hardcoded
 * sp values — all come from [DesignTokens.Typography].
 */
val AppTypography =
  Typography(
    displayLarge = DesignTokens.Typography.displayLarge,
    displayMedium = DesignTokens.Typography.displayMedium,
    headlineLarge = DesignTokens.Typography.headlineLarge,
    headlineMedium = DesignTokens.Typography.headlineMedium,
    titleLarge = DesignTokens.Typography.titleLarge,
    titleMedium = DesignTokens.Typography.titleMedium,
    bodyLarge = DesignTokens.Typography.bodyLarge,
    bodyMedium = DesignTokens.Typography.bodyMedium,
    labelLarge = DesignTokens.Typography.labelLarge,
    labelMedium = DesignTokens.Typography.labelMedium,
    labelSmall = DesignTokens.Typography.labelSmall,
  )