package io.sureshg.cmd

import io.airlift.airline.Arguments
import io.airlift.airline.Command
import io.airlift.airline.HelpOption
import io.airlift.airline.Option
import io.sureshg.crypto.InstallCerts
import io.sureshg.extn.*
import java.net.URL
import java.util.jar.Attributes.Name.*
import javax.inject.Inject

/**
 * Install command.
 *
 * @author Suresh
 */
@Command(name = "installcerts", description = "Creates PKCS12 TrustStore by retrieving server certificates")
class Install {

    @Inject
    lateinit var helpOption: HelpOption

    @Arguments(description = "Server URL. Default port is 443", usage = "<host>[:port]")
    var uri = ""

    @Option(name = arrayOf("-p"), description = "Trust store password. Default is 'changeit'")
    var storePasswd = "changeit"

    @Option(name = arrayOf("-a", "--all"), description = "Install all certs")
    var all = false

    @Option(name = arrayOf("-v", "--verbose"), description = "Verbose mode")
    var verbose = false

    @Option(name = arrayOf("-V", "--version"), description = "Show version")
    var showVersion = false

    /**
     * Split the host and port from the [uri]
     */
    val hostPort by lazy {
        try {
            val url = URL(uri)
            val port = url.port.takeIf { it != -1 } ?: url.defaultPort
            Pair(url.host, port)
        } catch(e: Exception) {
            val server = uri.split(":").takeIf { it.size > 1 } ?: listOf(uri, "443")
            Pair(server[0], server[1].toIntOrNull() ?: 443)
        }
    }

    /**
     * Tool version.
     */
    val buildInfo by lazy {
        Install::class.jarManifest?.let {
            val attr = it.mainAttributes
            BuildInfo(attr.getValue("Built-By"),
                    attr.getValue("Built-Date"),
                    attr.getValue(IMPLEMENTATION_VERSION))
        } ?: BuildInfo()
    }

    /**
     * Executes the command
     */
    fun run() {
        when {
            showVersion -> {
                val version = """|InstallCerts version: ${buildInfo.version ?: "N/A"}
                                 |Build Date: ${buildInfo.date ?: "N/A"}
                              """.trimMargin()
                println(version.bold.cyan)
                System.exit(0)
            }
            uri.isEmpty() -> throw IllegalArgumentException("Server URL can't be empty!")
        }
        InstallCerts.exec(this)
    }
}

/**
 * Build info class
 */
data class BuildInfo(val by: String? = null, val date: String? = null, val version: String? = null)