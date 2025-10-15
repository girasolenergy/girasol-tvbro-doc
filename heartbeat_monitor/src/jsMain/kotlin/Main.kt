package hello

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

@JsExport
fun init(): Promise<Unit> = MainScope().promise {
    FaviconPlugin.init()
    TitlePlugin.init()
    ThemeTogglePlugin.init()
    UpdatePlugin.init()
    SampleCardProviderPlugin.init()
    AutoUpdatePlugin.init()
    SortPlugin.init()
    FirebaseLoginPlugin.init()
    KanbanBroFirebaseHeartbeatCardProviderPlugin.init()
}
