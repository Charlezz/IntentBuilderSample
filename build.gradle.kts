buildscript {
    val kotlin_version by extra("1.5.20")
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(com.charlezz.intentbuilder.Dependencies.ClassPath.androidGradle)
        classpath(com.charlezz.intentbuilder.Dependencies.ClassPath.kotlin)
        classpath(com.charlezz.intentbuilder.Dependencies.ClassPath.ksp)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}

allprojects {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}