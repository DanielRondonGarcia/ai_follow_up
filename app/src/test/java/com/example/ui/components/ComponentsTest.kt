package com.example.ui.components

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.example.data.Account
import com.example.data.UsageLog
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for PR 2 components (Tasks 2.1-2.7).
 *
 * Each test uses createComposeRule with the app theme to verify the
 * component renders without crashing and displays expected text.
 * Robolectric runner allows stringResource resolution in unit tests.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ComponentsTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun providerBadge_openAI_displaysGptLabel() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          ProviderBadge(provider = "OpenAI")
        }
      }
    }
    composeTestRule.onNodeWithText("GPT").assertExists()
  }

  @Test
  fun providerBadge_anthropic_displaysClaudeLabel() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          ProviderBadge(provider = "Anthropic")
        }
      }
    }
    composeTestRule.onNodeWithText("CLAUDE").assertExists()
  }

  @Test
  fun providerBadge_ollama_displaysOllamaLabel() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          ProviderBadge(provider = "Ollama")
        }
      }
    }
    composeTestRule.onNodeWithText("OLLAMA").assertExists()
  }

  @Test
  fun errorBanner_displaysMessage() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          ErrorBanner(
            message = "Error de conexion",
            onDismiss = {},
          )
        }
      }
    }
    composeTestRule.onNodeWithText("Error de conexion").assertExists()
  }

  @Test
  fun sectionHeader_displaysTitle() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          SectionHeader(title = "Agentes Conectados")
        }
      }
    }
    composeTestRule.onNodeWithText("Agentes Conectados").assertExists()
  }

  @Test
  fun sectionHeader_withAction_displaysTitleAndAction() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          SectionHeader(
            title = "Seccion",
            action = { Text("Accion") },
          )
        }
      }
    }
    composeTestRule.onNodeWithText("Seccion").assertExists()
    composeTestRule.onNodeWithText("Accion").assertExists()
  }

  @Test
  fun agentOverviewCard_withLog_displaysEmail() {
    val account = Account(
      id = 1,
      provider = "OpenAI",
      email = "test@example.com",
      userId = "user1",
      planType = "plus",
      authToken = "",
      cookies = "",
      userAgent = "",
    )
    val log = UsageLog(
      accountId = 1,
      planType = "PLUS",
      primaryUsedPercent = 45.0,
      primaryResetAt = 0L,
      primaryWindowSeconds = 0L,
      primaryResetAfterSeconds = 3600L,
      secondaryUsedPercent = 30.0,
      secondaryResetAt = 0L,
      secondaryWindowSeconds = 0L,
      secondaryResetAfterSeconds = 86400L,
      hasCredits = true,
      unlimited = false,
      balance = "10.00",
      approxLocalUsed = 100,
      approxLocalLimit = 200,
      approxCloudUsed = 50,
      approxCloudLimit = 100,
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          AgentOverviewCard(
            account = account,
            latestLog = log,
            isExpired = false,
            onClick = {},
            onReauth = {},
          )
        }
      }
    }
    composeTestRule.onNodeWithText("test@example.com").assertExists()
  }

  @Test
  fun agentOverviewCard_nullLog_displaysSyncPending() {
    val account = Account(
      id = 1,
      provider = "OpenAI",
      email = "test@example.com",
      userId = "user1",
      planType = "plus",
      authToken = "",
      cookies = "",
      userAgent = "",
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          AgentOverviewCard(
            account = account,
            latestLog = null,
            isExpired = false,
            onClick = {},
            onReauth = {},
          )
        }
      }
    }
    composeTestRule.onNodeWithText("Sincronización pendiente").assertExists()
  }

  @Test
  fun rateLimitCard_displaysLabels() {
    val log = UsageLog(
      accountId = 1,
      planType = "PLUS",
      primaryUsedPercent = 50.0,
      primaryResetAt = 0L,
      primaryWindowSeconds = 0L,
      primaryResetAfterSeconds = 3600L,
      secondaryUsedPercent = 75.0,
      secondaryResetAt = 0L,
      secondaryWindowSeconds = 0L,
      secondaryResetAfterSeconds = 86400L,
      hasCredits = true,
      unlimited = false,
      balance = "10.00",
      approxLocalUsed = 100,
      approxLocalLimit = 200,
      approxCloudUsed = 50,
      approxCloudLimit = 100,
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          RateLimitCard(log = log)
        }
      }
    }
    composeTestRule.onNodeWithText("Ventana primaria").assertExists()
    composeTestRule.onNodeWithText("Ventana semanal").assertExists()
  }

  @Test
  fun creditsCard_displaysBalance() {
    val log = UsageLog(
      accountId = 1,
      planType = "PLUS",
      primaryUsedPercent = 0.0,
      primaryResetAt = 0L,
      primaryWindowSeconds = 0L,
      primaryResetAfterSeconds = 0L,
      secondaryUsedPercent = 0.0,
      secondaryResetAt = 0L,
      secondaryWindowSeconds = 0L,
      secondaryResetAfterSeconds = 0L,
      hasCredits = true,
      unlimited = false,
      balance = "10.00",
      approxLocalUsed = 0,
      approxLocalLimit = 0,
      approxCloudUsed = 0,
      approxCloudLimit = 0,
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          CreditsCard(log = log)
        }
      }
    }
    composeTestRule.onNodeWithText("10.00").assertExists()
  }

  @Test
  fun creditsCard_unlimited_displaysInfinity() {
    val log = UsageLog(
      accountId = 1,
      planType = "PLUS",
      primaryUsedPercent = 0.0,
      primaryResetAt = 0L,
      primaryWindowSeconds = 0L,
      primaryResetAfterSeconds = 0L,
      secondaryUsedPercent = 0.0,
      secondaryResetAt = 0L,
      secondaryWindowSeconds = 0L,
      secondaryResetAfterSeconds = 0L,
      hasCredits = false,
      unlimited = true,
      balance = "0",
      approxLocalUsed = 0,
      approxLocalLimit = 0,
      approxCloudUsed = 0,
      approxCloudLimit = 0,
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          CreditsCard(log = log)
        }
      }
    }
    // The infinity symbol should be present
    composeTestRule.onNodeWithText("\u221E").assertExists()
  }

  @Test
  fun usageChart_emptyLogs_displaysEmptyState() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          UsageChart(logs = emptyList())
        }
      }
    }
    composeTestRule.onNodeWithText("Historial de uso").assertExists()
  }

  @Test
  fun usageChart_withLogs_displaysTitle() {
    val log = UsageLog(
      accountId = 1,
      planType = "PLUS",
      primaryUsedPercent = 50.0,
      primaryResetAt = System.currentTimeMillis() / 1000,
      primaryWindowSeconds = 18000L,
      primaryResetAfterSeconds = 3600L,
      secondaryUsedPercent = 30.0,
      secondaryResetAt = System.currentTimeMillis() / 1000,
      secondaryWindowSeconds = 604800L,
      secondaryResetAfterSeconds = 86400L,
      hasCredits = true,
      unlimited = false,
      balance = "10.00",
      approxLocalUsed = 100,
      approxLocalLimit = 200,
      approxCloudUsed = 50,
      approxCloudLimit = 100,
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          UsageChart(logs = listOf(log))
        }
      }
    }
    composeTestRule.onNodeWithText("Historial de uso").assertExists()
  }
}