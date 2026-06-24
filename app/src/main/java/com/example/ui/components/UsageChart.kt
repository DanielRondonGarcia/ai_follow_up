package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.UsageLog
import com.example.ui.theme.DesignTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Usage history chart with Canvas-based line drawing.
 *
 * Token-driven: all colors, spacing, and radii reference [DesignTokens].
 * Axis labels use onSurface (>=4.5:1 contrast, replacing the previous
 * onSurfaceVariant which failed at 3.3:1). The chart carries a
 * contentDescription for screen readers describing the data point count.
 *
 * @param logs list of usage logs to plot (chronological order applied internally).
 * @param modifier optional layout modifier.
 */
@Composable
fun UsageChart(
  logs: List<UsageLog>,
  modifier: Modifier = Modifier,
) {
  var selectedTab by remember { mutableStateOf(0) } // 0 = Primary (Hours), 1 = Secondary (7 days)

  val historialLabel = stringResource(R.string.historial_de_uso)
  val cortoPlazoLabel = stringResource(R.string.corto_plazo)
  val sieteDiasLabel = stringResource(R.string.siete_dias)
  val emptyStateText = stringResource(R.string.aun_no_hay_datos)
  val cdChart = stringResource(R.string.cd_chart_usage, logs.size)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(DesignTokens.Radius.xxl),
      )
      .padding(DesignTokens.Spacing.lg)
      .semantics {
        this.contentDescription = cdChart
      },
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = historialLabel,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      // Segmented toggle button
      SingleChoiceSegmentedButtonRow {
        SegmentedButton(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) {
          Text(cortoPlazoLabel)
        }
        SegmentedButton(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) {
          Text(sieteDiasLabel)
        }
      }
    }

    Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

    if (logs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = emptyStateText,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
        )
      }
    } else {
      // Reverse chronological logs to display from oldest (left) to newest (right)
      val chronLogs = logs.sortedBy { it.timestamp }

      val points = chronLogs.map { log ->
        val percent = if (selectedTab == 0) log.primaryUsedPercent else log.secondaryUsedPercent
        percent.coerceIn(0.0, 100.0).toFloat()
      }

      val accentColor = if (selectedTab == 0) {
        MaterialTheme.colorScheme.primary
      } else {
        MaterialTheme.colorScheme.tertiary
      }

      // Grid uses onSurface at low alpha (decorative, not text; no contrast requirement)
      val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
      // Axis labels use onSurface (>=4.5:1), NOT onSurfaceVariant (was 3.3:1 FAIL)
      val textColor = MaterialTheme.colorScheme.onSurface

      Column {
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        ) {
          val width = size.width
          val height = size.height

          // Draw horizontal grid lines (0%, 25%, 50%, 75%, 100%)
          val stepY = height / 4f
          for (i in 0..4) {
            val y = i * stepY
            drawLine(
              color = gridColor,
              start = Offset(0f, y),
              end = Offset(width, y),
              strokeWidth = 1.dp.toPx(),
            )
          }

          if (points.size > 1) {
            val stepX = width / (points.size - 1)
            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { index, value ->
              val x = index * stepX
              val y = height - (value / 100f * height)

              if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
              } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
              }

              if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
              }
            }

            // Draw filled area with gradient
            drawPath(
              path = fillPath,
              brush = Brush.verticalGradient(
                colors = listOf(
                  accentColor.copy(alpha = 0.25f),
                  Color.Transparent,
                ),
              ),
            )

            // Draw path line
            drawPath(
              path = path,
              color = accentColor,
              style = Stroke(width = 3.dp.toPx()),
            )

            // Draw joint circles on each point
            points.forEachIndexed { index, value ->
              val x = index * stepX
              val y = height - (value / 100f * height)

              drawCircle(
                color = accentColor.copy(alpha = 0.3f),
                radius = 6.dp.toPx(),
                center = Offset(x, y),
              )
              drawCircle(
                color = accentColor,
                radius = 3.5.dp.toPx(),
                center = Offset(x, y),
              )
            }
          } else if (points.size == 1) {
            // Just one point - draw a horizontal line
            val y = height - (points[0] / 100f * height)
            drawLine(
              color = accentColor,
              start = Offset(0f, y),
              end = Offset(width, y),
              strokeWidth = 3.dp.toPx(),
            )
            drawCircle(
              color = accentColor,
              radius = 5.dp.toPx(),
              center = Offset(width / 2, y),
            )
          }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        // Timeline X-Axis labels (display up to 4 dates/times)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          val dateFormat = SimpleDateFormat("HH:mm\ndd/MM", Locale.getDefault())
          val sampleIndices = if (chronLogs.size <= 4) {
            chronLogs.indices.toList()
          } else {
            listOf(
              0,
              chronLogs.size / 3,
              (chronLogs.size * 2) / 3,
              chronLogs.size - 1,
            )
          }

          for (i in 0..3) {
            val log = chronLogs.getOrNull(sampleIndices.getOrNull(i) ?: -1)
            if (log != null) {
              Text(
                text = dateFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                textAlign = TextAlign.Center,
              )
            } else {
              Text(
                text = "",
                style = MaterialTheme.typography.labelMedium,
              )
            }
          }
        }
      }
    }
  }
}