import org.gradle.api.tasks.Delete
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

buildscript {
    var kotlinVersion: String by extra
    var kotlinEAPRepo: String by extra

    kotlinVersion = System.getProperty("kotlin.version")
    kotlinEAPRepo = System.getProperty("kotlin.eap.repo")

    repositories {
        gradleScriptKotlin()
    }
}

plugins {
    val ktPlugin = System.getProperty("kotlin.version")
    id("org.jetbrains.kotlin.jvm") version ktPlugin
}

val kotlinVersion: String by extra
val kotlinEAPRepo: String by extra

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

repositories {
    gradleScriptKotlin()
    maven { setUrl(kotlinEAPRepo) }
}

/**
 * Cleaning buildSrc before compilation.
 */
afterEvaluate {
    val clean: Delete by tasks
    val compileKotlin: KotlinCompile by tasks
    compileKotlin.dependsOn(clean)
}

/**
 * BuildSrc dependencies.
 */
dependencies {
    compile(gradleScriptKotlinApi())
    compile(kotlinModule("stdlib-jre8", kotlinVersion))
}

/**
 * Configures the [kotlin][KotlinProjectExtension] project extension.
 */
val Project.kotlin get() = extensions.getByName("kotlin") as KotlinProjectExtension

fun Project.kotlin(configure: KotlinProjectExtension.() -> Unit): Unit = extensions.configure("kotlin", configure)
