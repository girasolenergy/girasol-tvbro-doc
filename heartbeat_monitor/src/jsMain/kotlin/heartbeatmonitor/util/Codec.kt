package heartbeatmonitor.util


class Codec<I, O>(val i2o: (I) -> O, val o2i: (O) -> I)

inline fun <reified I, reified O> Codec<I, O>.array() = Codec<Array<I>, Array<O>>(
    { input -> Array(input.size) { i -> this.i2o(input[i]) } },
    { output -> Array(output.size) { i -> this.o2i(output[i]) } },
)

inline fun <reified I, reified O> Codec<I, O>.list() = Codec<List<I>, List<O>>(
    { input -> input.map { this.i2o(it) } },
    { output -> output.map { this.o2i(it) } }
)
