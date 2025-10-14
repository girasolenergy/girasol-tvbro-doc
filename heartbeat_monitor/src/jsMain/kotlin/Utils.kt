package hello

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

fun isPrime(n: Int): Boolean {
    if (n < 2) return false
    var d = 2
    while (d * d <= n) {
        if (n % d == 0) return false
        d++
    }
    return true
}

fun primeFactors(n: Int): List<Int> {
    var n = n
    val factors = mutableListOf<Int>()
    var d = 2
    while (n > 1) {
        while (n % d === 0) {
            factors += d
            n /= d
        }
        d++
        if (d * d > n && n > 1) {
            factors += n
            break
        }
    }
    return factors
}

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
