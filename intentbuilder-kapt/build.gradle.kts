plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":intentbuilder-annotation"))
    implementation("com.squareup:javapoet:1.13.0")
}