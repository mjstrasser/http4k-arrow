package servers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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

fun call(log: (String) -> Unit, client: HttpHandler, request: Request): Response {
    log("Calling ${request.uri}")
    val response = client(request)
    log("Got response: ${response.body}")
    return response
}

@OptIn(ExperimentalTime::class)
fun main() = runBlocking {
    val mark = TimeSource.Monotonic.markNow()
    fun log(msg: String) = logger.info { "[${mark.elapsedNow()}] $msg" }

    startServers()
    log("Started servers")

    val client = ApacheClient()
    log("Created client")

//    val resp = async { call(client, requests[0]) }
//    launch {
    val responses = requests.map { req -> async { call(::log, client, req) } }
//        log("Launched")
//    }.join()
    responses.awaitAll()//.map { resp -> log("${resp.status.code}: ${resp.body}") }

    stopServers()
    log("Stopped servers")

}
