package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CityLocation
import com.example.model.TurkishCities
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showCityDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val userSettings = uiState.userSettings

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("settings_screen_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Text(
                text = "Uygulama Ayarları",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold
            )
        }

        // 1. Konum & Şehir Seçimi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Konum ve Hesaplama",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ListItem(
                        headlineContent = { Text("Seçili Şehir") },
                        supportingContent = { Text("${uiState.selectedCity.name}, ${uiState.selectedCity.country} (Diyanet Standart)") },
                        leadingContent = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .clickable { showCityDialog = true }
                            .testTag("change_city_setting_item")
                    )
                }
            }
        }

        // 2. Gelişmiş Bildirim & Ezan Ayarları
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ezan ve Bildirimler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsSwitchRow(
                        title = "Sabah (İmsak) Bildirimi",
                        checked = userSettings.notifFajr,
                        onCheckedChange = { viewModel.updateNotificationToggle(fajr = it) }
                    )

                    SettingsSwitchRow(
                        title = "Güneş Doğuşu Bildirimi",
                        checked = userSettings.notifSunrise,
                        onCheckedChange = { viewModel.updateNotificationToggle(sunrise = it) }
                    )

                    SettingsSwitchRow(
                        title = "Öğle Ezanı Bildirimi",
                        checked = userSettings.notifDhuhr,
                        onCheckedChange = { viewModel.updateNotificationToggle(dhuhr = it) }
                    )

                    SettingsSwitchRow(
                        title = "İkindi Ezanı Bildirimi",
                        checked = userSettings.notifAsr,
                        onCheckedChange = { viewModel.updateNotificationToggle(asr = it) }
                    )

                    SettingsSwitchRow(
                        title = "Akşam Ezanı Bildirimi",
                        checked = userSettings.notifMaghrib,
                        onCheckedChange = { viewModel.updateNotificationToggle(maghrib = it) }
                    )

                    SettingsSwitchRow(
                        title = "Yatsı Ezanı Bildirimi",
                        checked = userSettings.notifIsha,
                        onCheckedChange = { viewModel.updateNotificationToggle(isha = it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingsSwitchRow(
                        title = "15 Dakika Önceden Hatırlat",
                        checked = userSettings.earlyReminder15Min,
                        onCheckedChange = { viewModel.updateNotificationToggle(early15 = it) }
                    )

                    ListItem(
                        headlineContent = { Text("Uyarı Sesi Türü") },
                        supportingContent = { Text(userSettings.soundType) },
                        leadingContent = { Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { showSoundDialog = true }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.sendTestNotification() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_notification_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Bildirimi Gönder", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Kişiselleştirilebilir Ana Sayfa
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kişiselleştirilebilir Ana Sayfa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Ana ekranda görüntülemek istediğiniz kartları seçiniz:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsSwitchRow(
                        title = "Günün Ayeti ve Hadisi Kartı",
                        checked = userSettings.showAyahHadithWidget,
                        onCheckedChange = { viewModel.updateWidgetVisibility(ayah = it) }
                    )

                    SettingsSwitchRow(
                        title = "Hızlı Zikirmatik Kartı",
                        checked = userSettings.showFastDhikrWidget,
                        onCheckedChange = { viewModel.updateWidgetVisibility(dhikr = it) }
                    )

                    SettingsSwitchRow(
                        title = "Kur'an Okuma İlerlemesi Kartı",
                        checked = userSettings.showQuranProgressWidget,
                        onCheckedChange = { viewModel.updateWidgetVisibility(quran = it) }
                    )

                    SettingsSwitchRow(
                        title = "Kaza Namazı Durumu Kartı",
                        checked = userSettings.showKazaWidget,
                        onCheckedChange = { viewModel.updateWidgetVisibility(kaza = it) }
                    )
                }
            }
        }

        // 4. Görünüm & Karanlık Mod
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Görünüm & Tema",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ListItem(
                        headlineContent = { Text("Tema Modu") },
                        supportingContent = { Text(userSettings.themeMode) },
                        leadingContent = { Icon(Icons.Filled.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { showThemeDialog = true }
                    )
                }
            }
        }

        // 5. Çevrimdışı Veri ve Hakkında
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudDone,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Çevrimdışı Senkronizasyon Aktif",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tüm namaz vakitleri, zikirler, kaza kayıtları ve Kur'an takibi Room veritabanında internet olmadan güvenle saklanır.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // City Selection Dialog
    if (showCityDialog) {
        CitySelectionDialog(
            currentCity = uiState.selectedCity,
            onCitySelected = { city ->
                viewModel.selectCity(city)
                showCityDialog = false
            },
            onDismiss = { showCityDialog = false }
        )
    }

    // Sound Selection Dialog
    if (showSoundDialog) {
        val soundOptions = listOf("Ezan", "Bip / Sesli", "Sadece Titreşim", "Sessiz")
        AlertDialog(
            onDismissRequest = { showSoundDialog = false },
            title = { Text("Bildirim Sesi Seçin") },
            text = {
                Column {
                    soundOptions.forEach { sound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateNotificationToggle(sound = sound)
                                    showSoundDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = userSettings.soundType == sound,
                                onClick = {
                                    viewModel.updateNotificationToggle(sound = sound)
                                    showSoundDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(sound, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        val themeOptions = listOf("Sistem", "Karanlık", "Aydınlık")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Tema Seçin") },
            text = {
                Column {
                    themeOptions.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = userSettings.themeMode == theme,
                                onClick = {
                                    viewModel.updateTheme(theme)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun CitySelectionDialog(
    currentCity: CityLocation,
    onCitySelected: (CityLocation) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = remember(searchQuery) {
        TurkishCities.list.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.country.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Şehir Seçin (81 İl & Dünya)") },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Şehir ara...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filteredCities) { city ->
                        val isSelected = city.name == currentCity.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCitySelected(city) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = city.country,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}
