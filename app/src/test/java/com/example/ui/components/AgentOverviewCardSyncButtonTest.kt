package com.example.ui.components

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.Account
import com.example.data.UsageLog
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for the per-card sync IconButton introduced in PR 2
 * (Tasks 2.10).
 *
 * Covers:
 * - The sync button is shown on a non-expired card.
 * - The sync button is absent on an expired card.
 * - Tapping the sync button invokes [AgentOverviewCard]'s onSyncAccount
 *   callback with the account id.
 *
 * The sync button is identified by its content description
 * "Sincronizar esta cuenta" (string resource cd_sync_account).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AgentOverviewCardSyncButtonTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private val healthyAccount = Account(
    id = 11,
    provider = "OpenAI",
    email = "healthy@sync.test",
    userId = "user-11",
    planType = "plus",
    authToken = "",
    cookies = "",
    userAgent = "",
  )

  private val expiredAccount = Account(
    id = 22,
    provider = "Ollama",
    email = "expired@sync.test",
    userId = "expired@sync.test",
    planType = "Free",
    authToken = "",
    cookies = "session=old",
    userAgent = "ua",
  )

  private val log = UsageLog(
    accountId = 11,
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

  @Test
  fun nonExpiredCard_showsSyncButton() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          AgentOverviewCard(
            account = healthyAccount,
            latestLog = log,
            isExpired = false,
            onClick = {},
            onReauth = {},
            onSyncAccount = {},
          )
        }
      }
    }

    // The sync button content description should be present.
    composeTestRule
      .onNodeWithContentDescription("Sincronizar esta cuenta")
      .assertIsDisplayed()
  }

  @Test
  fun expiredCard_hidesSyncButton() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          AgentOverviewCard(
            account = expiredAccount,
            latestLog = null,
            isExpired = true,
            onClick = {},
            onReauth = {},
            onSyncAccount = {},
          )
        }
      }
    }

    // The sync button must NOT appear on an expired card.
    composeTestRule
      .onNodeWithContentDescription("Sincronizar esta cuenta")
      .assertDoesNotExist()
    // Sanity: the expired card renders its re-auth CTA instead.
    composeTestRule.onNodeWithText("Volver a autenticar").assertExists()
  }

  @Test
  fun tappingSyncButton_invokesCallbackWithAccountId() {
    var syncId: Int? = null
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          AgentOverviewCard(
            account = healthyAccount,
            latestLog = log,
            isExpired = false,
            onClick = {},
            onReauth = {},
            onSyncAccount = { id -> syncId = id },
          )
        }
      }
    }

    composeTestRule
      .onNodeWithContentDescription("Sincronizar esta cuenta")
      .performClick()

    assertEquals(11, syncId)
  }

  @Test
  fun nonExpiredCard_doesNotShowReauthCta() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Surface {
          AgentOverviewCard(
            account = healthyAccount,
            latestLog = null,
            isExpired = false,
            onClick = {},
            onReauth = {},
            onSyncAccount = {},
          )
        }
      }
    }

    // The re-auth CTA belongs to the expired state only.
    composeTestRule.onNodeWithText("Volver a autenticar").assertDoesNotExist()
  }
}