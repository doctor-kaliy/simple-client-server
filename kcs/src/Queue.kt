import exceptions.ClientException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class Client(val socket: Socket) {
    val input: BufferedReader
    val output: PrintWriter
    init {
        try {
            input = BufferedReader(InputStreamReader(socket.getInputStream()))
        } catch (e: IOException) {
            throw ClientException("Error occurred while creating client's input channel: ${e.message}")
        }
        try {
            output = PrintWriter(socket.getOutputStream(), true)
        } catch (e: IOException) {
            throw ClientException("Error occurred while creating client's output channel: ${e.message}")
        }
    }
}

class Queue {
    private val clients = mutableListOf<Client>()

    fun add(socket: Socket) {
        synchronized(this) {
            val client = Client(socket)
            clients.add(client)
        }
    }

    fun poll(): List<Client> {
        synchronized(this) {
            val predicate: (Client) -> Boolean = { client ->
                client.socket.isClosed || client.socket.isInputShutdown || client.socket.isOutputShutdown
            }
            clients.filter(predicate).forEach { it.socket.use {} }
            clients.removeIf(predicate)
            return clients.filter { client ->
                try {
                    client.input.ready()
                } catch (_: IOException) {
                    false
                }
            }
        }
    }
}