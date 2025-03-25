import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Rozšířená třída pro sledování nikotinových sáčků
class NicotineTracker(
    private var dailyLimit: Int = 10
) {
    // Rozšířené datové struktury pro ukládání detailnějších informací
    data class PouchEntry(
        val timestamp: LocalDateTime,
        val brandName: String,
        val nicotineStrength: Int,
        val notes: String? = null
    )

    // Ukládání historie s více detaily
    private val pouchHistory = mutableListOf<PouchEntry>()
    private var remaining = dailyLimit
    private val historicalData = mutableMapOf<LocalDateTime, Int>()

    // Rozšířená metoda pro přidání sáčku s dalšími parametry
    fun addPouch(
        brandName: String = "Neuvedeno", 
        nicotineStrength: Int = 0, 
        notes: String? = null
    ) {
        if (remaining > 0) {
            val currentTime = LocalDateTime.now()
            
            // Přidání detailního záznamu
            val pouchEntry = PouchEntry(
                timestamp = currentTime,
                brandName = brandName,
                nicotineStrength = nicotineStrength,
                notes = notes
            )
            pouchHistory.add(pouchEntry)
            
            // Sledování spotřeby po dnech
            val currentDate = currentTime.toLocalDate()
            historicalData[currentDate] = (historicalData[currentDate] ?: 0) + 1

            remaining--
            val record = "Spotřeboval jsi 1 sáček (${brandName}), zbývá: $remaining"
            println(record)

            // Implementace adaptivních upozornění
            when {
                remaining <= 3 -> println("Upozornění: Zbývá jen $remaining sáčků!")
                remaining == dailyLimit / 2 -> println("Tip: Jsi v polovině denního limitu.")
            }
        } else {
            println("Denní limit vyčerpán!")
        }
    }

    // Pokročilé nastavení limitu s validací
    fun setLimit(newLimit: Int) {
        require(newLimit > 0) { "Limit musí být kladné číslo!" }
        
        dailyLimit = newLimit
        remaining = dailyLimit
        println("Denní limit nastaven na $dailyLimit sáčků.")
    }

    // Rozšířená metoda historie s možností filtrování
    fun showHistory(
        daysBack: Long = 7,
        filterBrand: String? = null
    ) {
        val cutoffDate = LocalDateTime.now().minusDays(daysBack)
        
        val filteredHistory = pouchHistory
            .filter { it.timestamp.isAfter(cutoffDate) }
            .filter { filterBrand == null || it.brandName == filterBrand }

        println("Historie spotřeby za posledních $daysBack dní:")
        if (filteredHistory.isEmpty()) {
            println("Žádná historie.")
        } else {
            filteredHistory.forEach { entry ->
                println("${entry.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}: " +
                        "${entry.brandName} (${entry.nicotineStrength} mg), " +
                        "Poznámky: ${entry.notes ?: 'Žádné'}")
            }
        }
    }

    // Analytické metody
    fun getConsumptionTrends(): Map<LocalDateTime, Int> {
        return historicalData
    }

    fun calculateAverageNicotineStrength(): Double {
        return if (pouchHistory.isNotEmpty()) {
            pouchHistory.map { it.nicotineStrength }.average()
        } else {
            0.0
        }
    }

    // Predikce a doporučení
    fun recommendOptimalLimit(): Int {
        val averageConsumption = pouchHistory.size.toDouble() / 30 // průměr za měsíc
        return (averageConsumption + 1).toInt()
    }

    // Gamifikace - systém úspěchů
    private val achievements = mutableSetOf<String>()

    fun checkAchievements() {
        // Příklady úspěchů
        if (pouchHistory.size >= 100) achievements.add("Konzument")
        if (pouchHistory.none { it.nicotineStrength > 10 }) achievements.add("Opatrný")
        
        println("Dosažené úspěchy: $achievements")
    }
}

// Rozšíření hlavní funkce pro interakci
fun main() {
    val tracker = NicotineTracker()

    while (true) {
        println("\n=======================================")
        println("Nabídka:")
        println("1. Přidat spotřebu sáčku")
        println("2. Nastavit denní limit")
        println("3. Zobrazit historii")
        println("4. Analytika a doporučení")
        println("5. Úspěchy")
        println("6. Ukončit program")
        print("Vyber možnost (1-6): ")

        when (readLine()) {
            "1" -> {
                print("Název značky: ")
                val brand = readLine() ?: "Neuvedeno"
                print("Síla nikotinu (mg): ")
                val strength = readLine()?.toIntOrNull() ?: 0
                print("Poznámky (volitelné): ")
                val notes = readLine()
                tracker.addPouch(brand, strength, notes)
            }
            "2" -> {
                print("Zadej nový denní limit: ")
                val newLimit = readLine()?.toIntOrNull()
                if (newLimit != null) {
                    tracker.setLimit(newLimit)
                } else {
                    println("Neplatný vstup!")
                }
            }
            "3" -> {
                print("Zobrazit historii za kolik dní zpět? (výchozí 7): ")
                val days = readLine()?.toLongOrNull() ?: 7
                print("Filtrovat podle značky? (volitelné): ")
                val brand = readLine()?.takeIf { it.isNotBlank() }
                tracker.showHistory(days, brand)
            }
            "4" -> {
                println("Průměrná síla nikotinu: ${tracker.calculateAverageNicotineStrength()} mg")
                println("Doporučený denní limit: ${tracker.recommendOptimalLimit()} sáčků")
            }
            "5" -> tracker.checkAchievements()
            "6" -> {
                println("Ukončuji program.")
                return
            }
            else -> {
                println("Neplatná volba, zkus to znovu.")
            }
        }
    }
}