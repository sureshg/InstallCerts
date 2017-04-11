package io.sureshg.extn

import sun.misc.HexDumpEncoder
import java.io.File
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8
import io.sureshg.extn.AnsiColor.*

/**
 * Common extension functions.
 *
 * @author Suresh G (@sur3shg)
 */

const val SPACE = " "

val LINE_SEP = System.lineSeparator()

val FILE_SEP = File.separator

/**
 * Prepend an empty string of size [col] to the string.
 *
 * Doesn't preserve original line endings.
 */
fun String.indent(col: Int) = prependIndent(SPACE.repeat(col))

/**
 * Prepend an empty string of size [col] to each string in the list.
 */
fun List<String>.indent(col: Int) = map { it.indent(col) }

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
 * Colorize the string.
 *
 * @param fg foreground [AnsiColor]
 * @param bg background  [AnsiColor]
 * @param prefix string to prepend
 */
fun String.color(fg: AnsiColor, bg: AnsiColor = DEFAULT_BG, prefix: String = "") = "${fg.eSeq}${bg.eSeq}$prefix ${this}${NO_COLOR.eSeq}"

/**
 * Formatted strings.
 */
val String.cyan get() = color(CYAN)

val String.red get() = color(RED)

val String.green get() = color(GREEN)

val String.yellow get() = color(YELLOW)

val String.bold get() = color(BOLD)

/**
 * Success string
 */
val String.sux get() = color(CYAN, prefix = "\u2713")

/**
 * Error string
 */
val String.err get() = color(LIGHT_RED, prefix = "\u2717")

/**
 * Warn string
 */
val String.warn get() = color(YELLOW, prefix = "\u27A4")

/**
 * High voltage string
 */
val String.highvolt get() = color(YELLOW, prefix = "\u26A1")

/**
 * Completed (Beer Glass) string.
 */
val String.done get() = prependIndent(" ").color(GREEN, prefix = "\uD83C\uDF7A")

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
