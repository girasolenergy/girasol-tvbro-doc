package heartbeatmonitor.util

import kotlin.js.json

external interface JsonObject

fun jsonObject(vararg pairs: Pair<String, Any?>) = json(*pairs).unsafeCast<JsonObject>()
fun Array<Pair<String, Any?>>.toJsonObject() = jsonObject(*this)
fun Iterable<Pair<String, Any?>>.toJsonObject() = this.toList().toTypedArray().toJsonObject()
fun Map<String, Any?>.toJson() = this.map { it.toPair() }.toJsonObject()

val JsonObject.sortedKeys get() = js("Object").keys(this.asDynamic()).unsafeCast<Array<String>>().sorted()
val JsonObject.sortedEntries get() = this.sortedKeys.map { key -> key to this.asDynamic()[key].unsafeCast<Any?>() }
fun JsonObject.toMap() = this.sortedEntries.toMap()

operator fun JsonObject.contains(key: String): Boolean {
    val propertyDescriptor = js("Object").getOwnPropertyDescriptor(this.asDynamic(), key)
    if (propertyDescriptor == null) return false // プロパティーを持っていない
    if (!propertyDescriptor.enumerable) return false // 列挙不可のプロパティーである
    return true
}

/**
 * [kotlin.js.Json]に対して、 `__proto__` などのキーで違いが出ます。
 * @return キーがない場合、undefinedではなくnullを返します。
 */
operator fun JsonObject.get(key: String): Any? {
    val propertyDescriptor = js("Object").getOwnPropertyDescriptor(this.asDynamic(), key)
    if (propertyDescriptor == null) return null // プロパティーを持っていない
    if (!propertyDescriptor.enumerable) return null // 列挙不可のプロパティーである
    return this.asDynamic()[key]
}

operator fun JsonObject.set(key: String, value: Any?) {
    this.asDynamic()[key] = value
}

fun JsonObject.remove(key: String) {
    js("Reflect").deleteProperty(this.asDynamic(), key)
}

object JsonUtils {
    fun isJsonObject(value: Any?) = value != null && jsTypeOf(value) == "object" && !isArray(value)
    fun asJsonObjectOrNull(value: Any?) = if (isJsonObject(value)) value.unsafeCast<JsonObject>() else null
    fun isArray(value: Any?) = js("Array").isArray(value) as Boolean
    fun asArrayOrNull(value: Any?) = if (isArray(value)) value.unsafeCast<Array<Any?>>() else null
}
