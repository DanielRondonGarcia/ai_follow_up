package com.example.ui.screens

import androidx.compose.material3.Surface
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
 * Integration test for screen composition (Task 3.7).
 *
 * Verifies that when DashboardScreen is composed with a fake accounts list,
 * the dashboard content (welcome card, section header, agent email) appears.
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
    val logs: List<UsageLog> = emptyList()

    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          DashboardScreen(
            accounts = accounts,
            allLogs = logs,
            isLoading = false,
            errorMessage = null,
            onAccountClick = {},
            onSyncAll = {},
            onClearError = {},
          )
        }
      }
    }

    // The section header "Tus agentes conectados" should appear.
    composeTestRule.onNodeWithText("Tus agentes conectados").assertExists()
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
            allLogs = emptyList(),
            isLoading = false,
            errorMessage = "Error de conexion",
            onAccountClick = {},
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
            allLogs = emptyList(),
            isLoading = false,
            errorMessage = null,
            onAccountClick = {},
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