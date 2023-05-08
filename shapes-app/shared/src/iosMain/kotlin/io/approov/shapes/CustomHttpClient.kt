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
import io.ktor.client.engine.darwin.Darwin

// UNCOMMENT THE LINES BELOW FOR APPROOV
//import io.ktor.client.engine.darwin.KtorNSURLSessionDelegate
//import platform.Foundation.NSURLSession
//import platform.Foundation.NSURLSessionDelegateProtocol

internal actual val ApplicationHttpClient: CustomHttpClient = IOSHttpClient()

class IOSHttpClient: CustomHttpClient  {
    override fun getClient(): HttpClient {
        return HttpClient(Darwin) {
            engine {
                // *** UNCOMMENT THE TWO LINES BELOW FOR APPROOV ***
                //if ((session != null) && (delegate != null))
                //    usePreconfiguredSession(session!!, delegate!!)
            }
        }
    }

    // *** UNCOMMENT THE COMPANION OBJECT BELOW FOR APPROOV ***
    /*companion object {
        var session: NSURLSession? = null
        var delegate: KtorNSURLSessionDelegate? = null

        fun setSession(pNSURLSession: NSURLSession) {
            session = pNSURLSession
        }

        fun getDelegate(): NSURLSessionDelegateProtocol {
            if (delegate == null)
                delegate = KtorNSURLSessionDelegate()
            return delegate!!
        }
    }*/
}
