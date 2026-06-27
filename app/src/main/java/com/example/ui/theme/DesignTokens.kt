package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Single source of truth for Compose design tokens.
 *
 * Hand-authored 3-tier bridge over tokens/{colors,spacing,typography,borders,motion}.json.
 * Components and screens MUST reference [DesignTokens] exclusively — never raw hex/sp/dp.
 *
 * Architecture:
 *   Primitives  -> raw palette (never reference directly in components)
 *   Semantic     -> purpose-based, swaps on isSystemInDarkTheme()
 *   Component    -> scoped to specific components (provider badges, etc.)
 */
object DesignTokens {

  // ---------------------------------------------------------------------------
  // COLOR
  // ---------------------------------------------------------------------------

  object Colors {

    /** Raw palette — never reference directly in component code. */
    object Primitives {
      // Gray
      val gray50 = Color(0xFFF9FAFB)
      val gray100 = Color(0xFFF3F4F6)
      val gray200 = Color(0xFFE5E7EB)
      val gray300 = Color(0xFFD1D5DB)
      val gray400 = Color(0xFF9CA3AF)
      val gray500 = Color(0xFF6B7280)
      val gray600 = Color(0xFF4B5563)
      val gray700 = Color(0xFF374151)
      val gray800 = Color(0xFF1F2937)
      val gray900 = Color(0xFF111827)
      val gray950 = Color(0xFF030712)

      // Blue
      val blue50 = Color(0xFFEFF6FF)
      val blue100 = Color(0xFFDBEAFE)
      val blue200 = Color(0xFFBFDBFE)
      val blue300 = Color(0xFF93C5FD)
      val blue400 = Color(0xFF60A5FA)
      val blue500 = Color(0xFF3B82F6)
      val blue600 = Color(0xFF2563EB)
      val blue700 = Color(0xFF1D4ED8)
      val blue800 = Color(0xFF1E40AF)
      val blue900 = Color(0xFF1E3A8A)
      val blue950 = Color(0xFF172554)

      // Green
      val green50 = Color(0xFFF0FDF4)
      val green100 = Color(0xFFDCFCE7)
      val green200 = Color(0xFFBBF7D0)
      val green300 = Color(0xFF86EFAC)
      val green400 = Color(0xFF4ADE80)
      val green500 = Color(0xFF22C55E)
      val green600 = Color(0xFF16A34A)
      val green700 = Color(0xFF15803D)
      val green800 = Color(0xFF166534)
      val green900 = Color(0xFF14532D)
      val green950 = Color(0xFF052E16)

      // Red
      val red50 = Color(0xFFFEF2F2)
      val red100 = Color(0xFFFEE2E2)
      val red200 = Color(0xFFFECACA)
      val red300 = Color(0xFFFCA5A5)
      val red400 = Color(0xFFF87171)
      val red500 = Color(0xFFEF4444)
      val red600 = Color(0xFFDC2626)
      val red700 = Color(0xFFB91C1C)
      val red800 = Color(0xFF991B1B)
      val red900 = Color(0xFF7F1D1D)
      val red950 = Color(0xFF450A0A)

      // Amber
      val amber50 = Color(0xFFFFFBEB)
      val amber100 = Color(0xFFFEF3C7)
      val amber200 = Color(0xFFFDE68A)
      val amber300 = Color(0xFFFCD34D)
      val amber400 = Color(0xFFFBBF24)
      val amber500 = Color(0xFFF59E0B)
      val amber600 = Color(0xFFD97706)
      val amber700 = Color(0xFFB45309)
      val amber800 = Color(0xFF92400E)
      val amber900 = Color(0xFF78350F)
      val amber950 = Color(0xFF451A03)

      // Emerald (OpenAI brand carry-forward from hotfix #143)
      val emerald700 = Color(0xFF047857)

      // Purple (tertiary accent)
      val purple50 = Color(0xFFFAF5FF)
      val purple100 = Color(0xFFF3E8FF)
      val purple200 = Color(0xFFE9D5FF)
      val purple300 = Color(0xFFD8B4FE)
      val purple400 = Color(0xFFC084FC)
      val purple500 = Color(0xFFA855F7)
      val purple600 = Color(0xFF9333EA)
      val purple700 = Color(0xFF7E22CE)
      val purple800 = Color(0xFF6B21A8)
      val purple900 = Color(0xFF581C87)
      val purple950 = Color(0xFF3B0764)

      val white = Color(0xFFFFFFFF)
      val black = Color(0xFF000000)
    }

    /**
     * Purpose-based semantic tokens. All @Composable getters swap on
     * isSystemInDarkTheme(). Primitives stay the same — only semantic swaps.
     */
    object Semantic {

      // -- Action ----------------------------------------------------------
      val actionPrimary: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.blue400 else Primitives.blue600

      val actionPrimaryHover: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.blue300 else Primitives.blue700

      val actionPrimaryActive: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.blue700 else Primitives.blue800

      val actionSecondary: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray800 else Primitives.gray100

      val actionSecondaryHover: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray700 else Primitives.gray200

      val actionDestructive: Color
        @Composable get() = Primitives.red600

      val actionDestructiveHover: Color
        @Composable get() = Primitives.red700

      // -- Text ------------------------------------------------------------
      val textPrimary: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray50 else Primitives.gray900

      val textSecondary: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray400 else Primitives.gray600

      val textTertiary: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray500 else Primitives.gray400

      val textDisabled: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray600 else Primitives.gray300

      val textOnAction: Color
        @Composable get() = Primitives.white

      val textOnDark: Color
        @Composable get() = Primitives.gray50

      val textLink: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.blue400 else Primitives.blue600

      val textLinkHover: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.blue300 else Primitives.blue800

      // -- Surface ---------------------------------------------------------
      val surfacePage: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray950 else Primitives.white

      val surfaceCard: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray900 else Primitives.white

      val surfaceRaised: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray800 else Primitives.white

      val surfaceSunken: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.black else Primitives.gray50

      val surfaceDisabled: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray800 else Primitives.gray100

      val surfaceVariant: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray800 else Primitives.gray100

      // -- Border ----------------------------------------------------------
      val borderDefault: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray800 else Primitives.gray200

      val borderStrong: Color
        @Composable get() = Primitives.gray500

      val borderFocus: Color
        @Composable get() = Primitives.blue500

      val borderError: Color
        @Composable get() = Primitives.red500

      val borderDisabled: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray800 else Primitives.gray200

      // -- Feedback --------------------------------------------------------
      val feedbackErrorBg: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.red900 else Primitives.red50

      val feedbackErrorText: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.red300 else Primitives.red800

      val feedbackErrorBorder: Color
        @Composable get() = Primitives.red300

      val feedbackErrorIcon: Color
        @Composable get() = Primitives.red600

      val feedbackWarningBg: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.amber900 else Primitives.amber50

      val feedbackWarningText: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.amber300 else Primitives.amber800

      val feedbackWarningBorder: Color
        @Composable get() = Primitives.amber300

      val feedbackWarningIcon: Color
        @Composable get() = Primitives.amber600

      val feedbackSuccessBg: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.green900 else Primitives.green50

      val feedbackSuccessText: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.green300 else Primitives.green800

      val feedbackSuccessBorder: Color
        @Composable get() = Primitives.green300

      val feedbackSuccessIcon: Color
        @Composable get() = Primitives.green600

      val feedbackInfoBg: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.blue900 else Primitives.blue50

      val feedbackInfoText: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.blue300 else Primitives.blue800

      val feedbackInfoBorder: Color
        @Composable get() = Primitives.blue300

      val feedbackInfoIcon: Color
        @Composable get() = Primitives.blue600

      // -- Interactive ------------------------------------------------------
      val interactiveHoverOverlay: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.white.copy(alpha = 0.08f) else Primitives.black.copy(alpha = 0.04f)

      val interactiveActiveOverlay: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.white.copy(alpha = 0.12f) else Primitives.black.copy(alpha = 0.08f)

      val interactiveSelectedBg: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.blue950 else Primitives.blue50

      val interactiveSelectedBorder: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.blue800 else Primitives.blue200

      val interactiveFocusRing: Color
        @Composable get() = Primitives.blue500

      // -- Container pairings (for Material3 ColorScheme) -------------------
      val primaryContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.blue800 else Primitives.blue100

      val onPrimaryContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.blue100 else Primitives.blue900

      val secondaryContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray700 else Primitives.gray200

      val onSecondaryContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray100 else Primitives.gray700

      val tertiary: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.purple950 else Primitives.purple100

      val onTertiary: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.purple100 else Primitives.purple950

      val tertiaryContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.purple800 else Primitives.purple50

      val onTertiaryContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.purple50 else Primitives.purple800

      val outline: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray500 else Primitives.gray500

      val outlineVariant: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.gray800 else Primitives.gray200

      val error: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.red400 else Primitives.red600

      val onError: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.red950 else Primitives.white

      val errorContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.red900 else Primitives.red50

      val onErrorContainer: Color
        @Composable get() = if (isSystemInDarkTheme()) Primitives.red300 else Primitives.red900
    }

    /**
     * Component-scoped tokens. Provider brand colors are NOT theme-swapped —
     * they are brand identity tokens carried verbatim from hotfix #143.
     * White text on all three passes WCAG AA (>=4.5:1):
     *   - OpenAI emerald-700  #047857 -> 5.48:1
     *   - Anthropic amber-700 #B45309 -> 5.02:1
     *   - Ollama gray-500     #6B7280 -> 4.83:1
     */
    object Component {
      val providerOpenAi = Primitives.emerald700
      val providerAnthropic = Primitives.amber700
      val providerOllama = Primitives.gray500
    }
  }

  // ---------------------------------------------------------------------------
  // SPACING — 4px base unit grid (from tokens/spacing.json)
  // ---------------------------------------------------------------------------

  object Spacing {
    val none = 0.dp
    val xs = 4.dp       // scale.1  — tight stack (icon + label)
    val sm = 8.dp       // scale.2  — default inline gap
    val md = 12.dp      // scale.3  — form field to label
    val lg = 16.dp      // scale.4  — between card sections
    val xl = 24.dp      // scale.6  — between logical groups
    val xxl = 32.dp     // scale.8  — major section separation
    val xxxl = 48.dp    // scale.12 — large container padding
    val huge = 64.dp    // scale.16 — page section separation
  }

  // ---------------------------------------------------------------------------
  // RADIUS — geometric scale (from task spec)
  // ---------------------------------------------------------------------------

  object Radius {
    val none = 0.dp
    val sm = 4.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val full = 9999.dp
  }

  // ---------------------------------------------------------------------------
  // TYPOGRAPHY — Major Third (1.25) scale mapped to Material3 text styles
  // Source: tokens/typography.json
  // ---------------------------------------------------------------------------

  object Typography {
    private val fontFamily = FontFamily.Default

    // Heading-1: 48px / 1.0 / Bold / -0.05em
    val displayLarge = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 48.sp,
      lineHeight = 48.sp,
      letterSpacing = (-2.4).sp,
    )

    // Heading-2: 36px / 1.25 / Bold / -0.025em
    val displayMedium = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 36.sp,
      lineHeight = 45.sp,
      letterSpacing = (-0.9).sp,
    )

    // Heading-3: 30px / 1.25 / Semibold / -0.025em
    val headlineLarge = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 30.sp,
      lineHeight = 37.5.sp,
      letterSpacing = (-0.75).sp,
    )

    // Heading-4: 24px / 1.375 / Semibold / 0em
    val headlineMedium = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 24.sp,
      lineHeight = 33.sp,
      letterSpacing = 0.sp,
    )

    // Heading-5: 20px / 1.375 / Semibold / 0em
    val titleLarge = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 20.sp,
      lineHeight = 27.5.sp,
      letterSpacing = 0.sp,
    )

    // Heading-6: 18px / 1.375 / Semibold / 0em
    val titleMedium = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 18.sp,
      lineHeight = 24.75.sp,
      letterSpacing = 0.sp,
    )

    // Body-lg: 18px / 1.625 / Regular / 0em
    val bodyLarge = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 18.sp,
      lineHeight = 29.25.sp,
      letterSpacing = 0.sp,
    )

    // Body-base: 16px / 1.5 / Regular / 0em
    val bodyMedium = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.sp,
    )

    // Label: 14px / 1.5 / Medium / 0em
    val labelLarge = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp,
      lineHeight = 21.sp,
      letterSpacing = 0.sp,
    )

    // Caption: 12px / 1.5 / Regular / 0.025em
    val labelMedium = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 18.sp,
      letterSpacing = 0.3.sp,
    )

    // Overline: 12px / 1.5 / Semibold / 0.1em
    val labelSmall = TextStyle(
      fontFamily = fontFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 12.sp,
      lineHeight = 18.sp,
      letterSpacing = 1.2.sp,
    )
  }

  // ---------------------------------------------------------------------------
  // SIZING — scalar sp tokens for micro font sizes and letter-spacing units
  // that do not map to a Material3 typography style. Used inside .copy() on
  // existing Material3 styles (labelSmall, bodySmall) to override a single
  // field without re-declaring fontFamily/weight/lineHeight.
  // Source: tokens/typography.json composite-style overrides
  // ---------------------------------------------------------------------------

  object Sizing {
    val micro8 = 8.sp              // badge "Activo" label
    val micro9 = 9.sp              // overline mini-labels (plan/credits card titles)
    val micro10 = 10.sp            // USD suffix, refresh label, estimation title
    val micro11 = 11.sp            // onboarding footnote, topbar subtitle
    val micro15 = 15.sp            // onboarding footnote lineHeight
    val small13 = 13.sp            // provider-card button label
    val small14 = 14.sp            // manual-config button label
    val hero22 = 22.sp             // onboarding subtitle lineHeight
    val letterSpacingUnit = 1.sp   // overline letter-spacing
  }

  // ---------------------------------------------------------------------------
  // MOTION — duration scale + easing (from tokens/motion.json)
  // ---------------------------------------------------------------------------

  object Motion {
    // Durations (ms)
    const val instant = 0
    const val fast = 100
    const val base = 200
    const val moderate = 300
    const val slow = 500

    // Reduced-motion fallback
    const val reducedMotionDuration = 0
  }
}

// ---------------------------------------------------------------------------
// Provider color resolver — single dispatch point, token-backed.
// Dispatch literals "OpenAI"/"Anthropic"/"Ollama" are ViewModel routing keys,
// NOT user-facing display. Display names come from string resources.
// ---------------------------------------------------------------------------

object ProviderColor {
  /**
   * Resolves the brand color for a provider dispatch key.
   * Colors are NOT theme-swapped — brand identity is constant across light/dark.
   */
  fun resolve(provider: String): Color =
    when (provider) {
      "Anthropic" -> DesignTokens.Colors.Component.providerAnthropic
      "Ollama" -> DesignTokens.Colors.Component.providerOllama
      else -> DesignTokens.Colors.Component.providerOpenAi
    }
}