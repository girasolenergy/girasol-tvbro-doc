package hello

import kotlinx.browser.document
import kotlinx.browser.window

object AutoUpdatePlugin : Plugin {
    override fun apply() {
        UiContainers.topbarRightContainer.prepend(
            document.createElement("div").also { container ->
                container.className = "topbar-property"
                container.append(
                    document.createElement("span").also { span ->
                        span.textContent = "Auto Update:"
                    },
                    document.createElement("select").also { select ->
                        var timerId: Int? = null

                        data class Option(val value: String, val intervalMilliseconds: Int, val title: String)

                        fun optionOf(value: String): Option? {
                            if (value.isEmpty()) return null
                            val unit = value.takeLast(1)
                            val amount = value.dropLast(1).toIntOrNull() ?: throw RuntimeException("Invalid interval value: $value")
                            if (amount <= 0) throw RuntimeException("Invalid interval value: $value")
                            return when (unit) {
                                "s" -> Option(value, amount * 1000, "$amount ${if (amount == 1) "Second" else "Seconds"}")
                                "m" -> Option(value, amount * 60 * 1000, "$amount ${if (amount == 1) "Minute" else "Minutes"}")
                                "h" -> Option(value, amount * 60 * 60 * 1000, "$amount ${if (amount == 1) "Hour" else "Hours"}")
                                else -> throw RuntimeException("Invalid interval value: $value")
                            }
                        }

                        val defaultOptions = listOf("10s", "30s", "1m", "2m", "5m", "10m", "30m", "1h", "2h", "6h", "12h").map { optionOf(it)!! }

                        fun applyOption(option: Option?) {
                            timerId?.let {
                                window.clearInterval(it)
                                timerId = null
                            }
                            if (option != null) {
                                timerId = window.setInterval({
                                    window.asDynamic().scheduleUpdate()
                                }, option.intervalMilliseconds)
                            }
                        }

                        fun registerOption(value: String, title: String) {
                            select.append(
                                document.createElement("option").also { optionElement ->
                                    optionElement.asDynamic().value = value
                                    optionElement.textContent = title
                                },
                            )
                        }

                        registerOption("", "None")
                        defaultOptions.forEach { option ->
                            registerOption(option.value, option.title)
                        }

                        select.addEventListener("change", {
                            val option = optionOf(select.asDynamic().value as String)
                            window.asDynamic().setPageParameter("r", option?.value)
                            applyOption(option)
                        })

                        fun syncFromLocation() {
                            val option = optionOf((window.asDynamic().getPageParameter("r") as String?) ?: "")
                            if (option != null && defaultOptions.none { it.value == option.value }) {
                                registerOption(option.value, option.title)
                            }
                            select.asDynamic().value = option?.value ?: ""
                            applyOption(option)
                        }
                        window.addEventListener("popstate", {
                            syncFromLocation()
                        })
                        syncFromLocation()

                    },
                )
            },
        )
    }
}
