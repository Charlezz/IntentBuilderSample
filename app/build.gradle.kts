import com.charlezz.intentbuilder.Dependencies

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
//    kotlin("kapt")
}

android {
    compileSdkVersion(Dependencies.compileSdk)
    buildToolsVersion(Dependencies.buildToolVersion)

    defaultConfig {
        applicationId("com.charlezz.intentbuilder.sample")
        minSdkVersion(Dependencies.minSdk)
        targetSdkVersion(Dependencies.targetSdk)
        versionCode(1)
        versionName("1.0")
    }

    buildTypes {

        getByName("debug") {
            sourceSets {
                getByName("main") {
                    java.srcDir(File("build/generated/ksp/debug/kotlin"))
                }
            }
        }
        getByName("release") {
            minifyEnabled(false)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            sourceSets {
                getByName("main") {
                    java.srcDir(File("build/generated/ksp/release/kotlin"))
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {

    implementation(project(":intentbuilder"))
    "ksp"(project(":intentbuilder-ksp"))
//    kapt(project(":intentbuilder-kapt"))


    Dependencies.Kotlin.run {
        implementation(stdlib)
    }

    Dependencies.AndroidX.run {
        implementation(constraintlayout)
        implementation(coreKtx)
        implementation(appCompat)
        implementation(material)
    }

    Dependencies.Test.run {
        testImplementation(junit4)
    }
}

ksp {
    arg("projectName", project.name)
}
