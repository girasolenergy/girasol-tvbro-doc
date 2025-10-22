package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.Card
import heartbeatmonitor.core.CardProvider
import heartbeatmonitor.core.Dispatcher
import heartbeatmonitor.util.firebase.FirebaseAppModule
import heartbeatmonitor.util.firebase.FirebaseAuthModule
import heartbeatmonitor.util.firebase.FirebaseStorageModule
import heartbeatmonitor.util.jsObjectOf
import heartbeatmonitor.util.new
import heartbeatmonitor.util.setImageBlob
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlinx.coroutines.yield
import onPluginLoaded
import org.w3c.dom.Image
import org.w3c.files.Blob
import kotlin.js.Date

object KanbanBroFirebaseHeartbeatCardProviderPlugin : AbstractPlugin("KanbanBroFirebaseHeartbeatCardProviderPlugin") {
    override suspend fun apply() {

        var providers = mutableListOf<(CoroutineScope) -> Deferred<Card>>()

        suspend fun rebuildForApp(app: dynamic, providers: MutableList<(CoroutineScope) -> Deferred<Card>>) {
            val user = FirebaseAuthModule.getAuth(app).currentUser

            if (user == null) {
                console.warn("No user logged in for app", app.name)
                return
            }

            val files = try {
                FirebaseStorageModule.listAll(FirebaseStorageModule.ref(FirebaseStorageModule.getStorage(app), "users/${user.uid}")).await()
            } catch (e: dynamic) {
                console.error("Failed to list heartbeat roots", e)
                return
            }

            files.prefixes.forEach { folderRef ->
                providers.add { coroutineScope ->
                    coroutineScope.async {
                        Dispatcher.dispatch {
                            yield()
                            val imageBytes = FirebaseStorageModule.getBytes(FirebaseStorageModule.ref(folderRef, "screenshot.png")).await()
                            yield()
                            val settingsBytes = FirebaseStorageModule.getBytes(FirebaseStorageModule.ref(folderRef, "settings.json")).await()
                            yield()
                            val metadata = FirebaseStorageModule.getMetadata(FirebaseStorageModule.ref(folderRef, "settings.json")).await()
                            yield()

                            val settings = JSON.parse<dynamic>(new(window.asDynamic().TextDecoder).decode(settingsBytes) as String)
                            val name = (settings.settings_code.settings.heartbeat_title as String?).takeUnless { it.isNullOrBlank() } ?: folderRef.name
                            console.log(name, settings, metadata)

                            Card(
                                mapOf(
                                    "name" to name,
                                    "updated" to Date.parse(metadata.updated),
                                ),
                                Image().also { img ->
                                    img.asDynamic().loading = "lazy"
                                    img.asDynamic().decoding = "async"
                                    setImageBlob(img, Blob(arrayOf(imageBytes), jsObjectOf("type" to "image/png")))
                                    img.alt = name
                                },
                                run {
                                    fun createAlert(message: String, level: Int): Card.Alert {
                                        return Card.Alert(
                                            document.createElement("span").also { span ->
                                                span.textContent = message
                                            },
                                            level,
                                        )
                                    }

                                    val alerts = mutableListOf<Card.Alert>()
                                    if (settings.main_activity_has_focus as Boolean? == false) alerts += createAlert("Lost Focus", 2)
                                    if (settings.main_activity_resumed as Boolean? == false) alerts += createAlert("Not Resumed", 2)
                                    if (settings.main_activity_ui_active as Boolean? == true) alerts += createAlert("UI Opened", 1)
                                    if (Date.now() - Date.parse(metadata.updated) >= 1000 * 60 * 60 * 2) {
                                        alerts += createAlert("No updates (2 Hours+)", 2)
                                    }
                                    alerts
                                },
                                run {
                                    val texts = mutableListOf<dynamic>()
                                    texts.add(document.createElement("div").also { div ->
                                        div.className = "name"
                                        div.textContent = name
                                    })
                                    texts.add(document.createElement("div").also { div ->
                                        div.className = "datetime"
                                        div.textContent = Date(metadata.updated).asDynamic().toLocaleString(undefined, jsObjectOf("dateStyle" to "medium", "timeStyle" to "medium"))
                                        div.asDynamic().title = metadata.updated
                                    })
                                    texts
                                },
                            ).also {
                                it.asDynamic()["_debug"] = jsObjectOf(
                                    "appName" to app.name,
                                    "app" to app,
                                    "user" to user,
                                    "settings" to settings,
                                    "metadata" to metadata,
                                )
                            }
                        }
                    }
                }
            }
        }

        suspend fun rebuild() {
            val providers2 = mutableListOf<(CoroutineScope) -> Deferred<Card>>()
            FirebaseLoginPlugin.appNames.value.forEach { appName ->
                rebuildForApp(FirebaseAppModule.getApp(appName), providers2)
            }
            providers = providers2
            Card.scheduleUpdate()
            console.log("[KanbanBroFirebaseHeartbeatCardProviderPlugin] Rebuilt: found ${providers.size} providers")
        }

        CardProvider.currentCardProviders += CardProvider { coroutineScope -> providers.map { p -> p(coroutineScope) } }

        FirebaseLoginPlugin.appNames.register { MainScope().promise { rebuild() } }
        val unsubscribers = jsObjectOf()
        FirebaseLoginPlugin.onAppAdded.register { app ->
            unsubscribers[app.name] = FirebaseAuthModule.onAuthStateChanged(FirebaseAuthModule.getAuth(app), { _ -> MainScope().promise { rebuild() } })
        }
        FirebaseLoginPlugin.onAppRemoved.register { app ->
            unsubscribers[app.name]!!()
        }
        onPluginLoaded.register { MainScope().promise { rebuild() } }

    }
}
