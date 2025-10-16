package hello.mirrg.kotlin.event

interface EmittableEventRegistry<E, I, O> : EventRegistry<I, O> {
    fun emit(event: E)
    fun emitTo(event: E, listener: (I) -> O)
}

fun EmittableEventRegistry<Unit, *, *>.emit() = this.emit(Unit)
fun <I, O> EmittableEventRegistry<Unit, I, O>.emitTo(listener: (I) -> O) = this.emitTo(Unit, listener)

val <I, O> EmittableEventRegistry<Unit, I, O>.initialEmit: EmittableEventRegistry<Unit, I, O>
    get() = object : EmittableEventRegistry<Unit, I, O> {
        override fun register(listener: (I) -> O): Remover {
            val remover = this@initialEmit.register(listener)
            this@initialEmit.emitTo(listener)
            return remover
        }

        override fun emit(event: Unit) = this@initialEmit.emit(event)
        override fun emitTo(event: Unit, listener: (I) -> O) = this@initialEmit.emitTo(event, listener)
    }
