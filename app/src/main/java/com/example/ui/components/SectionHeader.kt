package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.example.ui.theme.DesignTokens

/**
 * Section header with heading semantics for screen readers.
 *
 * @param title the section title text.
 * @param action optional trailing action composable (e.g. a TextButton).
 * @param modifier optional layout modifier.
 */
@Composable
fun SectionHeader(
  title: String,
  action: (@Composable () -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .semantics { heading() },
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier.semantics { heading() },
    )
    if (action != null) {
      action()
    }
  }
}