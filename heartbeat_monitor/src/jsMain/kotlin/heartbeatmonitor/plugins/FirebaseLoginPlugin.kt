package heartbeatmonitor.plugins

import KanbanBro
import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.UiContainers
import heartbeatmonitor.core.showDialog
import heartbeatmonitor.core.showToast
import heartbeatmonitor.util.getValue
import heartbeatmonitor.util.jsObjectOf
import heartbeatmonitor.util.new
import heartbeatmonitor.util.property
import heartbeatmonitor.util.setValue
import heartbeatmonitor.util.xmap
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import mirrg.kotlin.event.ObservableValue
import onPluginLoaded
import org.w3c.dom.CustomEvent
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.Image
import org.w3c.dom.events.Event
import kotlin.js.Promise

val firebaseConfig = jsObjectOf(
    "apiKey" to "AIzaSyDZrghHsrdUM6WN0ArOcIchEqn5y4bBZGk",
    "authDomain" to "kanbanbro.firebaseapp.com",
    "projectId" to "kanbanbro",
    "storageBucket" to "kanbanbro.firebasestorage.app",
    "messagingSenderId" to "716803851987",
    "appId" to "1:716803851987:web:0bcd6f8b2e8d7d0151f626",
    "measurementId" to "G-N3C4HWN670",
)

object FirebaseLoginPlugin : AbstractPlugin("FirebaseLoginPlugin") {

    val appNames = ObservableValue<List<String>>(listOf())
    var appNamesStorage by localStorage.property("kanbanbro.appNames")
        .xmap(
            { if (it != null) (JSON.parse(it) as Array<String>).toList() else listOf("[DEFAULT]") },
            { JSON.stringify(it.toTypedArray()) }
        )

    lateinit var applier: suspend () -> Unit

    override suspend fun init() {
        super.init()

        val firebaseApp = (js("import('https://www.gstatic.com/firebasejs/12.0.0/firebase-app.js')") as Promise<dynamic>).await()
        val getApp = firebaseApp.getApp
        val initializeApp = firebaseApp.initializeApp
        val deleteApp = firebaseApp.deleteApp

        val firebaseAuth = (js("import('https://www.gstatic.com/firebasejs/12.0.0/firebase-auth.js')") as Promise<dynamic>).await()
        val getAuth = firebaseAuth.getAuth
        val GoogleAuthProvider = firebaseAuth.GoogleAuthProvider
        val onAuthStateChanged = firebaseAuth.onAuthStateChanged
        val signInWithPopup = firebaseAuth.signInWithPopup
        val signOut = firebaseAuth.signOut
        val EmailAuthProvider = firebaseAuth.EmailAuthProvider
        val linkWithCredential = firebaseAuth.linkWithCredential
        val signInWithEmailAndPassword = firebaseAuth.signInWithEmailAndPassword

        KanbanBro.firebase = jsObjectOf(
            "getApp" to getApp,
            "getAuth" to getAuth,
            "onAuthStateChanged" to onAuthStateChanged,
        )

        KanbanBro.appsEvent = new(window.asDynamic().EventTarget)

        applier = {

            fun registerUserListener(app: dynamic, closeEvent: dynamic, fn: () -> Unit) {
                val unsubscriber = onAuthStateChanged(getAuth(app), { fn() })
                closeEvent.addEventListener("close", { unsubscriber() }, jsObjectOf("once" to true))
                fn()
            }

            fun createUserBadge(app: dynamic, closeEvent: dynamic): Element {
                return document.createElement("div").also { userBadgeDiv ->
                    userBadgeDiv.className = "user-badge"
                    registerUserListener(app, closeEvent) {
                        userBadgeDiv.asDynamic().style.display = if (getAuth(app).currentUser != null) null else "none"
                    }
                    userBadgeDiv.append(
                        document.createElement("div").also { avatarDiv ->
                            avatarDiv.className = "user-avatar"
                            registerUserListener(app, closeEvent) {
                                val user = getAuth(app).currentUser
                                avatarDiv.innerHTML = ""
                                if (user != null) {
                                    avatarDiv.append(
                                        Image().also { img ->
                                            img.src = user.photoURL
                                            img.alt = ""
                                            img.referrerPolicy = "no-referrer"
                                        },
                                    )
                                }
                            }
                        },
                        document.createElement("span").also { nameSpan ->
                            nameSpan.className = "user-name"
                            registerUserListener(app, closeEvent) {
                                val user = getAuth(app).currentUser
                                nameSpan.textContent = if (user == null) "" else user.displayName ?: user.email ?: "UID:${user.uid}"
                            }
                        },
                    )
                }
            }

            fun openLoginDialog(app: dynamic) {
                showDialog { container, dialogEvent ->
                    container.append(

                        // タイトル
                        document.createElement("div").also { titleDiv ->
                            titleDiv.textContent = "Account"
                            titleDiv.asDynamic().style.fontWeight = "700"
                        },

                        // ボタングループ
                        document.createElement("div").also { buttonsDiv ->
                            buttonsDiv.className = "dialog-container"
                            buttonsDiv.append(

                                // Googleログインボタン
                                document.createElement("button").also { googleButton ->
                                    (googleButton as HTMLButtonElement).type = "button"
                                    googleButton.textContent = "Sign in with Google"
                                    googleButton.classList.add("auth-button")
                                    googleButton.addEventListener("click", {
                                        MainScope().promise {
                                            googleButton.disabled = true
                                            try {
                                                val provider = new(GoogleAuthProvider)
                                                (signInWithPopup(getAuth(app), provider) as Promise<dynamic>).await()
                                                dialogEvent.dispatchEvent(Event("close"))
                                            } catch (e: dynamic) {
                                                console.error("Sign in failed", e)
                                                val msg = when (e?.code as String?) {
                                                    "auth/popup-blocked" -> "Popup was blocked by the browser."
                                                    "auth/popup-closed-by-user" -> "Popup closed before completing sign in."
                                                    else -> if (e != null && e.message != null) e.message as String else "$e"
                                                }
                                                showToast("Failed to sign in: $msg")
                                            } finally {
                                                googleButton.disabled = false
                                            }
                                        }
                                    })
                                    registerUserListener(app, dialogEvent) {
                                        googleButton.asDynamic().style.display = if (getAuth(app).currentUser != null) "none" else null
                                    }
                                },

                                // メールアドレスでログインボタン
                                document.createElement("button").also { emailSignInButton ->
                                    (emailSignInButton as HTMLButtonElement).type = "button"
                                    emailSignInButton.textContent = "Sign in with Email / Password"
                                    emailSignInButton.classList.add("auth-button")
                                    emailSignInButton.addEventListener("click", {
                                        MainScope().promise {
                                            emailSignInButton.disabled = true
                                            try {
                                                val email = window.prompt("Enter email address") ?: return@promise
                                                val password = window.prompt("Enter password") ?: return@promise
                                                (signInWithEmailAndPassword(getAuth(app), email, password) as Promise<dynamic>).await()
                                                showToast("Signed in successfully.")
                                                dialogEvent.dispatchEvent(Event("close"))
                                            } catch (e: dynamic) {
                                                console.error("Email sign in failed", e)
                                                val msg = when (e?.code as String?) {
                                                    "auth/invalid-email" -> "Invalid email address."
                                                    "auth/user-disabled" -> "This user account has been disabled."
                                                    "auth/user-not-found" -> "No user found with this email."
                                                    "auth/wrong-password" -> "Incorrect password."
                                                    "auth/too-many-requests" -> "Too many attempts. Try again later."
                                                    "auth/network-request-failed" -> "Network error. Please check your connection."
                                                    else -> if (e != null && e.message != null) e.message as String else "$e"
                                                }
                                                showToast("Failed to sign in: $msg")
                                            } finally {
                                                emailSignInButton.disabled = false
                                            }
                                        }
                                    })
                                    registerUserListener(app, dialogEvent) {
                                        emailSignInButton.asDynamic().style.display = if (getAuth(app).currentUser != null) "none" else null
                                    }
                                },

                                // メールアドレスをリンクボタン
                                document.createElement("button").also { linkEmailButton ->
                                    (linkEmailButton as HTMLButtonElement).type = "button"
                                    linkEmailButton.textContent = "Link Email / Password"
                                    linkEmailButton.classList.add("auth-button")
                                    linkEmailButton.addEventListener("click", {
                                        MainScope().promise {
                                            val currentUser = getAuth(app).currentUser
                                            if (currentUser == null) {
                                                showToast("Please sign in first.")
                                                return@promise
                                            }
                                            linkEmailButton.disabled = true
                                            try {
                                                val email = window.prompt("Enter email address") ?: return@promise
                                                val password = window.prompt("Enter password (min 6 chars)") ?: return@promise
                                                if (password.length < 6) {
                                                    showToast("Password must be at least 6 characters.")
                                                    return@promise
                                                }
                                                val credential = EmailAuthProvider.credential(email, password)
                                                (linkWithCredential(currentUser, credential) as Promise<dynamic>).await()
                                                showToast("Linked email/password to your account.")
                                                dialogEvent.dispatchEvent(Event("close"))
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
                                                linkEmailButton.disabled = false
                                            }
                                        }
                                    })
                                    registerUserListener(app, dialogEvent) {
                                        val user = getAuth(app).currentUser
                                        val hasPassword = user != null && user.providerData != undefined && (user.providerData as Array<dynamic>).any { p -> p != null && p.providerId == "password" }
                                        linkEmailButton.asDynamic().style.display = if (user != null && !hasPassword) null else "none"
                                    }
                                },

                                // ログアウトボタン
                                document.createElement("button").also { logoutButton ->
                                    (logoutButton as HTMLButtonElement).type = "button"
                                    logoutButton.textContent = "Logout"
                                    logoutButton.classList.add("auth-button")
                                    logoutButton.addEventListener("click", {
                                        MainScope().promise {
                                            logoutButton.disabled = true
                                            try {
                                                (signOut(getAuth(app)) as Promise<dynamic>).await()
                                                dialogEvent.dispatchEvent(Event("close"))
                                            } catch (e: dynamic) {
                                                console.error("Sign out failed", e)
                                                showToast("Failed to sign out: ${if (e != null && e.message != null) e.message as String else "$e"}")
                                            } finally {
                                                logoutButton.disabled = false
                                            }
                                        }
                                    })
                                    registerUserListener(app, dialogEvent) {
                                        logoutButton.asDynamic().style.display = if (getAuth(app).currentUser != null) null else "none"
                                    }
                                },

                                )
                        },

                        // Closeボタン
                        document.createElement("div").also { actionsDiv ->
                            actionsDiv.asDynamic().style.display = "flex"
                            actionsDiv.asDynamic().style.justifyContent = "end"
                            actionsDiv.asDynamic().style.gap = "8px"
                            actionsDiv.append(
                                document.createElement("button").also { cancelButton ->
                                    (cancelButton as HTMLButtonElement).type = "button"
                                    cancelButton.textContent = "Cancel"
                                    cancelButton.classList.add("dialog-button")
                                    cancelButton.addEventListener("click", { dialogEvent.dispatchEvent(Event("close")) })
                                },
                            )
                        },

                        )
                }
            }

            fun openAccountsDialog() {
                showDialog { container, dialogEvent ->
                    container.append(

                        // タイトル
                        document.createElement("div").also { titleDiv ->
                            titleDiv.textContent = "Accounts"
                            titleDiv.asDynamic().style.fontWeight = "700"
                        },

                        // ユーザーリスト
                        document.createElement("div").also { buttonsDiv ->
                            buttonsDiv.className = "dialog-container"
                            fun updateButtons() {
                                buttonsDiv.innerHTML = ""
                                appNames.value.forEach { appName ->
                                    val app = getApp(appName)
                                    buttonsDiv.append(
                                        document.createElement("div").also { buttonDiv ->
                                            buttonDiv.asDynamic().style.display = "flex"
                                            buttonDiv.asDynamic().style.gap = "12px"
                                            buttonDiv.asDynamic().style.alignItems = "center"
                                            buttonDiv.append(
                                                document.createElement("div").also { leftDiv ->
                                                    leftDiv.asDynamic().style.display = "flex"
                                                    leftDiv.asDynamic().style.gap = "12px"
                                                    leftDiv.asDynamic().style.alignItems = "center"
                                                    leftDiv.append(

                                                        // ログインボタン
                                                        document.createElement("button").also { loginButton ->
                                                            (loginButton as HTMLButtonElement).type = "button"
                                                            loginButton.classList.add("dialog-button")
                                                            loginButton.textContent = "Login"
                                                            loginButton.addEventListener("click", { openLoginDialog(app) })
                                                        },

                                                        // ユーザバッジ
                                                        createUserBadge(app, dialogEvent),

                                                        )
                                                },
                                                document.createElement("div").also { rightDiv ->
                                                    rightDiv.asDynamic().style.marginLeft = "auto"
                                                    rightDiv.append(

                                                        // ログアウトボタン
                                                        document.createElement("button").also { removeButton ->
                                                            (removeButton as HTMLButtonElement).type = "button"
                                                            removeButton.classList.add("dialog-button")
                                                            removeButton.textContent = "🗑️"
                                                            removeButton.addEventListener("click", {
                                                                MainScope().promise {
                                                                    (signOut(getAuth(app)) as Promise<dynamic>).await()
                                                                    deleteApp(app)
                                                                    appNames.value = appNames.value - (app.name as String)
                                                                    KanbanBro.appsEvent.dispatchEvent(CustomEvent("removed", jsObjectOf("detail" to app)))
                                                                }
                                                            })
                                                        },

                                                        )
                                                },
                                            )
                                        },
                                    )
                                }
                            }
                            appNames.register { updateButtons() }
                            updateButtons()
                        },

                        // 追加ボタン
                        document.createElement("button").also { addButton ->
                            (addButton as HTMLButtonElement).type = "button"
                            addButton.classList.add("dialog-transparent-button")
                            addButton.textContent = "＋"
                            addButton.addEventListener("click", {
                                val appName = if ("[DEFAULT]" in appNames.value) window.asDynamic().crypto.randomUUID() as String else "[DEFAULT]"
                                val app = initializeApp(firebaseConfig, appName)
                                appNames.value = appNames.value + appName
                                KanbanBro.appsEvent.dispatchEvent(CustomEvent("added", jsObjectOf("detail" to app)))
                            })
                        },

                        // Closeボタン
                        document.createElement("div").also { actionsDiv ->
                            actionsDiv.asDynamic().style.display = "flex"
                            actionsDiv.asDynamic().style.justifyContent = "end"
                            actionsDiv.asDynamic().style.gap = "8px"
                            actionsDiv.append(
                                document.createElement("button").also { closeButton ->
                                    (closeButton as HTMLButtonElement).type = "button"
                                    closeButton.textContent = "Close"
                                    closeButton.classList.add("dialog-button")
                                    closeButton.addEventListener("click", { dialogEvent.dispatchEvent(Event("close")) })
                                },
                            )
                        },

                        )
                }
            }

            // トップバー左のAccountsボタン
            UiContainers.topbarLeftContainer.append(
                document.createElement("div").also { container ->
                    container.className = "topbar-property"
                    container.append(
                        document.createElement("button").also { accountsButton ->
                            (accountsButton as HTMLButtonElement).type = "button"
                            accountsButton.textContent = "Accounts"
                            accountsButton.addEventListener("click", { openAccountsDialog() })
                        },
                    )
                },
            )

            // トップバー左のアバター
            UiContainers.topbarLeftContainer.append(
                document.createElement("div").also { container ->
                    container.className = "topbar-property"

                    var closeEvent: dynamic = null

                    fun updateAvatar() {
                        if (closeEvent != null) closeEvent.dispatchEvent(Event("close"))
                        closeEvent = new(window.asDynamic().EventTarget)

                        container.innerHTML = ""
                        appNames.value.forEach { appName ->
                            val app = getApp(appName)
                            container.append(
                                document.createElement("div").also { avatarDiv ->
                                    avatarDiv.className = "user-avatar"
                                    registerUserListener(app, closeEvent) {
                                        val user = getAuth(app).currentUser
                                        avatarDiv.innerHTML = ""
                                        avatarDiv.asDynamic().style.display = if (user != null) null else "none"
                                        if (user != null) {
                                            avatarDiv.append(
                                                Image().also { img ->
                                                    img.src = user.photoURL
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
                        initializeApp(firebaseConfig, appName)
                    }
                }
                appNames.value.forEach { appName ->
                    KanbanBro.appsEvent.dispatchEvent(CustomEvent("added", jsObjectOf("detail" to getApp(appName))))
                }
            }

        }
    }

    override suspend fun apply() = applier()
}
