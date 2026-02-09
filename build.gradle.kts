plugins {
    id("java-library")
    id("run-hytale")
}

group = findProperty("pluginGroup") as String? ?: "com.example"
version = findProperty("pluginVersion") as String? ?: "1.0.0"
description = findProperty("pluginDescription") as String? ?: "A Hytale plugin template"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Hytale Server API (provided by server at runtime)
    compileOnly(files("./libs/HytaleServer.jar"))

    // Gson - dostępny w Hytale Server, więc tylko compileOnly
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("org.jetbrains:annotations:24.1.0")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Configure server testing
runHytale {
    jarUrl = "./libs/HytaleServer.jar"
    assetsPath = "./libs/Assets.zip"
}

tasks {
    // Configure Java compilation
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
    }

    // Configure resource processing
    processResources {
        filteringCharset = Charsets.UTF_8.name()

        // Replace placeholders in manifest.json
        val props = mapOf(
            "group" to project.group,
            "version" to project.version,
            "description" to project.description
        )
        inputs.properties(props)

        filesMatching("manifest.json") {
            expand(props)
        }
    }

    // Configure JAR
    jar {
        archiveBaseName.set(rootProject.name)
    }

    // Configure tests
    test {
        useJUnitPlatform()
    }
}

// Configure Java toolchain
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}