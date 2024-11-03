import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`

    id("com.github.johnrengelman.shadow") version "8.1.1"

    // auto update dependencies with 'useLatestVersions' task
    id("se.patrikerdes.use-latest-versions") version "0.2.18"
    id("com.github.ben-manes.versions") version "0.51.0"
}

dependencies {
    val jadxVersion = "1.5.1-SNAPSHOT"
    val isJadxSnapshot = jadxVersion.endsWith("-SNAPSHOT")

    compileOnly("io.github.skylot:jadx-core:$jadxVersion") {
        isChanging = isJadxSnapshot
    }

    testImplementation("io.github.skylot:jadx-smali-input:$jadxVersion") {
        isChanging = isJadxSnapshot
    }
    testImplementation("ch.qos.logback:logback-classic:1.5.9")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.2")

    implementation ("com.google.code.gson:gson:2.8.9")

}


repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    google()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

version = System.getenv("VERSION") ?: "dev"

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    // Configure the shadow JAR task
    withType<ShadowJar> {
        archiveClassifier.set("") // remove '-all' suffix
    }

    // Copy the result JAR into "build/dist" directory
    register<Copy>("dist") {
        group = "jadx-plugin"
        dependsOn(withType<ShadowJar>())
        dependsOn(withType<Jar>())

        from(withType<ShadowJar>())
        into(layout.buildDirectory.dir("dist"))
    }
}

// Ensure the JAR file is created in the build/libs directory
tasks.withType<Jar> {
    archiveBaseName.set("jadx-xVision-plugin")
    archiveVersion.set(version.toString())
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}
