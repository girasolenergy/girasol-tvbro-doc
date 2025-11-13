package heartbeatmonitor.util


// Json Context

interface JsonContext {
    val path: String
}

context(context: JsonContext)
inline fun <T> wrap(block: () -> T): T {
    return try {
        block()
    } catch (e: dynamic) {
        throw IllegalArgumentException("${e.message} at ${context.path}", e)
    }
}


// Json Accessor

interface JsonAccessor<in I, out O> : JsonContext {
    fun get(): O

    /**
     * 親オブジェクトが存在しない場合は親オブジェクトを再帰的に作成します。
     * ただし、親階層のどこかに[JsonObject]でない値が既に存在する場合、例外をスローします。
     */
    fun set(value: I)
}


// Json Provider

interface JsonProvider : JsonContext {
    fun asJsonOrNull(): JsonObject?
    fun asJsonOrThrow(): JsonObject

    /**
     * [JsonObject]でない値が既に存在する場合、例外をスローします。
     */
    fun asJsonOrCreate(): JsonObject
}


// Json Wrapper

class JsonWrapper(val value: Any?) : JsonAccessor<Nothing, JsonWrapper>, JsonProvider {
    companion object {
        val NULL = JsonWrapper(null)
    }

    override val path get() = "$"
    override fun get() = this
    override fun set(value: Nothing) = value
    override fun asJsonOrNull() = JsonUtils.asJsonObjectOrNull(value)
    override fun asJsonOrThrow() = JsonUtils.asJsonObjectOrNull(value) ?: throw IllegalStateException("Value is not a Json at $path")
    override fun asJsonOrCreate() = JsonUtils.asJsonObjectOrNull(value) ?: throw IllegalStateException("Value is not a Json at $path")
}

fun Boolean.toJsonWrapper() = JsonWrapper(this)
fun String.toJsonWrapper() = JsonWrapper(this)
fun Array<*>.toJsonWrapper() = JsonWrapper(this)
fun JsonObject.toJsonWrapper() = JsonWrapper(this)

fun JsonWrapper.encodeToString() = JSON.stringify(this.value)

fun String.decodeFromJson() = JsonWrapper(JSON.parse(this))

fun JsonWrapper.deepClone() = this.encodeToString().decodeFromJson()

fun JsonWrapper.normalize(): JsonWrapper {
    fun Any?.normalize(): Any? {
        JsonUtils.asArrayOrNull(this)?.let { array ->
            return array.map { it.normalize() }.toTypedArray()
        }
        JsonUtils.asJsonObjectOrNull(this)?.let { json ->
            return json.sortedEntries.map { it.first to it.second.normalize() }.toJsonObject()
        }
        return this
    }
    return JsonWrapper(this.value.normalize())
}

fun JsonWrapper.encodeToNormalizedString() = this.normalize().encodeToString()

/**
 * 深い比較を行います。
 * オブジェクトのプロパティの順序の違いは無視されます。
 */
infix fun JsonWrapper.sameAs(other: JsonWrapper) = this.encodeToNormalizedString() == other.encodeToNormalizedString()


// Object Item Access

class JsonObjectItemAccessor(private val parent: JsonProvider, private val key: String) : JsonAccessor<JsonWrapper?, JsonWrapper?>, JsonProvider {
    override val path: String get() = "${parent.path}.$key"

    override fun get(): JsonWrapper? {
        val parentValue = parent.asJsonOrNull() ?: return null
        return if (key in parentValue) {
            JsonWrapper(parentValue[key])
        } else {
            null
        }
    }

    override fun set(value: JsonWrapper?) {
        if (value == null) {
            val parentValue = parent.asJsonOrNull() ?: return
            parentValue.remove(key)
        } else {
            val parentValue = parent.asJsonOrCreate()
            parentValue[key] = value.value
        }
    }

    override fun asJsonOrNull(): JsonObject? {
        val parentValue = parent.asJsonOrNull() ?: return null
        return JsonUtils.asJsonObjectOrNull(parentValue[key])
    }

    override fun asJsonOrThrow(): JsonObject {
        val parentValue = parent.asJsonOrThrow()
        return JsonUtils.asJsonObjectOrNull(parentValue[key]) ?: throw IllegalStateException("Not a Json at $path")
    }

    override fun asJsonOrCreate(): JsonObject {
        val parentValue = parent.asJsonOrCreate()
        return JsonUtils.asJsonObjectOrNull(parentValue[key]) ?: run {
            val value = jsonObject()
            parentValue[key] = value
            value
        }
    }
}

operator fun JsonProvider.get(key: String) = JsonObjectItemAccessor(this, key)


// Transform

class JsonTransformer<AI, AO, BI, BO>(val i2o: (AO) -> BO, val o2i: (BI) -> AI)

object JsonTransformers {
    val boolean = JsonTransformer<JsonWrapper, JsonWrapper, Boolean, Boolean>({ it.value as Boolean }, { it.toJsonWrapper() })
    val string = JsonTransformer<JsonWrapper, JsonWrapper, String, String>({ it.value.toString() }, { it.toJsonWrapper() })
}

fun <AI, AO, BI, BO> JsonAccessor<AI, AO>.xmap(i2o: (AO) -> BO, o2i: (BI) -> AI) = object : JsonAccessor<BI, BO> {
    override val path get() = this@xmap.path
    override fun get() = wrap { i2o(this@xmap.get()) }
    override fun set(value: BI) = this@xmap.set(wrap { o2i(value) })
}

fun <AI, AO, BI, BO> JsonAccessor<AI, AO>.xmap(transformer: JsonTransformer<AI, AO, BI, BO>) = object : JsonAccessor<BI, BO> {
    override val path get() = this@xmap.path
    override fun get() = wrap { transformer.i2o(this@xmap.get()) }
    override fun set(value: BI) = this@xmap.set(wrap { transformer.o2i(value) })
}

fun <AI : Any, AO : Any, BI : Any, BO : Any> JsonAccessor<AI?, AO?>.nonNullXmap(transform: JsonTransformer<AI, AO, BI, BO>) = object : JsonAccessor<BI?, BO?> {
    override val path get() = this@nonNullXmap.path
    override fun get(): BO? = wrap { transform.i2o(this@nonNullXmap.get() ?: return null) }
    override fun set(value: BI?) = this@nonNullXmap.set(if (value == null) null else wrap { transform.o2i(value) })
}

val JsonAccessor<JsonWrapper?, JsonWrapper?>.boolean get() = this.nonNullXmap(JsonTransformers.boolean)
val JsonAccessor<JsonWrapper?, JsonWrapper?>.string get() = this.nonNullXmap(JsonTransformers.string)
val <I : Any, O : Any> JsonAccessor<I?, O?>.nonNull: JsonAccessor<I, O> get() = this.xmap({ it!! }, { it })
