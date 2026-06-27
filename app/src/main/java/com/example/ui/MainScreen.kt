package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.navigation.NavRoute
import com.example.ui.navigation.initialRoute
import com.example.ui.navigation.routeForAccounts
import com.example.ui.screens.AccountDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OnboardingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier,
) {
  val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
  val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
  val latestLogByAccount by viewModel.latestLogByAccount.collectAsStateWithLifecycle()
  val logs by viewModel.usageLogs.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
  val expiredAccounts by viewModel.expiredAccounts.collectAsStateWithLifecycle()

  val showLoginWebView by viewModel.showLoginWebView.collectAsStateWithLifecycle()
  val loginProvider by viewModel.loginProvider.collectAsStateWithLifecycle()
  val showAddManualDialog by viewModel.showAddManualDialog.collectAsStateWithLifecycle()

  var showProfileSelector by remember { mutableStateOf(false) }
  var showClearHistoryConfirm by remember { mutableStateOf(false) }
  var navRoute by remember { mutableStateOf<NavRoute>(initialRoute(accounts)) }

  LaunchedEffect(accounts) { navRoute = routeForAccounts(accounts, navRoute) }

  val agentsLabel = stringResource(R.string.agentes_conectados_count, accounts.size)
  val backCd = stringResource(R.string.cd_volver_listado)
  val accountsCd = stringResource(R.string.cd_cuentas)
  val addAgentCd = stringResource(R.string.cd_agregar_agente)
  val manageCd = stringResource(R.string.gestionar_cuentas)
  val addAccountCd = stringResource(R.string.agregar_cuenta)

  Scaffold(
    topBar = {
      when (navRoute) {
        is NavRoute.AccountDetail -> DetailTopBar(activeAccount, { navRoute = NavRoute.Dashboard }, { showProfileSelector = true }, backCd, accountsCd)
        is NavRoute.Dashboard -> DashboardTopBar(agentsLabel, { showProfileSelector = true }, manageCd)
        is NavRoute.Onboarding -> OnboardingTopBar({ showProfileSelector = true }, addAccountCd)
      }
    },
    floatingActionButton = {
      if (navRoute is NavRoute.Dashboard) {
        FloatingActionButton(
          onClick = { showProfileSelector = true },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          shape = RoundedCornerShape(16.dp),
        ) { Icon(Icons.Default.Add, contentDescription = addAgentCd) }
      }
    },
  ) { paddingValues ->
    Box(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background),
    ) {
      AnimatedContent(
        targetState = navRoute,
        transitionSpec = {
          if (initialState is NavRoute.Onboarding || targetState is NavRoute.Onboarding) {
            fadeIn() togetherWith fadeOut()
          } else {
            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
          }
        },
        label = "navRoute",
      ) { route ->
        when (route) {
          is NavRoute.Onboarding -> OnboardingScreen(
            onAddAutoOpenAI = { viewModel.startLoginWebView("OpenAI") },
            onAddAutoAnthropic = { viewModel.startLoginWebView("Anthropic") },
            onAddAutoOllama = { viewModel.startLoginWebView("Ollama") },
            onAddManualClick = { viewModel.setShowAddManualDialog(true) },
          )
          is NavRoute.Dashboard -> DashboardScreen(
            accounts = accounts,
            latestLogByAccount = latestLogByAccount,
            isLoading = isLoading,
            errorMessage = errorMessage,
            expiredAccounts = expiredAccounts,
            onAccountClick = { id -> viewModel.switchAccount(id); navRoute = NavRoute.AccountDetail(id) },
            onReauth = { id ->
              val account = accounts.firstOrNull { it.id == id }
              if (account != null) viewModel.startReAuth(id, account.provider)
            },
            onSyncAccount = { id -> viewModel.syncAccount(id) },
            onSyncAll = { onComplete -> viewModel.syncAllAccounts(onComplete) },
            onClearError = { viewModel.clearError() },
          )
          is NavRoute.AccountDetail -> {
            val active = activeAccount
            AccountDetailScreen(
            activeAccount = activeAccount,
            logs = logs,
            isLoading = isLoading,
            errorMessage = errorMessage,
            isExpired = active != null && active.id in expiredAccounts,
            onSync = { viewModel.syncActiveAccount() },
            onReauth = {
              active?.let { viewModel.startReAuth(it.id, it.provider) }
            },
            onDeleteLogs = { showClearHistoryConfirm = true },
            onClearError = { viewModel.clearError() },
            onShowProfileSelector = { showProfileSelector = true },
          )
        }
        }
      }
    }
  }

  if (showProfileSelector) {
    ProfileSelectorSheet(
      accounts = accounts,
      activeAccount = activeAccount,
      onDismiss = { showProfileSelector = false },
      onSelect = { id -> viewModel.switchAccount(id); navRoute = NavRoute.AccountDetail(id); showProfileSelector = false },
      onDelete = { viewModel.deleteAccount(it) },
      onAddManualClick = { showProfileSelector = false; viewModel.setShowAddManualDialog(true) },
      onAddAutoClick = { provider -> showProfileSelector = false; viewModel.startLoginWebView(provider) },
    )
  }
  if (showAddManualDialog) {
    ManualDialogOverlay(
      onDismiss = { viewModel.setShowAddManualDialog(false) },
      onSave = { p, e, t, c, u, uid -> viewModel.handleManualAccountAdd(p, e, t, c, u, uid) },
    )
  }
  if (showLoginWebView) {
    WebViewLoginOverlay(
      provider = loginProvider,
      onDismiss = { viewModel.setShowLoginWebView(false) },
      onTokenCaptured = { p, t, c, u, uid -> viewModel.handleWebViewLoginSuccess(p, t, c, u, uid) },
    )
  }
  if (showClearHistoryConfirm) {
    ClearHistoryConfirmDialog(
      onConfirm = { viewModel.deleteLogs(); showClearHistoryConfirm = false },
      onDismiss = { showClearHistoryConfirm = false },
    )
  }
}