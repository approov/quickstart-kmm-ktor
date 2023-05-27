# Approov Quickstart: Kotlin Multiplatform for Mobile Ktor

This quickstart is written specifically for Android and iOS apps that are implemented using [`Kotlin Multiplatform for Mobile`](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html) and [`Ktor Client`](https://ktor.io/docs/welcome.html) for network requests. If this is not your situation then check if there is a more relevant quickstart guide available.

This quickstart provides the basic steps for integrating Approov into your app. A more detailed step-by-step guide using a [Shapes App Example](https://github.com/approov/quickstart-kmm-ktor/blob/main/SHAPES-EXAMPLE.md) is also available.

To follow this guide you should have received an onboarding email for a trial or paid Approov account.

Note that the minimum OS requirement for iOS is 11 and for Android the minimum SDK version is 21 (Android 5.0).

## ADDING THE APPROOV PACKAGE

### Android

The Approov integration is available via [`jitpack`](https:/jitpack.io). This allows inclusion into the project by simply specifying a dependency in Gradle. First add the `jitpack` repository at the top level in the project `settings.gradle.kts` file as follows:

```
dependencyResolutionManagement {
    repositories {
      ...
      maven {
            url = uri("https://jitpack.io")
      }
    }
}
```

The add the dependency in the `shared/build.gradle.kts` to allow it to be used in the shared project as follows:

```
val androidMain by getting {
      dependencies {
          ...
          implementation("com.github.approov:approov-service-okhttp:3.1.0")
      }
}
```

You must also add the dependency in `androidMain/build.gradle.kts`:

```
dependencies {
  ...
  implementation("com.github.approov:approov-service-okhttp:3.1.0")
}
```

### iOS

The Approov integration is available via [`CocoaPods`](https://cocoapods.org/). This allows inclusion into the project by simply specifying a dependency in a `Podfile` which should be placed in `iosApp/Podfile`. Create this file if not already present and include this content using an editor:

```
target 'iosApp' do
    use_frameworks!
    platform :ios, '11.0'
    pod 'approov-service-nsurlsession', '3.1.2', :source => "https://github.com/approov/approov-service-nsurlsession.git"
    pod 'approov-ios-sdk', '3.1.0', :source => "https://github.com/approov/approov-ios-sdk.git"
end
```

This includes an open source Approov specialized version of `NSURLSession` and also the closed source [Approov SDK](https://github.com/approov/approov-ios-sdk).

After creating or updating your `Podfile`, change the directory to `iosApp` in your project and type:

```
pod install
```

Note that once the pods have been installed you should open the Xcode project using the generated `iosApp.xcworkspace` as this contains the generated configuration for the pods. 

## CUSTOMIZING KTOR

In order to use Approov you must generate a specialized instantiation of `Ktor` for Android and iOS. This is only required for the construction of the `HttpClient` object, and your actual API requests can remain in your common code. Once completed, Approov tokens and/or secrets substitution will be made on API requests without any further need to modify the actual API request logic.

If you have code such as the following in `shared/src/commonMain`:

```Kotlin
val client = HttpClient()
val response: HttpResponse = client.get {
  url("https://your.domain/endpoint")
}
```

The you must modify this to use an `HttpClient` that is constructed in the platform specific parts of `shared`. Add the following code which declares an interface for obtaining a platform specific custom client:

```Kotlin
interface CustomHttpClient {
    fun getClient(): HttpClient
}
@SharedImmutable
internal expect val ApplicationHttpClient: CustomHttpClient
```

Change the original code to use the platform specific `HttpClient` as follows:

```Kotlin
val client = ApplicationHttpClient.getClient()
val response: HttpResponse = client.get {
  url("https://your.domain/endpoint")
}
```

Note that it is important that you obtain the `HttpClient` from the platform layer on each batch of requests. This is because for Android a new `HttpClient` needs to be constructed in order to obtain the latest certificate pins if they are being dynamically updated by Approov. Failure to do this means that the app would only be able to obtain new pins via an app restart.

You must then provide platform specific implementations as follows:

### Android

You must ensure the `OkHttp` based Ktor engine is installed in the `shared/build.gradle.kts` as follows:

```
val androidMain by getting {
      dependencies {
          ...
          implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
          ...
      }
}
```

Add the following code into the `shared/src/androidMain`:

```Kotlin
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.approov.service.okhttp.ApproovService

internal actual val ApplicationHttpClient: CustomHttpClient = AndroidHttpClient()

class AndroidHttpClient: CustomHttpClient  {
    override fun getClient(): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                preconfigured = ApproovService.getOkHttpClient()
            }
        }
    }
}
```

This provides a Ktor `HttpClient` that is specifically based on the `OkHttp` engine. This allows a [`preconfigured`](https://ktor.io/docs/http-client-engines.html#okhttp) client to be specified. This uses the Approov [`OkHttp`](https://github.com/approov/approov-service-okhttp) service integration to provide the specialized `OkHttp` client. This automatically adds Approov tokens and/or substitutes secrets in requests and includes dynamic pinning.

## iOS

The iOS implementation is a little more complex than Android, since there is a need to pass a custom `NSURLSession` into the shared layer from the app, which itself must use a Kotlin supplied session delegate object.

You must ensure the `Darwin` based Ktor engine is installed in the `shared/build.gradle.kts` as follows:

```
val iosMain by ... {
    ...
    dependencies {
        ...
        implementation("io.ktor:ktor-client-darwin:$ktorVersion")
        ...
    }
}
```

Add the following code into the `shared/src/iosMain`:

```Kotlin
import io.ktor.client.*
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.KtorNSURLSessionDelegate
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionDelegateProtocol

internal actual val ApplicationHttpClient: CustomHttpClient = IOSHttpClient()

class IOSHttpClient: CustomHttpClient  {
    override fun getClient(): HttpClient {
        return HttpClient(Darwin) {
            engine {
                if ((session != null) && (delegate != null))
                    usePreconfiguredSession(session!!, delegate!!)
            }
        }
    }

    companion object {
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
    }
}
```

This provides a Ktor `HttpClient` that is specifically based on the `Darwin` engine. This allows a [`preconfigured`](https://ktor.io/docs/http-client-engines.html#darwin) client to be specified. This uses the Approov [`NSURLSession`](https://github.com/approov/approov-service-nsurlsession) service integration to provide the specialized `NSURLSession`. This automatically adds Approov tokens and/or substitutes secrets in requests and includes dynamic pinning.

No reliable method was found to include the Approov `NSURLSession` and `SDK` Cocoapods directly into the shared part of the KMM project. Instead these dependencies are included directly in the `iosApp`. Thus the session must be set using the `setSession` method of the `companion` global object. The `getDelegate` method allows the mandatory Kotlin level delegate to also be provided to the `NSURLSession` instantiation. This is necessary for the `NSURLSession` delegate mechanisms to operate correctly.

## INITIALIZING APPROOV

The initialization of Approov must be performed in the iOS and Android apps, not in the shared code. The initialization must be placed in code which will only exceute once when the app is started and before any API requests that need to be protected with Approov.

The `<enter-your-config-string-here>` in these code snippets is a custom string that configures your Approov account access. This will have been provided in your Approov onboarding email.

## Android

You simply need to initialize Approov when the app is started, likely in the `onCreate` method when the app is launched in `androidApp/src`.

```Kotlin
import io.approov.service.okhttp.ApproovService

ApproovService.initialize(getApplicationContext(), "<enter-your-config-string-here>")
```

## iOS

The initialization procedure on iOS is slightly more complex. You need to initialize Approov once when the app is started, somewhere in `iosApp/src`.

```Swift
import approov_service_nsurlsession

ApproovService.initialize("<enter-your-config-string-here>", error: nil)
let delegate = IOSHttpClient.companion.getDelegate()
let session = ApproovNSURLSession.init(configuration: .default, delegate: delegate,
    delegateQueue: OperationQueue.current)
IOSHttpClient.companion.setSession(pNSURLSession: session!)
```

This calls the Objective-C implemented Approov `NSURLSession` from Swift. After Approov itself is initialized this obtains the Ktor delegate from `IOSHttpClient` shown earlier in the shared code and constructs a `ApproovNSURLSession` from it. This is then set in `IOSHttpClient` where it can be used as the preconfigured session for constructing a customized Ktor `HttpClient`.

## CHECKING IT WORKS
Once the initialization is called, it is possible for any network requests to have Approov tokens or secret substitutions made. Initially you won't have set which API domains to protect, so the requests will be unchanged. It will have called Approov though and made contact with the Approov cloud service. You will see `ApproovService` logging indicating `UNKNOWN_URL` (Android) or `unknown URL` (iOS).

On Android, you can see logging using [`logcat`](https://developer.android.com/studio/command-line/logcat) output from the device. You can see the specific Approov output using `adb logcat | grep ApproovService`. On iOS, look at the console output from the device using the [Console](https://support.apple.com/en-gb/guide/console/welcome/mac) app from MacOS. This provides console output for a connected simulator or physical device. Select the device and search for `ApproovService` to obtain specific logging related to Approov.

Your Approov onboarding email should contain a link allowing you to access [Live Metrics Graphs](https://approov.io/docs/latest/approov-usage-documentation/#metrics-graphs). After you've run your app with Approov integration you should be able to see the results in the live metrics within a minute or so. At this stage you could even release your app to get details of your app population and the attributes of the devices they are running upon.

## NEXT STEPS
To actually protect your APIs and/or secrets there are some further steps. Approov provides two different options for protection:

* [API PROTECTION](https://github.com/approov/quickstart-kmm-ktor/blob/main/API-PROTECTION.md): You should use this if you control the backend API(s) being protected and are able to modify them to ensure that a valid Approov token is being passed by the app. An [Approov Token](https://approov.io/docs/latest/approov-usage-documentation/#approov-tokens) is short lived crytographically signed JWT proving the authenticity of the call.

* [SECRETS PROTECTION](https://github.com/approov/quickstart-kmm-ktor/blob/main/SECRETS-PROTECTION.md): This allows app secrets, including API keys for 3rd party services, to be protected so that they no longer need to be included in the released app code. These secrets are only made available to valid apps at runtime.

Note that it is possible to use both approaches side-by-side in the same app.

See [REFERENCE](https://github.com/approov/quickstart-kmm-ktor/blob/main/REFERENCE.md) for a complete list of all of the Approov related methods.
