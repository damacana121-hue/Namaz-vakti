package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PrayerRecordEntity
import com.example.model.DailyDua
import com.example.model.DuaDataRepository
import com.example.model.PrayerType
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onNavigateToCityPicker: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToDhikr: () -> Unit,
    onNavigateToGemini: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val schedule = uiState.currentSchedule
    val todayRecord = uiState.todayRecord ?: PrayerRecordEntity(date = "")

    val calendar = uiState.selectedDate

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header & Location Bar
        item {
            HeaderLocationBar(
                cityName = uiState.selectedCity.name,
                countryName = uiState.selectedCity.country,
                dateString = schedule?.dateString ?: "",
                hijriDateString = schedule?.hijriDateString ?: "",
                onCityClick = onNavigateToCityPicker,
                onCalendarClick = {
                    try {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val newCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                viewModel.setSelectedDate(newCal)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    } catch (e: Exception) {
                        // ignore
                    }
                },
                onGpsClick = { viewModel.detectAndApplyGpsLocation(context) }
            )
        }

        // 2. Gemini Live Assistant Quick Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(onClick = onNavigateToGemini)
                    .testTag("gemini_home_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(ElegantAmethystSecondary, ElegantLavenderPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "İkizler Zekası",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = ElegantLavenderPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Sesli Asistan",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ElegantLavenderPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Namaz, kıble ve manevi sorularınızı sesli sorun",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Sesli Konuş",
                        tint = ElegantLavenderPrimary,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(4.dp)
                    )
                }
            }
        }

        // 3. Main Hero Countdown Card
        item {
            schedule?.let { s ->
                HeroCountdownCard(
                    schedule = s,
                    isAlignedWithQibla = uiState.isAlignedWithQibla,
                    onQiblaClick = onNavigateToQibla
                )
            }
        }

        // 3. Daily Prayer Times Timeline List
        item {
            schedule?.let { s ->
                PrayerTimesCard(
                    schedule = s,
                    userSettings = uiState.userSettings,
                    onToggleNotification = { type ->
                        when (type) {
                            PrayerType.FAJR -> viewModel.updateNotificationToggle(fajr = !uiState.userSettings.notifFajr)
                            PrayerType.SUNRISE -> viewModel.updateNotificationToggle(sunrise = !uiState.userSettings.notifSunrise)
                            PrayerType.DHUHR -> viewModel.updateNotificationToggle(dhuhr = !uiState.userSettings.notifDhuhr)
                            PrayerType.ASR -> viewModel.updateNotificationToggle(asr = !uiState.userSettings.notifAsr)
                            PrayerType.MAGHRIB -> viewModel.updateNotificationToggle(maghrib = !uiState.userSettings.notifMaghrib)
                            PrayerType.ISHA -> viewModel.updateNotificationToggle(isha = !uiState.userSettings.notifIsha)
                        }
                    }
                )
            }
        }

        // 4. Daily Prayer Check-in (5 Vakit Takip)
        item {
            DailyPrayerCheckInCard(
                todayRecord = todayRecord,
                onToggleDone = { type -> viewModel.togglePrayerCompleted(type) },
                onToggleJamaat = { type -> viewModel.toggleJamaat(type) }
            )
        }

        // 5. Customizable Widget: Günün Ayeti / Hadisi
        if (uiState.userSettings.showAyahHadithWidget) {
            item {
                DailyAyahHadithWidget()
            }
        }

        // 6. Customizable Widget: Hızlı Zikirmatik
        if (uiState.userSettings.showFastDhikrWidget && uiState.activeDhikr != null) {
            item {
                FastDhikrWidget(
                    activeDhikr = uiState.activeDhikr,
                    onTap = { viewModel.countDhikr() },
                    onOpenDhikrScreen = onNavigateToDhikr
                )
            }
        }

        // 7. Customizable Widget: Kur'an İlerlemesi
        if (uiState.userSettings.showQuranProgressWidget) {
            item {
                QuranProgressWidget(
                    latestProgress = uiState.latestQuranProgress,
                    onOpenQuran = onNavigateToQuran
                )
            }
        }

        // 8. Customizable Widget: Kaza Namazı Durumu
        if (uiState.userSettings.showKazaWidget && uiState.kazaRecord != null) {
            item {
                KazaSummaryWidget(
                    kazaRecord = uiState.kazaRecord,
                    onOpenKaza = onNavigateToDhikr
                )
            }
        }
    }
}

@Composable
fun HeaderLocationBar(
    cityName: String,
    countryName: String,
    dateString: String,
    hijriDateString: String,
    onCityClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onGpsClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onCityClick)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Konum",
                        tint = ElegantLavenderPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$cityName, $countryName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Şehir Seç",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$dateString • $hijriDateString",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onGpsClick,
                    modifier = Modifier.testTag("home_gps_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "GPS Konumu",
                        tint = ElegantLavenderPrimary
                    )
                }
                IconButton(
                    onClick = onCalendarClick,
                    modifier = Modifier.testTag("date_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Tarih Seç",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HeroCountdownCard(
    schedule: com.example.util.DailyPrayerSchedule,
    isAlignedWithQibla: Boolean,
    onQiblaClick: () -> Unit
) {
    val remainingSec = schedule.secondsRemainingToNext
    val hours = remainingSec / 3600
    val minutes = (remainingSec % 3600) / 60
    val seconds = remainingSec % 60

    val hoursStr = String.format("%02d", hours)
    val minutesStr = String.format("%02d", minutes)
    val secondsStr = String.format("%02d", seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("hero_countdown_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ElegantHeroGradientStart,
                            ElegantHeroGradientMid,
                            ElegantHeroGradientEnd
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Şu an: ${schedule.currentActivePrayer.titleTr} Vakti",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = ElegantLavenderOnContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AssistChip(
                        onClick = onQiblaClick,
                        label = { Text("Kıble Pusulası", color = Color.White, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Explore,
                                contentDescription = "Kıble",
                                tint = ElegantLavenderPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(alpha = 0.12f)
                        ),
                        border = null
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "${schedule.nextPrayer.type.titleTr.uppercase()} VAKTİNE KALAN SÜRE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElegantLavenderOnContainer.copy(alpha = 0.85f),
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Digital Countdown Blocks
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CountdownTimeUnitBlock(digits = hoursStr, unitLabel = "SAAT")
                    Text(":", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                    CountdownTimeUnitBlock(digits = minutesStr, unitLabel = "DAKİKA")
                    Text(":", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                    CountdownTimeUnitBlock(digits = secondsStr, unitLabel = "SANİYE")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Vakit Saati: ${schedule.nextPrayer.timeFormatted}",
                    style = MaterialTheme.typography.titleMedium,
                    color = ElegantLavenderPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { schedule.progressPercentToNext },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ElegantLavenderPrimary,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = schedule.currentActivePrayer.titleTr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(schedule.progressPercentToNext * 100).toInt()}% Tamamlandı",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = schedule.nextPrayer.type.titleTr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownTimeUnitBlock(digits: String, unitLabel: String) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = digits,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = Color.White
            )
            Text(
                text = unitLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = ElegantLavenderOnContainer.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun PrayerTimesCard(
    schedule: com.example.util.DailyPrayerSchedule,
    userSettings: com.example.data.repository.UserSettings,
    onToggleNotification: (PrayerType) -> Unit
) {
    val prayerList = listOf(
        Pair(schedule.fajr, userSettings.notifFajr),
        Pair(schedule.sunrise, userSettings.notifSunrise),
        Pair(schedule.dhuhr, userSettings.notifDhuhr),
        Pair(schedule.asr, userSettings.notifAsr),
        Pair(schedule.maghrib, userSettings.notifMaghrib),
        Pair(schedule.isha, userSettings.notifIsha)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("prayer_times_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Günün Namaz Vakitleri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            prayerList.forEachIndexed { index, (prayer, isNotifOn) ->
                val isActive = schedule.currentActivePrayer == prayer.type
                val isNext = schedule.nextPrayer.type == prayer.type

                PrayerTimeRowItem(
                    prayer = prayer,
                    isActive = isActive,
                    isNext = isNext,
                    isNotifOn = isNotifOn,
                    onToggleNotif = { onToggleNotification(prayer.type) }
                )

                if (index < prayerList.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerTimeRowItem(
    prayer: com.example.util.SinglePrayerTime,
    isActive: Boolean,
    isNext: Boolean,
    isNotifOn: Boolean,
    onToggleNotif: () -> Unit
) {
    val backgroundColor = when {
        isNext -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isNext) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (prayer.type) {
                        PrayerType.FAJR -> Icons.Filled.WbTwilight
                        PrayerType.SUNRISE -> Icons.Filled.WbSunny
                        PrayerType.DHUHR -> Icons.Filled.LightMode
                        PrayerType.ASR -> Icons.Filled.FilterDrama
                        PrayerType.MAGHRIB -> Icons.Filled.NightsStay
                        PrayerType.ISHA -> Icons.Filled.Bedtime
                    },
                    contentDescription = prayer.type.titleTr,
                    tint = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prayer.type.titleTr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isNext || isActive) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isNext) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Sıradaki",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else if (isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Şimdiki",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                Text(
                    text = prayer.type.arabicName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = prayer.timeFormatted,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onToggleNotif,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isNotifOn) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsOff,
                    contentDescription = "Bildirim",
                    tint = if (isNotifOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DailyPrayerCheckInCard(
    todayRecord: PrayerRecordEntity,
    onToggleDone: (PrayerType) -> Unit,
    onToggleJamaat: (PrayerType) -> Unit
) {
    val prayers = listOf(
        Triple(PrayerType.FAJR, todayRecord.fajrDone, todayRecord.fajrJamaat),
        Triple(PrayerType.DHUHR, todayRecord.dhuhrDone, todayRecord.dhuhrJamaat),
        Triple(PrayerType.ASR, todayRecord.asrDone, todayRecord.asrJamaat),
        Triple(PrayerType.MAGHRIB, todayRecord.maghribDone, todayRecord.maghribJamaat),
        Triple(PrayerType.ISHA, todayRecord.ishaDone, todayRecord.ishaJamaat)
    )

    val completedCount = todayRecord.completedCount()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("prayer_checkin_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bugünkü Namazlarım",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$completedCount / 5 Vakit Kılındı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (completedCount == 5) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (completedCount == 5) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Surface(
                    color = if (completedCount == 5) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (completedCount == 5) "Tebrikler! 🎉" else "%${(completedCount * 20)}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (completedCount == 5) SuccessGreen else MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                prayers.forEach { (type, isDone, isJamaat) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isDone) 2.dp else 1.dp,
                                    color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { onToggleDone(type) }
                                .testTag("checkin_${type.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Kılındı",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = type.titleTr.take(2),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = type.titleTr,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Cemaat Toggle Chip
                        FilterChip(
                            selected = isJamaat,
                            onClick = { onToggleJamaat(type) },
                            label = { Text("Cemaat", fontSize = 9.sp) },
                            modifier = Modifier.scale(0.8f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.padding(0.dp)
)

@Composable
fun DailyAyahHadithWidget() {
    val item = remember { DuaDataRepository.dailyAyahHadith.random() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("ayah_hadith_widget"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.FormatQuote,
                    contentDescription = "Günün Ayeti",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Günün Hikmeti",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.first,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "— ${item.second}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun FastDhikrWidget(
    activeDhikr: com.example.data.local.entity.DhikrEntity,
    onTap: () -> Unit,
    onOpenDhikrScreen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("fast_dhikr_widget"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.TouchApp,
                        contentDescription = "Zikir",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hızlı Zikirmatik",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeDhikr.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${activeDhikr.currentCount} / ${activeDhikr.targetCount} (Tur: ${activeDhikr.totalCyclesCompleted})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onTap,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("quick_dhikr_tap_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "+1",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun QuranProgressWidget(
    latestProgress: com.example.data.local.entity.QuranProgressEntity?,
    onOpenQuran: () -> Unit
) {
    val page = latestProgress?.lastPageRead ?: 1
    val juz = latestProgress?.lastJuz ?: 1
    val surah = latestProgress?.lastSurah ?: "Fâtiha"
    val percent = ((page.toFloat() / 604f) * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onOpenQuran)
            .testTag("quran_progress_widget"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = "Kur'an",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kur'an-ı Kerim İlerlemesi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Hatim: %$percent",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Son Kalınan: $juz. Cüz • $surah Suresi (Sayfa $page / 604)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { page / 604f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun KazaSummaryWidget(
    kazaRecord: com.example.data.local.entity.KazaRecordEntity,
    onOpenKaza: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onOpenKaza)
            .testTag("kaza_summary_widget"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.HistoryToggleOff,
                        contentDescription = "Kaza",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kaza Namazı Borçları",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Toplam Kaza Borcu: ${kazaRecord.totalPrayers} Vakit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Detay",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
