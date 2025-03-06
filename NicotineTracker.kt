class NicotineTracker(
    private var dailyLimit: Int = 10
) {
    private var remaining = dailyLimit
    private val history = mutableListOf<String>()

    fun addPouch() {
        if (remaining > 0) {
            remaining--
            val record = "Spotřeboval jsi 1 sáček, zbývá: $remaining"
            history.add(record)
            println(record)
            if (remaining <= 3) {
                println("Upozornění: Zbývá jen $remaining sáčků!")
            }
        } else {
            println("Denní limit vyčerpán!")
        }
    }

    fun setLimit(newLimit: Int) {
        if (newLimit > 0) {
            dailyLimit = newLimit
            remaining = dailyLimit
            println("Denní limit nastaven na $dailyLimit sáčků.")
        } else {
            println("Neplatný limit!")
        }
    }

    fun showHistory() {
        println("Historie spotřeby:")
        if (history.isEmpty()) {
            println("Žádná historie.")
        } else {
            history.forEach { println(it) }
        }
    }
}