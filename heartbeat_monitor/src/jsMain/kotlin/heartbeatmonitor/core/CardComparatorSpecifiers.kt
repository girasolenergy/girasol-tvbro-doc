package heartbeatmonitor.core

import heartbeatmonitor.util.getValue
import heartbeatmonitor.util.property
import heartbeatmonitor.util.setValue
import heartbeatmonitor.util.xmap
import kotlinx.browser.document
import kotlinx.browser.localStorage
import mirrg.kotlin.event.ObservableValue
import onPluginLoaded

external interface CardComparatorSpecifier

operator fun CardComparatorSpecifier.get(key: String): Any? {
    return asDynamic()[key]
}

operator fun CardComparatorSpecifier.set(key: String, value: Any?) {
    asDynamic()[key] = value
}

var CardComparatorSpecifier.type: String
    get() = this["type"] as String
    set(value) {
        this["type"] = value
    }

object CardComparatorSpecifiers {
    private const val LOCAL_STORAGE_KEY = "kanbanbro.cardComparatorSpecifiers"
    private var storage by localStorage.property(LOCAL_STORAGE_KEY)
        .xmap(
            { if (it != null) (JSON.parse(it) as Array<CardComparatorSpecifier>).toList() else listOf() },
            { JSON.stringify(it.toTypedArray()) },
        )

    val currentCardComparatorSpecifiers = ObservableValue(listOf<CardComparatorSpecifier>())

    fun init() {

        // Sort when changed
        currentCardComparatorSpecifiers.register {
            val cardElements = UiContainers.cards.cardElements.toMutableList()
            cardElements.sortWith { a, b ->
                CardComparator.currentComparator.compare(a.card, b.card)
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
