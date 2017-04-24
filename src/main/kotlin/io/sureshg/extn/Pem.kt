package io.sureshg.extn

import java.io.File
import java.nio.charset.StandardCharsets.US_ASCII
import java.security.AlgorithmParameters
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.EncryptedPrivateKeyInfo
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.text.RegexOption.IGNORE_CASE

/**
 * PEM util & extension functions.
 *
 * <pre>
 * The test PEM certificate files are created with the openssl
 * command line. They are self-signed certificates.
 *
 *   $ openssl genrsa -out rsa.key 2048
 *   $ openssl req -new -key rsa.key -out rsa.csr
 *   $ press [Enter] a bunch of times to accept default values for all fields.
 *   $ openssl x509 -req -days 10000 -in rsa.csr -signkey rsa.key -out rsa.crt
 *   # PKCS#1 -> PKCS#8
 *   $ openssl pkcs8 -topk8 -inform PEM -outform PEM -in rsa.key -out rsapriv8.pem -nocrypt
 * </pre>
 *
 * @author Suresh
 * @see [PemReader](https://goo.gl/FQNnKt)
 * @see [PKINotes](https://gist.github.com/awood/9338235)
 */


/**
 * PEM regex pattern for cert and private key (Header + Base64 Text + Footer)
 */
val PemCertPattern = "-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*CERTIFICATE[^-]*-+".toRegex(IGNORE_CASE)

val PemKeyPattern = "-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*PRIVATE\\s+KEY[^-]*-+".toRegex(IGNORE_CASE)

/**
 * Read the PrivateKey pem file.
 */
fun File.readPemPrivateKey(keyPasswd: CharArray? = null): List<PrivateKey> = if (isFile) {
    val content = readText(US_ASCII)
    PemKeyPattern.findAll(content).map {
        it.groupValues[1].base64DecodeBytes
    }.map {
        when (keyPasswd) {
            null -> PKCS8EncodedKeySpec(it)
            else -> {
                val keyInfo = EncryptedPrivateKeyInfo(it)
                val cipher = keyPasswd.pbeCipher(keyInfo.algName, keyInfo.algParameters)
                keyInfo.getKeySpec(cipher)
            }
        }
    }.map {
        try {
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePrivate(it)
        } catch (ignore: InvalidKeySpecException) {
            ignore.printStackTrace()
            val keyFactory = KeyFactory.getInstance("DSA")
            keyFactory.generatePrivate(it)
        }
    }.toList()
} else {
    throw InvalidKeyException("$path is not a files/exists.")
}


/**
 * Returns [Cipher] instance for this char array password
 * with password-based encryption (PBE).
 *
 * @param algo cipher algorithm
 * @param params cipher [AlgorithmParameters]
 * @param mode cipher mode ENCRYPT, DECRYPT, WRAP etc.
 */
fun CharArray.pbeCipher(algo: String, params: AlgorithmParameters, mode: Int = Cipher.DECRYPT_MODE): Cipher = let {
    val skf = SecretKeyFactory.getInstance(algo)
    val secretKey = skf.generateSecret(PBEKeySpec(it))
    Cipher.getInstance(algo).apply {
        init(mode, secretKey, params)
    }
}


/**
 * Decodes a PEM-encoded block to DER. PEM (Privacy-enhanced Electronic Mail)
 * is Base64 encoded DER certificate, enclosed between
 * "-----BEGIN CERTIFICATE-----" and "-----END CERTIFICATE-----". The string,
 * according to RFC 1421, can only contain characters in the base-64 alphabet
 * and whitespaces. ToDo - Replace with PemReader.
 *
 * @return the decoded bytes
 */
fun String.decodePEM(): ByteArray = base64DecodeBytes
