package mirrg.kotlin.event

interface SeparatedEventRegistry<I, O> {
    fun register(listener: (I) -> O)
    fun unregister(listener: (I) -> O)
}

fun <I, O> SeparatedEventRegistry<I, O>.toEventRegistry(): EventRegistry<I, O> {
    return object : EventRegistry<I, O> {
        override fun register(listener: (I) -> O): Remover {
            this@toEventRegistry.register(listener)
            return Remover {
                this@toEventRegistry.unregister(listener)
            }
        }
    }
}
