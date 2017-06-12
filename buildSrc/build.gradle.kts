buildscript {
    repositories {
        gradleScriptKotlin()
    }
}

plugins {
    val ktPlugin = System.getProperty("kotlin.version") ?: "1.1.2-2"
    kotlin("jvm", ktPlugin)
}

repositories {
    gradleScriptKotlin()
}

dependencies {
    compile(gradleScriptKotlinApi())
    compile(kotlin("stdlib-jre8"))
}

tasks.getByName("compileKotlin").dependsOn("clean")
