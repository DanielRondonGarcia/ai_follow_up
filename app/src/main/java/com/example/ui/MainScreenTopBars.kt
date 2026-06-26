package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Account

/**
 * Top bars for MainScreen, extracted to keep MainScreen.kt under 200 lines.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailTopBar(
  activeAccount: Account?,
  onBack: () -> Unit,
  onShowProfileSelector: () -> Unit,
  backCd: String,
  accountsCd: String,
) {
  val monitorTitle = when (activeAccount?.provider) {
    "Anthropic" -> stringResource(R.string.monitor_claude)
    "Ollama" -> stringResource(R.string.monitor_ollama)
    else -> stringResource(R.string.monitor_codex)
  }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = backCd,
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Troubleshoot,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp),
        )
      }
      Column {
        Text(
          text = monitorTitle,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = activeAccount?.email ?: "",
          style = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
          ),
        )
      }
    }
    IconButton(
      onClick = onShowProfileSelector,
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(Color.Transparent),
    ) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = accountsCd,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(24.dp),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DashboardTopBar(
  agentsConnectedLabel: String,
  onShowProfileSelector: () -> Unit,
  manageAccountsCd: String,
) {
  TopAppBar(
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Troubleshoot,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
          )
        }
        Column {
          Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = agentsConnectedLabel,
            style = MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp,
            ),
          )
        }
      }
    },
    actions = {
      IconButton(onClick = onShowProfileSelector) {
        Icon(
          imageVector = Icons.Default.PersonAdd,
          contentDescription = manageAccountsCd,
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OnboardingTopBar(
  onShowProfileSelector: () -> Unit,
  addAccountCd: String,
) {
  TopAppBar(
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Troubleshoot,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
          )
        }
        Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
      }
    },
    actions = {
      IconButton(onClick = onShowProfileSelector) {
        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = addAccountCd)
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
  )
}