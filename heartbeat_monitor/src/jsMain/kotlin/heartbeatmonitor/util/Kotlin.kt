package heartbeatmonitor.util

import kotlinx.coroutines.Deferred

suspend fun <T> Deferred<T>.awaitResult(): Result<T> {
    return try {
        Result.success(this.await())
    } catch (e: dynamic) {
        Result.failure(e)
    }
}

suspend inline fun <T> Deferred<T>.awaitOrElse(defaultValue: (dynamic) -> T): T {
    return try {
        this.await()
    } catch (e: dynamic) {
        defaultValue(e)
    }
}
