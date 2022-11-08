import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.simpleinvoice"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.odftoolkit:odfdom-java:0.10.0")
    implementation("org.odftoolkit:simple-odf:0.9.0")
    implementation("com.sun.mail:javax.mail:1.6.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
    implementation("org.odt2pdf", "odt2pdf", "1.0", classifier = "all")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
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
