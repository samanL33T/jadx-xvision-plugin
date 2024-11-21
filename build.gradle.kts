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

    testImplementation("io.github.skylot:jadx-dex-input:$jadxVersion") {
        isChanging = true
    }
    testImplementation("io.github.skylot:jadx-java-input:$jadxVersion") {
        isChanging = true
    }
    testImplementation("io.github.skylot:jadx-smali-input:$jadxVersion") {
        isChanging = true
    }
    testImplementation("io.github.skylot:jadx-kotlin-metadata:$jadxVersion") {
        isChanging = true
    }

    testImplementation("ch.qos.logback:logback-classic:1.5.9")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.2")

    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")

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
    withType<ShadowJar> {
        archiveClassifier.set("")
    }

    register<Copy>("dist") {
        group = "jadx-plugin"
        dependsOn(withType<ShadowJar>())
        dependsOn(withType<Jar>())

        from(withType<ShadowJar>())
        into(layout.buildDirectory.dir("dist"))
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
