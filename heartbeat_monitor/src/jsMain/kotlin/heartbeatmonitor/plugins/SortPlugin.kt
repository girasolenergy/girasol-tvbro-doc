package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.Card
import heartbeatmonitor.core.CardComparator
import heartbeatmonitor.core.CardComparatorSpecifier
import heartbeatmonitor.core.CardComparatorSpecifiers
import heartbeatmonitor.core.UiContainers
import heartbeatmonitor.core.get
import heartbeatmonitor.core.showDialog
import heartbeatmonitor.core.type
import heartbeatmonitor.util.createButtonElement
import heartbeatmonitor.util.createDivElement
import heartbeatmonitor.util.gap
import heartbeatmonitor.util.jsObjectOf
import kotlinx.browser.document
import mirrg.kotlin.event.emit
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

        val cyclerCardComparatorSpecifiers = arrayOf(
            jsObjectOf("type" to "name", "isDescending" to false),
            jsObjectOf("type" to "name", "isDescending" to true),
            jsObjectOf("type" to "updated", "isDescending" to true),
            jsObjectOf("type" to "updated", "isDescending" to false),
            jsObjectOf("type" to "empty"),
        )

        fun getNextCardComparatorSpecifier(cardComparatorSpecifier: CardComparatorSpecifier): CardComparatorSpecifier {
            val oldIndex = cyclerCardComparatorSpecifiers.indexOfFirst { cyclerCardComparatorSpecifier ->
                if (cyclerCardComparatorSpecifier.type != cardComparatorSpecifier.type) return@indexOfFirst false
                if (cyclerCardComparatorSpecifier.isDescending != undefined) {
                    if (cyclerCardComparatorSpecifier.isDescending != cardComparatorSpecifier["isDescending"] as Boolean) return@indexOfFirst false
                }
                true
            }
            if (oldIndex == -1) return cyclerCardComparatorSpecifiers[0]
            return cyclerCardComparatorSpecifiers[(oldIndex + 1) % cyclerCardComparatorSpecifiers.size]
        }

        fun openSortDialog() {
            showDialog { container, onClosed ->
                container.append(
                    document.createDivElement().also { titleDiv ->
                        titleDiv.textContent = "Sort"
                        titleDiv.style.fontWeight = "700"
                    },
                    document.createDivElement().also { buttonsDiv ->
                        buttonsDiv.className = "dialog-container"
                        fun updateButtons() {
                            buttonsDiv.innerHTML = ""
                            CardComparatorSpecifiers.currentCardComparatorSpecifiers.value.forEachIndexed { index, cardComparatorSpecifier ->
                                buttonsDiv.append(
                                    document.createDivElement().also { buttonDiv ->
                                        buttonDiv.style.display = "flex"
                                        buttonDiv.style.gap = "12px"
                                        buttonDiv.style.alignItems = "center"
                                        buttonDiv.append(
                                            document.createDivElement().also { leftDiv ->
                                                leftDiv.append(
                                                    document.createButtonElement().also { toggleButton ->
                                                        toggleButton.type = "button"
                                                        toggleButton.classList.add("dialog-button")
                                                        toggleButton.textContent = getTitle(cardComparatorSpecifier)
                                                        toggleButton.addEventListener("click", {
                                                            val cardComparatorSpecifiers = CardComparatorSpecifiers.currentCardComparatorSpecifiers.value.toMutableList()
                                                            cardComparatorSpecifiers[index] = getNextCardComparatorSpecifier(cardComparatorSpecifiers[index])
                                                            CardComparatorSpecifiers.currentCardComparatorSpecifiers.value = cardComparatorSpecifiers
                                                        })
                                                    },
                                                )
                                            },
                                            document.createDivElement().also { rightDiv ->
                                                rightDiv.style.marginLeft = "auto"
                                                rightDiv.append(
                                                    document.createButtonElement().also { removeButton ->
                                                        removeButton.type = "button"
                                                        removeButton.classList.add("dialog-button")
                                                        removeButton.textContent = "🗑️"
                                                        removeButton.addEventListener("click", {
                                                            val cardComparatorSpecifiers = CardComparatorSpecifiers.currentCardComparatorSpecifiers.value.toMutableList()
                                                            cardComparatorSpecifiers.removeAt(index)
                                                            CardComparatorSpecifiers.currentCardComparatorSpecifiers.value = cardComparatorSpecifiers
                                                        })
                                                    },
                                                )
                                            },
                                        )
                                    },
                                )
                            }
                        }

                        CardComparatorSpecifiers.currentCardComparatorSpecifiers.subscribe(onClosed.toSubscriber()) {
                            updateButtons()
                        }
                        updateButtons()
                    },
                    document.createButtonElement().also { addButton ->
                        addButton.type = "button"
                        addButton.classList.add("dialog-transparent-button")
                        addButton.textContent = "＋"
                        addButton.addEventListener("click", {
                            val cardComparatorSpecifiers = CardComparatorSpecifiers.currentCardComparatorSpecifiers.value.toMutableList()
                            cardComparatorSpecifiers.add(jsObjectOf("type" to "empty"))
                            CardComparatorSpecifiers.currentCardComparatorSpecifiers.value = cardComparatorSpecifiers
                        })
                    },
                    document.createDivElement().also { actionsDiv ->
                        actionsDiv.style.display = "flex"
                        actionsDiv.style.justifyContent = "end"
                        actionsDiv.style.gap = "8px"
                        actionsDiv.append(
                            document.createButtonElement().also { closeButton ->
                                closeButton.type = "button"
                                closeButton.textContent = "Close"
                                closeButton.classList.add("dialog-button")
                                closeButton.addEventListener("click", { onClosed.emit() })
                            },
                        )
                    },
                )
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
