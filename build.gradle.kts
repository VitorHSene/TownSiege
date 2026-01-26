import org.gradle.api.tasks.bundling.Zip

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.3.1"
}

group = findProperty("pluginGroup") as String? ?: "com.townssiege"
version = findProperty("pluginVersion") as String? ?: "1.0.0"
description = findProperty("pluginDescription") as String? ?: "A Hytale plugin template"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    compileOnly(files("libs/SimpleClaims.jar"))

    //c dependencies (will be bundled in JAR)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")

    //test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation(files("libs/HytaleServer.jar"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    // Configure Java compilation
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
    }
    
    // Configure resource processing
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        
        // Replace placeholders in manifest.json
        val props = mapOf(
            "group" to project.group,
            "version" to project.version,
            "description" to project.description
        )
        inputs.properties(props)
        
        filesMatching("manifest.json") {
            expand(props)
        }
    }
    
    // Configure ShadowJar (bundle dependencies)
    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")

        
        //minimize JAR size (removes unused classes)
        minimize()
    }
    
    //configure tests
    test {
        useJUnitPlatform()
    }
    
    //make build depend on shadowJar
    build {
        dependsOn(shadowJar)
    }
}

//configure Java toolchain
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}


val modsDir = "C:/Users/Vitor/Desktop/HytaleServer/Server/mods"
val assetPackDir = layout.projectDirectory.dir("src/main/resources/assetpack")

fun requireFile(path: String) {
    val f = file(path)
    if (!f.exists()) error("Missing file: $path")
}

fun requireDir(dirPath: String) {
    val d = file(dirPath)
    if (!d.exists() || !d.isDirectory) error("Missing directory: $dirPath")
}

tasks.register<Copy>("deployJar") {
    group = "deployment"
    description = "Builds the plugin jar and copies it to the server mods folder."

    dependsOn("shadowJar")

    from(layout.buildDirectory.dir("libs")) {
        include("*.jar")
        exclude("*-plain.jar")
    }
    into(modsDir)

    doLast {
        println("OK: Deployed jar to $modsDir")
    }
}

tasks.register("deployAll") {
    group = "deployment"
    description = "Deploys plugin jar + asset pack zip into the server mods folder."

    dependsOn("deployJar")

    doLast {
        copy {
            into(modsDir)
        }
        println("OK: Deployed jar + asset pack to $modsDir")
    }
}
