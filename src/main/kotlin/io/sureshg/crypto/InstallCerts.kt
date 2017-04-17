package io.sureshg.crypto

import io.sureshg.cmd.Install
import io.sureshg.extn.*
import java.io.File
import java.lang.System.exit
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSocket

/**
 * Creates a PKCS12 TrustStore by retrieving server's
 * certificates with JDK trusted certificates.
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
            val uin = System.console()?.readLine(" $keystoreFile file exists. Do you want to overwrite it (y/n)? ".warn) ?: "n"
            if (uin.toLowerCase() != "y") {
                println("Existing...".red)
                exit(-1)
            }
        }

        println("Loading default ca truststore...".cyan)
        val keyStore = CACertsKeyStore
        println("Opening connection to $host:$port...".cyan)
        val result = validateCerts(host, port, keyStore, args)

        when {
            result.chain.isEmpty() -> {
                println("Could not obtain server certificate chain!".err)
                exit(-1)
            }

            args.all -> {
                result.chain.forEachIndexed { idx, cert ->
                    println("\n${idx + 1}) ${cert.info().fg256()}")
                }
                exit(0)
            }

            result.valid -> {
                println("No errors, certificate is already trusted!".done)
                exit(0)
            }
        }

        println("Server sent ${result.chain.size} certificate(s)...".yellow)
        result.chain.forEachIndexed { idx, cert ->
            val alias = "$host-${idx + 1}"
            println("\n${idx + 1}) Adding certificate to keystore using alias ${alias.bold}...")
            println(cert.info().fg256())
            keyStore.setCertificateEntry(alias, cert)
            if (validateCerts(host, port, keyStore, args).valid) {
                println("Certificate is trusted. Saving the trustore...\n".cyan)
                keyStore.toPKCS12().store(keystoreFile.outputStream(), storePasswd.toCharArray())
                println("PKCS12 truststore saved to ${keystoreFile.absolutePath.bold}".done)
                exit(0)
            }
        }
    }

    /**
     * Validate the TLS server using given keystore.
     *
     * @param host server host
     * @param port server port
     * @param keystore trustore to make TLS connection
     * @param args install cli config.
     */
    private fun validateCerts(host: String, port: Int, keystore: KeyStore, args: Install): Result {
        val tm = keystore.defaultTrustManager.saving()
        val sslFactory = getSSLSockFactory("TLS", trustManagers = arrayOf(tm))
        val socket = sslFactory.createSocket(host, port) as SSLSocket

        try {
            println("\nStarting SSL handshake...".cyan)
            with(socket) {
                soTimeout = 5_000
                startHandshake()
                close()
            }
            return Result(true, tm.chain)
        } catch(e: SSLException) {
            if (args.verbose) {
                e.printStackTrace()
            }
            return Result(false, tm.chain)
        }
    }
}

/**
 * Holds the cert validation result. Mainly validation status and cert chain.
 */
data class Result(val valid: Boolean, val chain: List<X509Certificate>)







