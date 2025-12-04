package heartbeatmonitor.util.firebase

import kotlin.js.Promise

// https://firebase.google.com/docs/reference/js/app

external interface FirebaseApp {
    val name: String
}

external interface FirebaseOptions

@JsModule("firebase/app")
external object FirebaseAppModule {

    fun getApp(name: String = definedExternally): FirebaseApp

    fun initializeApp(): FirebaseApp

    fun initializeApp(options: FirebaseOptions, name: String = definedExternally): FirebaseApp

    fun deleteApp(app: FirebaseApp): Promise<Unit>

}
