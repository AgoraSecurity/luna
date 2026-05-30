# Splash Screen

The Luna application uses the `androidx.core:core-splashscreen` library to provide a modern, consistent splash screen experience across different Android versions.

## Implementation Details

### Dependency
The library is included in the project via Version Catalogs and the app-level build file:
- `gradle/libs.versions.toml`: `androidx-core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version = "1.2.0" }`
- `app/build.gradle.kts`: `implementation(libs.androidx.core.splashscreen)`

### Theme Configuration
A specific theme for the splash screen is defined in `app/src/main/res/values/themes.xml`:

```xml
<style name="Theme.App.Starting" parent="Theme.SplashScreen">
    <item name="windowSplashScreenAnimatedIcon">@mipmap/ic_launcher</item>
    <item name="windowSplashScreenBackground">#FFFFFF</item>
    <item name="postSplashScreenTheme">@style/Theme.Luna</item>
</style>
```

- `windowSplashScreenAnimatedIcon`: Sets the icon displayed in the center of the splash screen.
- `windowSplashScreenBackground`: Sets the background color of the splash screen.
- `postSplashScreenTheme`: Specifies the theme to be applied to the activity after the splash screen is dismissed.

### Android Manifest
The `MainActivity` is configured to use the starting theme in `AndroidManifest.xml`:

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:label="@string/app_name"
    android:theme="@style/Theme.App.Starting">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### Activity Integration
In `MainActivity.kt`, the splash screen is initialized before the call to `super.onCreate()`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
        LunaTheme {
            ...
        }
    }
}
```

## Customization
To change the splash screen icon or background color, modify the `Theme.App.Starting` style in `themes.xml`. If an animated icon is used, ensure it follows the [official Android guidelines](https://developer.android.com/develop/ui/views/launch/splash-screen).
