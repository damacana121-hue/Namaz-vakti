package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DhikrEntity
import com.example.data.local.entity.KazaRecordEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrKazaScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Zikirmatik", "Kaza Namazları")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("dhikr_kaza_screen")
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> ZikirmatikTab(
                dhikrList = uiState.dhikrList,
                activeDhikr = uiState.activeDhikr,
                onSelectDhikr = { viewModel.selectActiveDhikr(it) },
                onCount = { viewModel.countDhikr() },
                onReset = { viewModel.resetActiveDhikr() },
                onCreateNewDhikr = { title, arabic, meaning, target, cat ->
                    viewModel.createNewDhikr(title, arabic, meaning, target, cat)
                }
            )
            1 -> KazaTrackerTab(
                kazaRecord = uiState.kazaRecord ?: KazaRecordEntity(id = 1),
                onUpdateKaza = { f, d, a, m, i, w, fast ->
                    viewModel.updateKaza(f, d, a, m, i, w, fast)
                }
            )
        }
    }
}

@Composable
fun ZikirmatikTab(
    dhikrList: List<DhikrEntity>,
    activeDhikr: DhikrEntity?,
    onSelectDhikr: (DhikrEntity) -> Unit,
    onCount: () -> Unit,
    onReset: () -> Unit,
    onCreateNewDhikr: (String, String, String, Int, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val currentDhikr = activeDhikr ?: dhikrList.firstOrNull() ?: DhikrEntity(
        title = "Sübhânallâh",
        arabicText = "سُبْحَانَ اللَّهِ",
        turkishMeaning = "Allah eksikliklerden münezzehtir.",
        targetCount = 33
    )

    val progress = (currentDhikr.currentCount.toFloat() / currentDhikr.targetCount.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Dhikr selector row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kayıtlı Zikirler",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Yeni Ekle")
                }
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(dhikrList) { dhikr ->
                    FilterChip(
                        selected = dhikr.id == currentDhikr.id,
                        onClick = { onSelectDhikr(dhikr) },
                        label = { Text(dhikr.title, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Active Dhikr Display Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentDhikr.arabicText,
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentDhikr.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentDhikr.turkishMeaning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Hedef: ${currentDhikr.targetCount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tamamlanan Tur: ${currentDhikr.totalCyclesCompleted}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Big Digital Zikirmatik Counter Button
        item {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElegantHeroGradientMid,
                                ElegantHeroGradientStart,
                                ElegantHeroGradientEnd
                            )
                        )
                    )
                    .clickable { onCount() }
                    .testTag("big_dhikr_tap_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${currentDhikr.currentCount}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "DOKUN",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp
                        ),
                        color = ElegantLavenderPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Reset Button
        item {
            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("reset_dhikr_button")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sayacı Sıfırla")
            }
        }
    }

    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newArabic by remember { mutableStateOf("") }
        var newMeaning by remember { mutableStateOf("") }
        var newTarget by remember { mutableStateOf("33") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Yeni Zikir Ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Zikir Adı (Örn: Lâ ilâhe illallâh)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newArabic,
                        onValueChange = { newArabic = it },
                        label = { Text("Arapça Metin (İsteğe bağlı)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newMeaning,
                        onValueChange = { newMeaning = it },
                        label = { Text("Türkçe Anlamı") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTarget,
                        onValueChange = { newTarget = it.filter { c -> c.isDigit() } },
                        label = { Text("Hedef Sayı (Örn: 33, 99, 100)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            val target = newTarget.toIntOrNull() ?: 33
                            onCreateNewDhikr(newTitle, newArabic, newMeaning, target, "Özel")
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun KazaTrackerTab(
    kazaRecord: KazaRecordEntity,
    onUpdateKaza: (Int, Int, Int, Int, Int, Int, Int) -> Unit
) {
    val totalDebt = kazaRecord.totalPrayers

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Toplam Kaza Borcu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Günde 1 vakit kılarak ${totalDebt} günde bitebilir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Text(
                        text = "$totalDebt Vakit",
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Kaza items
        item {
            KazaCounterRow(
                title = "Sabah Namazı Kazası",
                rakats = "2 Rekat Farz",
                count = kazaRecord.fajrKaza,
                onAdd = { onUpdateKaza(1, 0, 0, 0, 0, 0, 0) },
                onMinus = { onUpdateKaza(-1, 0, 0, 0, 0, 0, 0) }
            )
        }

        item {
            KazaCounterRow(
                title = "Öğle Namazı Kazası",
                rakats = "4 Rekat Farz",
                count = kazaRecord.dhuhrKaza,
                onAdd = { onUpdateKaza(0, 1, 0, 0, 0, 0, 0) },
                onMinus = { onUpdateKaza(0, -1, 0, 0, 0, 0, 0) }
            )
        }

        item {
            KazaCounterRow(
                title = "İkindi Namazı Kazası",
                rakats = "4 Rekat Farz",
                count = kazaRecord.asrKaza,
                onAdd = { onUpdateKaza(0, 0, 1, 0, 0, 0, 0) },
                onMinus = { onUpdateKaza(0, 0, -1, 0, 0, 0, 0) }
            )
        }

        item {
            KazaCounterRow(
                title = "Akşam Namazı Kazası",
                rakats = "3 Rekat Farz",
                count = kazaRecord.maghribKaza,
                onAdd = { onUpdateKaza(0, 0, 0, 1, 0, 0, 0) },
                onMinus = { onUpdateKaza(0, 0, 0, -1, 0, 0, 0) }
            )
        }

        item {
            KazaCounterRow(
                title = "Yatsı Namazı Kazası",
                rakats = "4 Rekat Farz",
                count = kazaRecord.ishaKaza,
                onAdd = { onUpdateKaza(0, 0, 0, 0, 1, 0, 0) },
                onMinus = { onUpdateKaza(0, 0, 0, 0, -1, 0, 0) }
            )
        }

        item {
            KazaCounterRow(
                title = "Vitir Namazı Kazası",
                rakats = "3 Rekat Vacip",
                count = kazaRecord.witrKaza,
                onAdd = { onUpdateKaza(0, 0, 0, 0, 0, 1, 0) },
                onMinus = { onUpdateKaza(0, 0, 0, 0, 0, -1, 0) }
            )
        }

        item {
            KazaCounterRow(
                title = "Kaza Orucu",
                rakats = "Gün",
                count = kazaRecord.fastingKaza,
                onAdd = { onUpdateKaza(0, 0, 0, 0, 0, 0, 1) },
                onMinus = { onUpdateKaza(0, 0, 0, 0, 0, 0, -1) }
            )
        }
    }
}

@Composable
fun KazaCounterRow(
    title: String,
    rakats: String,
    count: Int,
    onAdd: () -> Unit,
    onMinus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = rakats,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(
                    onClick = onMinus,
                    enabled = count > 0,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Azalt",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.widthIn(min = 48.dp),
                    textAlign = TextAlign.Center
                )

                FilledIconButton(
                    onClick = onAdd,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Arttır",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
