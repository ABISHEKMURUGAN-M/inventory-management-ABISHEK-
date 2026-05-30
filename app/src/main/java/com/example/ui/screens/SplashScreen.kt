package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndustrialNavy
import com.example.ui.theme.IndustrialWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // Rotation animation for the Hanon turbine emblem
    val infiniteTransition = rememberInfiniteTransition(label = "turbine_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Fade-in animation for metadata text
    var textAlpha by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        ) { value, _ -> textAlpha = value }

        // Splash duration
        delay(2200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant Canvas-drawn turbine logo representing fluid flow and radiator cooling
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(160.dp)
                    .alpha(textAlpha)
            ) {
                val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                val outerRadius = size.width * 0.4f
                val circleColor = Color(0xFF00B4D8)
                val coreColor = Color(0xFF0077B6)

                // 1. Draw outer thermal energy path (dashed styled circle)
                drawCircle(
                    color = circleColor.copy(alpha = 0.3f),
                    radius = outerRadius,
                    center = centerOffset,
                    style = Stroke(width = 4f)
                )

                // 2. Rotate and draw fluid rotor blades (thermo-dynamic radiator symbols)
                rotate(rotationAngle, pivot = centerOffset) {
                    val bladeCount = 6
                    val bladeLength = outerRadius * 0.8f
                    for (i in 0 until bladeCount) {
                        val angleDegrees = (i * (360f / bladeCount))
                        rotate(angleDegrees, pivot = centerOffset) {
                            // Draw metallic fluid radiator vane / fin
                            val endPoint = androidx.compose.ui.geometry.Offset(centerOffset.x, centerOffset.y - bladeLength)
                            drawLine(
                                color = circleColor,
                                start = centerOffset,
                                end = endPoint,
                                strokeWidth = 14f,
                                cap = StrokeCap.Round
                            )
                            // Draw vane fin cap
                            drawCircle(
                                color = IndustrialWhite,
                                radius = 8f,
                                center = endPoint
                            )
                        }
                    }
                }

                // 3. Central mechanical bearing core
                drawCircle(
                    color = coreColor,
                    radius = 26f,
                    center = centerOffset
                )
                drawCircle(
                    color = IndustrialWhite,
                    radius = 12f,
                    center = centerOffset
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Brand title layout
            Text(
                text = "HANON SYSTEMS",
                color = IndustrialWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "RADIATOR PRODUCTION MONITORING APP",
                color = Color(0xFF00B4D8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(90.dp))

            Text(
                text = "Automated MES Terminal v2.6\nReal-Time Synchronized Engine",
                color = Color.LightGray.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}
