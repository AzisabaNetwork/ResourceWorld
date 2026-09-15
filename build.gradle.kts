plugins {
    java
}

group = "net.azisaba"
version = "1.3.2"
description = "Auto regenerate resource world."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://repo.onarandombox.com/content/groups/public/") {
        name = "onarandombox"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.mvplugins.multiverse.core:multiverse-core:5.8.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.processResources {
    val properties = mapOf(
        "name" to project.name,
        "version" to project.version,
        "description" to project.description,
    )
    inputs.properties(properties)
    filesMatching("plugin.yml") {
        expand(properties)
    }
}

tasks.jar {
    archiveFileName = "${project.name}.jar"
}
