import exceptions.RequestFormatException
import java.math.BigInteger

class FibonacciHandler : RequestHandler {
    private val fibonacci = mutableMapOf(0 to BigInteger("1"), 1 to BigInteger("1"))

    private fun getFibonacci(n: Int): BigInteger {
        var res = fibonacci[n]
        if (res == null) {
            res = getFibonacci(n - 1).add(getFibonacci(n - 2))
            fibonacci[n] = res
        }
        return res!!
    }

    override fun handle(request: String): String {
        try {
            val n = Integer.parseInt(request)
            if (n < 0) {
                throw RequestFormatException("Expected non negative integer value, but $n found")
            }
            return getFibonacci(n).toString()
        } catch (e: NumberFormatException) {
            throw RequestFormatException("Number expected, but $request found")
        }

    }
}