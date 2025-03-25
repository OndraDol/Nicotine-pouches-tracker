package com.example.nicotinetracker.utils

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nicotinetracker.data.Brand
import com.example.nicotinetracker.data.Pouch
import com.example.nicotinetracker.data.PouchDatabase
import com.example.nicotinetracker.data.PouchType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DataAnalyzerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PouchDatabase
    private lateinit var dataAnalyzer: DataAnalyzer
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context, 
            PouchDatabase::class.java
        ).allowMainThreadQueries().build()

        dataAnalyzer = DataAnalyzer(database)

        // Příprava testovacích dat
        runBlocking {
            val brandDao = database.brandDao()
            val pouchTypeDao = database.pouchTypeDao()
            val pouchDao = database.pouchDao()

            // Přidání testovacích značek
            val brand1 = Brand(name = "TestBrand1")
            val brand1Id = brandDao.insert(brand1)
            val brand2 = Brand(name = "TestBrand2")
            val brand2Id = brandDao.insert(brand2)

            // Přidání testovacích typů sáčků
            val pouchType1 = PouchType(name = "TestType1", nicotineStrength = 6f)
            val pouchType1Id = pouchTypeDao.insert(pouchType1)
            val pouchType2 = PouchType(name = "TestType2", nicotineStrength = 8f)
            val pouchType2Id = pouchTypeDao.insert(pouchType2)

            // Přidání testovacích sáčků pro různé dny a měsíce
            val baseTimestamp = System.currentTimeMillis()
            val pouches = listOf(
                Pouch(timestamp = baseTimestamp, brandId = brand1Id, pouchTypeId = pouchType1Id, nicotineStrength = 6f, weight = 0.7f, note = "Pouch 1"),
                Pouch(timestamp = baseTimestamp + 24*60*60*1000, brandId = brand1Id, pouchTypeId = pouchType1Id, nicotineStrength = 6f, weight = 0.7f, note = "Pouch 2"),
                Pouch(timestamp = baseTimestamp + 2*24*60*60*1000, brandId = brand2Id, pouchTypeId = pouchType2Id, nicotineStrength = 8f, weight = 0.8f, note = "Pouch 3"),
                Pouch(timestamp = baseTimestamp + 30*24*60*60*1000L, brandId = brand2Id, pouchTypeId = pouchType2Id, nicotineStrength = 8f, weight = 0.8f, note = "Pouch 4")
            )

            pouches.forEach { pouchDao.insert(it) }
        }
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun calculateAverageDailyNicotineIntake_calculatesCorrectly() = runBlocking {
        val averageNicotineIntake = dataAnalyzer.calculateAverageDailyNicotineIntake()
        
        // Očekávaný výpočet: ((6 * 0.7) + (6 * 0.7) + (8 * 0.8) + (8 * 0.8)) / 4 dny
        val expectedAverage = ((6 * 0.7) + (6 * 0.7) + (8 * 0.8) + (8 * 0.8)) / 4.0
        
        assertEquals(expectedAverage, averageNicotineIntake, 0.001)
    }

    @Test
    fun getMonthlyConsumptionTrend_calculatesCorrectly() = runBlocking {
        val monthlyTrend = dataAnalyzer.getMonthlyConsumptionTrend()
        
        assertEquals(2, monthlyTrend.size)
        
        // První měsíc by měl mít 3 sáčky
        val firstMonth = monthlyTrend[0]
        assertEquals(3, firstMonth.totalPouches)
        
        // Druhý měsíc by měl mít 1 sáček
        val secondMonth = monthlyTrend[1]
        assertEquals(1, secondMonth.totalPouches)
    }

    @Test
    fun calculateProgressScore_calculatesCorrectly() = runBlocking {
        val dailyLimit = 2  // Nastavení denního limitu na 2 sáčky
        val progressScore = dataAnalyzer.calculateProgressScore(dailyLimit)
        
        // Testovací data mají 3 dny s více než 2 sáčky
        assertEquals(1, progressScore.daysOverLimit)
        assertEquals(4, progressScore.totalTrackedDays)
        
        // Přesný výpočet compliance
        val expectedCompliancePercentage = ((4 - 1).toDouble() / 4) * 100
        assertEquals(expectedCompliancePercentage, progressScore.compliancePercentage, 0.001)
    }

    @Test
    fun getFavoriteProducts_findsCorrectProducts() = runBlocking {
        val favoriteProducts = dataAnalyzer.getFavoriteProducts()
        
        // Top značky
        assertEquals(2, favoriteProducts.topBrands.size)
        assertEquals("TestBrand1", favoriteProducts.topBrands[0].name)
        assertEquals(2, favoriteProducts.topBrands[0].count)
        
        // Top typy sáčků
        assertEquals(2, favoriteProducts.topPouchTypes.size)
        assertEquals("TestType2", favoriteProducts.topPouchTypes[0].name)
        assertEquals(2, favoriteProducts.topPouchTypes[0].count)
    }
}
