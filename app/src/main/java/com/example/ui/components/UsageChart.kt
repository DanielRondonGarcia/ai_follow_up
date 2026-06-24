package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UsageLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UsageChart(
    logs: List<UsageLog>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Primary (Hours), 1 = Secondary (7 days)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Historial de Uso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Segmented toggle button
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Corto Plazo", fontSize = 11.sp)
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("7 Días", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay datos de historial.\nPresiona Sincronizar para guardar un registro.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            // Reverse chronological logs to display from oldest (left) to newest (right)
            val chronLogs = logs.sortedBy { it.timestamp }
            
            val points = chronLogs.map { log ->
                val percent = if (selectedTab == 0) log.primaryUsedPercent else log.secondaryUsedPercent
                // Clamp to 0-100%
                percent.coerceIn(0.0, 100.0).toFloat()
            }

            val accentColor = if (selectedTab == 0) {
                MaterialTheme.colorScheme.primary // OpenAI emerald
            } else {
                Color(0xFFF59E0B) // Warning amber/yellow
            }

            val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

            Column {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
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
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (points.size > 1) {
                        val stepX = width / (points.size - 1)
                        val path = Path()
                        val fillPath = Path()

                        points.forEachIndexed { index, value ->
                            // Map value (0-100) to height coordinates (invert because y=0 is top)
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
                                    Color.Transparent
                                )
                            )
                        )

                        // Draw path line
                        drawPath(
                            path = path,
                            color = accentColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw joint circles on each point
                        points.forEachIndexed { index, value ->
                            val x = index * stepX
                            val y = height - (value / 100f * height)
                            
                            // Outer glowing circle
                            drawCircle(
                                color = accentColor.copy(alpha = 0.3f),
                                radius = 6.dp.toPx(),
                                center = Offset(x, y)
                            )
                            // Inner solid circle
                            drawCircle(
                                color = accentColor,
                                radius = 3.5.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    } else if (points.size == 1) {
                        // Just one point - draw a horizontal line or indicator
                        val y = height - (points[0] / 100f * height)
                        drawLine(
                            color = accentColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 3.dp.toPx()
                        )
                        drawCircle(
                            color = accentColor,
                            radius = 5.dp.toPx(),
                            center = Offset(width / 2, y)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timeline X-Axis labels (display up to 4 dates/times)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dateFormat = SimpleDateFormat("HH:mm\ndd/MM", Locale.getDefault())
                    val sampleIndices = if (chronLogs.size <= 4) {
                        chronLogs.indices.toList()
                    } else {
                        listOf(
                            0,
                            chronLogs.size / 3,
                            (chronLogs.size * 2) / 3,
                            chronLogs.size - 1
                        )
                    }

                    for (i in 0..3) {
                        val log = chronLogs.getOrNull(sampleIndices.getOrNull(i) ?: -1)
                        if (log != null) {
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                color = textColor,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        } else {
                            Text(
                                text = "",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}
