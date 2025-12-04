package mirrg.kotlin.event

interface Subscriber {
    fun <I, O> subscribe(eventRegistry: EventRegistry<I, O>, listener: (I) -> O): Remover
}

fun <I, O> EventRegistry<I, O>.subscribe(subscriber: Subscriber, listener: (I) -> O): Remover {
    return subscriber.subscribe(this, listener)
}

fun EventRegistry<Unit, Unit>.toSubscriber(): Subscriber {
    return object : Subscriber {
        override fun <I, O> subscribe(eventRegistry: EventRegistry<I, O>, listener: (I) -> O): Remover {
            var remover: Remover? = eventRegistry.register(listener)
            this@toSubscriber.once.register {
                remover?.remove()
                remover = null
            }
            return Remover {
                remover?.remove()
                remover = null
            }
        }
    }
}

@Suppress("FunctionName")
fun Terminator() = EventRegistry<Unit, Unit>().toSubscriber()
