package servers

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.http4k.client.ApacheClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.asServer
import kotlin.random.Random
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

fun Request.requestId() = this.header(REQUEST_ID_HEADER, Random.nextLong(0xfffffff).toString(16))

val requests = listOf(
        Request(Method.GET, "http://localhost:8000/").query("name", "Alginon").requestId(),
        Request(Method.GET, "http://localhost:8001/").query("number", "103").requestId(),
        Request(Method.GET, "http://localhost:8002/").query("delay", "2").requestId(),
        Request(Method.GET, "http://localhost:8001/").query("number", "54").requestId(),
        Request(Method.GET, "http://localhost:8003/").requestId(),
)

fun call(log: (String) -> Unit, client: HttpHandler, request: Request): Response {
    log("[${request.header(REQUEST_ID_HEADER)}] Calling ${request.uri}")
    return client(request)
}

@OptIn(ExperimentalTime::class)
fun main() = runBlocking {
    val mark = TimeSource.Monotonic.markNow()
    fun log(msg: String) = logger.info { "[${mark.elapsedNow()}] $msg" }
    fun logResponse(resp: Response) = log("[${resp.header(REQUEST_ID_HEADER)}] ${resp.status.code}: ${resp.body}")

    startServers()
    log("Started servers")

    val client = ApacheClient()

    var responseList: List<Response> = listOf()
    launch {
        log("Launched")
        val responses = requests.map { req -> async { call(::log, client, req) } }
        responseList = responses.awaitAll()
        log("End of launched scope")
    }.join()

    responseList.map(::logResponse)

    stopServers()
    log("Stopped servers")

}
