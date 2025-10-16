package mirrg.kotlin.event

abstract class MutableListEventRegistry<E, I, O> : EmittableEventRegistry<E, I, O> {

    private val listeners = mutableListOf<(I) -> O>()

    override fun emit(event: E) {
        val listeners = this.listeners.toList()
        listeners.forEach {
            emitTo(event, it)
        }
    }

    override fun register(listener: (I) -> O): Remover {
        listeners.add(listener)
        return Remover {
            listeners.remove(listener)
        }
    }

}

@Suppress("FunctionName")
fun <I, O> EventRegistry(): MutableListEventRegistry<I, I, O> = object : MutableListEventRegistry<I, I, O>() {
    override fun emitTo(event: I, listener: (I) -> O) {
        listener(event)
    }
}
