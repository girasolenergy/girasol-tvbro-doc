package heartbeatmonitor.util

import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.HTMLLinkElement
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.css.CSSStyleDeclaration

fun Document.createDivElement() = this.createElement("div").unsafeCast<HTMLDivElement>()
fun Document.createButtonElement() = this.createElement("button").unsafeCast<HTMLButtonElement>()
fun Document.createSpanElement() = this.createElement("span").unsafeCast<HTMLSpanElement>()
fun Document.createSelectElement() = this.createElement("select").unsafeCast<HTMLSelectElement>()
fun Document.createOptionElement() = this.createElement("option").unsafeCast<HTMLOptionElement>()
fun Document.createLinkElement() = this.createElement("link").unsafeCast<HTMLLinkElement>()
fun Document.createLabelElement() = this.createElement("label").unsafeCast<HTMLLabelElement>()
fun Document.createInputElement() = this.createElement("input").unsafeCast<HTMLInputElement>()
fun Document.createTextAreaElement() = this.createElement("textarea").unsafeCast<HTMLTextAreaElement>()

var CSSStyleDeclaration.gap: String
    get() = this.getPropertyValue("gap")
    set(value) = this.setProperty("gap", value)

var CSSStyleDeclaration.colorScheme: String
    get() = this.getPropertyValue("color-scheme")
    set(value) = this.setProperty("color-scheme", value)
