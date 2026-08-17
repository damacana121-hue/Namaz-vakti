package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DhikrDao
import com.example.data.local.dao.KazaRecordDao
import com.example.data.local.dao.PrayerRecordDao
import com.example.data.local.dao.QuranProgressDao
import com.example.data.local.entity.DhikrEntity
import com.example.data.local.entity.KazaRecordEntity
import com.example.data.local.entity.PrayerRecordEntity
import com.example.data.local.entity.QuranProgressEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PrayerRecordEntity::class,
        QuranProgressEntity::class,
        DhikrEntity::class,
        KazaRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun prayerRecordDao(): PrayerRecordDao
    abstract fun quranProgressDao(): QuranProgressDao
    abstract fun dhikrDao(): DhikrDao
    abstract fun kazaRecordDao(): KazaRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "namaz_vakti_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val dhikrs = listOf(
                DhikrEntity(
                    title = "Sübhânallâh",
                    arabicText = "سُبْحَانَ اللَّهِ",
                    turkishMeaning = "Allah her türlü eksiklikten ve noksanlıktan münezzehtir.",
                    targetCount = 33,
                    category = "Namaz Tesbihatı"
                ),
                DhikrEntity(
                    title = "Elhamdülillâh",
                    arabicText = "الْحَمْدُ لِلَّهِ",
                    turkishMeaning = "Hamd ve şükür yalnızca Allah'a mahsustur.",
                    targetCount = 33,
                    category = "Namaz Tesbihatı"
                ),
                DhikrEntity(
                    title = "Allâhu Ekber",
                    arabicText = "اللَّهُ أَكْبَرُ",
                    turkishMeaning = "Allah en büyüktür, yüceler yücesidir.",
                    targetCount = 33,
                    category = "Namaz Tesbihatı"
                ),
                DhikrEntity(
                    title = "Lâ ilâhe illallâh",
                    arabicText = "لَا إِلَهَ إِلَّا اللَّهُ",
                    turkishMeaning = "Allah'tan başka hiçbir ilah yoktur.",
                    targetCount = 100,
                    category = "Tevhid"
                ),
                DhikrEntity(
                    title = "Estağfirullâh el-Azîm",
                    arabicText = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ",
                    turkishMeaning = "Yüce Allah'tan bağışlanma ve mağfiret dilerim.",
                    targetCount = 100,
                    category = "İstiğfar"
                ),
                DhikrEntity(
                    title = "Salavât-ı Şerîfe",
                    arabicText = "اللَّهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ",
                    turkishMeaning = "Allah'ım! Efendimiz Hz. Muhammed'e ve âline salât ve selâm eyle.",
                    targetCount = 100,
                    category = "Salavat"
                ),
                DhikrEntity(
                    title = "Lâ havle velâ kuvvete illâ billâh",
                    arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                    turkishMeaning = "Güç ve kuvvet ancak yüce Allah'ın yardımıyladır.",
                    targetCount = 33,
                    category = "Tevekkül"
                )
            )
            database.dhikrDao().insertAll(dhikrs)

            database.kazaRecordDao().insertOrUpdate(
                KazaRecordEntity(
                    id = 1,
                    fajrKaza = 0,
                    dhuhrKaza = 0,
                    asrKaza = 0,
                    maghribKaza = 0,
                    ishaKaza = 0,
                    witrKaza = 0,
                    fastingKaza = 0
                )
            )

            // Seed initial quran progress
            database.quranProgressDao().insertProgress(
                QuranProgressEntity(
                    date = "2026-08-17",
                    lastPageRead = 1,
                    lastJuz = 1,
                    lastSurah = "Fâtiha",
                    pagesReadToday = 0
                )
            )
        }
    }
}
