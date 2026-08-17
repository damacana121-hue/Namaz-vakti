package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElegantLavenderPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel

@Composable
fun QiblaScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        viewModel.startCompass()
        onDispose { viewModel.stopCompass() }
    }

    val azimuth = uiState.compassOrientation.azimuthDegrees
    val qibla = uiState.qiblaBearing.toFloat()
    val aligned = uiState.isAlignedWithQibla
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = tween(90),
        label = "compass_rotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("qibla_screen_content"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kıble",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(330.dp)
                .testTag("compass_dial_box"),
            contentAlignment = Alignment.Center
        ) {
            CompassDialCanvas(
                azimuth = animatedAzimuth,
                qiblaBearing = qibla,
                isAligned = aligned
            )
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = if (aligned) "Kıble yönündesiniz" else "Kâbe yönünü takip edin",
            color = if (aligned) SuccessGreen else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${qibla.toInt()}°",
            color = ElegantLavenderPrimary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CompassDialCanvas(
    azimuth: Float,
    qiblaBearing: Float,
    isAligned: Boolean
) {
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline

    Canvas(Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 14.dp.toPx()

        drawCircle(surface, radius, center)
        drawCircle(outline, radius, center, style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx()))

        rotate(-azimuth, center) {
            for (i in 0 until 360 step 15) {
                val angle = Math.toRadians((i - 90).toDouble())
                val outer = Offset(
                    center.x + kotlin.math.cos(angle).toFloat() * radius,
                    center.y + kotlin.math.sin(angle).toFloat() * radius
                )
                val innerRadius = if (i % 45 == 0) radius - 20.dp.toPx() else radius - 11.dp.toPx()
                val inner = Offset(
                    center.x + kotlin.math.cos(angle).toFloat() * innerRadius,
                    center.y + kotlin.math.sin(angle).toFloat() * innerRadius
                )
                drawLine(outline, inner, outer, strokeWidth = if (i % 45 == 0) 3.dp.toPx() else 1.dp.toPx())
            }

            drawTextOnCompass("N", center.x - 7.dp.toPx(), center.y - radius + 28.dp.toPx(), Color.Red)
            drawTextOnCompass("E", center.x + radius - 28.dp.toPx(), center.y + 5.dp.toPx(), outline)
            drawTextOnCompass("S", center.x - 7.dp.toPx(), center.y + radius - 18.dp.toPx(), outline)
            drawTextOnCompass("W", center.x - radius + 18.dp.toPx(), center.y + 5.dp.toPx(), outline)

            val qAngle = Math.toRadians((qiblaBearing - 90f).toDouble())
            val tipRadius = radius - 28.dp.toPx()
            val tip = Offset(
                center.x + kotlin.math.cos(qAngle).toFloat() * tipRadius,
                center.y + kotlin.math.sin(qAngle).toFloat() * tipRadius
            )
            val leftAngle = qAngle + Math.toRadians(150.0)
            val rightAngle = qAngle - Math.toRadians(150.0)
            val left = Offset(center.x + kotlin.math.cos(leftAngle).toFloat() * 28.dp.toPx(), center.y + kotlin.math.sin(leftAngle).toFloat() * 28.dp.toPx())
            val right = Offset(center.x + kotlin.math.cos(rightAngle).toFloat() * 28.dp.toPx(), center.y + kotlin.math.sin(rightAngle).toFloat() * 28.dp.toPx())
            val pointer = androidx.compose.ui.graphics.Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(left.x, left.y)
                lineTo(right.x, right.y)
                close()
            }
            drawPath(pointer, if (isAligned) SuccessGreen else ElegantLavenderPrimary)
        }

        drawCircle(if (isAligned) SuccessGreen else ElegantLavenderPrimary, 20.dp.toPx(), center)
        drawCircle(surface, 7.dp.toPx(), center)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTextOnCompass(
    text: String,
    x: Float,
    y: Float,
    color: Color
) {
    drawContext.canvas.nativeCanvas.drawText(
        text,
        x,
        y,
        android.graphics.Paint().apply {
            this.color = color.hashCode()
            textSize = 18.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
    )
}
