package heartbeatmonitor.util.firebase

import kotlin.js.Promise

// https://firebase.google.com/docs/reference/js/auth

typealias NextFn<T> = (value: T) -> Unit

typealias Unsubscribe = () -> Unit

external interface Auth {
    val currentUser: User?
}

external interface UserInfo {
    val uid: String
    val photoURL: String?
    val email: String?
    val displayName: String?
    val providerId: String
}

external interface User : UserInfo {
    val providerData: Array<UserInfo>
}

external interface AuthProvider

external interface AuthCredential

external interface UserCredential

external interface PopupRedirectResolver

external interface ErrorFn

external interface CompleteFn

@JsModule("firebase/auth")
external object FirebaseAuthModule {

    class GoogleAuthProvider : AuthProvider

    class EmailAuthProvider {
        companion object {
            fun credential(email: String, password: String): EmailAuthCredential
        }
    }

    class EmailAuthCredential : AuthCredential

    fun getAuth(app: FirebaseApp = definedExternally): Auth

    fun onAuthStateChanged(auth: Auth, nextOrObserver: NextFn<User?>, error: ErrorFn = definedExternally, completed: CompleteFn = definedExternally): Unsubscribe

    fun signInWithPopup(auth: Auth, provider: AuthProvider, resolver: PopupRedirectResolver = definedExternally): Promise<UserCredential>

    fun signOut(auth: Auth): Promise<Unit>

    fun linkWithCredential(user: User, credential: AuthCredential): Promise<UserCredential>

    fun signInWithEmailAndPassword(auth: Auth, email: String, password: String): Promise<UserCredential>

}
