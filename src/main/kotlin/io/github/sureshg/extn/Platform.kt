package io.github.sureshg.extn

import io.github.sureshg.extn.Platform.Arch.x86
import io.github.sureshg.extn.Platform.Arch.x86_64
import io.github.sureshg.extn.Platform.OS.*
import kotlin.text.RegexOption.IGNORE_CASE

/**
 * A platform is a unique combination of CPU architecture and operating system. This class
 * attempts to determine the platform it is executing on by examining and normalizing the
 * <code>os.arch</code> and <code>os.name</code> system properties.
 *
 * @author Suresh
 */
object Platform {

    val os: OS
    val arch: Arch
    val user: String
    val isUnix: Boolean

    init {
        val osArch = System.getProperty("os.arch")
        val osName = System.getProperty("os.name")

        os = when {
            osName.matches(mac.type.toRegex(IGNORE_CASE)) -> mac
            osName.startsWith(linux.type, true) -> linux
            osName.startsWith(windows.type, true) -> windows
            osName.startsWith(freebsd.type, true) -> freebsd
            else -> throw IllegalStateException("Unsupported OS $osName")
        }

        arch = when {
            osArch.matches(x86_64.type.toRegex(IGNORE_CASE)) -> x86_64
            osArch.matches(x86.type.toRegex(IGNORE_CASE)) -> x86
            else -> throw IllegalStateException("Unsupported OS Arch $osArch")
        }

        user = System.getProperty("user.name")
        isUnix = (os != windows)
    }

    enum class OS(val type: String) {
        linux("linux"),
        freebsd("freebsd"),
        windows("win"),
        mac("mac os x|darwin")
    }

    enum class Arch(val type: String) {
        x86("x86|i386"),
        x86_64("x86_64|amd64")
    }
}