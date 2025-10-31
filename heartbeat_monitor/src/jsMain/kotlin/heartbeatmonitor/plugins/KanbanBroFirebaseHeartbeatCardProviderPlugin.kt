package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.Card
import heartbeatmonitor.core.CardProvider
import heartbeatmonitor.core.Dispatcher
import heartbeatmonitor.util.createDivElement
import heartbeatmonitor.util.createSpanElement
import heartbeatmonitor.util.decode
import heartbeatmonitor.util.firebase.FirebaseApp
import heartbeatmonitor.util.firebase.FirebaseAppModule
import heartbeatmonitor.util.firebase.FirebaseAuthModule
import heartbeatmonitor.util.firebase.FirebaseStorageModule
import heartbeatmonitor.util.firebase.Unsubscribe
import heartbeatmonitor.util.setImageBlob
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import onPluginLoaded
import org.w3c.dom.Image
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.json

object KanbanBroFirebaseHeartbeatCardProviderPlugin : AbstractPlugin("KanbanBroFirebaseHeartbeatCardProviderPlugin") {
    override suspend fun apply() {

        var providers = mutableListOf<(CoroutineScope) -> Deferred<Card>>()

        suspend fun rebuildForApp(app: FirebaseApp, providers: MutableList<(CoroutineScope) -> Deferred<Card>>) {
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
                            val imageBytes = FirebaseStorageModule.getBytes(FirebaseStorageModule.ref(folderRef, "screenshot.png")).asDeferred()
                            val settingsBytes = FirebaseStorageModule.getBytes(FirebaseStorageModule.ref(folderRef, "settings.json")).asDeferred()
                            val metadata = FirebaseStorageModule.getMetadata(FirebaseStorageModule.ref(folderRef, "settings.json")).asDeferred() // なぜかPromiseだと初手でundefinedが返る

                            val settings = JSON.parse<Json>(settingsBytes.await().decode())
                            val name = (settings["settings_code"].unsafeCast<Json>()["settings"].unsafeCast<Json>()["heartbeat_title"] as String?).takeUnless { it.isNullOrBlank() } ?: folderRef.name
                            console.log(name, settings, metadata.await())

                            Card(
                                mapOf(
                                    "name" to name,
                                    "updated" to Date.parse(metadata.await().updated),
                                ),
                            ) { cardDiv ->
                                cardDiv.append(
                                    document.createDivElement().also { screenshotDiv ->
                                        screenshotDiv.className = "screenshot"

                                        screenshotDiv.append(
                                            Image().also { img ->
                                                img.asDynamic().loading = "lazy"
                                                img.asDynamic().decoding = "async"
                                                setImageBlob(img, Blob(arrayOf(imageBytes.await()), BlobPropertyBag("image/png")))
                                                img.alt = name
                                            },
                                        )

                                        fun createAlert(message: String, level: Int): Card.Alert {
                                            return Card.Alert(
                                                document.createSpanElement().also { span ->
                                                    span.textContent = message
                                                },
                                                level,
                                            )
                                        }

                                        val alerts = mutableListOf<Card.Alert>()
                                        if (settings["main_activity_has_focus"] as Boolean? == false) alerts += createAlert("Lost Focus", 2)
                                        if (settings["main_activity_resumed"] as Boolean? == false) alerts += createAlert("Not Resumed", 2)
                                        if (settings["main_activity_ui_active"] as Boolean? == true) alerts += createAlert("UI Opened", 1)
                                        if (Date.now() - Date.parse(metadata.await().updated) >= 1000 * 60 * 60 * 2) {
                                            alerts += createAlert("No updates (2 Hours+)", 2)
                                        }

                                        if (alerts.isNotEmpty()) {
                                            cardDiv.classList.add("yellow-alert")
                                            screenshotDiv.classList.add("yellow-alert")
                                            if (alerts.any { a -> a.level === 2 }) {
                                                cardDiv.classList.add("red-alert")
                                            }

                                            screenshotDiv.append(
                                                document.createDivElement().also { alertsDiv ->
                                                    alertsDiv.className = "alerts"
                                                    alerts.forEach { alert ->
                                                        alertsDiv.append(
                                                            document.createDivElement().also { alertDiv ->
                                                                alertDiv.className = "alert alert-${alert.level}"
                                                                alertDiv.append(alert.message)
                                                            },
                                                        )
                                                    }
                                                },
                                            )
                                        }

                                    },
                                    document.createDivElement().also { textsDiv ->
                                        textsDiv.className = "texts"
                                        textsDiv.append(
                                            document.createDivElement().also { textDiv ->
                                                textDiv.append(
                                                    document.createDivElement().also { div ->
                                                        div.className = "name"
                                                        div.textContent = name
                                                    },
                                                    document.createDivElement().also { div ->
                                                        div.className = "datetime"
                                                        div.textContent = Date(metadata.await().updated).asDynamic().toLocaleString(undefined, json("dateStyle" to "medium", "timeStyle" to "medium"))
                                                        div.title = metadata.await().updated
                                                    },
                                                )
                                            },
                                        )
                                    },
                                )
                            }.also {
                                it.asDynamic()["_debug"] = json(
                                    "appName" to app.name,
                                    "app" to app,
                                    "user" to user,
                                    "settings" to settings,
                                    "metadata" to metadata.await(),
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
        val unsubscribers = mutableMapOf<String, Unsubscribe>()
        FirebaseLoginPlugin.onAppAdded.register { app ->
            unsubscribers[app.name] = FirebaseAuthModule.onAuthStateChanged(FirebaseAuthModule.getAuth(app), { _ -> MainScope().promise { rebuild() } })
        }
        FirebaseLoginPlugin.onAppRemoved.register { app ->
            unsubscribers[app.name]!!()
        }
        onPluginLoaded.register { MainScope().promise { rebuild() } }

    }
}
