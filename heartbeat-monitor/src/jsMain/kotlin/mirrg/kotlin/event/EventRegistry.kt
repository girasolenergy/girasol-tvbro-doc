package mirrg.kotlin.event

interface EventRegistry<I, O> {
    fun register(listener: (I) -> O): Remover
}

val <I, O> EventRegistry<I, O>.once
    get() = object : EventRegistry<I, O> {
        override fun register(listener: (I) -> O): Remover {
            var remover: Remover? = null
            val listener2 = { input: I ->
                val output = listener(input)
                remover!!.remove()
                output
            }
            remover = this@once.register(listener2)
            return remover
        }
    }
