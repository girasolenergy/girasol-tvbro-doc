import heartbeatmonitor.core.Card
import heartbeatmonitor.core.CardComparatorSpecifiers
import heartbeatmonitor.core.Theme
import heartbeatmonitor.plugins.AutoUpdatePlugin
import heartbeatmonitor.plugins.FaviconPlugin
import heartbeatmonitor.plugins.FirebaseLoginPlugin
import heartbeatmonitor.plugins.KanbanBroFirebaseHeartbeatCardProviderPlugin
import heartbeatmonitor.plugins.SampleCardProviderPlugin
import heartbeatmonitor.plugins.SortPlugin
import heartbeatmonitor.plugins.ThemeTogglePlugin
import heartbeatmonitor.plugins.TitlePlugin
import heartbeatmonitor.plugins.UpdatePlugin
import heartbeatmonitor.util.jsObjectOf
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import mirrg.kotlin.event.EventRegistry
import mirrg.kotlin.event.emit
import kotlin.js.Promise

val onPluginLoaded = EventRegistry<Unit, Unit>()

@JsExport
fun init(): Promise<Unit> = MainScope().promise {

    window.asDynamic().KanbanBro = jsObjectOf()

    Card.init()
    CardComparatorSpecifiers.init()
    Theme.init()


    FaviconPlugin.init()
    TitlePlugin.init()
    ThemeTogglePlugin.init()
    UpdatePlugin.init()
    SampleCardProviderPlugin.init()
    AutoUpdatePlugin.init()
    SortPlugin.init()
    FirebaseLoginPlugin.init()
    KanbanBroFirebaseHeartbeatCardProviderPlugin.init()


    FaviconPlugin.apply()

    TitlePlugin.apply()
    ThemeTogglePlugin.apply()
    AutoUpdatePlugin.apply()
    UpdatePlugin.apply()

    if (false) SampleCardProviderPlugin.apply()
    FirebaseLoginPlugin.apply()
    SortPlugin.apply()
    KanbanBroFirebaseHeartbeatCardProviderPlugin.apply()

    onPluginLoaded.emit()

}
