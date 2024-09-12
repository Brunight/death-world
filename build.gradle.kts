plugins {
    kotlin("jvm") version "2.0.10"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "dev.brunight"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        downloadPlugins {
            github("SkinsRestorer", "SkinsRestorer", "15.4.2", "SkinsRestorer.jar")
        }
        minecraftVersion("1.21.1")
        jvmArgs("-Dcom.mojang.eula.agree=true")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)
    }
}