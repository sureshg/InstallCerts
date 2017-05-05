package io.sureshg.crypto

import io.sureshg.cmd.Install
import io.sureshg.extn.*
import java.io.File
import java.net.InetSocketAddress
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

        when {
            !args.all && keystoreFile.isFile -> {
                val uin = System.console()?.readLine(" $keystoreFile file exists. Do you want to overwrite it (y/n)? ".warn) ?: "n"
                if (uin.toLowerCase() != "y") exit(-1) { "Existing...".red }
            }

            args.debug -> {
                println("Enabling TLS debug tracing...".warn)
                JSSEProp.Debug.set("all")
            }
        }

        println("Loading default ca truststore...".cyan)
        val keyStore = CACertsKeyStore
        println("Opening connection to $host:$port...".cyan)
        val result = validateCerts(host, port, keyStore, args)

        when {
            result.chain.isEmpty() -> exit(-1) { "Could not obtain server certificate chain!".err }

            args.all -> {
                // Print cert chain and last TLS session info.
                result.chain.forEachIndexed { idx, cert ->
                    val info = if (args.verbose) cert.toString() else cert.info()
                    println("\n${idx + 1}) ${info.fg256()}")
                }
                exit(0) { "\n${result.sessionInfo?.fg256()}" }
            }

            result.valid -> exit(0) { "No errors, certificate is already trusted!".done }
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
                exit(0) {
                    """|${"PKCS12 truststore saved to ${keystoreFile.absolutePath.bold}".done}
                       |
                       |To lists entries in the keystore, run
                       |${"keytool -list -keystore $keystoreFile --storetype pkcs12".yellow}
                    """.trimMargin()
                }
            }
        }

        exit(1) { "Something went wrong. Can't validate the cert chain!".err }
    }

    /**
     * Validate the TLS server using given keystore. It will skip the
     * cert chain validation if print certs (--all) option is enabled.
     *
     * @param host server host
     * @param port server port
     * @param keystore trustore to make TLS connection
     * @param args install cli config.
     */
    private fun validateCerts(host: String, port: Int, keystore: KeyStore, args: Install): Result {
        val validateChain = !args.all
        val tm = keystore.defaultTrustManager.saving(validateChain)
        val sslCtx = getSSLContext("TLS", trustManagers = arrayOf(tm))
        val socket = sslCtx.socketFactory.createSocket() as SSLSocket

        try {
            println("\nStarting SSL handshake...".cyan)
            socket.use {
                it.soTimeout = args.timeout
                it.connect(InetSocketAddress(host, port), args.timeout)
                it.startHandshake()
            }
            return Result(true, tm.chain, socket.session?.info())
        } catch(e: SSLException) {
            if (args.verbose) {
                e.printStackTrace()
            }
            return Result(false, tm.chain, socket.session?.info())
        }
    }
}

/**
 * Holds the cert validation result. Mainly validation status and cert chain.
 */
data class Result(val valid: Boolean, val chain: List<X509Certificate>, val sessionInfo: String?)







