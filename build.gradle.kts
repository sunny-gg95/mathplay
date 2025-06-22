plugins {
    kotlin("jvm") version "2.1.20"
    application
}

group = "com.sunnygg95.mathplay"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation("de.fabmax.kool:kool-core:0.17.0")
    implementation("de.fabmax.kool:kool-physics:0.17.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.sunnygg95.mathplay.MainKt")
}

sourceSets {
    main {
        resources.srcDirs("src/main/resources")
    }
}