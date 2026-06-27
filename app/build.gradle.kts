plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

val lifeLabApplicationId = providers.gradleProperty("LIFELAB_APPLICATION_ID").orElse("com.study.lifelab")
val lifeLabVersionCode = providers.gradleProperty("LIFELAB_VERSION_CODE").map { it.toInt() }.orElse(1)
val lifeLabVersionName = providers.gradleProperty("LIFELAB_VERSION_NAME").orElse("1.0")

val releaseStoreFile = providers.gradleProperty("LIFELAB_RELEASE_STORE_FILE")
val releaseStorePassword = providers.gradleProperty("LIFELAB_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = providers.gradleProperty("LIFELAB_RELEASE_KEY_ALIAS")
val releaseKeyPassword = providers.gradleProperty("LIFELAB_RELEASE_KEY_PASSWORD")
val hasReleaseSigningProperties = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { it.isPresent }

android {
    namespace = "com.example.lifelab"
    compileSdk = 35

    defaultConfig {
        applicationId = lifeLabApplicationId.get()
        minSdk = 26
        targetSdk = 35
        versionCode = lifeLabVersionCode.get()
        versionName = lifeLabVersionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigningProperties) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile.get())
                storePassword = releaseStorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigningProperties) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.04.01"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.04.01"))

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("com.google.dagger:hilt-android:2.52")
    implementation("io.coil-kt:coil-compose:2.7.0")

    ksp("com.google.dagger:hilt-android-compiler:2.52")
    ksp("androidx.room:room-compiler:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.20")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.room:room-testing:2.6.1")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
