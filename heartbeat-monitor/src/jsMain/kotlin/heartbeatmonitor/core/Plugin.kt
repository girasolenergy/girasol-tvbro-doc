package heartbeatmonitor.core

interface Plugin {
    suspend fun apply()
}

abstract class AbstractPlugin(val name: String) : Plugin {
    open suspend fun init() = Unit
}
