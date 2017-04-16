package io.sureshg.crypto

import io.sureshg.cmd.Install
import io.sureshg.extn.*
import java.io.File
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSocket

/**
 * Creates a PKCS12 TrustStore by retrieving server's certificates
 * with JDK trusted certificates.
 *
 * @author Suresh
 */
object InstallCerts {

    /**
     * Executes the action.
     */
    fun exec(args: Install) {
        val (host, port) = args.hostPort
        val storePasswd = args.storePasswd

        val keystoreFile = File(host.replace(".", "_").plus(".p12"))

        if (keystoreFile.isFile) {
            val uin: String? = System.console()?.readLine(" $keystoreFile PKCS12 file exists. Do you want to overwrite it (y/n)? ".warn)
            if (uin?.toLowerCase() != "y") {
                println("Existing...".red)
                System.exit(-1)
            }
        }

        println("Loading default ca truststore...".cyan)
        val keyStore = CACertsKeyStore
        val tm = keyStore.defaultTrustManager.saving()
        val sslFactory = getSSLSockFactory("TLS", trustManagers = arrayOf(tm))

        println("Opening connection to $host:$port...".cyan)
        val socket = sslFactory.createSocket(host, port) as SSLSocket

        try {
            with(socket) {
                soTimeout = 5_000
                startHandshake()
                close()
            }
            println(" No error, certificate is already trusted using jre ca certs!".sux)
            System.exit(0)
        } catch(e: SSLException) {
            e.printStackTrace()
        }

        if (tm.chain.isEmpty()) {
            println("Could not obtain server certificate chain".err)
            System.exit(-1)
        } else {

            tm.chain.forEach {
                println(it.info().fg256((RAND.nextDouble() * 255).toInt()))
            }

        }

    }


}





