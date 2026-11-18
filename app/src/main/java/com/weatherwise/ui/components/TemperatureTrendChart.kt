package com.weatherwise.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.weatherwise.domain.model.DailyForecast
import com.weatherwise.utils.TemperatureUnit
import com.weatherwise.utils.UnitConverter
import kotlin.math.roundToInt

@Composable
fun TemperatureTrendChart(
    dailyList: List<DailyForecast>,
    temperatureUnit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    if (dailyList.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Temperature Trend (High & Low)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            val maxTemps = dailyList.map { UnitConverter.getTemperatureValue(it.maxTempCelsius, temperatureUnit) }
            val minTemps = dailyList.map { UnitConverter.getTemperatureValue(it.minTempCelsius, temperatureUnit) }

            val overallMax = (maxTemps.maxOrNull() ?: 30.0) + 4.0
            val overallMin = (minTemps.minOrNull() ?: 10.0) - 4.0
            val range = (overallMax - overallMin).coerceAtLeast(1.0)

            val primaryColor = MaterialTheme.colorScheme.primary
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            val surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                val itemWidth = 80.dp
                val totalWidth = itemWidth * dailyList.size.coerceAtLeast(5)
                val chartHeight = 220.dp

                Canvas(
                    modifier = Modifier
                        .width(totalWidth)
                        .height(chartHeight)
                ) {
                    val w = size.width
                    val h = size.height
                    val paddingBottom = 40f
                    val paddingTop = 40f
                    val usableHeight = h - paddingTop - paddingBottom
                    val stepX = w / dailyList.size

                    val maxPoints = maxTemps.mapIndexed { index, temp ->
                        val x = (index * stepX) + (stepX / 2f)
                        val y = paddingTop + ((overallMax - temp) / range * usableHeight).toFloat()
                        Offset(x, y)
                    }

                    val minPoints = minTemps.mapIndexed { index, temp ->
                        val x = (index * stepX) + (stepX / 2f)
                        val y = paddingTop + ((overallMax - temp) / range * usableHeight).toFloat()
                        Offset(x, y)
                    }

                    // Draw Gradient Fill under Max curve
                    if (maxPoints.size > 1) {
                        val fillPath = Path().apply {
                            moveTo(maxPoints.first().x, maxPoints.first().y)
                            for (i in 0 until maxPoints.size - 1) {
                                val p0 = maxPoints[i]
                                val p1 = maxPoints[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                            lineTo(maxPoints.last().x, h - paddingBottom)
                            lineTo(maxPoints.first().x, h - paddingBottom)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF9800).copy(alpha = 0.35f),
                                    Color(0xFFFF9800).copy(alpha = 0.05f)
                                )
                            )
                        )
                    }

                    // Draw Max Temperature Curve (Orange / High)
                    if (maxPoints.size > 1) {
                        val maxPath = Path().apply {
                            moveTo(maxPoints.first().x, maxPoints.first().y)
                            for (i in 0 until maxPoints.size - 1) {
                                val p0 = maxPoints[i]
                                val p1 = maxPoints[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }
                        drawPath(
                            path = maxPath,
                            color = Color(0xFFFF9800),
                            style = Stroke(width = 5f)
                        )
                    }

                    // Draw Min Temperature Curve (Blue / Low)
                    if (minPoints.size > 1) {
                        val minPath = Path().apply {
                            moveTo(minPoints.first().x, minPoints.first().y)
                            for (i in 0 until minPoints.size - 1) {
                                val p0 = minPoints[i]
                                val p1 = minPoints[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }
                        drawPath(
                            path = minPath,
                            color = Color(0xFF29B6F6),
                            style = Stroke(width = 4f)
                        )
                    }

                    // Draw Dots and Labels
                    dailyList.forEachIndexed { index, forecast ->
                        val maxPt = maxPoints[index]
                        val minPt = minPoints[index]

                        // Max dot
                        drawCircle(
                            color = Color.White,
                            radius = 6f,
                            center = maxPt
                        )
                        drawCircle(
                            color = Color(0xFFFF9800),
                            radius = 4f,
                            center = maxPt
                        )

                        // Min dot
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = minPt
                        )
                        drawCircle(
                            color = Color(0xFF29B6F6),
                            radius = 3.5f,
                            center = minPt
                        )

                        // Draw native text for Max Temp
                        val maxTempStr = "${maxTemps[index].roundToInt()}°"
                        val minTempStr = "${minTemps[index].roundToInt()}°"
                        val dayLabel = forecast.dayLabel.take(3)

                        val paint = android.graphics.Paint().apply {
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 28f
                            isAntiAlias = true
                        }

                        // Max label above dot
                        paint.color = android.graphics.Color.argb(255, 255, 152, 0)
                        paint.isFakeBoldText = true
                        drawContext.canvas.nativeCanvas.drawText(
                            maxTempStr,
                            maxPt.x,
                            maxPt.y - 14f,
                            paint
                        )

                        // Min label below dot
                        paint.color = android.graphics.Color.argb(200, 41, 182, 246)
                        paint.isFakeBoldText = false
                        drawContext.canvas.nativeCanvas.drawText(
                            minTempStr,
                            minPt.x,
                            minPt.y + 26f,
                            paint
                        )

                        // Day label at the bottom
                        paint.color = android.graphics.Color.argb(
                            (onSurfaceColor.alpha * 255).toInt(),
                            (onSurfaceColor.red * 255).toInt(),
                            (onSurfaceColor.green * 255).toInt(),
                            (onSurfaceColor.blue * 255).toInt()
                        )
                        paint.textSize = 26f
                        paint.isFakeBoldText = true
                        drawContext.canvas.nativeCanvas.drawText(
                            dayLabel,
                            maxPt.x,
                            h - 8f,
                            paint
                        )
                    }
                }
            }
        }
    }
}
