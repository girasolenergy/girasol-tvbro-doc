package heartbeatmonitor.util

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.Image
import org.w3c.dom.events.Event
import org.w3c.dom.url.URL
import org.w3c.dom.url.URLSearchParams
import org.w3c.files.Blob
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

fun <T> suspendingPromise(block: suspend ((T) -> Unit, (dynamic) -> Unit) -> Unit): Promise<T> {
    return Promise { resolve, reject ->
        MainScope().launch {
            try {
                block(resolve, reject)
            } catch (e: dynamic) {
                reject(e)
            }
        }
    }
}

fun new(constructor: dynamic, vararg args: dynamic): dynamic = js("Reflect.construct")(constructor, args)

fun Json.toMap(): Map<String, Any?> {
    val dynamicThis = this.asDynamic()
    val map = mutableMapOf<String, Any?>()
    val keys = window.asDynamic().Object.keys(dynamicThis).unsafeCast<Array<String>>()
    for (key in keys) {
        map[key] = dynamicThis[key]
    }
    return map
}

fun Map<String, Any?>.toJson() = json(*this.map { it.toPair() }.toTypedArray())

fun getPageParameter(key: String): String? {
    val params = URLSearchParams(window.location.search)
    return params.get(key)
}

fun setPageParameter(key: String, value: String?) {
    val url = URL(window.location.href)
    if (value.isNullOrEmpty()) {
        url.searchParams.delete(key)
    } else {
        url.searchParams.set(key, value)
    }
    window.history.replaceState(null, "", url.href)
}

fun setImageBlob(img: Image, blob: Blob) {
    val url = URL.createObjectURL(blob)
    lateinit var cleanup: (Event) -> Unit
    cleanup = {
        URL.revokeObjectURL(url)
        img.removeEventListener("load", cleanup)
        img.removeEventListener("error", cleanup)
    }
    img.addEventListener("load", cleanup, json("once" to true))
    img.addEventListener("error", cleanup, json("once" to true))
    img.src = url
}
