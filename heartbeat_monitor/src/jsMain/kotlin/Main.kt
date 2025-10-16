package hello

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import org.w3c.dom.events.Event
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


    FaviconPlugin.apply().await()

    TitlePlugin.apply().await()
    ThemeTogglePlugin.apply().await()
    AutoUpdatePlugin.apply().await()
    UpdatePlugin.apply().await()

    if (false) SampleCardProviderPlugin.apply().await()
    FirebaseLoginPlugin.apply().await()
    SortPlugin.apply().await()
    KanbanBroFirebaseHeartbeatCardProviderPlugin.apply().await()

    KanbanBro.event.dispatchEvent(Event("pluginLoaded"))

}
