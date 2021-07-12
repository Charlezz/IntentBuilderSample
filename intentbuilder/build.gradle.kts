import com.charlezz.intentbuilder.Dependencies

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion (Dependencies.compileSdk)
    buildToolsVersion (Dependencies.buildToolVersion)

    defaultConfig {
        minSdkVersion (Dependencies.minSdk)
        targetSdkVersion (Dependencies.targetSdk)
        versionCode (1)
        versionName ("1.0")

        testInstrumentationRunner ("androidx.test.runner.AndroidJUnitRunner")
        consumerProguardFiles ("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled=false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(project(":intentbuilder-annotation"))

    Dependencies.AndroidX.run {
        implementation (coreKtx)
        implementation (appCompat)
        implementation (material)
    }
    implementation(Dependencies.Kotlin.reflect)

    testImplementation (Dependencies.Test.junit4)
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")



}