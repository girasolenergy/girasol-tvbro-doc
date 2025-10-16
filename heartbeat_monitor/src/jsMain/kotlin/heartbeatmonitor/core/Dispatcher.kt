package heartbeatmonitor.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object Dispatcher {
    private const val CONCURRENCY = 4

    private class Task<T>(val function: suspend () -> T) {
        val deferred = CompletableDeferred<T>()

        suspend fun dispatch() {
            val result = try {
                function()
            } catch (e: Throwable) {
                deferred.completeExceptionally(e)
                return
            }
            deferred.complete(result)
        }
    }

    private val queue = mutableListOf<Task<*>>()
    private var active = 0

    private fun schedule() {
        while (active < CONCURRENCY && queue.isNotEmpty()) {
            val task = queue.removeFirst()
            active++
            MainScope().launch {
                try {
                    task.dispatch()
                } finally {
                    active--
                    schedule()
                }
            }
        }
    }

    suspend fun <T> dispatch(function: suspend () -> T): T {
        val task = Task(function)
        queue += task
        schedule()
        return task.deferred.await()
    }

}
