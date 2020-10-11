package mjs

import mu.KotlinLogging
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

private val logger = KotlinLogging.logger {}

val helloServer: HttpHandler = { request -> Response(Status.OK).body("Hello, ${request.query("name")}!") }
