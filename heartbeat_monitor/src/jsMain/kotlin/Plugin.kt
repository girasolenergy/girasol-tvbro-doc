package hello

@JsExport
interface Plugin {
    fun apply()
}

@JsExport
fun getPlugin(name: String): Plugin? = Plugins.plugins[name]

@JsExport
fun getAllPlugins(): Array<Plugin> = Plugins.plugins.values.toTypedArray()

object Plugins {
    val plugins = mutableMapOf<String, Plugin>()
}
