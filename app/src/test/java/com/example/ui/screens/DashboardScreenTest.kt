package com.example.ui.screens

import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.data.Account
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Integration test for screen composition (Task 3.7).
 *
 * Verifies that when DashboardScreen is composed with a fake accounts list,
 * the dashboard content (section header, agent email) appears without the
 * removed welcome/control card.
 * Uses createComposeRule with the app theme and Robolectric for resource
 * resolution.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DashboardScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun dashboardScreen_withAccounts_composesAgentList() {
    val accounts = listOf(
      Account(
        id = 1,
        provider = "OpenAI",
        email = "agent@example.com",
        userId = "user1",
        planType = "plus",
        authToken = "",
        cookies = "",
        userAgent = "",
      ),
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          DashboardScreen(
            accounts = accounts,
            latestLogByAccount = emptyMap(),
            isLoading = false,
            errorMessage = null,
            expiredAccounts = emptySet(),
            onAccountClick = {},
            onReauth = {},
            onSyncAccount = {},
            onSyncAll = {},
            onClearError = {},
          )
        }
      }
    }

    // The section header "Tus agentes conectados" should appear.
    composeTestRule.onNodeWithText("Tus agentes conectados").assertExists()
    // The old control panel card should not take vertical space anymore.
    composeTestRule.onNodeWithText("Panel de control").assertDoesNotExist()
    // The agent email should appear in the AgentOverviewCard.
    composeTestRule.onNodeWithText("agent@example.com").assertExists()
  }

  @Test
  fun dashboardScreen_withError_displaysErrorBanner() {
    val accounts = listOf(
      Account(
        id = 1,
        provider = "OpenAI",
        email = "agent@example.com",
        userId = "user1",
        planType = "plus",
        authToken = "",
        cookies = "",
        userAgent = "",
      ),
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          DashboardScreen(
            accounts = accounts,
            latestLogByAccount = emptyMap(),
            isLoading = false,
            errorMessage = "Error de conexion",
            expiredAccounts = emptySet(),
            onAccountClick = {},
            onReauth = {},
            onSyncAccount = {},
            onSyncAll = {},
            onClearError = {},
          )
        }
      }
    }

    composeTestRule.onNodeWithText("Error de conexion").assertExists()
  }

  @Test
  fun dashboardScreen_emptyAccounts_stillComposes() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          DashboardScreen(
            accounts = emptyList(),
            latestLogByAccount = emptyMap(),
            isLoading = false,
            errorMessage = null,
            expiredAccounts = emptySet(),
            onAccountClick = {},
            onReauth = {},
            onSyncAccount = {},
            onSyncAll = {},
            onClearError = {},
          )
        }
      }
    }

    // Section header should still appear even with zero accounts.
    composeTestRule.onNodeWithText("Tus agentes conectados").assertExists()
  }
}
