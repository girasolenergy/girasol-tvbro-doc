package heartbeatmonitor.util

import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set

operator fun Storage.set(key: String, value: String?) {
    if (value == null) {
        this.removeItem(key)
    } else {
        this[key] = value
    }
}

fun Storage.property(key: String): Property<String?> = object : Property<String?> {
    override fun get(): String? {
        return this@property[key]
    }

    override fun set(value: String?) {
        this@property[key] = value
    }
}
