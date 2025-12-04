package heartbeatmonitor.util.firebase

import org.khronos.webgl.ArrayBuffer
import kotlin.js.Json
import kotlin.js.Promise

// https://firebase.google.com/docs/reference/js/storage

external interface FirebaseStorage

external interface StorageReference {
    val name: String
}

external interface ListResult {
    val prefixes: Array<StorageReference>
}

external interface FullMetadata {
    val updated: String
}

@JsModule("firebase/storage")
external object FirebaseStorageModule {

    fun getStorage(app: FirebaseApp = definedExternally, bucketUrl: String = definedExternally): FirebaseStorage

    fun ref(storage: FirebaseStorage, url: String = definedExternally): StorageReference

    fun ref(storageOrRef: StorageReference, path: String = definedExternally): StorageReference

    fun listAll(ref: StorageReference): Promise<ListResult>

    fun getBytes(ref: StorageReference, maxDownloadSizeBytes: Double = definedExternally): Promise<ArrayBuffer>

    fun getMetadata(ref: StorageReference): Promise<FullMetadata>

    fun uploadBytes(ref: StorageReference, data: ArrayBuffer, metadata: UploadMetadata = definedExternally): Promise<UploadResult>

}

external interface SettableMetadata {
    var cacheControl: String?
    var contentDisposition: String?
    var contentEncoding: String?
    var contentLanguage: String?
    var contentType: String?
    var customMetadata: Json?
}

fun SettableMetadata() = js("({})").unsafeCast<SettableMetadata>()

external interface UploadMetadata : SettableMetadata {
    var md5Hash: String?
}

fun UploadMetadata() = js("({})").unsafeCast<UploadMetadata>()

external interface UploadResult {
    val metadata: FullMetadata
    val ref: StorageReference
}
