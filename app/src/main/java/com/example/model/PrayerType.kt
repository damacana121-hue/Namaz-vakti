package com.example.model

enum class PrayerType(
    val titleTr: String,
    val arabicName: String,
    val description: String,
    val rakatTotal: Int
) {
    FAJR("İmsak", "الفجر", "Sabah namazı vakti başlangıcı", 4),
    SUNRISE("Güneş", "الشروق", "Güneş doğuşu, kerahet vakti başlangıcı", 0),
    DHUHR("Öğle", "الظهر", "Öğle namazı vakti", 10),
    ASR("İkindi", "العصر", "İkindi namazı vakti", 8),
    MAGHRIB("Akşam", "المغرب", "Akşam namazı vakti ve iftar", 5),
    ISHA("Yatsı", "العشاء", "Yatsı ve Vitir namazı vakti", 13)
}
