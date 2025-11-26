package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.Card
import heartbeatmonitor.core.CardComparator
import heartbeatmonitor.core.CardComparatorSpecifier
import heartbeatmonitor.core.CardComparatorSpecifiers
import heartbeatmonitor.core.UiContainers
import heartbeatmonitor.core.closeButton
import heartbeatmonitor.core.container
import heartbeatmonitor.core.frame
import heartbeatmonitor.core.get
import heartbeatmonitor.core.leftRight
import heartbeatmonitor.core.onClick
import heartbeatmonitor.core.right
import heartbeatmonitor.core.showDialog
import heartbeatmonitor.core.textButton
import heartbeatmonitor.core.textTransparentButton
import heartbeatmonitor.core.title
import heartbeatmonitor.core.type
import heartbeatmonitor.util.createButtonElement
import heartbeatmonitor.util.createDivElement
import kotlinx.browser.document
import mirrg.kotlin.event.subscribe
import mirrg.kotlin.event.toSubscriber
import kotlin.math.sign

object SortPlugin : AbstractPlugin("SortPlugin") {
    override suspend fun apply() {
        CardComparator.registry["empty"] = object : CardComparator {
            override fun compare(specifier: CardComparatorSpecifier, a: Card, b: Card) = 0
            override fun getTitle(specifier: CardComparatorSpecifier) = "Unsorted"
        }
        CardComparator.registry["name"] = object : CardComparator {
            override fun compare(specifier: CardComparatorSpecifier, a: Card, b: Card): Int {
                val cmp = (a.keys["name"] as String? ?: "").compareTo(b.keys["name"] as String? ?: "")
                return if (specifier["isDescending"] as Boolean) -cmp else cmp
            }

            override fun getTitle(specifier: CardComparatorSpecifier) = if (specifier["isDescending"] as Boolean) "Name (desc)" else "Name"
        }
        CardComparator.registry["updated"] = object : CardComparator {
            override fun compare(specifier: CardComparatorSpecifier, a: Card, b: Card): Int {
                val cmp = (a.keys["updated"] as Number? ?: 0.0).toDouble() - (b.keys["updated"] as Number? ?: 0.0).toDouble()
                return if (specifier["isDescending"] as Boolean) -cmp.sign.toInt() else cmp.sign.toInt()
            }

            override fun getTitle(specifier: CardComparatorSpecifier) = if (specifier["isDescending"] as Boolean) "Newest Update" else "Oldest Update"
        }

        fun getTitle(cardComparatorSpecifier: CardComparatorSpecifier): String {
            val cardComparator = CardComparator.registry[cardComparatorSpecifier.type]
            if (cardComparator == null) return "Invalid Comparator"
            return cardComparator.getTitle(cardComparatorSpecifier)
        }

        val cyclerCardComparatorSpecifiers = listOf(
            CardComparatorSpecifier("type" to "name", "isDescending" to false),
            CardComparatorSpecifier("type" to "name", "isDescending" to true),
            CardComparatorSpecifier("type" to "updated", "isDescending" to true),
            CardComparatorSpecifier("type" to "updated", "isDescending" to false),
            CardComparatorSpecifier("type" to "empty"),
        )

        fun getNextCardComparatorSpecifier(cardComparatorSpecifier: CardComparatorSpecifier): CardComparatorSpecifier {
            val oldIndex = cyclerCardComparatorSpecifiers.indexOf(cardComparatorSpecifier)
            if (oldIndex == -1) return cyclerCardComparatorSpecifiers[0]
            return cyclerCardComparatorSpecifiers[(oldIndex + 1) % cyclerCardComparatorSpecifiers.size]
        }

        fun openSortDialog() {
            showDialog {
                frame {
                    container {
                        title("Sort")
                        container {
                            fun updateButtons() {
                                innerHTML = ""
                                CardComparatorSpecifiers.currentCardComparatorSpecifiers.value.forEachIndexed { index, cardComparatorSpecifier ->
                                    leftRight({
                                        textButton(getTitle(cardComparatorSpecifier)) {
                                            onClick {
                                                val cardComparatorSpecifiers = CardComparatorSpecifiers.currentCardComparatorSpecifiers.value.toMutableList()
                                                cardComparatorSpecifiers[index] = getNextCardComparatorSpecifier(cardComparatorSpecifiers[index])
                                                CardComparatorSpecifiers.currentCardComparatorSpecifiers.value = cardComparatorSpecifiers
                                            }
                                        }
                                    }, {
                                        textButton("🗑️") {
                                            onClick {
                                                val cardComparatorSpecifiers = CardComparatorSpecifiers.currentCardComparatorSpecifiers.value.toMutableList()
                                                cardComparatorSpecifiers.removeAt(index)
                                                CardComparatorSpecifiers.currentCardComparatorSpecifiers.value = cardComparatorSpecifiers
                                            }
                                        }
                                    })
                                }
                            }

                            CardComparatorSpecifiers.currentCardComparatorSpecifiers.subscribe(onClosed.toSubscriber()) {
                                updateButtons()
                            }
                            updateButtons()
                        }
                        textTransparentButton("＋") {
                            onClick {
                                val cardComparatorSpecifiers = CardComparatorSpecifiers.currentCardComparatorSpecifiers.value.toMutableList()
                                cardComparatorSpecifiers.add(CardComparatorSpecifier("type" to "empty"))
                                CardComparatorSpecifiers.currentCardComparatorSpecifiers.value = cardComparatorSpecifiers
                            }
                        }
                        right {
                            closeButton()
                        }
                    }
                }
            }
        }

        UiContainers.topbarLeftContainer.append(
            document.createDivElement().also { container ->
                container.className = "topbar-property"
                container.append(
                    document.createButtonElement().also { sortButton ->
                        sortButton.type = "button"
                        sortButton.textContent = "Sort"

                        fun updateButton() {
                            sortButton.textContent = when (CardComparatorSpecifiers.currentCardComparatorSpecifiers.value.size) {
                                0 -> "Unsorted"
                                1 -> getTitle(CardComparatorSpecifiers.currentCardComparatorSpecifiers.value[0])
                                else -> getTitle(CardComparatorSpecifiers.currentCardComparatorSpecifiers.value[0]) + "+" + (CardComparatorSpecifiers.currentCardComparatorSpecifiers.value.size - 1)
                            }
                        }
                        CardComparatorSpecifiers.currentCardComparatorSpecifiers.register { updateButton() }
                        updateButton()

                        sortButton.addEventListener("click", { openSortDialog() })
                    },
                )
            },
        )
    }
}
