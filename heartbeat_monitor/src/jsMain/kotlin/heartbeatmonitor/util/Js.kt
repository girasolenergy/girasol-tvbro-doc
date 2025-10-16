package heartbeatmonitor.util

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.js.Promise

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

fun jsObjectOf(vararg pairs: Pair<String, dynamic>): dynamic {
    val obj = js("({})")
    pairs.forEach { (key, value) ->
        obj[key] = value
    }
    return obj
}

fun Map<String, dynamic>.toJsObject(): dynamic {
    val obj = js("({})")
    this.forEach { (key, value) ->
        obj[key] = value
    }
    return obj
}
