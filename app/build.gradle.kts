plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.tarmiga.luna"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.tarmiga.luna"
    minSdk = 29
    targetSdk = 35
    versionCode = 2
    versionName = "0.0.2"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      ndk { debugSymbolLevel = "SYMBOL_TABLE" }
    }
  }
  testOptions { unitTests.isReturnDefaultValues = true }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures { compose = true }
  lint {
    abortOnError = true
    checkReleaseBuilds = true
    warningsAsErrors = false
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  testImplementation(libs.junit)
  testImplementation(libs.mockito.core)
  testImplementation("org.json:json:20231013")
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)

  // Room
  implementation("androidx.room:room-runtime:2.8.4")
  implementation("androidx.room:room-ktx:2.8.4")
  ksp("androidx.room:room-compiler:2.8.4")

  // Navigation
  implementation(libs.androidx.navigation.compose)
}
