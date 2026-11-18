package com.weatherwise.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.weatherwise.domain.model.WeatherConditionType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun WeatherBackground(
    conditionType: WeatherConditionType?,
    animationsEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradient = when (conditionType) {
        WeatherConditionType.CLEAR_DAY -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1E88E5), Color(0xFF42A5F5), Color(0xFFFFB74D))
        )
        WeatherConditionType.CLEAR_NIGHT -> Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
        )
        WeatherConditionType.CLOUDY_DAY -> Brush.verticalGradient(
            colors = listOf(Color(0xFF37474F), Color(0xFF546E7A), Color(0xFF78909C))
        )
        WeatherConditionType.CLOUDY_NIGHT -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1A202C), Color(0xFF2D3748), Color(0xFF4A5568))
        )
        WeatherConditionType.RAINY, WeatherConditionType.DRIZZLE -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1C2833), Color(0xFF2C3E50), Color(0xFF34495E))
        )
        WeatherConditionType.SNOWY -> Brush.verticalGradient(
            colors = listOf(Color(0xFF455A64), Color(0xFF607D8B), Color(0xFF90A4AE))
        )
        WeatherConditionType.THUNDERSTORM -> Brush.verticalGradient(
            colors = listOf(Color(0xFF110726), Color(0xFF21153B), Color(0xFF311B92))
        )
        WeatherConditionType.ATMOSPHERE -> Brush.verticalGradient(
            colors = listOf(Color(0xFF3E4E59), Color(0xFF596E79), Color(0xFF7E909A))
        )
        null -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1976D2), Color(0xFF42A5F5), Color(0xFF90CAF9))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        if (animationsEnabled && conditionType != null) {
            when (conditionType) {
                WeatherConditionType.CLEAR_DAY -> SunAnimation()
                WeatherConditionType.CLEAR_NIGHT -> StarryNightAnimation()
                WeatherConditionType.RAINY, WeatherConditionType.DRIZZLE -> RainAnimation()
                WeatherConditionType.SNOWY -> SnowAnimation()
                WeatherConditionType.THUNDERSTORM -> ThunderstormAnimation()
                WeatherConditionType.CLOUDY_DAY, WeatherConditionType.CLOUDY_NIGHT, WeatherConditionType.ATMOSPHERE -> CloudAnimation()
            }
        }
        content()
    }
}

@Composable
private fun SunAnimation() {
    val transition = rememberInfiniteTransition(label = "sun_transition")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width * 0.85f
        val centerY = size.height * 0.15f
        val sunRadius = 50f

        // Draw sun core
        drawCircle(
            color = Color(0xFFFFD54F).copy(alpha = 0.5f),
            radius = sunRadius * 1.5f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color(0xFFFFB300),
            radius = sunRadius,
            center = Offset(centerX, centerY)
        )

        // Draw rotating rays
        val rayCount = 12
        val rayLength = 30f
        for (i in 0 until rayCount) {
            val angle = Math.toRadians((rotation + (i * (360.0 / rayCount)))).toFloat()
            val startX = centerX + (sunRadius + 8f) * cos(angle)
            val startY = centerY + (sunRadius + 8f) * sin(angle)
            val endX = centerX + (sunRadius + 8f + rayLength) * cos(angle)
            val endY = centerY + (sunRadius + 8f + rayLength) * sin(angle)

            drawLine(
                color = Color(0xFFFFE082).copy(alpha = 0.6f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 4f
            )
        }
    }
}

@Composable
private fun StarryNightAnimation() {
    val transition = rememberInfiniteTransition(label = "star_transition")
    val twinkle by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_twinkle"
    )

    val stars = remember {
        List(40) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 3f + 1.5f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEachIndexed { index, (xRatio, yRatio, r) ->
            val alpha = if (index % 2 == 0) twinkle else (1.2f - twinkle).coerceIn(0.2f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = r,
                center = Offset(size.width * xRatio, size.height * yRatio * 0.7f)
            )
        }

        // Crescent moon
        val moonCenter = Offset(size.width * 0.85f, size.height * 0.12f)
        drawCircle(
            color = Color(0xFFF1F5F9).copy(alpha = 0.9f),
            radius = 36f,
            center = moonCenter
        )
        drawCircle(
            color = Color(0xFF0F172A),
            radius = 32f,
            center = Offset(moonCenter.x - 12f, moonCenter.y - 8f)
        )
    }
}

@Composable
private fun RainAnimation() {
    val transition = rememberInfiniteTransition(label = "rain_transition")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain_progress"
    )

    val raindrops = remember {
        List(60) {
            Pair(Random.nextFloat(), Random.nextFloat())
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val dropLength = 28f
        val slant = 6f

        raindrops.forEach { (startXRatio, startYRatio) ->
            val yOffset = ((startYRatio + progress) % 1f) * size.height
            val xOffset = startXRatio * size.width

            drawLine(
                color = Color(0xFF90CAF9).copy(alpha = 0.45f),
                start = Offset(xOffset, yOffset),
                end = Offset(xOffset - slant, yOffset + dropLength),
                strokeWidth = 2.5f
            )
        }
    }
}

@Composable
private fun SnowAnimation() {
    val transition = rememberInfiniteTransition(label = "snow_transition")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snow_progress"
    )

    val snowflakes = remember {
        List(45) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 3.5f + 2f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        snowflakes.forEach { (xRatio, yRatio, r) ->
            val y = ((yRatio + progress) % 1f) * size.height
            val sway = sin(y * 0.02f) * 12f
            val x = (xRatio * size.width) + sway

            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = r,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun ThunderstormAnimation() {
    val transition = rememberInfiniteTransition(label = "storm_transition")
    val flashProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "storm_flash"
    )

    // Flash intensity peaks around progress 0.35 - 0.40
    val flashAlpha = when {
        flashProgress in 0.32f..0.36f -> 0.4f
        flashProgress in 0.38f..0.42f -> 0.75f
        else -> 0f
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Flash overlay
        if (flashAlpha > 0f) {
            drawRect(
                color = Color(0xFFEDE7F6).copy(alpha = flashAlpha),
                size = size
            )

            // Lightning bolt
            val boltPath = Path().apply {
                moveTo(size.width * 0.65f, 0f)
                lineTo(size.width * 0.58f, size.height * 0.22f)
                lineTo(size.width * 0.62f, size.height * 0.24f)
                lineTo(size.width * 0.52f, size.height * 0.45f)
            }
            drawPath(
                path = boltPath,
                color = Color.White.copy(alpha = flashAlpha),
                style = Stroke(width = 4f)
            )
        }
    }
}

@Composable
private fun CloudAnimation() {
    val transition = rememberInfiniteTransition(label = "cloud_transition")
    val cloudOffset by transition.animateFloat(
        initialValue = -150f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_drift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val yBase = size.height * 0.18f

        // Drifting cloud 1
        drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = 70f,
            center = Offset(size.width * 0.35f + cloudOffset, yBase)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = 50f,
            center = Offset(size.width * 0.22f + cloudOffset, yBase + 15f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = 60f,
            center = Offset(size.width * 0.48f + cloudOffset, yBase + 10f)
        )

        // Drifting cloud 2 (offset)
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = 80f,
            center = Offset(size.width * 0.75f - (cloudOffset * 0.7f), yBase * 1.5f)
        )
    }
}
