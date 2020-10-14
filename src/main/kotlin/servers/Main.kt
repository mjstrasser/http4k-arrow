package servers

import arrow.fx.IO
import arrow.fx.extensions.fx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.asServer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}


val allServers = listOf(
        helloServer.asServer(Netty(8000)),
        onlyOddServer.asServer(Netty(8001)),
        slowServer.asServer(Netty(8002)),
        failServer.asServer(Netty(8003))
)

fun startServers() = allServers.forEach(Http4kServer::start)
fun stopServers() = allServers.forEach(Http4kServer::stop)

fun callAll(): IO<Unit> = IO.fx {
    val client = ApacheClient()

    !effect { client(Request(Method.GET, "http://localhost:8000/").query("name", "Alginon")) }
    logger.info { "Sent request to helloServer" }

    !effect { client(Request(Method.GET, "http://localhost:8001/").query("number", "103")) }
    logger.info { "Sent odd request to onlyOddServer" }

    !effect { client(Request(Method.GET, "http://localhost:8001/").query("number", "54")) }
    logger.info { "Sent even request to onlyOddServer" }

    !effect { client(Request(Method.GET, "http://localhost:8002/").query("delay", "3")) }
    logger.info { "Sent 3-second delay request to delayServer" }

    !effect { client(Request(Method.GET, "http://localhost:8003/")) }
    logger.info { "Sent request to failServer" }
}


@OptIn(ExperimentalTime::class)
fun main() {
    val clock = TimeSource.Monotonic
    val mark = clock.markNow()

    startServers()
    logger.info { "Started [${mark.elapsedNow()}]" }

    val client = ApacheClient()

    client(Request(Method.GET, "http://localhost:8000/").query("name", "Alginon"))
    logger.info { "Sent request to helloServer [${mark.elapsedNow()}]" }

    client(Request(Method.GET, "http://localhost:8001/").query("number", "103"))
    logger.info { "Sent odd request to onlyOddServer [${mark.elapsedNow()}]" }

    client(Request(Method.GET, "http://localhost:8001/").query("number", "54"))
    logger.info { "Sent even request to onlyOddServer [${mark.elapsedNow()}]" }

    client(Request(Method.GET, "http://localhost:8002/").query("delay", "2"))
    logger.info { "Sent 2-second delay request to delayServer [${mark.elapsedNow()}]" }

    client(Request(Method.GET, "http://localhost:8003/"))
    logger.info { "Sent request to failServer [${mark.elapsedNow()}]" }

    stopServers()
    logger.info { "Stopped [${mark.elapsedNow()}]" }

}
