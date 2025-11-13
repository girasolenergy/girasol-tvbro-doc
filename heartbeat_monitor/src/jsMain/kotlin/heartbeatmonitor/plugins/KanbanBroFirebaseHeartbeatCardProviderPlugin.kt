package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.Card
import heartbeatmonitor.core.CardProvider
import heartbeatmonitor.core.Dispatcher
import heartbeatmonitor.core.actions
import heartbeatmonitor.core.closeButton
import heartbeatmonitor.core.container
import heartbeatmonitor.core.frame
import heartbeatmonitor.core.label
import heartbeatmonitor.core.leftRight
import heartbeatmonitor.core.showDialog
import heartbeatmonitor.core.showToast
import heartbeatmonitor.core.textBox
import heartbeatmonitor.core.title
import heartbeatmonitor.util.awaitOrElse
import heartbeatmonitor.util.createDivElement
import heartbeatmonitor.util.createSpanElement
import heartbeatmonitor.util.decode
import heartbeatmonitor.util.firebase.FirebaseApp
import heartbeatmonitor.util.firebase.FirebaseAppModule
import heartbeatmonitor.util.firebase.FirebaseAuthModule
import heartbeatmonitor.util.firebase.FirebaseStorageModule
import heartbeatmonitor.util.firebase.Unsubscribe
import heartbeatmonitor.util.randomUuid
import heartbeatmonitor.util.setImageBlob
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import mirrg.kotlin.event.once
import mirrg.kotlin.helium.mapOfNotNull
import mirrg.kotlin.helium.notBlankOrNull
import onPluginLoaded
import org.w3c.dom.Image
import org.w3c.dom.events.KeyboardEvent
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

                            val imageBytesDeferred = FirebaseStorageModule.getBytes(FirebaseStorageModule.ref(folderRef, "screenshot.png")).asDeferred()
                            val imageCache = async {
                                val bytes = imageBytesDeferred.awaitOrElse { e ->
                                    console.error("Failed to get screenshot for device ${folderRef.name}", e)
                                    return@async null
                                }
                                Blob(arrayOf(bytes), BlobPropertyBag("image/png"))
                            }

                            val settingsBytesDeferred = FirebaseStorageModule.getBytes(FirebaseStorageModule.ref(folderRef, "settings.json")).asDeferred()
                            val settingsCache = async {
                                val bytes = settingsBytesDeferred.awaitOrElse { e ->
                                    console.error("Failed to get settings for device ${folderRef.name}", e)
                                    return@async null
                                }
                                JSON.parse<Json>(bytes.decode())
                            }

                            val settingsOverridesBytesDeferred = FirebaseStorageModule.getBytes(FirebaseStorageModule.ref(folderRef, "settings_overrides.json")).asDeferred()
                            val settingsOverridesCache = async {
                                val bytes = settingsOverridesBytesDeferred.awaitOrElse { e ->
                                    return@async null
                                }
                                JSON.parse<Json>(bytes.decode())
                            }

                            val metadataDeferred = FirebaseStorageModule.getMetadata(FirebaseStorageModule.ref(folderRef, "settings.json")).asDeferred() // なぜかPromiseだと初手でundefinedが返る
                            val metadata = metadataDeferred.awaitOrElse { e ->
                                throw IllegalStateException("Failed to get metadata for device ${folderRef.name}", e)
                            }


                            fun <T> asyncSetting(default: () -> T, function: (Json) -> T?) = async {
                                settingsOverridesCache.await()?.let { settings ->
                                    function(settings)?.let { setting ->
                                        return@async setting
                                    }
                                }
                                settingsCache.await()?.let { settings ->
                                    function(settings)?.let { setting ->
                                        return@async setting
                                    }
                                }
                                default()
                            }

                            fun Json.getTitle() = (this["settings_code"].unsafeCast<Json>()["settings"].unsafeCast<Json>()["heartbeat_title"] as String?)?.notBlankOrNull
                            val titleCache = asyncSetting({ folderRef.name }) { it.getTitle() }
                            fun Json.getTags() = (this["settings_code"].unsafeCast<Json>()["settings"].unsafeCast<Json>()["heartbeat_tags"] as String?)?.notBlankOrNull
                            val tagsCache = asyncSetting({ "" }) { it.getTags() }


                            launch {
                                val it = json(
                                    "title" to titleCache.await(),
                                    "tags" to tagsCache.await(),
                                    "settings" to settingsCache.await(),
                                    "settingsOverrides" to settingsOverridesCache.await(),
                                    "metadata" to metadata,
                                )
                                console.log(it)
                            }


                            Card(
                                mapOfNotNull(
                                    "name" to titleCache.await(),
                                    "tags" to tagsCache.await(),
                                    "updated" to Date.parse(metadata.updated),
                                ),
                            ) { cardDiv ->
                                cardDiv.append(
                                    document.createDivElement().also { screenshotDiv ->
                                        screenshotDiv.className = "screenshot"

                                        screenshotDiv.append(document.createDivElement().also { screenshotPlaceholderDiv ->
                                            MainScope().launch {
                                                val image = imageCache.await()
                                                if (image != null) {
                                                    screenshotPlaceholderDiv.append(Image().also { img ->
                                                        img.asDynamic().loading = "lazy"
                                                        img.asDynamic().decoding = "async"
                                                        setImageBlob(img, image)
                                                        img.alt = titleCache.await()
                                                    })
                                                } else {
                                                    screenshotPlaceholderDiv.style.width = "300px"
                                                    screenshotPlaceholderDiv.style.height = "200px"
                                                    screenshotPlaceholderDiv.textContent = "No Image"
                                                    screenshotPlaceholderDiv.style.fontStyle = "italic"
                                                    screenshotPlaceholderDiv.style.fontSize = "200%"
                                                    screenshotPlaceholderDiv.style.color = "gray"
                                                    screenshotPlaceholderDiv.style.textAlign = "center"
                                                    screenshotPlaceholderDiv.style.lineHeight = "200px"
                                                }
                                            }
                                        })

                                        fun createAlert(message: String, level: Int): Card.Alert {
                                            return Card.Alert(
                                                document.createSpanElement().also { span ->
                                                    span.textContent = message
                                                },
                                                level,
                                            )
                                        }

                                        val alerts = mutableListOf<Card.Alert>()
                                        if (settingsCache.await()?.get("main_activity_has_focus") as Boolean? == false) alerts += createAlert("Lost Focus", 2)
                                        if (settingsCache.await()?.get("main_activity_resumed") as Boolean? == false) alerts += createAlert("Not Resumed", 2)
                                        if (settingsCache.await()?.get("main_activity_ui_active") as Boolean? == true) alerts += createAlert("UI Opened", 1)
                                        if (Date.now() - Date.parse(metadata.updated) >= 1000 * 60 * 60 * 2) alerts += createAlert("No updates (2 Hours+)", 2)

                                        if (alerts.isNotEmpty()) {
                                            cardDiv.classList.add("yellow-alert")
                                            screenshotDiv.classList.add("yellow-alert")
                                            if (alerts.any { a -> a.level == 2 }) {
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
                                                        div.textContent = titleCache.await()
                                                    },
                                                    document.createDivElement().also { div ->
                                                        div.className = "datetime"
                                                        div.textContent = Date(metadata.updated).asDynamic().toLocaleString(undefined, json("dateStyle" to "medium", "timeStyle" to "medium"))
                                                        div.title = metadata.updated
                                                    },
                                                )
                                            },
                                        )
                                    },
                                    document.createDivElement().also { screenshotDiv ->
                                        screenshotDiv.className = "highlight"
                                    },
                                )
                            }.also {
                                launch {
                                    it.asDynamic()["_debug"] = json(
                                        "appName" to app.name,
                                        "app" to app,
                                        "user" to user,
                                        "title" to titleCache.await(),
                                        "tags" to tagsCache.await(),
                                        "settings" to settingsCache.await(),
                                        "settingsOverrides" to settingsOverridesCache.await(),
                                        "metadata" to metadata,
                                    )
                                }
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
