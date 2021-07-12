import com.charlezz.intentbuilder.Dependencies
plugins {
    kotlin("jvm")
}

dependencies {
    implementation(Dependencies.Kotlin.stdlib)
    implementation(Dependencies.Ksp.symbolProcessingApi)
    implementation(project(":intentbuilder-annotation"))
}