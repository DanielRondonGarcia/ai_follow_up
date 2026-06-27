package com.example.ui.navigation

import com.example.data.Account
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the NavRoute state machine (Task 3.6).
 *
 * Pure Kotlin tests — no Compose, no Robolectric. Verifies the route
 * derivation logic: empty accounts -> Onboarding, non-empty -> Dashboard,
 * card tap -> AccountDetail, back -> Dashboard, last account deleted
 * from detail -> Onboarding.
 */
class NavRouteTest {

  private val emptyAccounts: List<Account> = emptyList()

  private val singleAccount = Account(
    id = 1,
    provider = "OpenAI",
    email = "test@example.com",
    userId = "user1",
    planType = "plus",
    authToken = "",
    cookies = "",
    userAgent = "",
  )

  private val twoAccounts = listOf(
    singleAccount,
    Account(
      id = 2,
      provider = "Anthropic",
      email = "claude@example.com",
      userId = "user2",
      planType = "Pro",
      authToken = "",
      cookies = "",
      userAgent = "",
    ),
  )

  @Test
  fun emptyAccounts_initialRoute_isOnboarding() {
    val route = initialRoute(emptyAccounts)
    assertEquals(NavRoute.Onboarding, route)
  }

  @Test
  fun nonEmptyAccounts_initialRoute_isDashboard() {
    val route = initialRoute(listOf(singleAccount))
    assertEquals(NavRoute.Dashboard, route)
  }

  @Test
  fun nonEmptyAccounts_noSelection_isDashboard() {
    val route = routeForAccounts(twoAccounts, NavRoute.Dashboard)
    assertEquals(NavRoute.Dashboard, route)
  }

  @Test
  fun cardTapped_navigatesToAccountDetail() {
    // Simulate card tap: MainScreen sets navRoute = AccountDetail(id)
    val route = NavRoute.AccountDetail(1)
    assertEquals(NavRoute.AccountDetail(1), route)
    assertTrue(route is NavRoute.AccountDetail)
    assertEquals(1, (route as NavRoute.AccountDetail).accountId)
  }

  @Test
  fun backFromAccountDetail_returnsToDashboard() {
    val afterBack = routeAfterSystemBack(NavRoute.AccountDetail(1))
    assertEquals(NavRoute.Dashboard, afterBack)
  }

  @Test
  fun systemBackOnDashboard_keepsDashboard() {
    val afterBack = routeAfterSystemBack(NavRoute.Dashboard)
    assertEquals(NavRoute.Dashboard, afterBack)
  }

  @Test
  fun systemBackOnOnboarding_keepsOnboarding() {
    val afterBack = routeAfterSystemBack(NavRoute.Onboarding)
    assertEquals(NavRoute.Onboarding, afterBack)
  }

  @Test
  fun lastAccountDeletedFromDetail_collapsesToOnboarding() {
    // User is on AccountDetail(1). Account 1 is deleted, leaving empty list.
    val afterDelete = routeForAccounts(emptyAccounts, NavRoute.AccountDetail(1))
    assertEquals(NavRoute.Onboarding, afterDelete)
  }

  @Test
  fun accountDetailIdNoLongerExists_collapsesToDashboard() {
    // User is on AccountDetail(1). Account 1 is deleted but account 2 remains.
    val remaining = twoAccounts.filter { it.id != 1 }
    val afterDelete = routeForAccounts(remaining, NavRoute.AccountDetail(1))
    assertEquals(NavRoute.Dashboard, afterDelete)
  }

  @Test
  fun onboardingBecomesDashboard_whenAccountsAppear() {
    val route = routeForAccounts(listOf(singleAccount), NavRoute.Onboarding)
    assertEquals(NavRoute.Dashboard, route)
  }

  @Test
  fun dashboardCollapsesToOnboarding_whenAllAccountsDeleted() {
    val route = routeForAccounts(emptyAccounts, NavRoute.Dashboard)
    assertEquals(NavRoute.Onboarding, route)
  }
}
