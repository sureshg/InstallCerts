package io.sureshg.extn

import sun.security.x509.X500Name
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*
import javax.security.auth.x500.X500Principal

/**
 * Certs/TLS extension functions.
 *
 * Default algorithms uses are,
 *
 * +-----------------------+-----------------------------+
 * |   SunJSEE Class       |  Algorithm or Protocol      |
 * +-----------------------------------------------------+
 * |   KeyStore            |      PKCS12                 |
 * +-----------------------------------------------------+
 * | KeyManagerFactory     |   PKIX, SunX509             |
 * +-----------------------------------------------------+
 * |TrustManagerFactory    |PKIX (X509/SunPKIX), SunX509 |
 * +-----------------------------------------------------+
 * |   SSLContext          |   TLSv1.1,TLSv1.2          |
 * +-----------------------------------------------------+
 *
 * @author Suresh G (@sur3shg)
 */

/**
 * Java [KeyStore] types.
 */
enum class KeyStoreType {
    JCEKS,
    JKS,
    DKS,
    PKCS11,
    PKCS12
}

/**
 * Returns true if the certificate is signed by the given [ca] cert., false otherwise.
 *
 * @param ca [X509Certificate] CA cert.
 */
fun X509Certificate.signedBy(ca: X509Certificate): Boolean {
    return if (issuerX500Principal != ca.subjectX500Principal) {
        false
    } else {
        try {
            this.verify(ca.publicKey)
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Returns true if the certificate is self-signed, false otherwise.
 */
val X509Certificate.selfSigned get() = signedBy(this)

/**
 * Returns short cert info string suitable for printing.
 */
fun X509Certificate.info() = let {
    """|Subject - ${it.subjectDN}
       |  Issuer : ${it.issuerDN}
       |  SHA1   : ${it.encoded.sha1}
       |  MD5    : ${it.encoded.md5}
       |  SAN    : ${it.subjectAlternativeNames?.flatten() ?: ""}
       |  Expiry : ${it.notAfter}
       """.trimMargin()
}

/**
 * Returns the [X500Name] from the [X500Principal]
 */
val X500Principal.x500Name get() : X500Name = X500Name.asX500Name(this)

/**
 * Returns the file name of the default JDK CA trust store.
 */
val CACerts = "${System.getProperty("java.home")}${FILE_SEP}lib${FILE_SEP}security${FILE_SEP}cacerts"

/**
 * Returns the default JDK CA trust store.
 */
val CACertsKeyStore by lazy {
    File(CACerts).takeIf(File::exists)?.let { file ->
        file.inputStream().use {
            KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(it, null as CharArray?) }
        }
    } ?: throw IllegalStateException("Can't find jre cacerts file $CACerts")
}

/**
 * An [X509TrustManager] to save the server/client cert chains.
 *
 * @param tm [X509TrustManager]
 */
class SavingTrustManager(private val tm: X509TrustManager) : X509TrustManager {

    var chain = listOf<X509Certificate>()

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = save(authType, chain, true)

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = save(authType, chain, false)

    private fun save(authType: String, chain: Array<X509Certificate>, server: Boolean) {
        this.chain = chain.toList()
        when (server) {
            true -> tm.checkServerTrusted(chain, authType)
            else -> tm.checkClientTrusted(chain, authType)
        }
    }
}

/**
 * Returns a [SavingTrustManager] for this trust manager.
 */
fun X509TrustManager.saving() = SavingTrustManager(this)

/**
 * Returns a [KeyStore] instance by loading this keystore file.
 *
 * @param type [KeyStoreType] string. By default it uses JDK default type.
 * @param passwd store password of the keystore.
 */
fun File.asKeyStore(type: String = KeyStore.getDefaultType(), passwd: CharArray? = null): KeyStore? {
    return if (isFile) {
        inputStream().use { fis ->
            KeyStore.getInstance(type).apply {
                load(fis, passwd)
            }
        }
    } else null
}

/**
 *  Returns a PKCS12 [KeyStore] instance by loading this keystore file.
 *
 *  @param passwd store password of the keystore.
 */
fun File.asP12KeyStore(passwd: CharArray? = null) = asKeyStore(KeyStoreType.PKCS12.name, passwd)

/**
 * Returns the default [X509TrustManager] for the [KeyStore]
 */
val KeyStore.defaultTrustManager get() = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).let { tm ->
    tm.init(this)
    tm.trustManagers[0] as X509TrustManager
}

/**
 * Returns the default [X509KeyManager] for the [KeyStore]
 */
fun KeyStore.defaultKeyManager(passwd: CharArray? = null) = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).let { km ->
    km.init(this, passwd)
    km.keyManagers[0] as X509KeyManager
}

/**
 * Returns a [SSLSocketFactory] instance for given [protocol] SSL context and X509 key and trust store managers.
 *
 * @param protocol SSL context protocol
 * @param keyManagers [X509KeyManager] which manages your keystore
 * @param trustManagers [X509TrustManager] which manages your trust store
 * @param secureRandom [SecureRandom] instance.
 */
fun getSSLSockFactory(protocol: String = "Default",
                      keyManagers: Array<out X509KeyManager>? = null,
                      trustManagers: Array<out X509TrustManager>? = null,
                      secureRandom: SecureRandom? = null) = SSLContext.getInstance(protocol).let {
    // No need to init the default SSLContext.
    if (protocol != "Default") {
        it.init(keyManagers, trustManagers, secureRandom)
    }
    it.socketFactory
}