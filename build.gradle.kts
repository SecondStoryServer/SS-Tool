import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.70"
}

group = "me.syari.ss.gun"

repositories {
    mavenCentral()
    maven ("https://papermc.io/repo/repository/maven-public/")
    maven("https://raw.github.com/sya-ri/SS-Core/tree/master/build/repo/me/syari/ss/core/SS-Core")
}

dependencies {
    implementation("com.destroystokyo.paper:paper-api:1.15.2-R0.1-SNAPSHOT")
    implementation("me.syari.ss.core:SS-Core:LATEST")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val jar by tasks.getting(Jar::class) {
    from(configurations.compile.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}