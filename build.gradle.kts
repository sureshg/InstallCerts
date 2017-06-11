import co.riiid.gradle.ReleaseTask
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import us.kirchmeier.capsule.manifest.CapsuleManifest
import us.kirchmeier.capsule.spec.ReallyExecutableSpec
import us.kirchmeier.capsule.task.*
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.*
import java.util.jar.Attributes.Name.*
import term.*

buildscript {
    var javaVersion: JavaVersion by extra
    var kotlinVersion: String by extra
    var kotlinEAPRepo: String by extra
    var wrapperVersion: String by extra

    javaVersion = JavaVersion.VERSION_1_8
    kotlinVersion = "kotlin.version".sysProp
    wrapperVersion = "wrapper.version".sysProp
    kotlinEAPRepo = "kotlin.eap.repo".sysProp

    repositories {
        gradleScriptKotlin()
    }

    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.14")
    }
}

val appVersion by project
val appAuthor by project
val javaVersion: JavaVersion by extra
val kotlinVersion: String by extra
val kotlinEAPRepo: String by extra
val wrapperVersion: String by extra
printHeader(appVersion)

plugins {
    java
    application
    idea
    val ktPlugin = "kotlin.version".sysProp
    id("org.jetbrains.kotlin.jvm") version ktPlugin
    id("us.kirchmeier.capsule") version "1.0.2"
    id("co.riiid.gradle") version "0.4.2"
}

apply {
    plugin<DokkaPlugin>()
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
    mainClassName = "${project.group}.MainKt"
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
        attributes(mapOf(
                "Built-By" to appAuthor,
                "Built-Date" to buildDateTime,
                "Build-Jdk" to "java.version".sysProp,
                "Build-Target" to javaVersion,
                "Build-OS" to "${"os.name".sysProp} ${"os.version".sysProp}",
                "Kotlin-Version" to kotlinVersion,
                "Created-By" to "Gradle ${gradle.gradleVersion}",
                IMPLEMENTATION_VERSION.toString() to appVersion,
                IMPLEMENTATION_TITLE.toString() to application.applicationName,
                IMPLEMENTATION_VENDOR.toString() to project.group,
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
        archivePath.setExecutable(true)
        println("Executable File: ${archivePath.absolutePath.bold}".done)
    }
}

/**
 * Generate doc using dokka.
 */
tasks.withType<DokkaTask> {
    val src = "src/main"
    val out = "$projectDir/docs"
    val format = DokkaFormat.Html
    doFirst {
        println("Cleaning doc directory ${out.bold}...".cyan)
        project.delete(out)
    }

    moduleName = ""
    sourceDirs = files(src)
    outputFormat = format.type
    outputDirectory = out
    jdkVersion = javaVersion.majorVersion.toInt()
    includes = listOf("README.md", "CHANGELOG.md")
    val mapping = LinkMapping().apply {
        dir = src
        url = "${githubRepo.url}/blob/master/$src"
        suffix = "#L"
    }
    linkMappings = arrayListOf(mapping)
    description = "Generate docs in ${format.desc} format."

    doLast {
        println("Generated ${format.desc} format docs to ${outputDirectory.bold}".done)
    }
}

/**
 * Set Github token and publish.
 */
github {
    val tag = version.toString()
    baseUrl = "https://api.github.com"
    owner = githubRepo.user
    repo = githubRepo.repo
    tagName = tag
    targetCommitish = "master"
    name = "${application.applicationName} v$version"
    val changelog = githubRepo.changelogUrl(branch = targetCommitish, tag = tag)
    body = "$name release. Check [CHANGELOG.md]($changelog) for details."
    setAssets(File(buildDir, "libs/${application.applicationName}").path)
}

tasks.withType<ReleaseTask> {
    doFirst {
        github.token = getEnv("GITHUB_TOKEN")
    }

    doLast {
        println("Published github release ${github.name}.".done)
        println("Release URL: ${githubRepo.releaseUrl().bold}")
    }

    description = "Publish Github release ${github.name}"
    dependsOn("makeExecutable")
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
