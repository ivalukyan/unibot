plugins {
    kotlin("jvm") version "2.0.21"
    id("com.google.devtools.ksp") version "2.1.10-1.0.29"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Telegram Bot
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.3.0")

    // Env
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // Google Api
    implementation("com.google.api-client:google-api-client:2.3.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20250115-2.0.0")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}