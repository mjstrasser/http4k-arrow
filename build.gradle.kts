plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    application
}

repositories {
    jcenter()
}

val kotlinLoggingVersion = "2.0.3"
val log4jVersion = "2.13.3"
val log4jKotlinVersion = "1.0.0"
val http4kVersion = "3.265.0"
val arrowVersion = "0.11.0"
val kotlinxVersion = "1.3.9"
val kotestVersion = "4.2.6"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")

    implementation("io.arrow-kt:arrow-fx:$arrowVersion")

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

    implementation(platform("org.http4k:http4k-bom:$http4kVersion"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-netty")
    implementation("org.http4k:http4k-client-apache")

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClassName = "mjs.AppKt"
}
