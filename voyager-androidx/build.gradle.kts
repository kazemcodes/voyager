plugins {
    kotlin("android")
    id("com.android.library")
    id("com.vanniktech.maven.publish")
}

setupModuleForAndroidxCompose(
    composeCompilerVersion = libs.versions.composeCompiler.get()
)

android {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    api(projects.voyagerCore)

    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.savedState)
    implementation(libs.lifecycle.viewModelKtx)
    implementation(libs.lifecycle.viewModelCompose)
    api(libs.compose.runtimeSaveable)

    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.junit.api)
}
