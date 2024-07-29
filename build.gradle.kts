import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.simpleinvoice"
version = "1.0.2"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.odftoolkit:odfdom-java:0.12.0")
    implementation("org.odftoolkit:simple-odf:0.9.0")
    implementation("com.sun.mail:javax.mail:1.6.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("org.odt2pdf", "odt2pdf", "1.0", classifier = "all")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(20)
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("simpleinvoice")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "MainKt"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
