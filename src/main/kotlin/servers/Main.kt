package servers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.http4k.client.ApacheClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.asServer
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
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

val requests = flowOf(
        Request(Method.GET, "http://localhost:8000/").query("name", "Alginon"),
        Request(Method.GET, "http://localhost:8001/").query("number", "103"),
        Request(Method.GET, "http://localhost:8001/").query("number", "54"),
        Request(Method.GET, "http://localhost:8002/").query("delay", "2"),
        Request(Method.GET, "http://localhost:8003/"),
)

@OptIn(ExperimentalTime::class)
fun callDirectly(mark: TimeMark, client: HttpHandler) {
    fun log(msg: String) = logger.info { "[${mark.elapsedNow()}] $msg" }

    client(Request(Method.GET, "http://localhost:8000/").query("name", "Alginon"))
    log("Sent request to helloServer")

    client(Request(Method.GET, "http://localhost:8001/").query("number", "103"))
    log("Sent odd request to onlyOddServer")

    client(Request(Method.GET, "http://localhost:8001/").query("number", "54"))
    log("Sent even request to onlyOddServer")

    client(Request(Method.GET, "http://localhost:8002/").query("delay", "2"))
    log("Sent 2-second delay request to delayServer")

    client(Request(Method.GET, "http://localhost:8003/"))
    log("Sent request to failServer")

}

@OptIn(ExperimentalTime::class)
fun main() {
    val clock = TimeSource.Monotonic
    val mark = clock.markNow()

    fun log(msg: String) = logger.info { "[${mark.elapsedNow()}] $msg" }

    startServers()
    log("Started servers")

    val client = ApacheClient()
    log("Created client")

    callDirectly(mark, client)

    runBlocking {
        log("Created coroutine scope")
        val flow = requests.map { req ->
            client(req)
        }
        log("Created flow")
        flow.map { response ->
            log("Got response: $response")
        }
    }

    stopServers()
    log("Stopped servers")

}
