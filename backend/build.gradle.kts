plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
}

group = "com.cs446g15"
version = "0.0.1"

application {
    mainClass.set("com.cs446g15.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("com.google.apis:google-api-services-playintegrity:v1-rev20240317-2.0.0")
    implementation("com.google.auth:google-auth-library-credentials:1.10.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.10.0")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.68")
    implementation(project(":common"))
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
