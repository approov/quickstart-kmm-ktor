# Secrets Protection
You should use this option if you wish to protect app secrets, including API keys. Rather than build secrets into an app, where they might be reverse engineered, they are only provided at runtime by Approov for apps that pass attestation. This substantially improves your protection and prevents these secrets being abused by attackers.

These additional steps require access to the [Approov CLI](https://approov.io/docs/latest/approov-cli-tool-reference/), please follow the [Installation](https://approov.io/docs/latest/approov-installation/) instructions.

## ADDING API DOMAINS
In order for secrets or API keys to be protected when being transmitted externally by the app, it is necessary to inform Approov about the domains on which they may be sent. Execute the following command:

```
approov api -add your.domain -noApproovToken
```

This informs Approov that it should be active for the domain, but does not need to send Approov tokens for it. Adding the domain uses [Managed Trust Roots](https://approov.io/docs/latest/approov-usage-documentation/#managed-trust-roots) to ensure that the channel will be protected against Man-in-the-Middle (MitM) attacks.

## MIGRATING THE SECRET INTO APPROOV
It is assumed that you already have some client secrets and/or API keys in your app that you would like to migrate for [protection by Approov](https://approov.io/docs/latest/approov-usage-documentation/#secure-strings). You must inform Approov what the value of each secret is as follows:

```
approov secstrings -addKey your-secret-name -predefinedValue your-secret-value
```

> Note that this command requires an [admin role](https://approov.io/docs/latest/approov-usage-documentation/#account-access-roles).

These values can be changed at any time and will propagate within 5 minutes to all running instances of your apps. Since earlier released versions of the app may have already leaked `your-secret-value`, you may wish to refresh the secret at some later point when any older version of the app is no longer in use. You can of course do this update over-the-air using Approov without any need to modify the app.

You can define up to 16 different secret values in this way.

## SUBSTITUTING THE SECRET AUTOMATICALLY
If the secret is presented in an API header or query parameter, and you are able to use the `ApproovService` networking stack, then Approov can automatically substitute the secret value at runtime. You should use this method wherever possible.

If the published code of your app currently uses `your-secret-value` then replace it with the value `your-secret-name`. This provides a placeholder value which can then be automatically substituted with the actual secret value at runtime, for validly attesting apps. The shipped app code will only contain the placeholder values.

If the secret value needs to be provided on the header `your-header` then it is necessary to notify the `ApproovService` that the header is subject to substitution. You do this by making the call once, after initialization.

On Android:

```kotlin
ApproovService.addSubstitutionHeader("your-header", null)
```

On iOS:

```Swift
ApproovService.addSubstitutionHeader("your-header", requiredPrefix: nil)
```

With this in place Approov should replace the `your-secret-name` with `your-secret-value` as required when the app passes attestation. Since the mapping lookup is performed on the secret name you have the flexibility of providing different secrets on different API calls, even if they are passed with the same header name.

You can see a [worked example](https://github.com/approov/quickstart-kmm-ktor/blob/main/SHAPES-EXAMPLE.md#shapes-app-with-secrets-protection) for the Shapes app.

If the secret value is provided as a parameter in a URL query string with the name `your-param` then it is necessary to notify Approov that the query parameter is subject to substitution.  You are recommended to make this call just after initializing the `ApproovService` in the app:

```
ApproovService.addSubstitutionQueryParam("your-param")
```

After this Approov should transform any instance of a URL such as `https://your.domain/endpoint?your-param=your-secret-name` into `https://your.domain/endpoint?your-param=your-secret-vlaue`.

## OBTAINING THE SECRET EXPLICITLY
In some cases it might not be possible to automatically substitute a secret in a header or query parameter. This might be because the secret is used in other ways in your application.

In this case it is possible to make an explicit call at runtime to obtain the secret value, for apps passing attestation. Here is an example for using the required method in `ApproovService`.

On Android:

```kotlin
import io.approov.service.okhttp.ApproovException
import io.approov.service.okhttp.ApproovRejectionException
import io.approov.service.okhttp.ApproovNetworkException

...

var secret: String?
try {
    secret = ApproovService.fetchSecureString("your-secret-name", null)
}
catch(e: ApproovRejectionException) {
    // failure due to the attestation being rejected, e.getARC() and e.getRejectionReasons() may be
    // used to present information to the user (note e.getRejectionReasons() is only available if the
    // feature is enabled, otherwise it is always an empty string)
}
catch(e: ApproovNetworkException) {
    // failure due to a potentially temporary networking issue, allow for a user initiated retry
}
catch(e: ApproovException) {
   // a more permanent error, see e.getMessage()
}
// use secret as required, but never cache or store its value - note secret will be null if
// the provided secret name is not defined
```

On iOS:

```Swift
var secret: String?
do {
    try secret = ApproovService.fetchSecureString("your-secret-name", newDef: nil)
} catch let error as NSError {
    if let type = error.userInfo["type"] as? String {
        if (type == "rejection") {
            // failure due to the attestation being rejected, see error.userInfo["message"] - Attestation
            // Response Code (ARC) for the failure will be provided in error.userInfo["rejectionARC"] and
            // comma separated reasons may be provided in error.userInfo["rejectionReasons"]
        } else if (type == "network") {
            // failure due to a potentially temporary networking issue, allow for a user initiated retry,
            // see error.userInfo["message"]
        } else {
            // a more permanent error, see error.userInfo["message"]
        }
    }
} catch {
    // unexpected error
}
// use secret as required, but never cache or store its value - note secret will be nil if the
// provided secret name is not defined
```

> **IMPORTANT:** The secrets obtained should only ever be communicated externally from the app over channels using the Approov networking stack and which have been added as protected API domains. If not then it is possible for them to be intercepted by a Man-in-the-Middle (MitM) attack.

## HANDLING REJECTIONS
If the app is not recognized as being valid by Approov then an exception is thrown on the network request and the API call is not completed. The secret value will never be communicated to the app in this case.

If you wish to provide more specific information about the cause of the rejection then you must use the [precheck](#prechecking) capability that can provide more detailed information in the error returned to the failure function. Note that since this code needs to be implemented in the platform specific app rather than the shared code, you will likely need to implement this as an exceptional case to handle situations where the attestation is failing.

If you wish to provide more detailed rejection cause feedback then enable the [Rejection Reasons](https://approov.io/docs/latest/approov-usage-documentation/#rejection-reasons) feature:

```
approov policy -setRejectionReasons on
```

> Note that this command requires an [admin role](https://approov.io/docs/latest/approov-usage-documentation/#account-access-roles).

You will then be able to get information in rejection reasons in the form of a comma separated list of [device properties](https://approov.io/docs/latest/approov-usage-documentation/#device-properties) responsible for causing the rejection.

## ADD YOUR SIGNING CERTIFICATE TO APPROOV
You should add the signing certificate used to sign apps so that Approov can recognize your app as being official.

Codesigning must also be enabled, if you need assistance please check [Microsoft's codesigning support](https://docs.microsoft.com/en-us/xamarin/ios/deploy-test/provisioning/) or [Android deploy signing](https://docs.microsoft.com/en-us/xamarin/android/deploy-test/signing/?tabs=macos). Make sure you have selected the correct project (Shapes.App.iOS), build mode (Release) and target device (Generic Device) settings.

### Android
Add the local certificate used to sign apps in Android Studio. The following assumes it is in PKCS12 format:

```
approov appsigncert -add ~/.android/debug.keystore -storePassword android -autoReg
```

Note, on Windows you need to substitute \ for / in the above command and the full path specified for the user home directory instead of ~.

See [Android App Signing Certificates](https://approov.io/docs/latest/approov-usage-documentation/#android-app-signing-certificates) if your keystore format is not recognized or if you have any issues adding the certificate. This also provides information about adding certificates for when releasing to the Play Store. Note also that you need to apply specific [Android Obfuscation](https://approov.io/docs/latest/approov-usage-documentation/#android-obfuscation) rules when creating an app release.

### iOS
These are available in your Apple development account portal. Go to the initial screen showing program resources:

![Apple Program Resources](readme-images/program-resources.png)

Click on `Certificates` and you will be presented with the full list of development and distribution certificates for the account. Click on the certificate being used to sign applications from your particular Xcode installation and you will be presented with the following dialog:

![Download Certificate](readme-images/download-cert.png)

Now click on the `Download` button and a file with a `.cer` extension is downloaded, e.g. `development.cer`. Add it to Approov with:

```
approov appsigncert -add development.cer -autoReg
```

If it is not possible to download the correct certificate from the portal then it is also possible to [add app signing certificates from the app](https://approov.io/docs/latest/approov-usage-documentation/#adding-apple-app-signing-certificates-from-app).

> **IMPORTANT:** Apps built to run on the iOS simulator are not code signed and thus auto-registration does not work for them. In this case you can consider [forcing a device ID to pass](https://approov.io/docs/latest/approov-usage-documentation/#forcing-a-device-id-to-pass) to get a valid attestation.

## FURTHER OPTIONS
See [Exploring Other Approov Features](https://approov.io/docs/latest/approov-usage-documentation/#exploring-other-approov-features) for information about additional Approov features you may wish to try.

### Development Key
You may wish to [set a development key](https://approov.io/docs/latest/approov-usage-documentation/#using-a-development-key) in order to force an app to be passed, if it may be resigned by a different app signing certificate to which you don't have access. Perform the call:

```
ApproovService.setDevKey("uDW9FuLVpL1_4zo1")
```

See [using a development key](https://approov.io/docs/latest/approov-usage-documentation/#using-a-development-key) to understand how to obtain the development key which is the parameter to the call.

### Header Prefixes
In some cases the value to be substituted on a header may be prefixed by some fixed string. A common case is the presence of `Bearer` included in an authorization header to indicate the use of a bearer token. In this case you can specify a prefix as follows:

```
ApproovService.addSubstitutionHeader("Authorization", "Bearer ");
```

This causes the `Bearer` prefix to be stripped before doing the lookup for the substitution, and the `Bearer` prefix added to the actual secret value as part of the substitution.

### App Instance Secure Strings
In addition to secret values defined in the Approov cloud, it is also possible to get and set secure string values independently for each app instance. These are never communicated to the Approov cloud service, but are encrypted at rest using keys which can only be retrieved by passing apps. You can use this feature to protect user authorization tokens issued to individual apps or other sensitive customer data, for instance.

App instance secure strings can be set and retrived using the [secret fetching code](#obtaining-the-secret-explicitly). You can define a new value for a given secret name by passing a value in the second parameter of `fetchSecureString`, rather than `null`. An empty string may be used to delete the secure string completely.

### Prefetching
If you wish to reduce the latency associated with fetching the first Approov token, then make this call just after initializing the `ApproovService` in the app:

```
ApproovService.prefetch()
```

This initiates the process of fetching an Approov token in the background as soon as Approov is initialized, so that a cached token is available immediately when subsequently needed.

Note that there is no point in performing a prefetch if you are using token binding and the binding value is changed.

### Prechecking
You may wish to do an early check in your app to present a warning to the user if it is not going to be able to access secrets because it fails the attestation process. You should make the check immediately after initializing the `ApproovService` in the app to determine if execution should proceed or not.

On Android:

```kotlin
import io.approov.service.okhttp.ApproovException
import io.approov.service.okhttp.ApproovRejectionException
import io.approov.service.okhttp.ApproovNetworkException

...

try {
    ApproovService.precheck()
}
catch(e: ApproovRejectionException) {
    // failure due to the attestation being rejected, e.getARC() and e.getRejectionReasons() may
    // be used to present information to the user (note e.getRejectionReasons() is only available
    // if the feature is enabled, otherwise it is always an empty string)
}
catch(e: ApproovNetworkException) {
    // failure due to a potentially temporary networking issue, allow for a user initiated retry
}
catch(e: ApproovException) {
   // a more permanent error, see e.getMessage()
}
```

On iOS:

```Swift
var error: NSError?
ApproovService.precheck(&error)
if error != nil {
    if let type = error!.userInfo["type"] as? String {
        if (type == "rejection") {
            // failure due to the attestation being rejected, see error.userInfo.message - Attestation
            // Response Code (ARC) for the failure will be provided in error.userInfo["rejectionARC"] and
            // comma separated reasons may be provided in error.userInfo["rejectionReasons"]
        } else if (type == "network") {
            // failure due to a potentially temporary networking issue, allow for a user initiated retry,
            // see error.userInfo["message"]
        } else {
            // a more permanent error, see error.userInfo["message"]
        }
    }
}
```

> Note you should NEVER use this as the only form of protection in your app, this is simply to provide an early indication of failure to your users as a convenience. You must always also have secrets essential to the operation of your app, or access to backend API services, protected with Approov. This is because, although the Approov attestation itself is heavily secured, it may be possible for an attacker to bypass its result or prevent it being called at all. When the app is dependent on the secrets protected, it is not possible for them to be obtained at all without passing the attestation.
