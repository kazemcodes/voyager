plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.vanniktech.maven.publish")
}

setupModuleForComposeMultiplatform(fullyMultiplatform = true)

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.voyagerCore)
                api(projects.voyagerNavigator)
                api(compose.runtime)
                api(compose.material)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.api)
                runtimeOnly(libs.junit.engine)
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.compose.activity)
            }
        }
    }
}
