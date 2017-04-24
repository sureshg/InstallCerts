import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import us.kirchmeier.capsule.manifest.CapsuleManifest
import us.kirchmeier.capsule.spec.ReallyExecutableSpec
import us.kirchmeier.capsule.task.*
import org.gradle.jvm.tasks.Jar
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.jar.Attributes.Name.*

buildscript {
    var javaVersion: JavaVersion by extra
    var kotlinVersion: String by extra
    var kotlinEAPRepo: String by extra
    var wrapperVersion: String by extra

    javaVersion = JavaVersion.VERSION_1_8
    kotlinVersion = "1.1.2-eap-77"
    wrapperVersion = "4.0-20170421144052+0000"
    kotlinEAPRepo = "https://dl.bintray.com/kotlin/kotlin-eap-1.1"

    repositories {
        gradleScriptKotlin()
    }
}

val appVersion by project
val appAuthor by project
val javaVersion: JavaVersion by extra
val kotlinVersion: String by extra
val kotlinEAPRepo: String by extra
val wrapperVersion: String by extra
printHeader()

plugins {
    java
    application
    idea
    id("org.jetbrains.kotlin.jvm") version "1.1.1"
    id("us.kirchmeier.capsule") version "1.0.2"
}

base {
    group = "io.sureshg"
    version = appVersion
    description = "Install Certs!"
}

/**
 * Configure java compiler options.
 */
java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

/**
 * Configure application plugin
 */
application {
    applicationName = rootProject.name
    mainClassName = "io.sureshg.MainKt"
}

/**
 * Configure kotlin compiler options.
 */
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

repositories {
    gradleScriptKotlin()
    maven { setUrl(kotlinEAPRepo) }
}

dependencies {
    compile(kotlinModule("stdlib-jre8", kotlinVersion))
    compile("io.airlift:airline:0.7")
}

/**
 * Add jar manifests.
 */
tasks.withType<Jar> {
    manifest {
        attributes(mapOf("Built-By" to appAuthor,
                "Built-Date" to buildDateTime,
                IMPLEMENTATION_VERSION.toString() to appVersion,
                IMPLEMENTATION_TITLE.toString() to application.applicationName,
                MAIN_CLASS.toString() to application.mainClassName))
    }
}

/**
 * Make executable
 */
task<FatCapsule>("makeExecutable") {
    val minJavaVer = javaVersion.toString()
    val appName = application.applicationName
    val appMainClass = application.mainClassName
    archiveName = appName
    reallyExecutable = ReallyExecutableSpec().regular()
    capsuleManifest = CapsuleManifest().apply {
        premainClass = "Capsule"
        mainClass = "Capsule"
        applicationName = appName
        applicationClass = appMainClass
        applicationVersion = version
        jvmArgs = listOf("-client")
        minJavaVersion = minJavaVer
    }
    description = "Create $archiveName executable."
    dependsOn("clean")

    doLast {
        println("Executable File: $archivePath")
    }
}

/**
 * Generate Gradle Script Kotlin wrapper.
 */
task<Wrapper>("wrapper") {
    description = "Generate Gradle Script Kotlin wrapper v$wrapperVersion"
    distributionType = ALL
    distributionUrl = getGskURL(wrapperVersion)
    doFirst {
        println(description)
    }
}


/**
 * Set default task
 */
defaultTasks("makeExecutable")

fun printHeader() {
    val header = """
                 ===============================
                  Building Install Certs v$appVersion
                 ===============================
                 """.trimIndent()
    println(header)
    println()
}

val buildDateTime get() = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a z"))

fun getGskURL(version: String, type: DistributionType = ALL) = "https://repo.gradle.org/gradle/dist-snapshots/gradle-script-kotlin-$version-${type.name.toLowerCase()}.zip"
