package heartbeatmonitor.plugins

import KanbanBro
import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.util.jsObjectOf
import heartbeatmonitor.util.new
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import org.w3c.dom.Image
import org.w3c.files.Blob
import kotlin.js.Date
import kotlin.js.Promise

object KanbanBroFirebaseHeartbeatCardProviderPlugin : AbstractPlugin("KanbanBroFirebaseHeartbeatCardProviderPlugin") {
    override suspend fun apply() {
        val firebaseStorage = (js("import('https://www.gstatic.com/firebasejs/12.0.0/firebase-storage.js')") as Promise<dynamic>).await()
        val getStorage = firebaseStorage.getStorage
        val ref = firebaseStorage.ref
        val listAll = firebaseStorage.listAll
        val getBytes = firebaseStorage.getBytes
        val getMetadata = firebaseStorage.getMetadata

        var providers = mutableListOf<(dynamic) -> Promise<dynamic>>()

        suspend fun rebuildForApp(app: dynamic, providers: MutableList<(dynamic) -> Promise<dynamic>>) {
            val user = KanbanBro.firebase.getAuth(app).currentUser

            if (user == null) {
                console.warn("No user logged in for app", app.name)
                return
            }

            val files = try {
                (listAll(ref(getStorage(app), "users/${user.uid}")) as Promise<dynamic>).await()
            } catch (e: dynamic) {
                console.error("Failed to list heartbeat roots", e)
                return
            }

            (files.prefixes as Array<dynamic>).forEach { folderRef ->
                providers.add { signal: dynamic ->
                    KanbanBro.dispatcher {
                        MainScope().promise {
                            signal.throwIfAborted()
                            val imageBytes = (getBytes(ref(folderRef, "screenshot.png")) as Promise<dynamic>).await()
                            signal.throwIfAborted()
                            val settingsBytes = (getBytes(ref(folderRef, "settings.json")) as Promise<dynamic>).await()
                            signal.throwIfAborted()
                            val metadata = (getMetadata(ref(folderRef, "settings.json")) as Promise<dynamic>).await()
                            signal.throwIfAborted()

                            val settings = JSON.parse<dynamic>(new(window.asDynamic().TextDecoder).decode(settingsBytes) as String)
                            val name = settings.settings_code.settings.heartbeat_title ?: folderRef.name
                            console.log("$name", settings, metadata)

                            jsObjectOf(
                                "keys" to jsObjectOf(
                                    "name" to name,
                                    "updated" to Date.parse(metadata.updated),
                                ),
                                "image" to Image().also { img ->
                                    img.asDynamic().loading = "lazy"
                                    img.asDynamic().decoding = "async"
                                    window.asDynamic().setImageBlob(img, Blob(arrayOf(imageBytes), jsObjectOf("type" to "image/png")))
                                    img.alt = name
                                },
                                "alerts" to run {
                                    fun createAlert(message: String, level: Int): dynamic {
                                        return jsObjectOf(
                                            "message" to document.createElement("span").also { span ->
                                                span.textContent = message
                                            },
                                            "level" to level,
                                        )
                                    }

                                    val alerts = mutableListOf<dynamic>()
                                    if (settings.main_activity_has_focus as Boolean? == false) alerts.add(createAlert("Lost Focus", 2))
                                    if (settings.main_activity_resumed as Boolean? == false) alerts.add(createAlert("Not Resumed", 2))
                                    if (settings.main_activity_ui_active as Boolean? == true) alerts.add(createAlert("UI Opened", 1))
                                    if (Date.now() - Date.parse(metadata.updated as String) >= 1000 * 60 * 60 * 2) {
                                        alerts.add(createAlert("No updates (2 Hours+)", 2))
                                    }
                                    alerts.toTypedArray()
                                },
                                "texts" to run {
                                    val texts = mutableListOf<dynamic>()
                                    texts.add(document.createElement("div").also { div ->
                                        div.className = "name"
                                        div.textContent = name
                                    })
                                    texts.add(document.createElement("div").also { div ->
                                        div.className = "datetime"
                                        div.textContent = Date(metadata.updated as String).asDynamic().toLocaleString(undefined, jsObjectOf("dateStyle" to "medium", "timeStyle" to "medium"))
                                        div.asDynamic().title = "${metadata.updated}"
                                    })
                                    texts.toTypedArray()
                                },
                                "_debug" to jsObjectOf(
                                    "appName" to app.name,
                                    "app" to app,
                                    "user" to user,
                                    "settings" to settings,
                                    "metadata" to metadata,
                                ),
                            )
                        }
                    }
                }
            }
        }

        suspend fun rebuild() {
            val providers2 = mutableListOf<(dynamic) -> Promise<dynamic>>()
            (KanbanBro.appNames as Array<dynamic>).forEach { appName ->
                rebuildForApp(KanbanBro.firebase.getApp(appName), providers2)
            }
            providers = providers2
            window.asDynamic().scheduleUpdate()
            console.log("[KanbanBroFirebaseHeartbeatCardProviderPlugin] Rebuilt: found ${providers.size} providers")
        }

        KanbanBro.cardProviders.push { signal: dynamic -> providers.map { p -> p(signal) }.toTypedArray() }

        KanbanBro.appsEvent.addEventListener("appNamesChanged", { MainScope().promise { rebuild() } })
        val unsubscribers = jsObjectOf()
        KanbanBro.appsEvent.addEventListener("added", { e: dynamic ->
            unsubscribers[e.detail.name] = KanbanBro.firebase.onAuthStateChanged(KanbanBro.firebase.getAuth(e.detail), { _ -> MainScope().promise { rebuild() } })
        })
        KanbanBro.appsEvent.addEventListener("removed", { e: dynamic ->
            unsubscribers[e.detail.name]!!()
        })
        KanbanBro.event.addEventListener("pluginLoaded", { MainScope().promise { rebuild() } })

    }
}
