package heartbeatmonitor.core

import heartbeatmonitor.util.Codec
import heartbeatmonitor.util.JsonObject
import heartbeatmonitor.util.getValue
import heartbeatmonitor.util.list
import heartbeatmonitor.util.property
import heartbeatmonitor.util.setValue
import heartbeatmonitor.util.toJson
import heartbeatmonitor.util.toMap
import heartbeatmonitor.util.xmap
import kotlinx.browser.document
import kotlinx.browser.localStorage
import mirrg.kotlin.event.ObservableValue
import onPluginLoaded

data class CardComparatorSpecifier(val map: Map<String, Any?>) {
    companion object {
        val JSON_CODEC = Codec<Any?, CardComparatorSpecifier>(
            { input -> CardComparatorSpecifier(input.unsafeCast<JsonObject>().toMap()) },
            { output -> output.map.toJson() },
        )
    }
}

fun CardComparatorSpecifier(vararg pairs: Pair<String, Any?>) = CardComparatorSpecifier(mapOf(*pairs))

operator fun CardComparatorSpecifier.get(key: String) = this.map[key]
fun CardComparatorSpecifier.with(key: String, value: Any?) = CardComparatorSpecifier(this.map + (key to value))

val CardComparatorSpecifier.type get() = this["type"] as String

object CardComparatorSpecifiers {
    private const val LOCAL_STORAGE_KEY = "kanbanbro.cardComparatorSpecifiers"
    private var storage by localStorage.property(LOCAL_STORAGE_KEY)
        .xmap(
            { if (it != null) (JSON.parse(it) as Array<Any?>).toList() else emptyList() },
            { JSON.stringify(it.toTypedArray()) },
        )
        .xmap(CardComparatorSpecifier.JSON_CODEC.list())

    val currentCardComparatorSpecifiers = ObservableValue(emptyList<CardComparatorSpecifier>())

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
