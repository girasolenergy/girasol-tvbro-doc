package heartbeatmonitor.core

import KanbanBro
import heartbeatmonitor.util.new
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.asList

object Card {
    fun init() {

        var currentUpdateAbortController: dynamic = null

        window.asDynamic().scheduleUpdate = {
            console.log("[scheduleUpdate] Scheduling update")

            if (currentUpdateAbortController != null) {
                currentUpdateAbortController.abort()
                currentUpdateAbortController = null
            }
            val abortController = new(window.asDynamic().AbortController)
            currentUpdateAbortController = abortController

            UiContainers.cards.innerHTML = ""
            KanbanBro.cardProviders.flatMap { provider -> provider(abortController.signal) }.forEach { cardPromise ->
                cardPromise.then { card ->
                    abortController.signal.throwIfAborted()

                    val cardContainer = UiContainers.cards

                    val refNode = cardContainer.children.asList().firstOrNull { child ->
                        window.asDynamic().compareCards(child.asDynamic().card, card) > 0
                    }

                    cardContainer.insertBefore(
                        document.createElement("div").also { cardDiv ->
                            cardDiv.className = "card"
                            cardDiv.asDynamic().card = card

                            cardDiv.append(
                                document.createElement("div").also { screenshotDiv ->
                                    screenshotDiv.className = "screenshot"
                                    screenshotDiv.append(card.image)

                                    if (card.alerts.length > 0) {
                                        cardDiv.classList.add("yellow-alert")
                                        screenshotDiv.classList.add("yellow-alert")
                                        if (card.alerts.some { a -> a.level === 2 }) {
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

        KanbanBro.event.addEventListener("pluginLoaded", { window.asDynamic().scheduleUpdate() })

    }
}
