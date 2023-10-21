import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun spring(sub: String, dependency: String, version: String)
= listOf(
        "org.springframework" + if (sub.isNotEmpty()) ".$sub" else "",
        dependency,
        version
)
        .filter { it.isNotEmpty() }
        .joinToString(":")

fun boot(dependency: String, version: String) = spring("boot", "spring-boot-$dependency", version)

fun boot(dependency: String) = boot(dependency, "")

plugins {
    id("org.springframework.boot") version "2.7.6"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
}

group = "kr.enak.luya"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation(boot("starter-web"))
    implementation(boot("starter-webflux"))
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.3")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.3")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    "2.1.2".also { ktorVersion ->
        implementation("io.ktor:ktor-client-core:$ktorVersion")
        implementation("io.ktor:ktor-client-websockets-jvm:$ktorVersion")
        implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
        implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    }

    implementation("org.reflections:reflections:0.10.2")

    implementation("com.github.twitch4j:twitch4j:1.17.0")
    implementation("com.github.philippheuer.events4j:events4j-handler-reactor:0.11.0")

    implementation("club.minnced:discord-webhooks:0.8.2")
    implementation("net.dv8tion:JDA:5.0.0-beta.5") {
        exclude(module = "opus-java")
    }

    annotationProcessor(boot("configuration-processor"))
    testImplementation(boot("starter-test"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
