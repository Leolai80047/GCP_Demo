package io.kraftsman.handlers

import com.github.javafaker.Faker
import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import io.kraftsman.model.News
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.helper.HttpConnection
import java.net.HttpURLConnection
import java.util.*

class SimpleJsonHandler: HttpFunction {
    override fun service(request: HttpRequest, response: HttpResponse) {
        if ("GET" != request.method.toUpperCase()) {
            with(response) {
                setStatusCode(HttpURLConnection.HTTP_BAD_METHOD)
                writer.write("Bad method")
            }

            return
        }

        val limit = request.queryParameters["limit"]?.first()?.toIntOrNull()?: 10

        val faker = Faker.instance(Locale.TRADITIONAL_CHINESE)

        val news = (1..limit).map {
            News(
                id = it.toString(),
                title = "",
                author = faker.name().fullName(),
                content = faker.lorem().paragraph(),
                coverUrl = "https://${faker.internet().url()}",
                permalink =  "https://${faker.internet().url()}",
                publishedAt = null
            )
        }

        with(response) {
            setStatusCode(HttpURLConnection.HTTP_OK)
            setContentType("application/json")
            writer.write(
                Json.encodeToString(
                    mapOf("data" to news)
                )
            )
        }
    }
}