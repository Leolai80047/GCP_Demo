package io.kraftsman.handlers

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import java.io.IOException
import java.net.HttpURLConnection

class PlainTextHandler: HttpFunction {

    @Throws(IOException::class)
    override fun service(request: HttpRequest, response: HttpResponse) {

        if ("GET" != request.method.toUpperCase()) {
            with(response) {
                setStatusCode(HttpURLConnection.HTTP_BAD_METHOD)
                writer.write("Bad method")
            }
            return
        }


        with(response) {
            setStatusCode(HttpURLConnection.HTTP_OK)
            setContentType("plain/text")
            writer.write("Hello, Cloud Functions")
        }
    }
}