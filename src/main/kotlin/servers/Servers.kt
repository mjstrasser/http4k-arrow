package servers

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

val helloServer: HttpHandler = { request -> Response(Status.OK).body("Hello, ${request.query("name")}!") }

fun String?.oddOrNull(): Int? = this?.toIntOrNull()?.let { if (it % 2 != 0) it else null }

val onlyOddServer: HttpHandler = { request ->
    request.query("number").oddOrNull()?.let {
        Response(Status.OK)
    } ?: Response(Status.BAD_REQUEST)
}

val slowServer: HttpHandler = { request ->
    val delay = request.query("delay")?.toIntOrNull() ?: 0
    Thread.sleep(delay * 1000L)
    Response(Status.OK)
}

val failServer: HttpHandler = { request -> Response(Status.INTERNAL_SERVER_ERROR) }
