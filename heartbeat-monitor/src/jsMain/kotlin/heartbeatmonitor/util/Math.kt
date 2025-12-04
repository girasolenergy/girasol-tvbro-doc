package heartbeatmonitor.util

fun isPrime(n: Int): Boolean {
    if (n < 2) return false
    var d = 2
    while (d * d <= n) {
        if (n % d == 0) return false
        d++
    }
    return true
}

fun primeFactors(n: Int): List<Int> {
    var n = n
    val factors = mutableListOf<Int>()
    var d = 2
    while (n > 1) {
        while (n % d === 0) {
            factors += d
            n /= d
        }
        d++
        if (d * d > n && n > 1) {
            factors += n
            break
        }
    }
    return factors
}
