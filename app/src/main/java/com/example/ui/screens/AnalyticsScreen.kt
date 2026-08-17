package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WeeklyPrayerAnalytics
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainUiState

@Composable
fun AnalyticsScreen(
    uiState: MainUiState,
    modifier: Modifier = Modifier
) {
    val analytics = uiState.weeklyAnalytics ?: WeeklyPrayerAnalytics(
        dayLabels = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"),
        dailyCompletedCounts = listOf(4, 5, 5, 4, 5, 5, 4),
        totalCompletedPrayers = 32,
        weeklySuccessRatePercent = 91,
        currentStreakDays = 7,
        mostConsistentPrayer = "Sabah",
        jamaatCount = 12
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("analytics_screen_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Title & Header
        item {
            Column {
                Text(
                    text = "Haftalık İbadet Analitiği",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Son 7 günlük namaz ve ibadet istatistikleriniz",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Performance KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Success Rate Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Haftalık Başarı",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "%${analytics.weeklySuccessRatePercent}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${analytics.totalCompletedPrayers} / 35 Vakit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                // Streak Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "İstikrar Serisi",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${analytics.currentStreakDays} Gün 🔥",
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Kesintisiz İbadet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Weekly 7-Day Interactive Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_chart_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Günlük Kılınan Namaz Sayısı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Maks 5 Vakit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Custom Canvas Bar Chart
                    WeeklyBarChart(
                        dayLabels = analytics.dayLabels,
                        counts = analytics.dailyCompletedCounts
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ChartLegendItem(color = MaterialTheme.colorScheme.primary, label = "5 Vakit (Tam)")
                        ChartLegendItem(color = MaterialTheme.colorScheme.tertiary, label = "1-4 Vakit")
                        ChartLegendItem(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), label = "Kılınmadı")
                    }
                }
            }
        }

        // Detailed Analytical Insights
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Detaylı Analiz & İpuçları",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    InsightRowItem(
                        icon = Icons.Filled.Star,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "En İstikrarlı Vakit",
                        description = "${analytics.mostConsistentPrayer} namazı vakti bu hafta en yüksek devamlılıkla kılındı."
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    InsightRowItem(
                        icon = Icons.Filled.Groups,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Cemaatle Kılınan Namazlar",
                        description = "Bu hafta ${analytics.jamaatCount} vakit namaz cemaat ile eda edildi. Cemaatle kılınan namaz 27 kat daha sevaptır."
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    InsightRowItem(
                        icon = Icons.Filled.MenuBook,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = "Kur'an Okuma İlerlemesi",
                        description = "Toplam ${uiState.latestQuranProgress?.lastPageRead ?: 1} sayfa okundu. Hatminizin %${((uiState.latestQuranProgress?.lastPageRead ?: 1) / 6.04).toInt()}'si tamamlandı."
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    InsightRowItem(
                        icon = Icons.Filled.HistoryToggleOff,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Kaza Namazı Durumu",
                        description = "Kalan kaza namazı borcunuz: ${uiState.kazaRecord?.totalPrayers ?: 0} vakit. Her vakit sonrası 1 kaza kılarak hızla eritebilirsiniz."
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyBarChart(
    dayLabels: List<String>,
    counts: List<Int>
) {
    val barColorFull = MaterialTheme.colorScheme.primary
    val barColorPartial = MaterialTheme.colorScheme.tertiary
    val barColorEmpty = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val width = size.width
            val height = size.height
            val barCount = counts.size
            val barSpacing = width / (barCount * 2)
            val barWidth = barSpacing * 1.2f

            // Draw horizontal grid lines
            for (level in 1..5) {
                val y = height - (level / 5f) * (height - 30.dp.toPx()) - 10.dp.toPx()
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw bars
            counts.forEachIndexed { index, count ->
                val x = barSpacing + index * (barWidth + barSpacing)
                val barHeightFraction = (count / 5f).coerceIn(0f, 1f)
                val barPixelHeight = barHeightFraction * (height - 40.dp.toPx())
                val topY = height - barPixelHeight - 10.dp.toPx()

                val barColor = when (count) {
                    5 -> barColorFull
                    in 1..4 -> barColorPartial
                    else -> barColorEmpty
                }

                // Background track
                drawRoundRect(
                    color = barColorEmpty,
                    topLeft = Offset(x, 10.dp.toPx()),
                    size = Size(barWidth, height - 20.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                // Foreground filled bar
                if (count > 0) {
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, topY),
                        size = Size(barWidth, barPixelHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day labels below bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayLabels.forEachIndexed { idx, label ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${counts.getOrElse(idx) { 0 }}/5",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@Composable
fun InsightRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
