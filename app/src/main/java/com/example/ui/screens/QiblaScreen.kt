package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel
import kotlin.math.*

@Composable
fun QiblaScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        viewModel.startCompass()
        onDispose {
            viewModel.stopCompass()
        }
    }

    val azimuth = uiState.compassOrientation.azimuthDegrees
    val qiblaBearing = uiState.qiblaBearing.toFloat()
    val isAligned = uiState.isAlignedWithQibla

    // Permission launcher for fine/coarse GPS location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectAndApplyGpsLocation(context)
        } else {
            Toast.makeText(context, "Otomatik konum için GPS izni gereklidir.", Toast.LENGTH_SHORT).show()
        }
    }

    // Smooth rotation animation
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = tween(durationMillis = 120),
        label = "compass_rotation"
    )

    // Calculate heading difference to Qibla
    var headingDiff = (qiblaBearing - azimuth) % 360f
    if (headingDiff < -180f) headingDiff += 360f
    if (headingDiff > 180f) headingDiff -= 360f
    val absDiff = abs(headingDiff).toInt()

    val turnDirectionText = when {
        isAligned -> "KIBLE HİZALANDI! 🕌"
        headingDiff > 0 -> "Sağa ${absDiff}° Dönün"
        else -> "Sola ${absDiff}° Dönün"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("qibla_screen_content"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Location & GPS Auto-Detect Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = ElegantLavenderPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.selectedCity.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kıble Açısı: ${String.format("%.1f", uiState.qiblaBearing)}° (Kuzeyden)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElegantLavenderPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Kaaba distance badge
                    Surface(
                        color = ElegantAmethystSecondary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Kabe Mesafesi",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantLavenderPrimary
                            )
                            Text(
                                text = "${uiState.distanceToKaabaKm.toInt()} km",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GPS Auto-detect button
                OutlinedButton(
                    onClick = {
                        val hasFine = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val hasCoarse = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasFine || hasCoarse) {
                            viewModel.detectAndApplyGpsLocation(context)
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qibla_gps_auto_detect_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElegantLavenderPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantLavenderPrimary.copy(alpha = 0.5f))
                ) {
                    if (uiState.isGpsDetecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = ElegantLavenderPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GPS ile Konum Algılanıyor...")
                    } else {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GPS ile Konumumu Otomatik Algıla")
                    }
                }

                if (uiState.gpsStatusMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.gpsStatusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 2. Alignment Guide Status Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (isAligned) SuccessGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = if (isAligned) androidx.compose.foundation.BorderStroke(2.dp, SuccessGreen) else null
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isAligned) Icons.Filled.CheckCircle else Icons.Filled.CompassCalibration,
                    contentDescription = null,
                    tint = if (isAligned) SuccessGreen else ElegantLavenderPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = turnDirectionText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAligned) SuccessGreen else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 3. Animated Compass Dial
        Box(
            modifier = Modifier
                .size(300.dp)
                .testTag("compass_dial_box"),
            contentAlignment = Alignment.Center
        ) {
            // Compass background dial canvas
            CompassDialCanvas(
                azimuth = animatedAzimuth,
                qiblaBearing = qiblaBearing,
                isAligned = isAligned
            )

            // Center Kaaba Indicator Badge
            Surface(
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                color = if (isAligned) SuccessGreen else MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🕋",
                            fontSize = 26.sp
                        )
                        Text(
                            text = "KABE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAligned) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 4. Live Angle & Tilt Readouts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cihaz Açısı",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${animatedAzimuth.toInt()}°",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hedef Kıble",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${qiblaBearing.toInt()}°",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ElegantLavenderPrimary
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sensör Durumu",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (uiState.compassOrientation.isSensorAvailable) "Aktif ✓" else "Manuel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.compassOrientation.isSensorAvailable) SuccessGreen else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 5. Manual Angle / Simulator Mode (Useful for testing without physical magnetometer)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pusula Test & Hassasiyet Ayarı",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.isManualCompassAngle) {
                        TextButton(onClick = { viewModel.useHardwareSensor() }) {
                            Text("Sensöre Dön", fontSize = 12.sp, color = ElegantLavenderPrimary)
                        }
                    }
                }

                Text(
                    text = "Emülatörde veya manyetik sensörsüz cihazlarda açıyı elle ayarlayabilirsiniz:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Slider(
                    value = uiState.compassOrientation.azimuthDegrees,
                    onValueChange = { viewModel.setManualCompassAngle(it) },
                    valueRange = 0f..360f,
                    modifier = Modifier.testTag("manual_compass_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = ElegantLavenderPrimary,
                        activeTrackColor = ElegantLavenderPrimary
                    )
                )
            }
        }

        // 6. Calibration Tips Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = ElegantLavenderPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Doğru sonuç için telefonunuzu düz bir zemine koyunuz ve manyetik kılıflardan uzak tutunuz. Cihazınızı havada yatay '8' çizerek pusula sensörünü kalibre edebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun CompassDialCanvas(
    azimuth: Float,
    qiblaBearing: Float,
    isAligned: Boolean
) {
    val primaryColor = ElegantLavenderPrimary
    val secondaryColor = ElegantAmethystSecondary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 16.dp.toPx()

        // Outer rim glow
        drawCircle(
            color = if (isAligned) SuccessGreen.copy(alpha = 0.4f) else primaryColor.copy(alpha = 0.2f),
            radius = radius + 8.dp.toPx(),
            center = center,
            style = Stroke(width = if (isAligned) 6.dp.toPx() else 3.dp.toPx())
        )

        // Inner dial body
        drawCircle(
            color = surfaceColor,
            radius = radius,
            center = center
        )

        // Rotate canvas according to device azimuth (North moves relative to device)
        rotate(degrees = -azimuth, pivot = center) {
            // Draw tick marks every 15 degrees and Cardinal letters
            for (i in 0 until 360 step 15) {
                val angleRad = Math.toRadians(i.toDouble())
                val isMajor = i % 90 == 0
                val isMedium = i % 45 == 0 && !isMajor
                val tickLength = if (isMajor) 16.dp.toPx() else if (isMedium) 10.dp.toPx() else 6.dp.toPx()
                val strokeWidth = if (isMajor) 3.5.dp.toPx() else if (isMedium) 2.dp.toPx() else 1.2.dp.toPx()

                val startX = center.x + (radius - tickLength) * sin(angleRad).toFloat()
                val startY = center.y - (radius - tickLength) * cos(angleRad).toFloat()
                val endX = center.x + radius * sin(angleRad).toFloat()
                val endY = center.y - radius * cos(angleRad).toFloat()

                val tickColor = when (i) {
                    0 -> Color(0xFFEF4444) // North in Red
                    else -> outlineColor.copy(alpha = 0.6f)
                }

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // Draw North Pointer (Red Arrow)
            val northPath = Path().apply {
                moveTo(center.x, center.y - radius + 22.dp.toPx())
                lineTo(center.x - 12.dp.toPx(), center.y - 42.dp.toPx())
                lineTo(center.x + 12.dp.toPx(), center.y - 42.dp.toPx())
                close()
            }
            drawPath(path = northPath, color = Color(0xFFEF4444))

            // Draw Qibla Direction Indicator Needle (Glowing Arrow pointing to Kaaba)
            rotate(degrees = qiblaBearing, pivot = center) {
                val qiblaNeedlePath = Path().apply {
                    moveTo(center.x, center.y - radius + 6.dp.toPx())
                    lineTo(center.x - 14.dp.toPx(), center.y - 54.dp.toPx())
                    lineTo(center.x, center.y - 40.dp.toPx())
                    lineTo(center.x + 14.dp.toPx(), center.y - 54.dp.toPx())
                    close()
                }
                drawPath(
                    path = qiblaNeedlePath,
                    color = if (isAligned) SuccessGreen else primaryColor
                )

                // Small Kaaba marker point at the tip
                drawCircle(
                    color = if (isAligned) SuccessGreen else primaryColor,
                    radius = 8.dp.toPx(),
                    center = Offset(center.x, center.y - radius + 8.dp.toPx())
                )
            }
        }
    }
}
