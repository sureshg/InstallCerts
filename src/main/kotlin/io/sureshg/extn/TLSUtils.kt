package io.sureshg.extn

import sun.security.x509.X500Name
import java.io.File
import java.security.KeyStore
import java.security.cert.X509Certificate
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
val CACertsKeyStore get() = File(CACerts).takeIf { it.exists() }?.let { file ->
    file.inputStream().use {
        KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(it, null as CharArray?)
        }
    }
}