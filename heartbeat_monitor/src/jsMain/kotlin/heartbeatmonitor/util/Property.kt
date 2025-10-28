package heartbeatmonitor.util

import kotlin.reflect.KProperty

interface Property<T> {
    fun get(): T
    fun set(value: T)
}

operator fun <T> Property<T>.getValue(thisRef: Any?, property: KProperty<*>): T = get()
operator fun <T> Property<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)

fun <I, O> Property<I>.xmap(i2o: (I) -> O, o2i: (O) -> I): Property<O> = object : Property<O> {
    override fun get() = i2o(this@xmap.get())
    override fun set(value: O) = this@xmap.set(o2i(value))
}

fun <I, O> Property<I>.xmap(codec: Codec<I, O>): Property<O> = this.xmap(
    { input -> codec.i2o(input) },
    { output -> codec.o2i(output) },
)
