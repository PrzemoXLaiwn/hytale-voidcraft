plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("run-hytale") {
            id = "run-hytale"
            implementationClass = "RunHytalePlugin"
        }
    }
}
