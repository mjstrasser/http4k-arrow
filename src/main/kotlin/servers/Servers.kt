package servers

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then

const val REQUEST_ID_HEADER = "Request-Id"

val requestIdFilter: Filter = Filter { next ->
    { request -> next(request).header(REQUEST_ID_HEADER, request.header(REQUEST_ID_HEADER)) }
}

val helloServer: HttpHandler = requestIdFilter.then { request -> Response(Status.OK).body("Hello, ${request.query("name")}!") }

fun String?.oddOrNull(): Int? = this?.toIntOrNull()?.let { if (it % 2 != 0) it else null }

val onlyOddServer: HttpHandler = requestIdFilter.then { request ->
    request.query("number").oddOrNull()?.let {
        Response(Status.OK).body("$it")
    } ?: Response(Status.BAD_REQUEST).body("bad")
}

val slowServer: HttpHandler = requestIdFilter.then { request ->
    val delay = request.query("delay")?.toIntOrNull() ?: 0
    Thread.sleep(delay * 1000L)
    Response(Status.OK).body("Waited!!")
}

val failServer: HttpHandler = requestIdFilter.then { Response(Status.INTERNAL_SERVER_ERROR).body("error") }
