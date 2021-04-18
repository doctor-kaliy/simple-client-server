import exceptions.RequestFormatException
import java.math.BigInteger

class FibonacciHandler : RequestHandler {

    companion object FibonacciHandler {
        private val ONE: BigInteger = BigInteger.ONE
    }

    private fun getFibonacci(n: Int): BigInteger {
        return when (n) {
            0 -> ONE
            1 -> ONE
            else -> {
                var cur = ONE
                var prev = ONE
                for (i in 2..n) {
                    val new = cur + prev
                    prev = cur
                    cur = new
                }
                cur
            }
        }
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