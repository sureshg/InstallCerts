package io.sureshg.extn

import sun.misc.HexDumpEncoder
import java.io.File
import java.io.IOException
import java.net.JarURLConnection
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest
import java.util.*
import java.util.jar.Attributes
import java.util.jar.Manifest
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.KClass
import kotlin.text.Charsets.US_ASCII
import kotlin.text.Charsets.UTF_8

/**
 * Common extension functions.
 *
 * @author Suresh G (@sur3shg)
 */

const val SPACE = " "

val LINE_SEP = System.lineSeparator()

val FILE_SEP = File.separator

/**
 * Prints the [Any.toString] to console.
 */
inline val Any?.p get() = println(this)

/**
 * Random number generator.
 */
val RAND = Random(System.nanoTime())

/**
 * Prepend an empty string of size [col] to the string.
 *
 * Doesn't preserve original line endings.
 */
fun String.indent(col: Int) = prependIndent(SPACE.repeat(col))

/**
 * Prepend an empty string of size [col] to each string in the list by skipping first [skip] strings.
 *
 * @param skip number of head elements to skip from indentation.
 *             Default to 0 if it's out of bound of list size [0..size]
 */
fun List<String>.indent(col: Int, skip: Int = 0): List<String> {
    val skipCount = if (skip in 0..size) skip else 0
    return mapIndexed { idx, str -> if (idx < skipCount) str else str.indent(col) }
}

/**
 * Convert [Byte] to hex. '0x100' OR is used to preserve the leading zero in case of single hex digit.
 */
val Byte.hex get() = Integer.toHexString(toInt() and 0xFF or 0x100).substring(1, 3).toUpperCase()

/**
 * Convert [Byte] to octal. '0x200' OR is used to preserve the leading zero in case of two digit octal.
 */
val Byte.oct get() = Integer.toOctalString(toInt() and 0xFF or 0x200).substring(1, 4)

/**
 * Convert [ByteArray] to hex.
 */
val ByteArray.hex get() = map(Byte::hex).joinToString(" ")

/**
 * Convert [ByteArray] into the classic: "Hexadecimal Dump".
 */
val ByteArray.hexDump get() = HexDumpEncoder().encode(this)

/**
 * Convert [ByteArray] to octal
 */
val ByteArray.oct get() = map(Byte::oct).joinToString(" ")

/**
 * Hex and Octal util methods for Int and Byte
 */
val Int.hex get() = Integer.toHexString(this).toUpperCase()

val Int.oct get() = Integer.toOctalString(this)

val Byte.hi get() = toInt() and 0xF0 shr 4

val Byte.lo get() = toInt() and 0x0F

/**
 * Convert string to hex.
 */
val String.hex: String get() = toByteArray(UTF_8).hex

/**
 * Convert String to octal
 */
val String.oct: String get() = toByteArray(UTF_8).oct

/**
 * IPV4 regex pattern
 */
val ip_regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".toRegex()

val String.isIPv4 get() = matches(ip_regex)

/**
 *  Create an MD5 hash of a string.
 */
val String.md5 get() = hash(toByteArray(UTF_8), "MD5")

/**
 *  Create an SHA1 hash of a string.
 */
val String.sha1 get() = hash(toByteArray(UTF_8), "SHA-1")

/**
 *  Create an SHA256 hash of a string.
 */
val String.sha256 get() = hash(toByteArray(UTF_8), "SHA-256")

/**
 * Encodes the string into Base64 encoded one.
 */
inline val String.base64 get() = Base64.getEncoder().encodeToString(toByteArray(US_ASCII))

/**
 * Decodes the base64 string.
 */
inline val String.base64Decode get() = base64DecodeBytes.toString(US_ASCII)

/**
 * Decodes the base64 string to byte array. It removes all extra spaces in the
 * input string before doing the base64 decode operation.
 */
inline val String.base64DecodeBytes: ByteArray get() {
    val str = replace("\\s+".toRegex(), "")
    return Base64.getDecoder().decode(str)
}

/**
 *  Create an MD5 hash of [ByteArray].
 */
val ByteArray.md5 get() = hash(this, "MD5")

/**
 *  Create an SHA1 hash of [ByteArray].
 */
val ByteArray.sha1 get() = hash(this, "SHA-1")

/**
 *  Create an SHA256 hash of [ByteArray].
 */
val ByteArray.sha256 get() = hash(this, "SHA-256")

/**
 * Encodes all bytes from the byte array into a newly-allocated byte array using the Base64 encoding scheme.
 */
inline val ByteArray.base64: ByteArray get() = Base64.getEncoder().encode(this)

/**
 * Decodes base64 byte array.
 */
inline val ByteArray.base64Decode: ByteArray get() = Base64.getDecoder().decode(this)

/**
 * Get the root cause by walks through the exception chain to the last element,
 * "root" of the tree, using [Throwable.getCause], and returns that exception.
 */
val Throwable?.rootCause: Throwable? get() {
    var cause = this
    while (cause?.cause != null) {
        cause = cause.cause
    }
    return cause
}

/**
 * Find the [msg] hash using the given hashing [algo]
 */
private fun hash(msg: ByteArray, algo: String): String {
    val md = MessageDigest.getInstance(algo)
    md.reset()
    md.update(msg)
    val msgDigest = md.digest()
    return msgDigest.hex
}

/**
 * Encrypt this string with HMAC-SHA1 using the specified [key].
 *
 * @param key Encryption key
 * @return Encrypted output
 */
fun String.hmacSHA1(key: String): ByteArray {
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(SecretKeySpec(key.toByteArray(UTF_8), "HmacSHA1"))
    return mac.doFinal(toByteArray(UTF_8))
}

/**
 * Pad this String to a desired multiple on the right using a specified character.
 *
 * @param padding Padding character.
 * @param multipleOf Number which the length must be a multiple of.
 */
fun String.rightPadString(padding: Char, multipleOf: Int): String {
    if (isEmpty()) throw IllegalArgumentException("Must supply non-empty string")
    if (multipleOf < 2) throw  IllegalArgumentException("Multiple ($multipleOf) must be greater than one.")
    val needed = multipleOf - (length % multipleOf)
    return padEnd(length + needed, padding)
}

/**
 * Normalize a string to a desired length by repeatedly appending itself and/or truncating.
 *
 * @param desiredLength Desired length of string.
 */
fun String.normalizeString(desiredLength: Int): String {
    if (isEmpty()) throw IllegalArgumentException("Must supply non-empty string")
    if (desiredLength < 0) throw IllegalArgumentException("Desired length ($desiredLength) must be greater than zero.")
    var buf = this
    if (length < desiredLength) {
        buf = repeat(desiredLength / length + 1)
    }
    return buf.substring(0, desiredLength)
}

/**
 * Encrypt this string with AES-128 using the specified [key].
 * Ported from - https://goo.gl/J1H3e5
 *
 * @param key Encryption key.
 * @return Encrypted output.
 */
fun String.aes128Encrypt(key: String): ByteArray {
    val nkey = key.normalizeString(16)
    val msg = rightPadString('{', 16)
    val cipher = Cipher.getInstance("AES/ECB/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(nkey.toByteArray(UTF_8), "AES"))
    return cipher.doFinal(msg.toByteArray(UTF_8))
}

/**
 * Deletes the files or directory (recursively) represented by this path.
 */
fun Path.delete() {
    if (Files.notExists(this)) {
        return
    }
    if (Files.isDirectory(this)) {
        Files.walkFileTree(this, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes) = let {
                Files.delete(file)
                FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException) = let {
                Files.delete(dir)
                FileVisitResult.CONTINUE
            }
        })
    } else {
        Files.delete(this)
    }
}

/**
 * Exits the system with [msg]
 */
fun exit(status: Int, msg: (() -> String)? = null) {
    if (msg != null) {
        println(msg())
    }
    System.exit(status)
}

/**
 * Returns the jar [Manifest] of the class. Returns [null] if the class
 * is not bundled in a jar (Classes in an unpacked class hierarchy).
 */
inline val <T : Any> KClass<T>.jarManifest: Manifest? get() {
    val res = java.getResource("${java.simpleName}.class")
    val conn = res.openConnection()
    return if (conn is JarURLConnection) conn.manifest else null
}

/**
 * Common build info attributes
 */
enum class BuildInfo(val attr: String) {
    Author("Built-By"),
    Date("Built-Date"),
    JDK("Build-Jdk"),
    Target("Build-Target"),
    OS("Build-OS"),
    KotlinVersion("Kotlin-Version"),
    CreatedBy("Created-By"),
    Title(Attributes.Name.IMPLEMENTATION_TITLE.toString()),
    Vendor(Attributes.Name.IMPLEMENTATION_VENDOR.toString()),
    AppVersion(Attributes.Name.IMPLEMENTATION_VERSION.toString())
}

/**
 * Returns the [BuildInfo] attribute value from jar manifest [Attributes]
 */
fun Attributes?.getVal(name: BuildInfo): String = this?.getValue(name.attr) ?: "N/A"