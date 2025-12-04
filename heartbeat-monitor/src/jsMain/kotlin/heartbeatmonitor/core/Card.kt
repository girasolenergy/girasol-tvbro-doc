package heartbeatmonitor.core

import heartbeatmonitor.util.createDivElement
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import onPluginLoaded
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import kotlin.coroutines.EmptyCoroutineContext

class Card(
    val keys: Map<String, Any?>,
    val configurator: suspend (CardElement) -> Unit,
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

                    val refNode = UiContainers.cards.cardElements.firstOrNull { child ->
                        CardComparator.currentComparator.compare(child.card, card) > 0
                    }

                    UiContainers.cards.insertBefore(
                        document.createCardElement().also { cardDiv ->
                            cardDiv.className = "card"
                            cardDiv.card = card
                            launch {
                                card.configurator(cardDiv)
                            }
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

    class Alert(val message: Element, val level: Int)

}

abstract external class CardElement : HTMLDivElement {
    var card: Card
}

fun Document.createCardElement() = this.createDivElement().unsafeCast<CardElement>()
