package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.UiContainers
import heartbeatmonitor.core.actions
import heartbeatmonitor.core.closeButton
import heartbeatmonitor.core.container
import heartbeatmonitor.core.element
import heartbeatmonitor.core.frame
import heartbeatmonitor.core.leftRight
import heartbeatmonitor.core.onClick
import heartbeatmonitor.core.showDialog
import heartbeatmonitor.core.showToast
import heartbeatmonitor.core.textButton
import heartbeatmonitor.core.textTransparentButton
import heartbeatmonitor.core.title
import heartbeatmonitor.util.createButtonElement
import heartbeatmonitor.util.createDivElement
import heartbeatmonitor.util.createSpanElement
import heartbeatmonitor.util.firebase.FirebaseApp
import heartbeatmonitor.util.firebase.FirebaseAppModule
import heartbeatmonitor.util.firebase.FirebaseAuthModule
import heartbeatmonitor.util.firebase.FirebaseOptions
import heartbeatmonitor.util.getValue
import heartbeatmonitor.util.property
import heartbeatmonitor.util.randomUuid
import heartbeatmonitor.util.setValue
import heartbeatmonitor.util.xmap
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import mirrg.kotlin.event.EmittableEventRegistry
import mirrg.kotlin.event.EventRegistry
import mirrg.kotlin.event.ObservableValue
import mirrg.kotlin.event.emit
import mirrg.kotlin.event.once
import onPluginLoaded
import org.w3c.dom.Element
import org.w3c.dom.Image
import kotlin.js.json

val firebaseConfig = json(
    "apiKey" to "AIzaSyDZrghHsrdUM6WN0ArOcIchEqn5y4bBZGk",
    "authDomain" to "kanbanbro.firebaseapp.com",
    "projectId" to "kanbanbro",
    "storageBucket" to "kanbanbro.firebasestorage.app",
    "messagingSenderId" to "716803851987",
    "appId" to "1:716803851987:web:0bcd6f8b2e8d7d0151f626",
    "measurementId" to "G-N3C4HWN670",
).unsafeCast<FirebaseOptions>()

object FirebaseLoginPlugin : AbstractPlugin("FirebaseLoginPlugin") {

    val appNames = ObservableValue<List<String>>(emptyList())
    var appNamesStorage by localStorage.property("kanbanbro.appNames")
        .xmap(
            { if (it != null) (JSON.parse(it) as Array<String>).toList() else listOf("[DEFAULT]") },
            { JSON.stringify(it.toTypedArray()) }
        )

    val onAppAdded = EventRegistry<FirebaseApp, Unit>()
    val onAppRemoved = EventRegistry<FirebaseApp, Unit>()

    override suspend fun apply() {

        fun registerUserListener(app: FirebaseApp, onClosed: EmittableEventRegistry<Unit, Unit, Unit>, fn: () -> Unit) {
            val unsubscriber = FirebaseAuthModule.onAuthStateChanged(FirebaseAuthModule.getAuth(app), { fn() })
            onClosed.once.register {
                unsubscriber()
            }
            fn()
        }

        fun createUserBadge(app: FirebaseApp, onClosed: EmittableEventRegistry<Unit, Unit, Unit>): Element {
            return document.createDivElement().also { userBadgeDiv ->
                userBadgeDiv.className = "user-badge"
                registerUserListener(app, onClosed) {
                    userBadgeDiv.style.display = if (FirebaseAuthModule.getAuth(app).currentUser != null) "" else "none"
                }
                userBadgeDiv.append(
                    document.createDivElement().also { avatarDiv ->
                        avatarDiv.className = "user-avatar"
                        registerUserListener(app, onClosed) {
                            val user = FirebaseAuthModule.getAuth(app).currentUser
                            avatarDiv.innerHTML = ""
                            if (user != null) {
                                avatarDiv.append(
                                    Image().also { img ->
                                        if (user.photoURL == null) {
                                            console.log("No photoURL for user: ${user.uid}")
                                        } else {
                                            img.src = user.photoURL!!
                                        }
                                        img.alt = ""
                                        img.referrerPolicy = "no-referrer"
                                    },
                                )
                            }
                        }
                    },
                    document.createSpanElement().also { nameSpan ->
                        nameSpan.className = "user-name"
                        registerUserListener(app, onClosed) {
                            val user = FirebaseAuthModule.getAuth(app).currentUser
                            nameSpan.textContent = if (user == null) "" else user.displayName ?: user.email ?: "UID:${user.uid}"
                        }
                    },
                )
            }
        }

        fun openLoginDialog(app: FirebaseApp) {
            showDialog {
                frame {
                    container {
                        title("Account")
                        container {

                            // Googleログインボタン
                            textButton("Log in with Google") {
                                onClick {
                                    MainScope().promise {
                                        disabled = true
                                        try {
                                            val provider = FirebaseAuthModule.GoogleAuthProvider()
                                            FirebaseAuthModule.signInWithPopup(FirebaseAuthModule.getAuth(app), provider).await()
                                            onClosed.emit()
                                        } catch (e: dynamic) {
                                            console.error("Log in failed", e)
                                            val msg = when (e?.code as String?) {
                                                "auth/popup-blocked" -> "Popup was blocked by the browser."
                                                "auth/popup-closed-by-user" -> "Popup closed before completing sign in."
                                                else -> if (e != null && e.message != null) e.message as String else "$e"
                                            }
                                            showToast("Failed to log in: $msg")
                                        } finally {
                                            disabled = false
                                        }
                                    }
                                }
                                registerUserListener(app, onClosed) {
                                    style.display = if (FirebaseAuthModule.getAuth(app).currentUser != null) "none" else ""
                                }
                            }

                            // メールアドレスでログインボタン
                            textButton("Log in with Email / Password") {
                                onClick {
                                    MainScope().promise {
                                        disabled = true
                                        try {
                                            val email = window.prompt("Enter email address") ?: return@promise
                                            val password = window.prompt("Enter password") ?: return@promise
                                            FirebaseAuthModule.signInWithEmailAndPassword(FirebaseAuthModule.getAuth(app), email, password).await()
                                            showToast("Logged in successfully.")
                                            onClosed.emit()
                                        } catch (e: dynamic) {
                                            console.error("Email log in failed", e)
                                            val msg = when (e?.code as String?) {
                                                "auth/invalid-email" -> "Invalid email address."
                                                "auth/user-disabled" -> "This user account has been disabled."
                                                "auth/user-not-found" -> "No user found with this email."
                                                "auth/wrong-password" -> "Incorrect password."
                                                "auth/too-many-requests" -> "Too many attempts. Try again later."
                                                "auth/network-request-failed" -> "Network error. Please check your connection."
                                                else -> if (e != null && e.message != null) e.message as String else "$e"
                                            }
                                            showToast("Failed to log in: $msg")
                                        } finally {
                                            disabled = false
                                        }
                                    }
                                }
                                registerUserListener(app, onClosed) {
                                    style.display = if (FirebaseAuthModule.getAuth(app).currentUser != null) "none" else ""
                                }
                            }

                            // メールアドレスをリンクボタン
                            textButton("Link Email / Password") {
                                onClick {
                                    MainScope().promise {
                                        val currentUser = FirebaseAuthModule.getAuth(app).currentUser
                                        if (currentUser == null) {
                                            showToast("Please log in first.")
                                            return@promise
                                        }
                                        disabled = true
                                        try {
                                            val email = window.prompt("Enter email address") ?: return@promise
                                            val password = window.prompt("Enter password (min 6 chars)") ?: return@promise
                                            if (password.length < 6) {
                                                showToast("Password must be at least 6 characters.")
                                                return@promise
                                            }
                                            val credential = FirebaseAuthModule.EmailAuthProvider.credential(email, password)
                                            FirebaseAuthModule.linkWithCredential(currentUser, credential).await()
                                            showToast("Linked email/password to your account.")
                                            onClosed.emit()
                                        } catch (e: dynamic) {
                                            console.error("Link email failed", e)
                                            val msg = when (e?.code as String?) {
                                                "auth/credential-already-in-use" -> "This email is already in use by another account."
                                                "auth/provider-already-linked" -> "Email/password is already linked to this account."
                                                "auth/requires-recent-login" -> "Please reauthenticate and try again."
                                                else -> if (e != null && e.message != null) e.message as String else "$e"
                                            }
                                            showToast("Failed to link: $msg")
                                        } finally {
                                            disabled = false
                                        }
                                    }
                                }
                                registerUserListener(app, onClosed) {
                                    val user = FirebaseAuthModule.getAuth(app).currentUser
                                    val hasPassword = user != null && user.providerData.any { p -> p.providerId == "password" }
                                    style.display = if (user != null && !hasPassword) "" else "none"
                                }
                            }

                            // ログアウトボタン
                            textButton("Log out") {
                                onClick {
                                    MainScope().promise {
                                        disabled = true
                                        try {
                                            FirebaseAuthModule.signOut(FirebaseAuthModule.getAuth(app)).await()
                                            onClosed.emit()
                                        } catch (e: dynamic) {
                                            console.error("Log out failed", e)
                                            showToast("Failed to log out: ${if (e != null && e.message != null) e.message as String else "$e"}")
                                        } finally {
                                            disabled = false
                                        }
                                    }
                                }
                                registerUserListener(app, onClosed) {
                                    style.display = if (FirebaseAuthModule.getAuth(app).currentUser != null) "" else "none"
                                }
                            }

                        }
                        actions {
                            closeButton("Cancel")
                        }
                    }
                }
            }
        }

        fun openAccountsDialog() {
            showDialog {
                frame {
                    container {
                        title("Accounts")

                        // ユーザーリスト
                        container {
                            fun updateButtons() {
                                innerHTML = ""
                                appNames.value.forEach { appName ->
                                    val app = FirebaseAppModule.getApp(appName)
                                    leftRight({

                                        // ログインボタン
                                        textButton("Log in") {
                                            onClick {
                                                openLoginDialog(app)
                                            }
                                        }

                                        // ユーザバッジ
                                        element(createUserBadge(app, onClosed))

                                    }, {

                                        // ログアウトボタン
                                        textButton("🗑️") {
                                            onClick {
                                                MainScope().promise {
                                                    FirebaseAuthModule.signOut(FirebaseAuthModule.getAuth(app)).await()
                                                    FirebaseAppModule.deleteApp(app)
                                                    appNames.value = appNames.value - app.name
                                                    onAppRemoved.emit(app)
                                                }
                                            }
                                        }

                                    })
                                }
                            }
                            appNames.register { updateButtons() }
                            updateButtons()
                        }

                        // 追加ボタン
                        textTransparentButton("＋") {
                            onClick {
                                val appName = if ("[DEFAULT]" in appNames.value) randomUuid() else "[DEFAULT]"
                                val app = FirebaseAppModule.initializeApp(firebaseConfig, appName)
                                appNames.value = appNames.value + appName
                                onAppAdded.emit(app)
                            }
                        }

                        actions {
                            closeButton()
                        }
                    }
                }
            }
        }

        // トップバー左のAccountsボタン
        UiContainers.topbarLeftContainer.append(
            document.createDivElement().also { container ->
                container.className = "topbar-property"
                container.append(
                    document.createButtonElement().also { accountsButton ->
                        accountsButton.type = "button"
                        accountsButton.textContent = "Accounts"
                        accountsButton.addEventListener("click", { openAccountsDialog() })
                    },
                )
            },
        )

        // トップバー左のアバター
        UiContainers.topbarLeftContainer.append(
            document.createDivElement().also { container ->
                container.className = "topbar-property"

                var onClosed = EventRegistry<Unit, Unit>()

                fun updateAvatar() {
                    onClosed.emit()
                    onClosed = EventRegistry()

                    container.innerHTML = ""
                    appNames.value.forEach { appName ->
                        val app = FirebaseAppModule.getApp(appName)
                        container.append(
                            document.createDivElement().also { avatarDiv ->
                                avatarDiv.className = "user-avatar"
                                registerUserListener(app, onClosed) {
                                    val user = FirebaseAuthModule.getAuth(app).currentUser
                                    avatarDiv.innerHTML = ""
                                    avatarDiv.style.display = if (user != null) "" else "none"
                                    if (user != null) {
                                        avatarDiv.append(
                                            Image().also { img ->
                                                if (user.photoURL == null) {
                                                    console.log("No photoURL for user: ${user.uid}")
                                                } else {
                                                    img.src = user.photoURL!!
                                                }
                                                img.alt = ""
                                                img.referrerPolicy = "no-referrer"
                                            },
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
                appNames.register { updateAvatar() }
                updateAvatar()
            },
        )

        // appNames
        appNames.register {
            appNamesStorage = appNames.value
        }
        onPluginLoaded.register {
            appNames.value = appNamesStorage.also {
                it.forEach { appName ->
                    FirebaseAppModule.initializeApp(firebaseConfig, appName)
                }
            }
            appNames.value.forEach { appName ->
                onAppAdded.emit(FirebaseAppModule.getApp(appName))
            }
        }

    }
}
