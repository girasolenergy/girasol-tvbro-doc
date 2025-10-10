package hello

fun main() {
    Plugins.plugins["FaviconPlugin"] = FaviconPlugin
    Plugins.plugins["TitlePlugin"] = TitlePlugin
    Plugins.plugins["UpdatePlugin"] = UpdatePlugin
}
