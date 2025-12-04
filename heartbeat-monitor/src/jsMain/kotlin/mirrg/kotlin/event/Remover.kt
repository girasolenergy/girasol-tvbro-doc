package mirrg.kotlin.event

fun interface Remover {
    fun remove()
}

operator fun Remover.plus(other: Remover): Remover {
    return Remover {
        this.remove()
        other.remove()
    }
}

fun Iterable<Remover>.flatten(): Remover {
    return Remover {
        this.forEach {
            it.remove()
        }
    }
}
