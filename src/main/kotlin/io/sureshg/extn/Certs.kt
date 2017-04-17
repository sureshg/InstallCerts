package io.sureshg.extn

import io.sureshg.extn.KeyStoreType.PKCS12
import sun.security.x509.*
import java.io.File
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.KeyStore.PasswordProtection
import java.security.SecureRandom
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
 * Returns a [SavingTrustManager] for this trust manager.
 */
fun X509TrustManager.saving() = SavingTrustManager(this)


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
 */
fun KeyStore.toPKCS12(keyPasswd: CharArray? = null): KeyStore {
    return if (type.toUpperCase() != PKCS12.name) {
        val ks = KeyStore.getInstance(PKCS12.name)
        ks.load(null, null)
        val keyProtParams = PasswordProtection(keyPasswd)
        aliases().toList().forEach {
            val entry = if (isKeyEntry(it)) getEntry(it, keyProtParams) else getEntry(it, null)
            ks.setEntry(it, entry, keyProtParams)
        }
        ks
    } else {
        this
    }
}


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
 * Decodes a PEM-encoded block to DER. PEM (Privacy-enhanced Electronic Mail)
 * is Base64 encoded DER certificate, enclosed between
 * "-----BEGIN CERTIFICATE-----" and "-----END CERTIFICATE-----". The string,
 * according to RFC 1421, can only contain characters in the base-64 alphabet
 * and whitespaces.
 *
 * @return the decoded bytes
 */
fun String.decodePEM(): ByteArray {
    val src = replace("\\s+".toRegex(), "").toByteArray(StandardCharsets.ISO_8859_1)
    return Base64.getDecoder().decode(src)
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
 * Verify the keystore public/private key certificate againt the CRL.
 *
 * @see [Keytool](https://goo.gl/4eKRfc)
 */
fun KeyStore.verifyCRL(crl: CRL): String? {
    val xcrl = crl as X509CRLImpl
    aliases().toList()
            .map { getCertificate(it) }
            .filterIsInstance<X509Certificate>()
            .filter { it.subjectX500Principal == xcrl.issuerX500Principal }
            .forEach {
                try {
                    crl.verify(it.publicKey)
                    return getCertificateAlias(it)
                } catch(e: Exception) {
                }
            }
    return null
}