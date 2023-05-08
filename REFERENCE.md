# Reference
This provides a reference for all of the methods defined on `ApproovService`. This layer must be imported in the platform specific app rather than the `shared` code, so the interfaces are equivalent but slightly different between Android Kotlin and iOS Swift. To make the methods available you must import the definitions as follows.


## Android
On Android this imports access to the Java implementation that is called from Kotlin:

```Kotlin
import io.approov.service.okhttp.ApproovService
```

Various methods may throw an `ApproovException` if there is a problem. The method `getMessage()` provides a descriptive message.

If a method throws an `ApproovNetworkException` (a subclass of `ApproovException`) then this indicates the problem was caused by a networking issue, and a user initiated retry should be allowed.

If a method throws an `ApproovRejectionException` (a subclass of `ApproovException`) the this indicates the problem was that the app failed attestation. An additional method `getARC()` provides the [Attestation Response Code](https://approov.io/docs/latest/approov-usage-documentation/#attestation-response-code), which could be provided to the user for communication with your app support to determine the reason for failure, without this being revealed to the end user. The method `getRejectionReasons()` provides the [Rejection Reasons](https://approov.io/docs/latest/approov-usage-documentation/#rejection-reasons) if the feature is enabled, providing a comma separated list of reasons why the app attestation was rejected.

## iOS
On iOS this imports access to the Objective-C implementation that is called from Swift:

```Swift
import approov_service_nsurlsession
```

Various methods may throw or return an `NSError` if there is a problem. The `NSError` generated contains a dictionary with the following defined keys in the `userInfo` map of the `NSError`:

* `message`: A descriptive error message.
* `type`: Type of the error which may be `general`, `network` or `rejection`. If the type is `network` then this indicates that the error was caused by a temporary networking issue, so an option should be provided to the user to retry.
* `rejectionARC`: Only provided for a `rejection` error type. Provides the [Attestation Response Code](https://approov.io/docs/latest/approov-usage-documentation/#attestation-response-code), which could be provided to the user for communication with your app support to determine the reason for failure, without this being revealed to the end user.
* `rejectionReasons`: Only provided for a `rejection` error type. If the [Rejection Reasons](https://approov.io/docs/latest/approov-usage-documentation/#rejection-reasons) feature is enabled, this provides a comma separated list of reasons why the app attestation was rejected.

## Initialize
This initializes the Approov SDK and thus enables the Approov features. The `config` will have been provided in the initial onboarding or email or can be [obtained](https://approov.io/docs/latest/approov-usage-documentation/#getting-the-initial-sdk-configuration) using the Approov CLI. This will generate an error if a second attempt is made at initialization with a different `config`.

On Android:

```Kotlin
void initialize(Context context, String config)
```

The [application context](https://developer.android.com/reference/android/content/Context#getApplicationContext()) must be provided using the `context` parameter.

On iOS:

```Swift
public static func initialize(_ config: String, error: NSErrorPointer)
```

This provides an `NSError` if the initialization fails.

## GetOkHttpClient (Android Only)
Gets the `OkHttpClient` that enables the Approov service. This adds the Approov token in a header to requests, performs and header or query parameter substitutions and also pins the connections. The `OkHttpClient` is constructed lazily on demand but is cached if there are no changes. You *MUST* always obtain the `OkHttpClient` using this method for all requests, to ensure an up to date client is used with the latest dynamic pins.

```Kotlin
OkHttpClient getOkHttpClient()
```

If Approov has not been initialized, then this provides an `OkHttpClient` without any Approov protection. Use `setOkHttpClientBuilder` to provide any special properties.

## SetOkHttpClientBuilder (Android Only)
Sets the `OkHttpClient.Builder` to be used for constructing the Approov `OkHttpClient`. This allows a custom configuration to be set, with additional interceptors and properties.

```Kotlin
void setOkHttpClientBuilder(OkHttpClient.Builder builder)
```

## SetProceedOnNetworkFail
Indicates that the network interceptor should proceed anyway if it is not possible to obtain an Approov token due to a networking failure. If this is called then the backend API can receive calls without the expected Approov token header being added, or without header/query parameter substitutions being made. This should only ever be used if there is some particular reason, perhaps due to local network conditions, that you believe that traffic to the Approov cloud service will be particularly problematic.

On Android:

```Kotlin
void setProceedOnNetworkFail(Boolean proceed)
```

On iOS:

```Swift
public static func setProceedOnNetworkFailure(_ proceed: Bool)
```

Note that this should be used with *CAUTION* because it may allow a connection to be established before any dynamic pins have been received via Approov, thus potentially opening the channel to a MitM.

## SetApproovHeader
Sets the header that the Approov token is added on, as well as an optional prefix String (such as "`Bearer `"). Pass in an empty string if you do not wish to have a prefix. By default the token is provided on `Approov-Token` with no prefix.

On Android:

```Kotlin
void setApproovHeader(String header, String? prefix)
```

On iOS there are two independent methods to set the header and an optional prefix:

```Swift
public static func setApproovTokenHeader(_ newHeader: String)
public static func setApproovTokenPrefix(_ newHeaderPrefix: String)
```

## SetBindingHeader
Sets a binding `header` that may be present on requests being made. This is for the [token binding](https://approov.io/docs/latest/approov-usage-documentation/#token-binding) feature. A header should be chosen whose value is unchanging for most requests (such as an Authorization header). If the `header` is present, then a hash of the `header` value is included in the issued Approov tokens to bind them to the value. This may then be verified by the backend API integration.

On Android:

```Kotlin
void setBindingHeader(String header)
```

On iOS:

```Swift
public static func setBindingHeader(_ newHeader: String)
```

## AddSubstitutionHeader
Adds the name of a `header` which should be subject to [secure strings](https://approov.io/docs/latest/approov-usage-documentation/#secure-strings) substitution. This means that if the `header` is present then the value will be used as a key to look up a secure string value which will be substituted into the `header` value instead. This allows easy migration to the use of secure strings. A prefix may be specified to deal with cases such as the use of "`Bearer `" prefixed before values in an authorization header. If this is not required then simply use an empty string.

On Android:

```Kotlin
void addSubstitutionHeader(String header, String? requiredPrefix)
```

On iOS:

```Swift
public static func addSubstitutionHeader(_ header: String, prefix: String?)
```

## RemoveSubstitutionHeader
Removes a `header` previously added using `addSubstitutionHeader`.

On Android:

```Kotlin
void removeSubstitutionHeader(String header)
```

On iOS:

```Swift
public static func removeSubstitutionHeader(_ header: String)
```

## AddSubstitutionQueryParam
Adds a `key` name for a query parameter that should be subject to [secure strings](https://approov.io/docs/latest/approov-usage-documentation/#secure-strings) substitution. This means that if the query parameter is present in a URL then the value will be used as a key to look up a secure string value which will be substituted as the query parameter value instead. This allows easy migration to the use of secure strings.

On Android:

```Kotlin
void addSubstitutionQueryParam(String key)
```

On iOS:

```Swift
public static func addSubstitutionQueryParam(_ key: String)
```

## RemoveSubstitutionQueryParam
Removes a query parameter `key` name previously added using `addSubstitutionQueryParam`.

On Android:

```Kotlin
void removeSubstitutionQueryParam(String key)
```

On iOS:

```Swift
public static func removeSubstitutionQueryParam(_ key: String)
```

## AddExclusionURLRegex
Adds an exclusion URL [regular expression](https://regex101.com/) via the `urlRegex` parameter. If a URL for a request matches this regular expression then it will not be subject to any Approov protection.

On Android:

```Kotlin
void addExclusionURLRegex(String urlRegex)
```

On iOS:

```Swift
public static func addExclusionURLReg(_ urlRegex: String)
```

Note that this facility must be used with *EXTREME CAUTION* due to the impact of dynamic pinning. Pinning may be applied to all domains added using Approov, and updates to the pins are received when an Approov fetch is performed. If you exclude some URLs on domains that are protected with Approov, then these will be protected with Approov pins but without a path to update the pins until a URL is used that is not excluded. Thus you are responsible for ensuring that there is always a possibility of calling a non-excluded URL, or you should make an explicit call to fetchToken if there are persistent pinning failures. Conversely, use of those option may allow a connection to be established before any dynamic pins have been received via Approov, thus potentially opening the channel to a MitM.

## RemoveExclusionURLRegex
Removes an exclusion URL regular expression (`urlRegex`) previously added using `addExclusionURLRegex`.

On Android:

```Kotlin
void removeExclusionURLRegex(String urlRegex)
```

On iOS:

```Swift
public static func removeExclusionURLReg(_ urlRegex: String)
```

## Prefetch
Performs a background fetch to lower the effective latency of a subsequent token fetch or secure string fetch by starting the operation earlier so the subsequent fetch may be able to use cached data.

On Android:

```Kotlin
void prefetch()
```

On iOS:

```Swift
public static func prefetch()
```

## Precheck
Performs a precheck to determine if the app will pass attestation. This requires [secure strings](https://approov.io/docs/latest/approov-usage-documentation/#secure-strings) to be enabled for the account, although no strings need to be set up. This will likely require network access so may take some time to complete, and should not be called from the UI thread.

On Android:

```Kotlin
@Throws(ApproovException::class)
void precheck()
```

This throws `ApproovException` if the precheck failed.

On iOS:

```Swift
public static func precheck(_ error: NSErrorPointer)
```

This provides an `NSError` if the precheck failed.

## GetDeviceID
Gets the [device ID](https://approov.io/docs/latest/approov-usage-documentation/#extracting-the-device-id) used by Approov to identify the particular device that the SDK is running on. Note that different Approov apps on the same device will return a different ID. Moreover, the ID may be changed by an uninstall and reinstall of the app.

On Android:

```Kotlin
@Throws(ApproovException::class)
String getDeviceID()
```

This throws `ApproovException` if there was a problem obtaining the device ID.

On iOS:

```Swift
public static func getDeviceID() -> String!
```

This returns `nil` if there is an error obtaining the device ID.

## SetDataHashInToken
Directly sets the [token binding](https://approov.io/docs/latest/approov-usage-documentation/#token-binding) hash from the given `data` to be included in subsequently fetched Approov tokens. If the hash is different from any previously set value then this will cause the next token fetch operation to fetch a new token with the correct payload data hash. The hash appears in the `pay` claim of the Approov token as a base64 encoded string of the SHA256 hash of the data. Note that the data is hashed locally and never sent to the Approov cloud service. This is an alternative to using `SetBindingHeader` and you should not use both methods at the same time.

On Android:

```Kotlin
@Throws(ApproovException::class)
void setDataHashInToken(String data)
```

This throws `ApproovException` if there was a problem changing the data hash.

On iOS:

```Swift
public static func setDataHashInToken(_ data: String)
```

## FetchToken
Performs an Approov token fetch for the given `url`. This should be used in situations where it is not possible to use the networking interception to add the token. This will likely require network access so may take some time to complete, and should not be called from the UI thread.

On Android:

```Kotlin
@Throws(ApproovException::class)
String fetchToken(String url)
```

On iOS:

```Swift
public static func fetchToken(_ url: String) throws -> String
```

This throws `ApproovException` (Android) or `NSError` (iOS) if there was a problem obtaining the token.

## GetMessageSignature
Gets the [message signature](https://approov.io/docs/latest/approov-usage-documentation/#message-signing) for the given `message`. This uses an account specific message signing key that is transmitted to the SDK after a successful fetch if the facility is enabled for the account. Note that if the attestation failed then the signing key provided is actually random so that the signature will be incorrect. An Approov token should always be included in the message being signed and sent alongside this signature to prevent replay attacks.

On Android:

```Kotlin
@Throws(ApproovException::class)
String getMessageSignature(String message)
```

This throws `ApproovException` if there was a problem obtaining the signature.

On iOS:

```Swift
public static func getMessageSignature(_ message: String) -> String?
```

This returns `nil` if there was an error obtaining the signature.

## FetchSecureString
Fetches a [secure string](https://approov.io/docs/latest/approov-usage-documentation/#secure-strings) with the given `key`. If `newDef` is not `null` then a secure string for the particular app instance may be defined. In this case the new value is returned as the secure string. Use of an empty string for `newDef` removes the string entry. Note that the returned string should NEVER be cached by your app, you should call this function when it is needed.

On Android:

```Kotlin
@Throws(ApproovException::class)
String? fetchSecureString(String key, String? newDef)
```

On iOS:

```Swift
public static func fetchSecureString(_ key: String, newDef: String?) throws -> String?
```

This throws `ApproovException` (Android) or `NSError` (iOS) if there was a problem obtaining the secure string. This may require network access so may take some time to complete, and should not be called from the UI thread.

## FetchCustomJWT
Fetches a [custom JWT](https://approov.io/docs/latest/approov-usage-documentation/#custom-jwts) with the given marshaled JSON `payload`.

On Android:

```Kotlin
@Throws(ApproovException::class)
String fetchCustomJWT(String payload)
```

On iOS:

```Swift
public static func fetchCustomJWT(_ payload: String) throws -> String
```

This throws `ApproovException` (Android) or `NSError` (iOS) if there was a problem obtaining the custom JWT. This will require network access so may take some time to complete, and should not be called from the UI thread.
