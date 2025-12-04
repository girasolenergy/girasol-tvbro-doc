package mirrg.kotlin.event

class ObservableValue<T>(default: T) : MutableListEventRegistry<Unit, T, Unit>() {

    var value: T = default
        set(value) {
            if (value != field) {
                field = value
                emit()
            }
        }

    override fun emitTo(event: Unit, listener: (T) -> Unit) {
        listener(value)
    }

}
