package hello

fun main() {
    Plugins.plugins["FaviconPlugin"] = FaviconPlugin
    Plugins.plugins["TitlePlugin"] = TitlePlugin
    Plugins.plugins["ThemeTogglePlugin"] = ThemeTogglePlugin
    Plugins.plugins["UpdatePlugin"] = UpdatePlugin
    Plugins.plugins["SampleCardProviderPlugin"] = SampleCardProviderPlugin
    Plugins.plugins["AutoUpdatePlugin"] = AutoUpdatePlugin
}
