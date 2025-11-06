package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.UiContainers
import heartbeatmonitor.core.showDialog
import heartbeatmonitor.core.showToast
import heartbeatmonitor.util.createButtonElement
import heartbeatmonitor.util.createDivElement
import heartbeatmonitor.util.createSpanElement
import heartbeatmonitor.util.firebase.FirebaseApp
import heartbeatmonitor.util.firebase.FirebaseAppModule
import heartbeatmonitor.util.firebase.FirebaseAuthModule
import heartbeatmonitor.util.firebase.FirebaseOptions
import heartbeatmonitor.util.gap
import heartbeatmonitor.util.getValue
import heartbeatmonitor.util.property
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
            showDialog { context ->
                context.container.append(

                    // タイトル
                    document.createDivElement().also { titleDiv ->
                        titleDiv.textContent = "Account"
                        titleDiv.style.fontWeight = "700"
                    },

                    // ボタングループ
                    document.createDivElement().also { buttonsDiv ->
                        buttonsDiv.className = "dialog-container"
                        buttonsDiv.append(

                            // Googleログインボタン
                            document.createButtonElement().also { googleButton ->
                                googleButton.type = "button"
                                googleButton.textContent = "Log in with Google"
                                googleButton.classList.add("auth-button")
                                googleButton.addEventListener("click", {
                                    MainScope().promise {
                                        googleButton.disabled = true
                                        try {
                                            val provider = FirebaseAuthModule.GoogleAuthProvider()
                                            FirebaseAuthModule.signInWithPopup(FirebaseAuthModule.getAuth(app), provider).await()
                                            context.onClosed.emit()
                                        } catch (e: dynamic) {
                                            console.error("Log in failed", e)
                                            val msg = when (e?.code as String?) {
                                                "auth/popup-blocked" -> "Popup was blocked by the browser."
                                                "auth/popup-closed-by-user" -> "Popup closed before completing sign in."
                                                else -> if (e != null && e.message != null) e.message as String else "$e"
                                            }
                                            showToast("Failed to log in: $msg")
                                        } finally {
                                            googleButton.disabled = false
                                        }
                                    }
                                })
                                registerUserListener(app, context.onClosed) {
                                    googleButton.style.display = if (FirebaseAuthModule.getAuth(app).currentUser != null) "none" else ""
                                }
                            },

                            // メールアドレスでログインボタン
                            document.createButtonElement().also { emailSignInButton ->
                                emailSignInButton.type = "button"
                                emailSignInButton.textContent = "Log in with Email / Password"
                                emailSignInButton.classList.add("auth-button")
                                emailSignInButton.addEventListener("click", {
                                    MainScope().promise {
                                        emailSignInButton.disabled = true
                                        try {
                                            val email = window.prompt("Enter email address") ?: return@promise
                                            val password = window.prompt("Enter password") ?: return@promise
                                            FirebaseAuthModule.signInWithEmailAndPassword(FirebaseAuthModule.getAuth(app), email, password).await()
                                            showToast("Logged in successfully.")
                                            context.onClosed.emit()
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
                                            emailSignInButton.disabled = false
                                        }
                                    }
                                })
                                registerUserListener(app, context.onClosed) {
                                    emailSignInButton.style.display = if (FirebaseAuthModule.getAuth(app).currentUser != null) "none" else ""
                                }
                            },

                            // メールアドレスをリンクボタン
                            document.createButtonElement().also { linkEmailButton ->
                                linkEmailButton.type = "button"
                                linkEmailButton.textContent = "Link Email / Password"
                                linkEmailButton.classList.add("auth-button")
                                linkEmailButton.addEventListener("click", {
                                    MainScope().promise {
                                        val currentUser = FirebaseAuthModule.getAuth(app).currentUser
                                        if (currentUser == null) {
                                            showToast("Please log in first.")
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
                                            val credential = FirebaseAuthModule.EmailAuthProvider.credential(email, password)
                                            FirebaseAuthModule.linkWithCredential(currentUser, credential).await()
                                            showToast("Linked email/password to your account.")
                                            context.onClosed.emit()
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
                                registerUserListener(app, context.onClosed) {
                                    val user = FirebaseAuthModule.getAuth(app).currentUser
                                    val hasPassword = user != null && user.providerData.any { p -> p.providerId == "password" }
                                    linkEmailButton.style.display = if (user != null && !hasPassword) "" else "none"
                                }
                            },

                            // ログアウトボタン
                            document.createButtonElement().also { logoutButton ->
                                logoutButton.type = "button"
                                logoutButton.textContent = "Log out"
                                logoutButton.classList.add("auth-button")
                                logoutButton.addEventListener("click", {
                                    MainScope().promise {
                                        logoutButton.disabled = true
                                        try {
                                            FirebaseAuthModule.signOut(FirebaseAuthModule.getAuth(app)).await()
                                            context.onClosed.emit()
                                        } catch (e: dynamic) {
                                            console.error("Log out failed", e)
                                            showToast("Failed to log out: ${if (e != null && e.message != null) e.message as String else "$e"}")
                                        } finally {
                                            logoutButton.disabled = false
                                        }
                                    }
                                })
                                registerUserListener(app, context.onClosed) {
                                    logoutButton.style.display = if (FirebaseAuthModule.getAuth(app).currentUser != null) "" else "none"
                                }
                            },

                            )
                    },

                    // Closeボタン
                    document.createDivElement().also { actionsDiv ->
                        actionsDiv.style.display = "flex"
                        actionsDiv.style.justifyContent = "end"
                        actionsDiv.style.gap = "8px"
                        actionsDiv.append(
                            document.createButtonElement().also { cancelButton ->
                                cancelButton.type = "button"
                                cancelButton.textContent = "Cancel"
                                cancelButton.classList.add("dialog-button")
                                cancelButton.addEventListener("click", { context.onClosed.emit() })
                            },
                        )
                    },

                    )
            }
        }

        fun openAccountsDialog() {
            showDialog { context ->
                context.container.append(

                    // タイトル
                    document.createDivElement().also { titleDiv ->
                        titleDiv.textContent = "Accounts"
                        titleDiv.style.fontWeight = "700"
                    },

                    // ユーザーリスト
                    document.createDivElement().also { buttonsDiv ->
                        buttonsDiv.className = "dialog-container"
                        fun updateButtons() {
                            buttonsDiv.innerHTML = ""
                            appNames.value.forEach { appName ->
                                val app = FirebaseAppModule.getApp(appName)
                                buttonsDiv.append(
                                    document.createDivElement().also { buttonDiv ->
                                        buttonDiv.style.display = "flex"
                                        buttonDiv.style.gap = "12px"
                                        buttonDiv.style.alignItems = "center"
                                        buttonDiv.append(
                                            document.createDivElement().also { leftDiv ->
                                                leftDiv.style.display = "flex"
                                                leftDiv.style.gap = "12px"
                                                leftDiv.style.alignItems = "center"
                                                leftDiv.append(

                                                    // ログインボタン
                                                    document.createButtonElement().also { loginButton ->
                                                        loginButton.type = "button"
                                                        loginButton.classList.add("dialog-button")
                                                        loginButton.textContent = "Log in"
                                                        loginButton.addEventListener("click", { openLoginDialog(app) })
                                                    },

                                                    // ユーザバッジ
                                                    createUserBadge(app, context.onClosed),

                                                    )
                                            },
                                            document.createDivElement().also { rightDiv ->
                                                rightDiv.style.marginLeft = "auto"
                                                rightDiv.append(

                                                    // ログアウトボタン
                                                    document.createButtonElement().also { removeButton ->
                                                        removeButton.type = "button"
                                                        removeButton.classList.add("dialog-button")
                                                        removeButton.textContent = "🗑️"
                                                        removeButton.addEventListener("click", {
                                                            MainScope().promise {
                                                                FirebaseAuthModule.signOut(FirebaseAuthModule.getAuth(app)).await()
                                                                FirebaseAppModule.deleteApp(app)
                                                                appNames.value = appNames.value - app.name
                                                                onAppRemoved.emit(app)
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
                    document.createButtonElement().also { addButton ->
                        addButton.type = "button"
                        addButton.classList.add("dialog-transparent-button")
                        addButton.textContent = "＋"
                        addButton.addEventListener("click", {
                            val appName = if ("[DEFAULT]" in appNames.value) window.asDynamic().crypto.randomUUID() as String else "[DEFAULT]"
                            val app = FirebaseAppModule.initializeApp(firebaseConfig, appName)
                            appNames.value = appNames.value + appName
                            onAppAdded.emit(app)
                        })
                    },

                    // Closeボタン
                    document.createDivElement().also { actionsDiv ->
                        actionsDiv.style.display = "flex"
                        actionsDiv.style.justifyContent = "end"
                        actionsDiv.style.gap = "8px"
                        actionsDiv.append(
                            document.createButtonElement().also { closeButton ->
                                closeButton.type = "button"
                                closeButton.textContent = "Close"
                                closeButton.classList.add("dialog-button")
                                closeButton.addEventListener("click", { context.onClosed.emit() })
                            },
                        )
                    },

                    )
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
