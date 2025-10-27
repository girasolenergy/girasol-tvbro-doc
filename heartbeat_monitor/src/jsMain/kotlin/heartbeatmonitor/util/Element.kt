package heartbeatmonitor.util

import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLLinkElement
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.css.CSSStyleDeclaration

fun Document.createDivElement() = this.createElement("div") as HTMLDivElement
fun Document.createButtonElement() = this.createElement("button") as HTMLButtonElement
fun Document.createSpanElement() = this.createElement("span") as HTMLSpanElement
fun Document.createSelectElement() = this.createElement("select") as HTMLSelectElement
fun Document.createOptionElement() = this.createElement("option") as HTMLOptionElement
fun Document.createLinkElement() = this.createElement("link") as HTMLLinkElement

var CSSStyleDeclaration.gap: String
    get() = this.getPropertyValue("gap")
    set(value) = this.setProperty("gap", value)
