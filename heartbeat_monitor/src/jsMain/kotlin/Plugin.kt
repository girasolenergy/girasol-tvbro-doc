package hello

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

@JsExport
interface Plugin {
    fun apply(): Promise<Unit>
}

@JsExport
fun getPlugin(name: String): Plugin? = Plugins.plugins[name]

@JsExport
fun getAllPlugins(): Array<Plugin> = Plugins.plugins.values.toTypedArray()

object Plugins {
    val plugins = mutableMapOf<String, Plugin>()
}


abstract class AbstractPlugin : Plugin {
    final override fun apply(): Promise<Unit> {
        return MainScope().promise {
            applyImpl()
        }
    }

    protected abstract suspend fun applyImpl()
}
