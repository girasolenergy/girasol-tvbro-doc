package heartbeatmonitor.core

import heartbeatmonitor.util.getValue
import heartbeatmonitor.util.property
import heartbeatmonitor.util.setValue
import heartbeatmonitor.util.xmap
import kotlinx.browser.document
import kotlinx.browser.localStorage
import mirrg.kotlin.event.ObservableValue
import onPluginLoaded
import org.w3c.dom.asList

object CardComparatorSpecifiers {
    private const val LOCAL_STORAGE_KEY = "kanbanbro.cardComparatorSpecifiers"
    private var storage by localStorage.property(LOCAL_STORAGE_KEY)
        .xmap(
            { if (it != null) (JSON.parse(it) as Array<dynamic>).toList() else listOf() },
            { JSON.stringify(it.toTypedArray()) },
        )

    val currentCardComparatorSpecifiers = ObservableValue(listOf<dynamic>())

    fun init() {

        // Sort when changed
        currentCardComparatorSpecifiers.register {
            val cardElements = UiContainers.cards.children.asList().toMutableList()
            cardElements.sortWith { a, b ->
                CardComparator.currentComparator.compare(a.asDynamic().card, b.asDynamic().card)
            }
            val fragment = document.createDocumentFragment()
            cardElements.forEach { cardElement ->
                fragment.appendChild(cardElement)
            }
            UiContainers.cards.appendChild(fragment)
        }

        // Storage
        onPluginLoaded.register {
            currentCardComparatorSpecifiers.value = storage
        }
        currentCardComparatorSpecifiers.register {
            storage = currentCardComparatorSpecifiers.value
        }

    }
}
