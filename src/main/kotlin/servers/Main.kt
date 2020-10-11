package servers

import mu.KotlinLogging
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.asServer

private val logger = KotlinLogging.logger {}


val allServers = listOf(
        helloServer.asServer(Netty(8000)),
        onlyOddServer.asServer(Netty(8001)),
        slowServer.asServer(Netty(8002)),
        failServer.asServer(Netty(8003))
)

fun startServers() = allServers.forEach(Http4kServer::start)
fun stopServers() = allServers.forEach(Http4kServer::stop)

fun main() {
    startServers()
    logger.info { "Started" }

    val client = ApacheClient()

    client(Request(Method.GET, "http://localhost:8000/").query("name", "Alginon"))
    logger.info { "Sent request to helloServer" }
    client(Request(Method.GET, "http://localhost:8001/").query("number", "103"))
    logger.info { "Sent odd request to onlyOddServer" }
    client(Request(Method.GET, "http://localhost:8001/").query("number", "54"))
    logger.info { "Sent even request to onlyOddServer" }
    client(Request(Method.GET, "http://localhost:8002/").query("delay", "3"))
    logger.info { "Sent 3-second delay request to delayServer" }
    client(Request(Method.GET, "http://localhost:8003/"))
    logger.info { "Sent request to failServer" }

    stopServers()
    logger.info { "Stopped" }
}
