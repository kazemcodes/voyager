import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

private fun BaseExtension.setupAndroid() {
    compileSdkVersion(33)
    defaultConfig {
        minSdk = 21
        targetSdk = 33

        versionCode = 1
        versionName = "1.0"
    }
}

fun Project.setupModuleForAndroidxCompose(
    composeCompilerVersion: String,
    withKotlinExplicitMode: Boolean = true,
) {
    val androidExtension: BaseExtension = extensions.findByType<LibraryExtension>()
        ?: extensions.findByType<com.android.build.gradle.AppExtension>()
        ?: error("Could not found Android application or library plugin applied on module $name")

    androidExtension.apply {
        setupAndroid()

        buildFeatures.apply {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = composeCompilerVersion
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        testOptions {
            unitTests.all {
                it.useJUnitPlatform()
            }
        }

        (this as ExtensionAware).extensions.configure<KotlinJvmOptions> {
            configureKotlinJvmOptions(withKotlinExplicitMode)
        }
    }
}

fun Project.setupModuleForComposeMultiplatform(
    withKotlinExplicitMode: Boolean = true,
    fullyMultiplatform: Boolean = false,
) {
    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper> {
        extensions.configure<KotlinMultiplatformExtension> {
            if (withKotlinExplicitMode) {
                explicitApi()
            }

            android {
                publishAllLibraryVariants()
            }
            jvm("desktop")

            if (fullyMultiplatform) {
                macosX64()
                macosArm64()
                iosX64("uikitX64")
                iosArm64("uikitArm64")
                iosSimulatorArm64("uikitSimulatorArm64")
            }

            sourceSets {
                /* Source sets structure
                common
                  ├─ jvm
                      ├─ android
                      ├─ desktop
                 */
                val commonMain by getting
                val commonTest by getting
                val jvmMain by creating {
                    dependsOn(commonMain)
                }
                val jvmTest by creating {
                    dependsOn(commonTest)
                }


                val desktopMain by getting {
                    dependsOn(jvmMain)
                }
                val androidMain by getting {
                    dependsOn(jvmMain)
                }
                val desktopTest by getting {
                    dependsOn(jvmTest)
                }
                val androidTest by getting {
                    dependsOn(jvmTest)
                }

                if (fullyMultiplatform) {
                    val nativeMain by creating {
                        dependsOn(commonMain)
                    }
                    val macosMain by creating {
                        dependsOn(nativeMain)
                    }
                    val macosX64Main by getting {
                        dependsOn(macosMain)
                    }
                    val macosArm64Main by getting {
                        dependsOn(macosMain)
                    }
                    val uikitMain by creating {
                        dependsOn(nativeMain)
                    }
                    val uikitX64Main by getting {
                        dependsOn(uikitMain)
                    }
                    val uikitArm64Main by getting {
                        dependsOn(uikitMain)
                    }
                    val uikitSimulatorArm64Main by getting {
                        dependsOn(uikitMain)
                    }
                }
            }
        }

        findAndroidExtension().apply {
            setupAndroid()
            sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions.configureKotlinJvmOptions(withKotlinExplicitMode)
        }
    }
}

private fun KotlinJvmOptions.configureKotlinJvmOptions(
    enableExplicitMode: Boolean
) {
    jvmTarget = JavaVersion.VERSION_1_8.toString()

    if (enableExplicitMode) freeCompilerArgs += "-Xexplicit-api=strict"
}

private fun Project.findAndroidExtension(): BaseExtension = extensions.findByType<LibraryExtension>()
    ?: extensions.findByType<com.android.build.gradle.AppExtension>()
    ?: error("Could not found Android application or library plugin applied on module $name")
