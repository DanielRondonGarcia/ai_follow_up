package com.example.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.R
import com.example.ui.theme.DesignTokens
import com.example.ui.theme.ProviderColor

/**
 * Non-interactive badge that displays the provider brand color.
 *
 * Brand colors are component tokens (emerald-700/amber-700/gray-500), NOT
 * theme-swapped. White text on all three passes WCAG AA (>=4.5:1):
 *   OpenAI 5.48:1 | Anthropic 5.02:1 | Ollama 4.83:1
 *
 * @param provider dispatch literal ("OpenAI"/"Anthropic"/"Ollama"), NOT display text.
 * @param modifier optional layout modifier.
 */
@Composable
fun ProviderBadge(
  provider: String,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val providerName = when (provider) {
    "Anthropic" -> context.getString(R.string.provider_anthropic)
    "Ollama" -> context.getString(R.string.provider_ollama)
    else -> context.getString(R.string.provider_openai)
  }
  val cdProvider = stringResource(R.string.cd_provider, providerName)
  val badgeColor = ProviderColor.resolve(provider)

  Surface(
    color = badgeColor,
    shape = RoundedCornerShape(DesignTokens.Radius.sm),
    modifier = modifier.semantics {
      this.contentDescription = cdProvider
    },
  ) {
    Text(
      text = when (provider) {
        "Anthropic" -> "CLAUDE"
        "Ollama" -> "OLLAMA"
        else -> "GPT"
      },
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        color = Color.White,
      ),
      modifier = Modifier.padding(
        horizontal = DesignTokens.Spacing.md,
        vertical = DesignTokens.Spacing.xs,
      ),
    )
  }
}