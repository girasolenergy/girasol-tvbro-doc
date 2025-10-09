package hello

fun main() {
    Plugins.plugins["TitlePlugin"] = TitlePlugin
    Plugins.plugins["UpdatePlugin"] = UpdatePlugin
}
