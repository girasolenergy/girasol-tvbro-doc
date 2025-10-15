package hello

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.promise
import org.w3c.dom.Image
import kotlin.js.Promise
import kotlin.math.floor

object SampleCardProviderPlugin : AbstractPlugin() {
    override suspend fun applyImpl() {
        KanbanBro.cardProviders.push { signal: dynamic ->
            (0 until 100).map { index ->
                MainScope().promise {
                    signal.throwIfAborted()
                    val number = (KanbanBro.dispatcher {
                        MainScope().promise {
                            signal.throwIfAborted()
                            delay(20L + floor(window.asDynamic().Math.random() * 120.0).toLong())
                            signal.throwIfAborted()
                            index + 1
                        }
                    } as Promise<Int>).await()
                    signal.throwIfAborted()
                    jsObjectOf(
                        "keys" to jsObjectOf(
                            "name" to "$number",
                        ),
                        "image" to Image().also { img ->
                            img.asDynamic().loading = "lazy"
                            img.asDynamic().decoding = "async"
                            img.src = if (isPrime(number)) {
                                "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Leonardo_da_Vinci_%281452-1519%29_-_The_Last_Supper_%281495-1498%29.jpg/1920px-Leonardo_da_Vinci_%281452-1519%29_-_The_Last_Supper_%281495-1498%29.jpg"
                            } else {
                                "https://upload.wikimedia.org/wikipedia/commons/2/20/Zeus_Otricoli_Pio-Clementino_Inv257.jpg"
                            }
                            img.alt = "${number}番目の画像"
                        },
                        "alerts" to run {
                            fun createAlert(message: String, level: Int): dynamic {
                                return jsObjectOf(
                                    "message" to document.createElement("span").also { span ->
                                        span.textContent = message
                                    },
                                    "level" to level,
                                )
                            }

                            val alerts = mutableListOf<dynamic>()
                            if (number % 3 === 0) alerts.add(createAlert("3の倍数", 2))
                            if (number % 5 === 0) alerts.add(createAlert("5の倍数", 1))
                            alerts.toTypedArray()
                        },
                        "texts" to run {
                            val texts = mutableListOf<dynamic>()
                            val factors = if (number == 1) mutableListOf() else primeFactors(number)
                            factors
                                .groupBy { it }
                                .map { it.value.joinToString("-") { n -> "$n" } }
                                .forEach {
                                    texts += document.createElement("div").also { textDiv ->
                                        textDiv.textContent = it
                                    }
                                }
                            texts.toTypedArray()
                        },
                    )
                }
            }.toTypedArray()
        }
    }
}
