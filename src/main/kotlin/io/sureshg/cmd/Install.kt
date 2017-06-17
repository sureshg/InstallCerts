package io.sureshg.cmd

import io.airlift.airline.Arguments
import io.airlift.airline.Command
import io.airlift.airline.HelpOption
import io.airlift.airline.Option
import io.sureshg.crypto.InstallCerts
import io.sureshg.extn.BuildInfo.*
import io.sureshg.extn.bold
import io.sureshg.extn.cyan
import io.sureshg.extn.getVal
import io.sureshg.extn.jarManifest
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

    @Option(name = arrayOf("-p", "--passwd"), description = "Trust store password. Default is 'changeit'")
    var storePasswd = "changeit"

    @Option(name = arrayOf("-a", "--all"), description = "Show all certs and exits")
    var all = false

    @Option(name = arrayOf("-v", "--verbose"), description = "Verbose mode")
    var verbose = false

    @Option(name = arrayOf("-t", "--timeout"), description = "TLS connect and read timeout (ms). Default is 5000 millis")
    var timeout = 5_000

    @Option(name = arrayOf("-d", "--debug"), description = "Enable TLS debug tracing")
    var debug = false

    @Option(name = arrayOf("-x", "--no-jdk-cacerts"), description = "Don't include JDK CA certs in trust store")
    var noJdkCaCerts = false

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
     * Build info attributes
     */
    val buildAttrs by lazy { Install::class.jarManifest?.mainAttributes }

    /**
     * Executes the command
     */
    fun run() {
        when {
            showVersion -> {
                val version = """|InstallCerts version : ${buildAttrs.getVal(AppVersion)}
                                        |JDK Version          : ${buildAttrs.getVal(JDK)}
                                        |Kotlin Version       : ${buildAttrs.getVal(KotlinVersion)}
                                        |Build Date           : ${buildAttrs.getVal(Date)}
                                    """.trimMargin()
                println(version.bold.cyan)
                System.exit(0)
            }
            uri.isEmpty() -> throw IllegalArgumentException("Server URL can't be empty!")
        }
        InstallCerts.exec(this)
    }
}
