package hello

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

interface Plugin {
    fun apply(): Promise<Unit>
}

abstract class AbstractPlugin(val name: String) : Plugin {
    final override fun apply(): Promise<Unit> {
        return MainScope().promise {
            applyImpl()
        }
    }

    protected abstract suspend fun applyImpl()

    open suspend fun init() = Unit
}
