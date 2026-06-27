package com.example.ui.navigation

import com.example.data.Account

/**
 * Sealed navigation route for the AI Agent Monitor single-activity app.
 *
 * Three screens, linear flow, no deep links or back-stack:
 *  - [Onboarding] when there are no accounts yet.
 *  - [Dashboard] when accounts exist but none is selected for detail.
 *  - [AccountDetail] when the user taps a card (holds the account id).
 *
 * Hosted by MainScreen via `AnimatedContent(targetState = navRoute)`.
 * The state machine that derives the route lives in [initialRoute] and
 * [routeForAccounts]; both are pure functions so they can be unit-tested
 * without Compose.
 */
sealed class NavRoute {

  /** Empty state: no accounts configured yet. */
  object Onboarding : NavRoute()

  /** Parent dashboard: list of agent cards. */
  object Dashboard : NavRoute()

  /** Detail view for a single account. */
  data class AccountDetail(val accountId: Int) : NavRoute()
}

/**
 * Returns the initial route given the current account list.
 * Empty list -> Onboarding; otherwise Dashboard.
 */
fun initialRoute(accounts: List<Account>): NavRoute =
  if (accounts.isEmpty()) NavRoute.Onboarding else NavRoute.Dashboard

/**
 * Returns the route that should be active given the current account list
 * and the current route. Used to react to account-list mutations (e.g.
 * the last account being deleted from the detail screen).
 *
 * Rules:
 *  - Empty accounts always collapse to [NavRoute.Onboarding].
 *  - Non-empty accounts collapse [NavRoute.AccountDetail] whose id no
 *    longer exists back to [NavRoute.Dashboard].
 *  - [NavRoute.Onboarding] becomes [NavRoute.Dashboard] once accounts appear.
 */
fun routeForAccounts(
  accounts: List<Account>,
  current: NavRoute,
): NavRoute = when {
  accounts.isEmpty() -> NavRoute.Onboarding
  current is NavRoute.Onboarding -> NavRoute.Dashboard
  current is NavRoute.AccountDetail && accounts.none { it.id == current.accountId } ->
    NavRoute.Dashboard
  else -> current
}

/**
 * Returns the route after the system Back action.
 *
 * The app owns navigation with [NavRoute] instead of a Navigation Component
 * back stack, so Android Back needs to be mapped explicitly for detail routes.
 */
fun routeAfterSystemBack(current: NavRoute): NavRoute = when (current) {
  is NavRoute.AccountDetail -> NavRoute.Dashboard
  else -> current
}
