plugins {
    `kotlin-dsl`
    val ktPlugin = System.getProperty("kotlin.version") ?: "1.1.3"
    kotlin("jvm", ktPlugin)
}

repositories {
    jcenter()
}

dependencies {
    compile(gradleKotlinDsl())
    compile(kotlin("stdlib-jre8"))
}

tasks.getByName("compileKotlin").dependsOn("clean")
