package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ProviderBadge
import com.example.ui.theme.DesignTokens
import com.example.ui.theme.ProviderColor

/**
 * Onboarding / empty-state screen.
 *
 * Shown when the account list is empty. Offers three provider auto-link
 * cards plus a manual-configuration fallback. Provider card background and
 * border colors use [DesignTokens.Colors.Primitives] light/dark variants
 * (same palette the pre-redesign OnboardingState used, now centralized).
 *
 * @param onAddAutoOpenAI callback to start OpenAI web login.
 * @param onAddAutoAnthropic callback to start Anthropic web login.
 * @param onAddAutoOllama callback to start Ollama web login.
 * @param onAddManualClick callback to open the manual-add dialog.
 * @param modifier optional layout modifier.
 */
@Composable
fun OnboardingScreen(
  onAddAutoOpenAI: () -> Unit,
  onAddAutoAnthropic: () -> Unit,
  onAddAutoOllama: () -> Unit,
  onAddManualClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isDark = androidx.compose.foundation.isSystemInDarkTheme()
  val context = LocalContext.current

  val gptCardBg = if (isDark) DesignTokens.Colors.Primitives.gray950 else DesignTokens.Colors.Primitives.green50
  val gptCardBorder = if (isDark) DesignTokens.Colors.Primitives.gray800 else DesignTokens.Colors.Primitives.green200
  val gptTitleColor = if (isDark) DesignTokens.Colors.Primitives.green400 else DesignTokens.Colors.Primitives.green800

  val cldCardBg = if (isDark) DesignTokens.Colors.Primitives.gray900 else DesignTokens.Colors.Primitives.amber100
  val cldCardBorder = if (isDark) DesignTokens.Colors.Primitives.gray700 else DesignTokens.Colors.Primitives.amber200
  val cldTitleColor = if (isDark) DesignTokens.Colors.Primitives.amber400 else DesignTokens.Colors.Primitives.amber800

  val ollCardBg = if (isDark) DesignTokens.Colors.Primitives.gray900 else DesignTokens.Colors.Primitives.gray100
  val ollCardBorder = if (isDark) DesignTokens.Colors.Primitives.gray700 else DesignTokens.Colors.Primitives.gray300
  val ollTitleColor = if (isDark) DesignTokens.Colors.Primitives.gray200 else DesignTokens.Colors.Primitives.gray800

  val vincularLabel = stringResource(R.string.vincular_auto)
  val manualLabel = stringResource(R.string.configuracion_manual_avanzado)
  val cdOpenAI = stringResource(R.string.cd_vincular_openai)
  val cdAnthropic = stringResource(R.string.cd_vincular_anthropic)
  val cdOllama = stringResource(R.string.cd_vincular_ollama)
  val cdManual = stringResource(R.string.cd_configuracion_manual)

  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(DesignTokens.Spacing.xl),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg),
      modifier = Modifier.widthIn(max = 450.dp),
    ) {
      // Hero visual icon
      Box(
        modifier = Modifier
          .size(100.dp)
          .background(
            brush = Brush.radialGradient(
              colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                Color.Transparent,
              ),
            ),
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Speed,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(60.dp),
        )
      }

      Text(
        text = stringResource(R.string.seguimiento_de_agentes),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
      )

      Text(
        text = stringResource(R.string.onboarding_subtitulo),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp,
      )

      Text(
        text = stringResource(R.string.selecciona_proveedor),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = DesignTokens.Spacing.xs),
      )

      // Provider cards
      ProviderOnboardingCard(
        provider = "OpenAI",
        title = stringResource(R.string.openai_chatgpt),
        description = stringResource(R.string.openai_chatgpt_desc),
        cardBg = gptCardBg,
        cardBorder = gptCardBorder,
        titleColor = gptTitleColor,
        buttonColor = ProviderColor.resolve("OpenAI"),
        buttonLabel = vincularLabel,
        contentDescription = cdOpenAI,
        onClick = onAddAutoOpenAI,
      )

      ProviderOnboardingCard(
        provider = "Anthropic",
        title = stringResource(R.string.anthropic_claude),
        description = stringResource(R.string.anthropic_claude_desc),
        cardBg = cldCardBg,
        cardBorder = cldCardBorder,
        titleColor = cldTitleColor,
        buttonColor = ProviderColor.resolve("Anthropic"),
        buttonLabel = vincularLabel,
        contentDescription = cdAnthropic,
        onClick = onAddAutoAnthropic,
      )

      ProviderOnboardingCard(
        provider = "Ollama",
        title = stringResource(R.string.ollama_cloud),
        description = stringResource(R.string.ollama_cloud_desc),
        cardBg = ollCardBg,
        cardBorder = ollCardBorder,
        titleColor = ollTitleColor,
        buttonColor = ProviderColor.resolve("Ollama"),
        buttonLabel = vincularLabel,
        contentDescription = cdOllama,
        onClick = onAddAutoOllama,
      )

      Spacer(modifier = Modifier.height(DesignTokens.Spacing.none))

      OutlinedButton(
        onClick = onAddManualClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .semantics { this.contentDescription = cdManual },
        shape = RoundedCornerShape(DesignTokens.Radius.md),
      ) {
        Icon(imageVector = Icons.Default.Settings, contentDescription = null)
        Spacer(modifier = Modifier.padding(horizontal = DesignTokens.Spacing.xs))
        Text(manualLabel, fontSize = 14.sp)
      }

      Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

      // Informative footnote
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
            fontSize = 11.sp,
            lineHeight = 15.sp,
          )
        }
      }
    }
  }
}

/**
 * Single provider card on the onboarding screen.
 */
@Composable
private fun ProviderOnboardingCard(
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
        Text(buttonLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}