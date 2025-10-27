package heartbeatmonitor.plugins

import heartbeatmonitor.core.AbstractPlugin
import heartbeatmonitor.core.Card
import heartbeatmonitor.core.CardProvider
import heartbeatmonitor.core.Dispatcher
import heartbeatmonitor.util.createDivElement
import heartbeatmonitor.util.createSpanElement
import heartbeatmonitor.util.isPrime
import heartbeatmonitor.util.primeFactors
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import org.w3c.dom.Image
import kotlin.math.floor

object SampleCardProviderPlugin : AbstractPlugin("SampleCardProviderPlugin") {
    override suspend fun apply() {
        CardProvider.currentCardProviders += CardProvider { coroutineScope ->
            (0 until 100).map { index ->
                coroutineScope.async {
                    yield()
                    val number = Dispatcher.dispatch {
                        yield()
                        delay(20L + floor(window.asDynamic().Math.random() * 120.0).toLong())
                        yield()
                        index + 1
                    }
                    yield()
                    Card(
                        mapOf(
                            "name" to "$number",
                        ),
                        Image().also { img ->
                            img.asDynamic().loading = "lazy"
                            img.asDynamic().decoding = "async"
                            img.src = if (isPrime(number)) {
                                "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Leonardo_da_Vinci_%281452-1519%29_-_The_Last_Supper_%281495-1498%29.jpg/1920px-Leonardo_da_Vinci_%281452-1519%29_-_The_Last_Supper_%281495-1498%29.jpg"
                            } else {
                                "https://upload.wikimedia.org/wikipedia/commons/2/20/Zeus_Otricoli_Pio-Clementino_Inv257.jpg"
                            }
                            img.alt = "${number}番目の画像"
                        },
                        run {
                            fun createAlert(message: String, level: Int): Card.Alert {
                                return Card.Alert(
                                    document.createSpanElement().also { span ->
                                        span.textContent = message
                                    },
                                    level,
                                )
                            }

                            val alerts = mutableListOf<Card.Alert>()
                            if (number % 3 === 0) alerts += createAlert("3の倍数", 2)
                            if (number % 5 === 0) alerts += createAlert("5の倍数", 1)
                            alerts
                        },
                        run {
                            val texts = mutableListOf<dynamic>()
                            val factors = if (number == 1) mutableListOf() else primeFactors(number)
                            factors
                                .groupBy { it }
                                .map { it.value.joinToString("-") { n -> "$n" } }
                                .forEach {
                                    texts += document.createDivElement().also { textDiv ->
                                        textDiv.textContent = it
                                    }
                                }
                            texts
                        },
                    )
                }
            }
        }
    }
}
