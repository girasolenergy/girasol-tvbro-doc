package heartbeatmonitor.util

import kotlinx.browser.window

fun randomUuid() = window.asDynamic().crypto.randomUUID() as String
