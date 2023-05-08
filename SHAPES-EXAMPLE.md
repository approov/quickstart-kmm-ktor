# Shapes Example

This quickstart is written specifically for Android and iOS apps that are implemented using [`Kotlin Multiplatform for Mobile`](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html) and [`Ktor Client`](https://ktor.io/docs/welcome.html) for network requests. This quickstart provides a detailed step-by-step example of integrating Approov into an app using a simple `Shapes` example that shows a geometric shape based on a request to an API backend that can be protected with Approov.

## WHAT YOU WILL NEED
* Access to a trial or paid Approov account
* The `approov` command line tool [installed](https://approov.io/docs/latest/approov-installation/) with access to your account
* [Android Studio](https://developer.android.com/studio) installed (version Flamingo 2022.2.1 is used in this guide) if you will build the Android app. Note that the `ANDROID_HOME` value must be properly defined to allow building.
* An installation of Java 11 to support the Gradle plugins used. Note that if this is not globally available then follow the instructions when first trying to build the project to change `JAVA_HOME` or edit `Settings -> Build, Execution, Deployment -> Build Tools -> Gradle` and change the use of the `Gradle JDK` to be a specific version rather than from the `gradle-wrapper.properties` file.
* The [`Kotlin Multiplatform Mobile Plugin`](https://kotlinlang.org/docs/multiplatform-mobile-plugin-releases.html) installed in Android Studio (version 0.5.3 was used in this guide). Amongst other things, this allows iOS apps to be launched directly from Android Studio.
* [Xcode](https://developer.apple.com/xcode/) installed (version 14.3 is used in this guide) to build iOS version of application
* [Cocoapods](https://cocoapods.org) installed to support iOS building (1.11.3 used in this guide)
* An iOS device or simulator if you are using the iOS platform
* An Android device or emulator if you are using the Android platform
* The content of this repo

## RUNNING THE SHAPES APP WITHOUT APPROOV

Open the folder `shapes-app` folder using `File->Open` in Android Studio. Select the `androidApp` in the `Run/Debug` configuration and then press `Run`. This should launch the app on an Android device or simulator.

You will see two buttons:

<p>
    <img src="readme-images/app-startup.png" width="256" title="Shapes App Startup">
</p>

Click on the `Say Hello` button and you should see this:

<p>
    <img src="readme-images/hello-okay.png" width="256" title="Hello Okay">
</p>

This checks the connectivity by connecting to the endpoint `https://shapes.approov.io/v1/hello`. Now press the `Get Shape` button and you will see this (or a different shape):

<p>
    <img src="readme-images/shapes-good.png" width="256" title="Shapes Good">
</p>

This contacts `https://shapes.approov.io/v1/shapes` to get the name of a random shape. This endpoint is protected with an API key that is built into the code, and therefore can be easily extracted from the app.

You can run on iOS either by selecting `iosApp` in Android Studio and running (if you have the `KMM` plugin installd in Android Studio) or by opening the `.xcproject` in Xcode and running from there. You will see a very similiar app screen layout in the iOS version.

The subsequent steps of this guide show you how to provide better protection, either using an Approov Token or by migrating the API key to become an Approov managed secret.

## ADD THE APPROOV DEPENDENCY

The addition of Approov requires the SDK and a middleware layer to be added to the project. Both these layers are platform specific and need to be added into the app parts of the project rather than the `shared` part. However, the implementation does allow the `Ktor` calls in the `shared` code to use the appropriate platform specific implementation so that API access code can be common between both platforms. You will need to follow the instructions for both Android and iOS.

## Android

The Approov integration is available via [`jitpack`](https://jitpack.io). This allows inclusion into the project by simply specifying a dependency in the `gradle` files for the app. Follow these steps:

1. `jitpack` needs to be added in the `settings.gradle.kts` file in the top level of the project. Uncomment the relevant lines.

2. The dependency to `approov-service-okhttp` needs to be added to the `build.gradle.kts` file in the `shared` project. Uncomment the relevant lines.

3. The dependency to `approov-service-okhttp` also needs to be added to the `build.gradle.kts` file in the `androidApp` project. Uncomment the relevant lines.

Make sure you do a Gradle sync (by selecting `Sync Now` in the banner at the top of the modified `.gradle` file) after making these changes.

Note that `approov-service-okhttp` is actually an open source wrapper layer that allows you to easily use Approov with `OkHttp`. This has a further dependency to the closed source Approov SDK itself.

## iOS

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

## ENSURE THE SHAPES API IS ADDED

In order for Approov tokens to be generated or secrets managed for the shapes endpoint, it is necessary to inform Approov about it. Execute the following command:

```
approov api -add shapes.approov.io
```

Note that any Approov tokens for this domain will be automatically signed with the specific secret for this domain, rather than the normal one for your account.

## MODIFY THE SHARED CODE TO USE APPROOV

Shared code, both platform agnostic and specific, needs to be modified to use Approov. Firstly, open `shared/src/commonMain/kotlin/io/approov/shapes/ApplicationApi.kt` and make the commenting changes indicated for the `SHAPES_API_KEY` for Approov usage.

For Android, open `shared/src/androidMain/kotlin/io/approov/shapes/CustomHttpClient.kt` and uncomment the line for the `import` and line to set the `preconfigured` engine.

For iOS, open `shared/src/androidMain/kotlin/io/approov/shapes/CustomHttpClient.kt` and uncomment the lines for the `import` statements, and lines to call the `setPreconfiguredSession` and the lines associated with the `companion object`.

## MODIFY THE APP TO USE APPROOV

You will need to initialize Approov in both the Android and iOS apps. The Approov SDK needs a configuration string to identify the account associated with the app. It will have been provided in the Approov onboarding email (it will be something like `#123456#K/XPlLtfcwnWkzv99Wj5VmAxo4CrU267J1KlQyoz8Qo=`). Copy this to replace the text `<enter-your-config-string-here>`.

## Android

In the file `androidApp/src/main/java/io/approov/shapes/android/MainActivity.kt` uncomment the line that imports `io.approov.service.okhttp.ApproovService` and also the line that calls `ApproovService.initialize`, filling in your actual configuration string.

## iOS

In the file `iosApp/iosApp/ContentView.swift` uncomment the line that imports `approov_service_nsurlsession` and also the the lines that call `ApproovService.initialize` (filling in your actual configuration string) and subsequent lins that setup the session.

## REGISTER YOUR APP WITH APPROOV

Now run the app again on your chosen platform, using the previous instructions, to ensure that the generated build outputs are up to date.

### Android

You should build the APK using `Build -> Build Bundle(s) / APK(s) -> Build APK(s)` in Android Studio. Follow the `locate` link in the dialog that pops up when the buid is complete. Find the path of the generated APK (which may be called `androidApp-debug.apk` for a basic debug build).

You can use the `approov` CLI registration command as follows:

```
approov registration -add /path/to/APK
```

### iOS

You should [build an IPA](https://approov.io/docs/latest/approov-usage-documentation/#ios-ipa-extraction) with Xcode. You can then use the `approov` CLI registration command as follows:

```
approov registration -add /path/to/IPA
```

This makes a permanent registration for the provided `IPA`.

If you are building and running on an iOS simulator then there will be no `.ipa` file and you must ensure the app [always passes](https://approov.io/docs/latest/approov-usage-documentation/#adding-a-device-security-policy) on your simulator without needing to perform a registration.

> **IMPORTANT:** The registration takes around 30 seconds to propagate across the Approov Cloud Infrastructure, therefore don't try to run the app again before this time has elapsed. During development of your app you can ensure it [always passes](https://approov.io/docs/latest/approov-usage-documentation/#adding-a-device-security-policy) on your device to not have to register the app each time you modify it.

## SHAPES APP WITH APPROOV API PROTECTION

Do not make any further code changes and run the app again either from Android Studio (either Android or iOS) or run the iOS app from Xcode. Now press the `fetch` button. You should now see this (or another shape):

<p>
    <img src="readme-images/shapes-good.png" width="256" title="Shapes Good">
</p>

This means that the app is obtaining a validly signed Approov token to present to the shapes endpoint.

> **NOTE:** Running the app on an Android emulator or iOS simulator will not provide valid Approov tokens. You will need to ensure it always passes on your the device (see below).

## WHAT IF I DON'T GET SHAPES

If you don't get a valid shape then there are some things you can try. Remember this may be because the device you are using has some characteristics that cause rejection for the currently set [Security Policy](https://approov.io/docs/latest/approov-usage-documentation/#security-policies) on your account:

* Ensure that the version of the app you are running is exactly the one you registered with Approov. Also, if you are running the app from a debugger then valid tokens are not issued.
* On Android, look at the [`logcat`](https://developer.android.com/studio/command-line/logcat) output from the device. You can see the specific Approov output using `adb logcat | grep ApproovService`. This will show lines including the loggable form of any tokens obtained by the app. You can easily [check](https://approov.io/docs/latest/approov-usage-documentation/#loggable-tokens) the validity and find out any reason for a failure.
* On iOS, look at the console output from the device using the [Console](https://support.apple.com/en-gb/guide/console/welcome/mac) app from MacOS. This provides console output for a connected simulator or physical device. Select the device and search for `ApproovService` to obtain specific logging related to Approov. This will show lines including the loggable form of any tokens obtained by the app. You can easily [check](https://approov.io/docs/latest/approov-usage-documentation/#loggable-tokens) the validity and find out any reason for a failure.
* Consider using an [Annotation Policy](https://approov.io/docs/latest/approov-usage-documentation/#annotation-policies) during initial development to directly see why the device is not being issued with a valid token.
* Use `approov metrics` to see [Live Metrics](https://approov.io/docs/latest/approov-usage-documentation/#live-metrics) of the cause of failure.
* You can use a debugger, simulator or emulator and get valid Approov tokens on a specific device by ensuring it [always passes](https://approov.io/docs/latest/approov-usage-documentation/#adding-a-device-security-policy). As a shortcut, when you are first setting up, you can add a [device security policy](https://approov.io/docs/latest/approov-usage-documentation/#adding-a-device-security-policy) using the `latest` shortcut as discussed so that the `device ID` doesn't need to be extracted from the logs or an Approov token.

## SHAPES APP WITH SECRETS PROTECTION

This section provides an illustration of an alternative option for Approov protection if you are not able to modify the backend to add an Approov Token check.

Firstly, revert any previous change to `shared/src/commonMain/kotlin/io/approov/shapes/ApplicationApi.kt` for `SHAPES_URL` so that it uses `https://shapes.approov.io/v1/shapes/`, which simply checks for an API key.

Secondly, the `SHAPES_API_KEY` should also be changed to `shapes_api_key_placeholder` by commenting/uncommenting the relevant lines, effectively removing the actual API key out of the code.

We need to inform Approov that it needs to substitute the placeholder value for the real API key on the `api-key` header. This is done seperately for each platform:

For Android, in the file `androidApp/src/main/java/io/approov/shapes/android/MainActivity.kt` uncomment the line that calls `Approov.addSubstitutionHeader`.

For iOS, in the file `iosApp/iosApp/ContentView.swift` uncomment the line that calls `Approov.addSubstitutionHeader`.

Next we enable the [Secure Strings](https://approov.io/docs/latest/approov-usage-documentation/#secure-strings) feature:

```
approov secstrings -setEnabled
```

> Note that this command requires an [admin role](https://approov.io/docs/latest/approov-usage-documentation/#account-access-roles).

You must inform Approov that it should map `shapes_api_key_placeholder` to `yXClypapWNHIifHUWmBIyPFAm` (the actual API key) in requests as follows:

```
approov secstrings -addKey shapes_api_key_placeholder -predefinedValue yXClypapWNHIifHUWmBIyPFAm
```

> Note that this command also requires an [admin role](https://approov.io/docs/latest/approov-usage-documentation/#account-access-roles).

Run the app again without making any changes to the app and press the `fetch` button. You should now see this (or another shape):

<p>
    <img src="readme-images/shapes-good.png" width="256" title="Shapes Good">
</p>

This means that the registered app is able to access the API key, even though it is no longer embedded in the app code, and provide it to the shapes request.
