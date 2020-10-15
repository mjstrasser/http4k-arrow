package servers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.http4k.client.ApacheClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.asServer
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private val logger = KotlinLogging.logger {}


val allServers = listOf(
        helloServer.asServer(Netty(8000)),
        onlyOddServer.asServer(Netty(8001)),
        slowServer.asServer(Netty(8002)),
        failServer.asServer(Netty(8003))
)

fun startServers() = allServers.forEach(Http4kServer::start)
fun stopServers() = allServers.forEach(Http4kServer::stop)

val requests = listOf(
        Request(Method.GET, "http://localhost:8000/").query("name", "Alginon"),
        Request(Method.GET, "http://localhost:8001/").query("number", "103"),
        Request(Method.GET, "http://localhost:8002/").query("delay", "2"),
        Request(Method.GET, "http://localhost:8001/").query("number", "54"),
        Request(Method.GET, "http://localhost:8003/"),
)

fun call(client: HttpHandler, request: Request): Response {
    return client(request)
}

@OptIn(ExperimentalTime::class)
fun main() {
    val mark = TimeSource.Monotonic.markNow()
    fun log(msg: String) = logger.info { "[${mark.elapsedNow()}] $msg" }

    startServers()
    log("Started servers")

    val client = ApacheClient()
    log("Created client")

    runBlocking {
        requests.map { withContext(Dispatchers.Default) { call(client, it).also { log("${it.body}") } } }
    }

    stopServers()
    log("Stopped servers")

}
