package hello

import kotlinx.browser.document
import kotlinx.browser.window

object SortPlugin : AbstractPlugin("SortPlugin") {
    override suspend fun applyImpl() {
        KanbanBro.cardComparators["empty"] = jsObjectOf(
            "compare" to { _: dynamic, _: dynamic, _: dynamic -> 0 },
            "getTitle" to { _: dynamic -> "Unsorted" },
        )
        KanbanBro.cardComparators["name"] = jsObjectOf(
            "compare" to { specifier: dynamic, a: dynamic, b: dynamic ->
                val cmp = (a.keys?.name as String? ?: "").compareTo(b.keys?.name as String? ?: "")
                if (specifier.isDescending as Boolean) -cmp else cmp
            },
            "getTitle" to { specifier: dynamic -> if (specifier.isDescending as Boolean) "Name (desc)" else "Name" },
        )
        KanbanBro.cardComparators["updated"] = jsObjectOf(
            "compare" to { specifier: dynamic, a: dynamic, b: dynamic ->
                val cmp = (a.keys?.updated as Number? ?: 0.0).toDouble() - (b.keys?.updated as Number? ?: 0.0).toDouble()
                if (specifier.isDescending as Boolean) -cmp else cmp
            },
            "getTitle" to { specifier: dynamic -> if (specifier.isDescending as Boolean) "Newest Update" else "Oldest Update" },
        )

        fun getTitle(cardComparatorSpecifier: dynamic): String {
            val cardComparator = KanbanBro.cardComparators[cardComparatorSpecifier.type]
            if (cardComparator == null) return "Invalid Comparator"
            return cardComparator.getTitle(cardComparatorSpecifier) as String
        }

        val cyclerCardComparatorSpecifiers = arrayOf(
            jsObjectOf("type" to "name", "isDescending" to false),
            jsObjectOf("type" to "name", "isDescending" to true),
            jsObjectOf("type" to "updated", "isDescending" to true),
            jsObjectOf("type" to "updated", "isDescending" to false),
            jsObjectOf("type" to "empty"),
        )

        fun getNextCardComparatorSpecifier(cardComparatorSpecifier: dynamic): dynamic {
            val oldIndex = cyclerCardComparatorSpecifiers.indexOfFirst { cyclerCardComparatorSpecifier ->
                if (cyclerCardComparatorSpecifier.type != cardComparatorSpecifier.type) return@indexOfFirst false
                if (cyclerCardComparatorSpecifier.isDescending != undefined) {
                    if (cyclerCardComparatorSpecifier.isDescending != cardComparatorSpecifier.isDescending) return@indexOfFirst false
                }
                true
            }
            if (oldIndex == -1) return cyclerCardComparatorSpecifiers[0]
            return cyclerCardComparatorSpecifiers[(oldIndex + 1) % cyclerCardComparatorSpecifiers.size]
        }

        fun openSortDialog() {
            window.asDynamic().showDialog { container: dynamic, dialogEvent: dynamic ->
                container.append(
                    document.createElement("div").also { titleDiv ->
                        titleDiv.textContent = "Sort"
                        titleDiv.asDynamic().style.fontWeight = "700"
                    },
                    document.createElement("div").also { buttonsDiv ->
                        buttonsDiv.className = "dialog-container"
                        fun updateButtons() {
                            buttonsDiv.innerHTML = ""
                            (KanbanBro.cardComparatorSpecifiers as Array<dynamic>).forEachIndexed { index, cardComparatorSpecifier ->
                                buttonsDiv.append(
                                    document.createElement("div").also { buttonDiv ->
                                        buttonDiv.asDynamic().style.display = "flex"
                                        buttonDiv.asDynamic().style.gap = "12px"
                                        buttonDiv.asDynamic().style.alignItems = "center"
                                        buttonDiv.append(
                                            document.createElement("div").also { leftDiv ->
                                                leftDiv.append(
                                                    document.createElement("button").also { toggleButton ->
                                                        toggleButton.asDynamic().type = "button"
                                                        toggleButton.classList.add("dialog-button")
                                                        toggleButton.textContent = getTitle(cardComparatorSpecifier)
                                                        toggleButton.addEventListener("click", {
                                                            val cardComparatorSpecifiers = (KanbanBro.cardComparatorSpecifiers as Array<dynamic>).toMutableList()
                                                            cardComparatorSpecifiers[index] = getNextCardComparatorSpecifier(cardComparatorSpecifiers[index])
                                                            window.asDynamic().setCardComparatorSpecifiers(cardComparatorSpecifiers.toTypedArray())
                                                        })
                                                    },
                                                )
                                            },
                                            document.createElement("div").also { rightDiv ->
                                                rightDiv.asDynamic().style.marginLeft = "auto"
                                                rightDiv.append(
                                                    document.createElement("button").also { removeButton ->
                                                        removeButton.asDynamic().type = "button"
                                                        removeButton.classList.add("dialog-button")
                                                        removeButton.textContent = "🗑️"
                                                        removeButton.addEventListener("click", {
                                                            val cardComparatorSpecifiers = (KanbanBro.cardComparatorSpecifiers as Array<dynamic>).toMutableList()
                                                            cardComparatorSpecifiers.removeAt(index)
                                                            window.asDynamic().setCardComparatorSpecifiers(cardComparatorSpecifiers.toTypedArray())
                                                        })
                                                    },
                                                )
                                            },
                                        )
                                    },
                                )
                            }
                        }
                        KanbanBro.event.addEventListener("cardComparatorSpecifiersChanged", { updateButtons() })
                        updateButtons()
                    },
                    document.createElement("button").also { addButton ->
                        addButton.asDynamic().type = "button"
                        addButton.classList.add("dialog-transparent-button")
                        addButton.textContent = "＋"
                        addButton.addEventListener("click", {
                            val cardComparatorSpecifiers = (KanbanBro.cardComparatorSpecifiers as Array<dynamic>).toMutableList()
                            cardComparatorSpecifiers.add(jsObjectOf("type" to "empty"))
                            window.asDynamic().setCardComparatorSpecifiers(cardComparatorSpecifiers.toTypedArray())
                        })
                    },
                    document.createElement("div").also { actionsDiv ->
                        actionsDiv.asDynamic().style.display = "flex"
                        actionsDiv.asDynamic().style.justifyContent = "end"
                        actionsDiv.asDynamic().style.gap = "8px"
                        actionsDiv.append(
                            document.createElement("button").also { closeButton ->
                                closeButton.asDynamic().type = "button"
                                closeButton.textContent = "Close"
                                closeButton.classList.add("dialog-button")
                                closeButton.addEventListener("click", { dialogEvent.dispatchEvent(new(window.asDynamic().Event, "close")) })
                            },
                        )
                    },
                )
            }
        }

        UiContainers.topbarLeftContainer.append(
            document.createElement("div").also { container ->
                container.className = "topbar-property"
                container.append(
                    document.createElement("button").also { sortButton ->
                        sortButton.asDynamic().type = "button"
                        sortButton.textContent = "Sort"

                        fun updateButton() {
                            sortButton.textContent = when (KanbanBro.cardComparatorSpecifiers.length as Int) {
                                0 -> "Unsorted"
                                1 -> getTitle(KanbanBro.cardComparatorSpecifiers[0])
                                else -> getTitle(KanbanBro.cardComparatorSpecifiers[0]) + "+" + ((KanbanBro.cardComparatorSpecifiers.length as Int) - 1)
                            }
                        }
                        KanbanBro.event.addEventListener("cardComparatorSpecifiersChanged", { updateButton() })
                        updateButton()

                        sortButton.addEventListener("click", { openSortDialog() })
                    },
                )
            },
        )
    }
}
