/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.8/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    // Mockito Core dependency
    testImplementation("org.mockito:mockito-core:5.5.0")

    // If you want to use Mockito with JUnit 5
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)
    implementation("org.json:json:20171018") // For org.json library
    implementation("com.google.code.gson:gson:2.10.1") // For Gson library
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass = "css.App"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

sourceSets {
    test {
        resources {
            srcDir("src/test/resources")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}