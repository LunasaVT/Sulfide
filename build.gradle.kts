plugins {
    kotlin("jvm") version "2.3.0"
    id("net.fabricmc.fabric-loom-remap") version "1.15-SNAPSHOT"
    id("ploceus") version "1.15-SNAPSHOT"
}

group = "dev.lunasa"
version = "1.4-SNAPSHOT"

val lwjglVersion = "3.4.1"
val jomlVersion = "1.10.8"
val lwjglNatives = "natives-windows"

val shadow by configurations.creating

repositories {
    mavenCentral()
    maven("https://api.modrinth.com/maven/")
    maven {
        name = "legacy-fabric"
        url = uri("https://maven.legacyfabric.net/")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("net.legacyfabric:legacy-yarn:1.8.9+build.4:v2")
    modImplementation("net.fabricmc:fabric-loader:0.18.3")
    modImplementation("maven.modrinth:radium-mod:0.8.10+mc1.8.9")

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("it.unimi.dsi:fastutil:8.5.14")
    implementation("org.slf4j:slf4j-api:2.0.17")

    shadow(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(17)
}

configurations.all {
    exclude(group = "org.lwjgl.lwjgl")
}

ploceus {
    setIntermediaryGeneration(2)
}

tasks {
    jar {
        shadow.forEach { from(zipTree(it)) { exclude("META-INF", "META-INF/**") } }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    compileJava {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}