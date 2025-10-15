package hello

import kotlinx.browser.document

object FaviconPlugin : AbstractPlugin("FaviconPlugin") {
    override suspend fun applyImpl() {
        document.head!!.append(
            document.createElement("link").also { link ->
                link.asDynamic().rel = "icon"
                link.asDynamic().href = "../icon.png"
            },
        )
    }
}
