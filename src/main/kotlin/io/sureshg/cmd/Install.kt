package io.sureshg.cmd

import io.airlift.airline.Arguments
import io.airlift.airline.Command
import io.airlift.airline.HelpOption
import io.airlift.airline.Option
import io.sureshg.extn.AnsiColor.*
import io.sureshg.extn.color
import io.sureshg.extn.sux
import java.net.URL
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
    val version by lazy {
      Install::class.java.`package`.implementationVersion
    }

    /**
     * Executes the command
     */
    fun run() {
        if (uri.isEmpty()) throw IllegalArgumentException("Server URL can't be empty!")
        if(showVersion) {
            println("".sux)
            System.exit(0)
        }
    }

    override fun toString() = "Install(uri=$uri, hostPort=$hostPort)"
}