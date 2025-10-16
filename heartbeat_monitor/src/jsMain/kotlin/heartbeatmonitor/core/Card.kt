package heartbeatmonitor.core

import KanbanBro
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.w3c.dom.asList
import kotlin.coroutines.EmptyCoroutineContext

class Card(
    val keys: Map<String, dynamic>,
    val image: dynamic,
    val alerts: List<Alert>,
    val texts: List<dynamic>,
) {
    companion object {

        private var currentUpdateCoroutineScope: CoroutineScope? = null

        fun scheduleUpdate() {
            console.log("[scheduleUpdate] Scheduling update")

            if (currentUpdateCoroutineScope != null) {
                currentUpdateCoroutineScope!!.cancel("Aborted by new update")
                currentUpdateCoroutineScope = null
            }
            val coroutineScope = CoroutineScope(EmptyCoroutineContext)
            currentUpdateCoroutineScope = coroutineScope

            UiContainers.cards.innerHTML = ""
            CardProvider.currentCardProviders.flatMap { provider -> provider.generate(coroutineScope) }.forEach { cardDeferred ->
                coroutineScope.launch {
                    val card = cardDeferred.await()

                    yield()

                    val cardContainer = UiContainers.cards

                    val refNode = cardContainer.children.asList().firstOrNull { child ->
                        CardComparator.currentComparator.compare(child.asDynamic().card, card) > 0
                    }

                    cardContainer.insertBefore(
                        document.createElement("div").also { cardDiv ->
                            cardDiv.className = "card"
                            cardDiv.asDynamic().card = card

                            cardDiv.append(
                                document.createElement("div").also { screenshotDiv ->
                                    screenshotDiv.className = "screenshot"
                                    screenshotDiv.append(card.image)

                                    if (card.alerts.isNotEmpty()) {
                                        cardDiv.classList.add("yellow-alert")
                                        screenshotDiv.classList.add("yellow-alert")
                                        if (card.alerts.any { a -> a.level === 2 }) {
                                            cardDiv.classList.add("red-alert")
                                        }

                                        screenshotDiv.append(
                                            document.createElement("div").also { alertsDiv ->
                                                alertsDiv.className = "alerts"
                                                card.alerts.forEach { alert ->
                                                    alertsDiv.append(
                                                        document.createElement("div").also { alertDiv ->
                                                            alertDiv.className = "alert alert-${alert.level}"
                                                            alertDiv.append(alert.message)
                                                        },
                                                    )
                                                }
                                            },
                                        )
                                    }
                                },
                                document.createElement("div").also { textsDiv ->
                                    textsDiv.className = "texts"
                                    textsDiv.append(
                                        document.createElement("div").also { textDiv ->
                                            card.texts.forEach { text ->
                                                textDiv.append(text)
                                            }
                                        },
                                    )
                                },
                            )
                        },
                        refNode,
                    )
                }
            }
        }

        fun init() {
            KanbanBro.event.addEventListener("pluginLoaded", { scheduleUpdate() })
        }

    }

    class Alert(val message: dynamic, val level: Int)

}
