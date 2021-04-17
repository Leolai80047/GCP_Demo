package io.kraftsman.handlers

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import io.kraftsman.model.News
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import tw.ktrssreader.generated.CustomChannelParser
import java.net.HttpURLConnection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class RssDetailHandler: HttpFunction {
    private val rssSource = mapOf(
        "kotlin-blog" to "https://blog.jetbrains.com/kotlin/feed/",
        "andy-blog" to "https://andyludeveloper.medium.com/feed",
        "drawson-medium" to "https://drawson.medium.com/feed",
    )

    override fun service(request: HttpRequest, response: HttpResponse) {
        if ("GET" != request.method.toUpperCase()) {
            with(response) {
                setStatusCode(HttpURLConnection.HTTP_BAD_METHOD)
                writer.write("Bad method")
            }
            return
        }

        val news = mutableListOf<News>()
        val okHttpClient = OkHttpClient()

        rssSource.forEach { name, url ->
            val rssRequest = Request.Builder().url(url).build()
            val xmlString = okHttpClient.newCall(rssRequest).execute()?.body?.string()?: ""
            val rssChannel = CustomChannelParser.parse(xmlString)
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss", Locale.ENGLISH)

            rssChannel.items.forEachIndexed { index, item ->

                val parsedPubDate = item.pubDate?.substringAfter(", ")
                    ?.replace(" +0000", "")
                    ?.replace(" GMT", "")
                    ?.trim()
                    .toString()

                news.add(
                    News(
                        id = "$name-${index + 1}",
                        title = item.title ?: "",
                        author = item.author ?: "",
                        content =  item.description ?: "",
                        coverUrl = item.featuredImage ?: "",
                        permalink = item.link ?: "",
                        publishedAt = LocalDateTime.parse(parsedPubDate, formatter),
                    )

                )


            }
        }


        with(response) {

            setStatusCode(HttpURLConnection.HTTP_OK)
            setContentType("plain/text")
            writer.write(
                Json.encodeToString(
                    mapOf("data" to news)
                )
            )
        }
    }
}