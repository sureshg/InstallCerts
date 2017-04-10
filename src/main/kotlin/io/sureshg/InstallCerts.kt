package io.sureshg

import java.util.jar.Manifest

/**
 *
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 * @Since 4/10/17
 */

class Test
fun main(args: Array<String>) {
    println("Hello Kotlin")

    println(Test::class.java.`package`.implementationVersion)
    println(Test::class.java.`package`.implementationTitle)
}