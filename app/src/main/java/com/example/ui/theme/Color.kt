package com.example.ui.theme

/**
 * Color token bridge — thin aliases to [DesignTokens].
 *
 * The source of truth for all color values lives in [DesignTokens.Colors].
 * These top-level vals remain for backward compatibility with files that
 * have not yet been rewired to consume [DesignTokens] directly (MainScreen.kt,
 * ProfileSelector.kt). PR 3 will update those imports; this file shrinks as
 * each consumer migrates.
 *
 * No hardcoded hex literals remain here — every value delegates to
 * [DesignTokens.Colors.Component] or [DesignTokens.Colors.Primitives].
 */

// Provider brand colors — delegate to DesignTokens.Colors.Component.
// Brand identity tokens, NOT theme-swapped. White text passes WCAG AA:
//   OpenAI emerald-700  -> 5.48:1
//   Anthropic amber-700 -> 5.02:1
//   Ollama gray-500     -> 4.83:1
val ProviderOpenAi = DesignTokens.Colors.Component.providerOpenAi
val ProviderAnthropic = DesignTokens.Colors.Component.providerAnthropic
val ProviderOllama = DesignTokens.Colors.Component.providerOllama