package io.sureshg.extn

import io.sureshg.extn.KeyStoreType.PKCS12
import sun.security.x509.*
import java.io.File
import java.net.URI
import java.security.KeyStore
import java.security.KeyStore.PasswordProtection
import java.security.SecureRandom
import java.security.Security
import java.security.cert.*
import java.util.*
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
 *
 * @see [JavaKeytoolSource](https://goo.gl/M23uhr)
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
 * JSSE system properties for customization.
 *
 * @param prop property name
 * @param desc Detailed description of the property
 * @param system indicate whether it's a System or Security property.
 *               [true] if it's customized by setting System property.
 */
enum class JSSEProp(val prop: String, val desc: String, val system: Boolean = true) {
    Debug("javax.net.debug", "Debugging SSL/TLS Connections."),
    KeyStore("javax.net.ssl.keyStore", "Default keystore"),
    KeyStoreType("javax.net.ssl.keyStoreType", "Default keystore type"),
    KeyStorePassword("javax.net.ssl.keyStorePassword", "Default keystore password"),
    KeyStoreProvider("javax.net.ssl.keyStoreProvider", "Default keystore provider"),
    TrustStore("javax.net.ssl.trustStore", "Default truststore"),
    TrustStoreType("javax.net.ssl.trustStoreType", "Default truststore type"),
    TrustStorePassword("javax.net.ssl.trustStorePassword", "Default truststore password"),
    TrustStoreProvider("javax.net.ssl.trustStoreProvider", "Default truststore provider"),
    ProxyHost("https.proxyHost", "Default HTTPS proxy host"),
    ProxyPort("https.proxyPort", "Default HTTPS proxy port"),
    HttpsCipherSuites("https.cipherSuites", "Default cipher suites"),
    HttpsProtocols("https.protocols", "Default HTTPS handshaking protocols"),
    TLSProtocols("jdk.tls.client.protocols", "Default Enabled TLS Protocols"),
    CertPathDisabledAlgos("jdk.certpath.disabledAlgorithms", "Disabled certificate verification cryptographic algorithms", false),
    TLSDisabledAlgos("jdk.tls.disabledAlgorithms", "Disabled/Restricted Algorithms", false);

    /**
     * Sets the JSSE system/security property to the given value.
     */
    fun set(value: String) {
        when (system) {
            true -> System.setProperty(prop, value)
            else -> Security.setProperty(prop, value)
        }
    }
}

/**
 * Returns the file name of the default JDK CA trust store.
 */
val CACerts = "${System.getProperty("java.home")}${FILE_SEP}lib${FILE_SEP}security${FILE_SEP}cacerts"


/**
 * Returns the default JDK CA trust store.
 */
val CACertsKeyStore by lazy {
    File(CACerts).takeIf { it.isFile }?.let { file ->
        file.inputStream().use {
            KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                load(it, null as CharArray?)
            }
        }
    } ?: throw IllegalStateException("Can't find jre ca certs file $CACerts")
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
            verify(ca.publicKey)
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
 * For SAN, it returns the GenericName pair -> Filtering
 * the string names and indents the list to display properly.
 */
fun X509Certificate.info() = run {
    """|Subject - $subjectDN
       |  Issuer : $issuerDN
       |  SHA1   : ${encoded.sha1}
       |  MD5    : ${encoded.md5}
       |  SAN    : ${subjectAlternativeNames?.flatten()?.filterIsInstance<String>()?.indent(11, skip = 1)?.joinToString(LINE_SEP) ?: ""}
       |  Expiry : $notAfter
     """.trimMargin()
}

/**
 * Returns short ssl session info string suitable for printing.
 */
fun SSLSession.info() = run {
    """|SSL-Session:
       |  Protocol    : $protocol
       |  CipherSuite : $cipherSuite
       |  Session-ID  : ${id?.hex ?: ""}
       |  Timeout     : ${sessionContext?.sessionTimeout ?: ""}
       |  Create Time : ${Date(creationTime)}
       |  Access Time : ${Date(lastAccessedTime)}
       |  Values      : ${this.valueNames?.joinToString() ?: ""}
     """.trimMargin()
}

/**
 * Returns the [X500Name] from the [X500Principal]
 */
val X500Principal.x500Name get() : X500Name = X500Name.asX500Name(this)


/**
 * Returns a [SavingTrustManager] for this trust manager.
 *
 * @param validate [true] to validate certificate chains. It's enabled by default.
 */
fun X509TrustManager.saving(validate: Boolean = true) = SavingTrustManager(this, validate)


/**
 * Returns a [KeyStore] instance by loading this keystore file.
 *
 * @param type [KeyStoreType] string. By default it uses JDK default type.
 * @param storePasswd store password of the keystore.
 */
fun File.toKeyStore(type: String = KeyStore.getDefaultType(), storePasswd: CharArray? = null): KeyStore? {
    return if (isFile) {
        inputStream().use { fis ->
            KeyStore.getInstance(type).apply {
                load(fis, storePasswd)
            }
        }
    } else null
}


/**
 *  Returns a PKCS12 [KeyStore] instance by loading this keystore file.
 *
 *  @param storePasswd store password of the keystore.
 */
fun File.toP12KeyStore(storePasswd: CharArray? = null) = toKeyStore(PKCS12.name, storePasswd)


/**
 * Returns a new [PKCS12] keystore using this keystore entries.
 * Returns the same instance if it's already a [PKCS12] one.
 *
 * @param keyPasswd key entry password.
 * @param aliasFilter filter regex to include the specific aliases only.
 */
fun KeyStore.toPKCS12(keyPasswd: CharArray? = null, aliasFilter: Regex? = null): KeyStore {
    return if (type.toUpperCase() != PKCS12.name) {
        val ks = KeyStore.getInstance(PKCS12.name)
        ks.load(null, null)
        val keyProtParams = PasswordProtection(keyPasswd)
        aliases().toList().filter {
            when {
                aliasFilter != null -> it.matches(aliasFilter)
                else -> true
            }
        }.forEach {
            val entry = if (isKeyEntry(it)) getEntry(it, keyProtParams) else getEntry(it, null)
            ks.setEntry(it, entry, keyProtParams)
        }
        ks
    } else {
        this
    }
}

/**
 * Return [X509TrustManager] for the [KeyStore]
 */
inline val KeyStore.trustManagers get() = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).let { tm ->
    tm.init(this)
    tm.trustManagers.filterIsInstance<X509TrustManager>()
}

/**
 * Returns the default [X509TrustManager] for the [KeyStore]
 */
inline val KeyStore.defaultTrustManager get() = trustManagers[0]


/**
 * Returns [X509KeyManager] for the [KeyStore]
 */
fun KeyStore.keyManagers(passwd: CharArray? = null) = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).let { km ->
    km.init(this, passwd)
    km.keyManagers.filterIsInstance<X509KeyManager>()
}

/**
 * Returns the default [X509KeyManager] for the [KeyStore]
 */
fun KeyStore.defaultKeyManager(passwd: CharArray? = null) = keyManagers(passwd)[0]


/**
 * Returns a [SSLContext] instance for given [protocol] and X509 key and trust store managers.
 * TrustManagers decide whether to allow connections and KeyManagers decide which key material
 * to use.
 *
 * @param protocol SSL context protocol
 * @param keyManagers [X509KeyManager] which manages your keystore
 * @param trustManagers [X509TrustManager] which manages your trust store
 * @param secureRandom [SecureRandom] instance.
 */
fun getSSLContext(protocol: String = "Default",
                  keyManagers: Array<out X509KeyManager>? = null,
                  trustManagers: Array<out X509TrustManager>? = null,
                  secureRandom: SecureRandom? = null) = SSLContext.getInstance(protocol).apply {
    // No need to init the default SSLContext.
    if (protocol != "Default") {
        init(keyManagers, trustManagers, secureRandom)
    }
}

/**
 * An [X509TrustManager] to save the server/client cert chains.
 *
 * @param tm delegating [X509TrustManager]
 * @param validate [true] to validate certificate chains. It's enabled by default.
 * @param chain certificate chain
 *
 * @param tm [X509TrustManager]
 */
class SavingTrustManager(private val tm: X509TrustManager,
                         val validate: Boolean = true,
                         var chain: List<X509Certificate> = listOf<X509Certificate>()) : X509TrustManager {

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = save(authType, chain, true)

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = save(authType, chain, false)

    private fun save(authType: String, chain: Array<X509Certificate>, server: Boolean) {
        this.chain = chain.toList()
        if (validate) {
            when (server) {
                true -> tm.checkServerTrusted(chain, authType)
                else -> tm.checkClientTrusted(chain, authType)
            }
        }
    }
}

/**
 * Loads CRLs from a [URI] source
 */
fun URI.loadCRLs() = when (scheme) {
    "ldap" -> {
        val ldapCertStore = CertStore.getInstance("LDAP", { this })
        ldapCertStore.getCRLs(X509CRLSelector()).toList()
    }
    else -> {
        // Read the full stream before feeding to X509Factory.
        val bytes = toURL().openStream().readBytes()
        CertificateFactory.getInstance("X509").generateCRLs(bytes.inputStream()).toList()
    }

}

/**
 * Loads CRLs from a [File] source
 */
fun File.loadCRLs() = CertificateFactory.getInstance("X509").generateCRLs(inputStream()).toList()

/**
 * Returns CRLs described in a X509Certificate's CRLDistributionPoints
 * Extension. Only those containing a general name of type URI are read.
 * ToDo - Different name should point to same CRL.
 */
fun X509Certificate.readCRLs(): List<X509CRL> {
    return X509CertImpl.toImpl(this).crlDistributionPointsExtension?.let {
        it.get(CRLDistributionPointsExtension.POINTS)
                .map { it.fullName }
                .filterNotNull()
                .flatMap { it.names() }
                .filter { it.type == GeneralNameInterface.NAME_URI }
                .flatMap {
                    val uri = (it.name as URIName).uri
                    uri.loadCRLs().filterIsInstance<X509CRL>()
                }
    } ?: emptyList<X509CRL>()
}

/**
 * Verify the keystore public/private key certificate against the CRL.
 *
 * @return alias of the verified cert, else returns null.
 * @see [Keytool](https://goo.gl/4eKRfc)
 */
fun KeyStore.verifyCRL(crl: X509CRL): String? {
    aliases().toList()
            .map { getCertificate(it) }
            .filterIsInstance<X509Certificate>()
            .filter { it.subjectX500Principal == crl.issuerX500Principal }
            .forEach {
                try {
                    crl.verify(it.publicKey)
                    return getCertificateAlias(it)
                } catch(e: Exception) {
                }
            }
    return null
}