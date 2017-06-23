import BuildInfo.*
import co.riiid.gradle.ReleaseTask
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import us.kirchmeier.capsule.manifest.CapsuleManifest
import us.kirchmeier.capsule.spec.ReallyExecutableSpec
import us.kirchmeier.capsule.task.*
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.*
import term.*

buildscript {
    var javaVersion: JavaVersion by extra { JavaVersion.VERSION_1_8 }
    var kotlinVersion: String by extra { "kotlin.version".sysProp }
    var kotlinEAPRepo: String by extra { "kotlin.eap.repo".sysProp }
    var wrapperVersion: String by extra { "wrapper.version".sysProp }

    repositories {
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    }

    dependencies {
        val dokkaVersion = "dokka.version".sysProp
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$dokkaVersion")
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
    val gradleVersion = "gradle-versions.version".sysProp
    kotlin("jvm", ktPlugin)
    id("com.github.ben-manes.versions") version gradleVersion
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
    jcenter()
    maven { setUrl(kotlinEAPRepo) }
}

dependencies {
    compile(kotlin("stdlib-jre8", kotlinVersion))
    compile("io.airlift:airline:0.7")
}

/**
 * Add jar manifests.
 */
tasks.withType<Jar> {
    manifest {
        attributes(mapOf(
                Author.attr to appAuthor,
                Date.attr to buildDateTime,
                JDK.attr to "java.version".sysProp,
                BuildTarget.attr to javaVersion,
                OS.attr to "${"os.name".sysProp} ${"os.version".sysProp}",
                KotlinVersion.attr to kotlinVersion,
                CreatedBy.attr to "Gradle ${gradle.gradleVersion}",
                AppVersion.attr to appVersion,
                Title.attr to application.applicationName,
                Vendor.attr to project.group,
                MainClass.attr to application.mainClassName))
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
        jvmArgs = listOf("-client", "-Djava.security.egd=file:/dev/./urandom")
        minJavaVersion = minJavaVer
    }
    description = "Create $archiveName executable."
    dependsOn("clean")

    doLast {
        archivePath.setExecutable(true)
        val size = archivePath.length().toBinaryPrefixString()
        println("Executable File: ${archivePath.absolutePath.bold} (${size.bold})".done)
    }
}

tasks.withType<DokkaTask> {
    val src = "src/main"
    val out = "$projectDir/docs"
    val format = DokkaFormat.Html
    doFirst {
        println("Cleaning doc directory ${out.bold}...".cyan)
        project.delete(fileTree(out) {
            exclude("logos/**", "templates/**")
        })
    }

    moduleName = ""
    sourceDirs = files(src)
    outputFormat = format.type
    outputDirectory = out
    skipEmptyPackages = true
    jdkVersion = javaVersion.majorVersion.toInt()
    includes = listOf("README.md", "CHANGELOG.md")
    val mapping = LinkMapping().apply {
        dir = src
        url = "${githubRepo.url}/blob/master/$src"
        suffix = "#L"
    }
    linkMappings = arrayListOf(mapping)
    description = "Generate ${project.name} v$appVersion docs in ${format.desc} format."

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
    description = "Generate Gradle Script Kotlin wrapper $wrapperVersion"
    distributionType = ALL
    distributionUrl = gradleKotlinDslUrl(wrapperVersion)
    doFirst {
        println(description)
    }
}

/**
 * Set default task
 */
defaultTasks("makeExecutable")
