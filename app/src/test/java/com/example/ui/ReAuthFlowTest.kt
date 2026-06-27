package com.example.ui

import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.Account
import com.example.data.UsageLog
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose UI tests for the expired-session state and re-auth CTA flow.
 *
 * Covers the spec scenarios:
 * - An account in the expired set renders the "Sesion expirada" state and a
 *   "Volver a autenticar" button on the dashboard card.
 * - Tapping the re-auth CTA invokes the onReauth callback with the account id.
 * - A non-expired account renders normally (no expired badge, no CTA).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReAuthFlowTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private val expiredAccount = Account(
    id = 7,
    provider = "Ollama",
    email = "expired@test.com",
    userId = "expired@test.com",
    planType = "Pro",
    authToken = "",
    cookies = "session=old",
    userAgent = "ua",
  )

  private val healthyAccount = Account(
    id = 9,
    provider = "OpenAI",
    email = "healthy@test.com",
    userId = "user-9",
    planType = "plus",
    authToken = "",
    cookies = "",
    userAgent = "",
  )

  @Test
  fun expiredAccount_showsExpiredBadgeAndReauthCta() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          DashboardScreen(
            accounts = listOf(expiredAccount),
            allLogs = emptyList(),
            isLoading = false,
            errorMessage = null,
            expiredAccounts = setOf(7),
            onAccountClick = {},
            onReauth = {},
            onSyncAll = {},
            onClearError = {},
          )
        }
      }
    }

    composeTestRule.onNodeWithText("Sesion expirada").assertExists()
    composeTestRule.onNodeWithText("Volver a autenticar").assertExists()
  }

  @Test
  fun healthyAccount_doesNotShowExpiredBadge() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          DashboardScreen(
            accounts = listOf(healthyAccount),
            allLogs = emptyList(),
            isLoading = false,
            errorMessage = null,
            expiredAccounts = emptySet(),
            onAccountClick = {},
            onReauth = {},
            onSyncAll = {},
            onClearError = {},
          )
        }
      }
    }

    // The expired label must NOT appear for a healthy account.
    composeTestRule.onNodeWithText("Sesion expirada").assertDoesNotExist()
    composeTestRule.onNodeWithText("Volver a autenticar").assertDoesNotExist()
  }

  @Test
  fun tappingReauthCta_invokesCallbackWithAccountId() {
    var reauthId: Int? = null
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          DashboardScreen(
            accounts = listOf(expiredAccount),
            allLogs = emptyList(),
            isLoading = false,
            errorMessage = null,
            expiredAccounts = setOf(7),
            onAccountClick = {},
            onReauth = { id -> reauthId = id },
            onSyncAll = {},
            onClearError = {},
          )
        }
      }
    }

    composeTestRule.onNodeWithText("Volver a autenticar").performClick()
    org.junit.Assert.assertEquals(7, reauthId)
  }

  @Test
  fun mixedList_expiredAndHealthy_onlyExpiredShowsBadge() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          DashboardScreen(
            accounts = listOf(expiredAccount, healthyAccount),
            allLogs = emptyList(),
            isLoading = false,
            errorMessage = null,
            expiredAccounts = setOf(7),
            onAccountClick = {},
            onReauth = {},
            onSyncAll = {},
            onClearError = {},
          )
        }
      }
    }

    // The expired badge appears (for id=7) and the healthy email is visible.
    composeTestRule.onNodeWithText("Sesion expirada").assertExists()
    composeTestRule.onNodeWithText("healthy@test.com").assertExists()
  }
}