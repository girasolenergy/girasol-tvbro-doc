package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.Card
import heartbeatmonitor.core.CardProvider
import heartbeatmonitor.core.Dispatcher
import heartbeatmonitor.core.actions
import heartbeatmonitor.core.container
import heartbeatmonitor.core.frame
import heartbeatmonitor.core.label
import heartbeatmonitor.core.leftRight
import heartbeatmonitor.core.onClick
import heartbeatmonitor.core.showDialog
import heartbeatmonitor.core.showToast
import heartbeatmonitor.core.textBox
import heartbeatmonitor.core.textButton
import heartbeatmonitor.core.title
import heartbeatmonitor.util.JsonWrapper
import heartbeatmonitor.util.awaitOrElse
import heartbeatmonitor.util.boolean
import heartbeatmonitor.util.createDivElement
import heartbeatmonitor.util.createSpanElement
import heartbeatmonitor.util.decode
import heartbeatmonitor.util.decodeFromJson
import heartbeatmonitor.util.deepClone
import heartbeatmonitor.util.encode
import heartbeatmonitor.util.encodeToNormalizedString
import heartbeatmonitor.util.firebase.FirebaseApp
import heartbeatmonitor.util.firebase.FirebaseAppModule
import heartbeatmonitor.util.firebase.FirebaseAuthModule
import heartbeatmonitor.util.firebase.FirebaseStorageModule
import heartbeatmonitor.util.firebase.Unsubscribe
import heartbeatmonitor.util.firebase.UploadMetadata
import heartbeatmonitor.util.get
import heartbeatmonitor.util.jsonObject
import heartbeatmonitor.util.randomUuid
import heartbeatmonitor.util.sameAs
import heartbeatmonitor.util.setImageBlob
import heartbeatmonitor.util.string
import heartbeatmonitor.util.toJsonWrapper
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import mirrg.kotlin.event.EventRegistry
import mirrg.kotlin.event.emit
import mirrg.kotlin.event.initialEmit
import mirrg.kotlin.event.subscribe
import mirrg.kotlin.event.toSubscriber
import mirrg.kotlin.helium.mapOfNotNull
import mirrg.kotlin.helium.notBlankOrNull
import onPluginLoaded
import org.w3c.dom.Image
import org.w3c.dom.events.KeyboardEvent
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.js.Date
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
                                bytes.decode().decodeFromJson()
                            }

                            val settingsOverridesBytesDeferred = FirebaseStorageModule.getBytes(FirebaseStorageModule.ref(folderRef, "settings_overrides.json")).asDeferred()
                            val settingsOverridesCache = async {
                                val bytes = settingsOverridesBytesDeferred.awaitOrElse { e ->
                                    return@async null
                                }
                                bytes.decode().decodeFromJson()
                            }

                            val metadataDeferred = FirebaseStorageModule.getMetadata(FirebaseStorageModule.ref(folderRef, "settings.json")).asDeferred() // なぜかPromiseだと初手でundefinedが返る
                            val metadata = metadataDeferred.awaitOrElse { e ->
                                throw IllegalStateException("Failed to get metadata for device ${folderRef.name}", e)
                            }


                            fun <T> asyncSetting(default: () -> T, function: (JsonWrapper) -> T?) = async {
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

                            fun JsonWrapper.getTitle() = this["settings_code"]["settings"]["heartbeat_title"].string.get()?.notBlankOrNull
                            fun JsonWrapper.setTitle(title: String?) = this["settings_code"]["settings"]["heartbeat_title"].string.set(title?.notBlankOrNull)
                            val titleCache = asyncSetting({ folderRef.name }) { it.getTitle() }
                            fun JsonWrapper.getTags() = this["settings_code"]["settings"]["heartbeat_tags"].string.get()?.notBlankOrNull
                            fun JsonWrapper.setTags(tags: String?) = this["settings_code"]["settings"]["heartbeat_tags"].string.set(tags?.notBlankOrNull)
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
                                cardDiv.classList.add("clickable")
                                cardDiv.tabIndex = 0
                                cardDiv.setAttribute("role", "button")

                                var spaceDown = false
                                cardDiv.addEventListener("keydown", { e ->
                                    e as KeyboardEvent
                                    when (e.key) {
                                        "Enter" -> {
                                            e.preventDefault()
                                            cardDiv.click()
                                        }

                                        " " -> {
                                            e.preventDefault()
                                            spaceDown = true
                                        }
                                    }
                                })
                                cardDiv.addEventListener("keyup", { e ->
                                    e as KeyboardEvent
                                    when (e.key) {
                                        " " -> {
                                            if (spaceDown) {
                                                spaceDown = false
                                                cardDiv.click()
                                            }
                                        }
                                    }
                                })

                                cardDiv.addEventListener("click", {
                                    showDialog {
                                        frame {
                                            container {
                                                title("Card")

                                                val subscriber = onClosed.toSubscriber()
                                                val onFormUpdate = EventRegistry<Unit, Unit>()
                                                val onModifySettingsOverrides = EventRegistry<JsonWrapper, Unit>()

                                                leftRight({
                                                    val textBoxId = randomUuid()
                                                    label("Title") {
                                                        this@label.htmlFor = textBoxId
                                                    }
                                                    textBox(settingsCache.await()?.getTitle() ?: folderRef.name) {
                                                        this@textBox.id = textBoxId
                                                        this@textBox.value = settingsOverridesCache.await()?.getTitle() ?: ""
                                                        onModifySettingsOverrides.subscribe(subscriber) { settingsOverrides ->
                                                            settingsOverrides.setTitle(this@textBox.value.trim())
                                                        }
                                                        this@textBox.addEventListener("input", {
                                                            onFormUpdate.emit()
                                                        })
                                                    }
                                                }, {

                                                })
                                                leftRight({
                                                    val textBoxId = randomUuid()
                                                    label("Tags") {
                                                        this@label.htmlFor = textBoxId
                                                    }
                                                    textBox(settingsCache.await()?.getTags() ?: "") {
                                                        this@textBox.id = textBoxId
                                                        this@textBox.value = settingsOverridesCache.await()?.getTags() ?: ""
                                                        onModifySettingsOverrides.subscribe(subscriber) { settingsOverrides ->
                                                            settingsOverrides.setTags(this@textBox.value.trim())
                                                        }
                                                        this@textBox.addEventListener("input", {
                                                            onFormUpdate.emit()
                                                        })
                                                    }
                                                }, {

                                                })

                                                actions {
                                                    textButton("Close") {
                                                        onClick {
                                                            if (this@textButton.disabled) return@onClick
                                                            this@textButton.disabled = true
                                                            MainScope().launch {
                                                                try {
                                                                    val oldSettingsOverrides = settingsOverridesCache.await() ?: jsonObject().toJsonWrapper()
                                                                    val newSettingsOverrides = oldSettingsOverrides.deepClone()
                                                                    onModifySettingsOverrides.emit(newSettingsOverrides)
                                                                    val noChanges = newSettingsOverrides sameAs oldSettingsOverrides

                                                                    if (noChanges) return@launch

                                                                    console.log("Settings overrides changing:", oldSettingsOverrides, newSettingsOverrides)
                                                                    FirebaseStorageModule.uploadBytes(
                                                                        FirebaseStorageModule.ref(folderRef, "settings_overrides.json"),
                                                                        newSettingsOverrides.encodeToNormalizedString().encode(),
                                                                        UploadMetadata().also {
                                                                            it.contentType = "application/json; charset=utf-8"
                                                                        }
                                                                    ).asDeferred().await()
                                                                    console.log("Settings overrides changed.")
                                                                    showToast("Settings saved")
                                                                    Card.scheduleUpdate() // TODO このカードのみ再取得

                                                                } finally {
                                                                    onClosed.emit()
                                                                }
                                                            }
                                                        }
                                                        var updateJob: Job? = null
                                                        onFormUpdate.initialEmit.subscribe(subscriber) {
                                                            updateJob?.cancel()
                                                            updateJob = MainScope().launch {
                                                                val oldSettingsOverrides = settingsOverridesCache.await() ?: jsonObject().toJsonWrapper()
                                                                val newSettingsOverrides = oldSettingsOverrides.deepClone()
                                                                onModifySettingsOverrides.emit(newSettingsOverrides)
                                                                val noChanges = newSettingsOverrides sameAs oldSettingsOverrides

                                                                this@textButton.textContent = if (noChanges) "Close" else "Save and Close"
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                })

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
                                        if (settingsCache.await()?.let { it["main_activity_has_focus"].boolean }?.get() == false) alerts += createAlert("Lost Focus", 2)
                                        if (settingsCache.await()?.let { it["main_activity_resumed"].boolean }?.get() == false) alerts += createAlert("Not Resumed", 2)
                                        if (settingsCache.await()?.let { it["main_activity_ui_active"].boolean }?.get() == true) alerts += createAlert("UI Opened", 1)
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
