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
}

group = "kr.enak.luya"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation(boot("starter-web"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.github.twitch4j:twitch4j:1.12.0")
    implementation("com.github.philippheuer.events4j:events4j-handler-reactor:0.11.0")

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
