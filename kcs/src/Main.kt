import exceptions.ClientException
import exceptions.RequestFormatException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Thread.sleep
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException
import kotlin.concurrent.thread

fun client(host: String, port: Int) {
    Socket(host, port).use { clientSocket ->
        try {
            PrintWriter(clientSocket.getOutputStream(), true).use { output ->
                try {
                    BufferedReader(InputStreamReader(clientSocket.getInputStream())).use { input ->
                        while (true) {
                            val userCommand = readLine()
                            if (userCommand == "") {
                                break
                            }
                            try {
                                val data = Integer.parseInt(userCommand)
                                output.println(data)
                                val response = input.readLine()
                                println(response)
                            } catch (e: NumberFormatException) {
                                println("New line should be equal to \\n or be an integer number")
                            }
                        }
                    }
                } catch (e: IOException) {
                    System.err.println("Error while receiving response from server: ${e.message}")
                }
            }
        } catch (e: IOException) {
            System.err.println("Error while sending data to server: ${e.message}")
        }
    }
}

fun server(host: String, port: Int, requestHandler: RequestHandler) {
    val serverSocket = ServerSocket(port, 50, InetAddress.getByName(host))
    val queue = Queue()
    thread(start = true) {
        while (true) {
            val clients = queue.poll()
            clients.forEach { client ->
                with(client) {
                    try {
                        val request = input.readLine()
                        if (request != null) {
                            var response: String
                            try {
                                response = requestHandler.handle(request)
                            } catch (e: RequestFormatException) {
                                response = "Request format error occurred: ${e.message}"
                                System.err.println(response)
                            }
                            try {
                                output.println(response)
                            } catch (e: IOException) {
                                socket.use {}
                                System.err.println("Error occurred while sending response: ${e.message}")
                            }
                        }
                    } catch (e: IOException) {
                        socket.use {}
                        System.err.println("Error occurred while receiving request: ${e.message}")
                    }
                }
            }
            sleep(200)
        }
    }

    while (true) {
        try {
            val socket = serverSocket.accept()
            queue.add(socket)
        } catch (e: ClientException) {
            System.err.println("Error while adding new client. ${e.message}")
        } catch (e: IOException) {
            System.err.println("Error while connecting new client: ${e.message}")
        }
    }
}

fun main(args: Array<String>) {
    if (args.size == 3) {
        when(args[0]) {
            "client" -> {
                try {
                    client(args[1], Integer.parseInt(args[2]))
                } catch (e: UnknownHostException) {
                    System.err.println("Unknown host error: ${e.message}")
                } catch (e: IOException) {
                    System.err.println("Error while creating a connection: ${e.message}")
                }
            }
            "server" -> {
                try {
                    server(args[1], Integer.parseInt(args[2]), FibonacciHandler())
                } catch (e: IOException) {
                    System.err.println("Problem while starting server: ${e.message}")
                } catch (e: NumberFormatException) {
                    System.err.println("Second argument should be a number")
                }
            }
            else -> {
                System.err.println("Invalid first argument ${args[0]}. Expected client or server")
            }
        }
    } else {
        System.err.println("Wrong argument's format.\n Expected: server <host> <port> or client <host> <port>")
    }
}