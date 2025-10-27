package heartbeatmonitor.core

import heartbeatmonitor.util.createDivElement
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import onPluginLoaded
import org.w3c.dom.Document
import org.w3c.dom.HTMLDivElement
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

                    val refNode = UiContainers.cards.cardElements.firstOrNull { child ->
                        CardComparator.currentComparator.compare(child.card, card) > 0
                    }

                    UiContainers.cards.insertBefore(
                        document.createCardElement().also { cardDiv ->
                            cardDiv.className = "card"
                            cardDiv.card = card

                            cardDiv.append(
                                document.createDivElement().also { screenshotDiv ->
                                    screenshotDiv.className = "screenshot"
                                    screenshotDiv.append(card.image)

                                    if (card.alerts.isNotEmpty()) {
                                        cardDiv.classList.add("yellow-alert")
                                        screenshotDiv.classList.add("yellow-alert")
                                        if (card.alerts.any { a -> a.level === 2 }) {
                                            cardDiv.classList.add("red-alert")
                                        }

                                        screenshotDiv.append(
                                            document.createDivElement().also { alertsDiv ->
                                                alertsDiv.className = "alerts"
                                                card.alerts.forEach { alert ->
                                                    alertsDiv.append(
                                                        document.createDivElement().also { alertDiv ->
                                                            alertDiv.className = "alert alert-${alert.level}"
                                                            alertDiv.append(alert.message)
                                                        },
                                                    )
                                                }
                                            },
                                        )
                                    }
                                },
                                document.createDivElement().also { textsDiv ->
                                    textsDiv.className = "texts"
                                    textsDiv.append(
                                        document.createDivElement().also { textDiv ->
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
            onPluginLoaded.register { scheduleUpdate() }
        }

    }

    class Alert(val message: dynamic, val level: Int)

}

abstract external class CardElement : HTMLDivElement {
    var card: Card
}

fun Document.createCardElement() = this.createDivElement().unsafeCast<CardElement>()
