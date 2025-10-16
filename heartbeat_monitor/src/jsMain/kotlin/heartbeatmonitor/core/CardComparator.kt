package heartbeatmonitor.core

interface CardComparator {
    companion object {
        val registry = mutableMapOf<String, CardComparator>()

        val currentComparator: Comparator<Card>
            get() = Comparator { a, b ->
                CardComparatorSpecifiers.currentCardComparatorSpecifiers.value.forEach { cardComparatorSpecifier ->
                    val cardComparator = registry[cardComparatorSpecifier.type as String] ?: return@forEach
                    val cmp = cardComparator.compare(cardComparatorSpecifier, a, b)
                    if (cmp != 0) return@Comparator cmp
                }
                0
            }

    }

    fun compare(specifier: dynamic, a: Card, b: Card): Int

    fun getTitle(specifier: dynamic): String

}
