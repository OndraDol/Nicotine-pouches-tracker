fun main() {
    val tracker = NicotineTracker() // Vytvoříme tracker s výchozím limitem 10

    while (true) {
        println("=======================================")
        println("Nabídka:")
        println("1. Přidat spotřebu sáčku")
        println("2. Nastavit denní limit")
        println("3. Zobrazit historii")
        println("4. Ukončit program")
        print("Vyber možnost (1-4): ")

        when (readLine()) {
            "1" -> {
                // Přidat sáček
                tracker.addPouch()
            }
            "2" -> {
                // Nastavit denní limit
                print("Zadej nový denní limit: ")
                val newLimit = readLine()?.toIntOrNull()
                if (newLimit != null) {
                    tracker.setLimit(newLimit)
                } else {
                    println("Neplatný vstup!")
                }
            }
            "3" -> {
                // Zobrazit historii
                tracker.showHistory()
            }
            "4" -> {
                // Ukončit program
                println("Ukončuji program.")
                return
            }
            else -> {
                println("Neplatná volba, zkus to znovu.")
            }
        }
        println()
    }
}