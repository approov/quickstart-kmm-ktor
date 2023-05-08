//
// MIT License
//
// Copyright (c) 2016-present, Critical Blue Ltd.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
// (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
// ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
// THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package io.approov.shapes

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlin.native.concurrent.SharedImmutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@SharedImmutable
internal expect val ApplicationDispatcher: CoroutineDispatcher

interface CustomHttpClient {
    fun getClient(): HttpClient
}
@SharedImmutable
internal expect val ApplicationHttpClient: CustomHttpClient

@Serializable
data class ShapeResponse(val shape: String?)

class ApplicationApi {
    private val HELLO_URL = "https://shapes.approov.io/v1/hello/"

    //*** COMMENT THE LINE BELOW TO USE APPROOV API PROTECTION
    private val SHAPES_URL = "https://shapes.approov.io/v1/shapes/"

    //*** UNCOMMENT THE LINE BELOW TO USE APPROOV API PROTECTION
    //private val SHAPES_URL = "https://shapes.approov.io/v3/shapes/"

    //*** COMMENT THE LINE BELOW FOR APPROOV USING SECRETS PROTECTION
    private val SHAPES_API_KEY = "yXClypapWNHIifHUWmBIyPFAm"

    //*** COMMENT THE LINE BELOW FOR APPROOV USING SECRETS PROTECTION
    //private val SHAPES_API_KEY = "shapes_api_key_placeholder"

    private val client = ApplicationHttpClient.getClient()

    fun hello(callback: (String) -> Unit) {
        GlobalScope.launch(ApplicationDispatcher) {
            try {
                val response: HttpResponse = client.get {
                    url(HELLO_URL)
                }
                if (response.status.value in 200..299) {
                    callback("success")
                } else {
                    callback("failure")
                }
            }
            catch (e: Exception) {
                callback("exception: $e")
            }
        }
    }

    fun shapes(callback: (String) -> Unit) {
        GlobalScope.launch(ApplicationDispatcher) {
            try {
                val response: HttpResponse = client.get {
                    url(SHAPES_URL)
                    headers {
                        set("Api-Key", SHAPES_API_KEY)
                    }
                }
                if (response.status.value in 200..299) {
                    val shapeResponse = Json {
                        ignoreUnknownKeys = true
                    }.decodeFromString<ShapeResponse>(response.bodyAsText())
                    val shape: String? = shapeResponse.shape
                    if (shape != null) {
                        callback(shape)
                    } else {
                        callback("unknown")
                    }
                } else {
                    callback("failure")
                }
            }
            catch (e: Exception) {
                callback("exception: $e")
            }
        }
    }
}
