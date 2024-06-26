package net.perfectdreams.directdiscordcdn

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    val http = HttpClient(CIO)

    val server = embeddedServer(Netty, port = 8080) {
        routing {
            // Example: https://cdn.discordapp.com/attachments/547119872568459284/1033477554612478063/lori_profile.png
            get("/{type}/{id}/{id2}/{file}") {
                val type = call.parameters["type"]
                if (type != "attachments" && type != "ephemeral-attachments") {
                    call.respondText(
                        "Not an attachment!",
                        status = HttpStatusCode.UnprocessableEntity,
                    )
                    return@get
                }
                val id = call.parameters["id"]
                val id2 = call.parameters["id2"]
                val file = call.parameters["file"]
                val fileExtension = file?.substringAfterLast(".")

                println(call.request.queryString())
                val originalFileResponse = http.get("https://cdn.discordapp.com/$type/$id/$id2/$file?${call.request.queryString()}")
                val bytes = originalFileResponse.readBytes()

                call.respondBytes(
                    bytes,
                    when (fileExtension) {
                        "txt" -> ContentType.Text.Plain
                        "log" -> ContentType.Text.Plain
                        "png" -> ContentType.Image.PNG
                        "gif" -> ContentType.Image.GIF
                        "jpeg", "jpg" -> ContentType.Image.JPEG
                        else -> ContentType.Any
                    }
                )
            }
        }
    }
    server.start(true)
}