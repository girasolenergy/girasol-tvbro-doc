package heartbeatmonitor.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

fun interface CardProvider {
    companion object {
        val currentCardProviders = mutableListOf<CardProvider>()
    }

    fun generate(scope: CoroutineScope): List<Deferred<Card>>

}
