package io.github.sureshg.extn

/**
 * ANSI color/graphics extension functions.
 *
 *  @author Suresh G
 *
 * @see [ANSI_escape_code](https://en.wikipedia.org/wiki/ANSI_escape_code#graphics)
 * @see [Lihaoyi_Blog](http://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html)
 * @see [TLDP](http://www.tldp.org/HOWTO/Bash-Prompt-HOWTO/x329.html)
 * @see [CusrorMovement](http://www.tldp.org/HOWTO/Bash-Prompt-HOWTO/x361.html)
 * @see [EscapeSeq](http://www.isthe.com/chongo/tech/comp/ansi_escapes.html)
 * @see [PythonTerminalColor](https://github.com/reorx/python-terminal-color/blob/master/color_simple.py)
 */
enum class AnsiColor(vararg val codes: Int) {
    // Base attributes
    Reset(0),
    Bold(1),
    Faint(2),
    Italic(3),
    Underline(4),
    BlinkSlow(5),
    BlinkRapid(6),
    ReverseVideo(7),
    Concealed(8),
    CrossedOut(9),
    Normal(22),
    NoStandout(23),
    NoUnderline(24),
    NoBlink(25),

    // Foreground text colors
    Black(30),
    DarkGray(1, 30),
    Red(31),
    LightRed(1, 31),
    Green(32),
    LightGreen(1, 32),
    Yellow(33),
    LightYellow(1, 33),
    Blue(34),
    LightBlue(1, 34),
    Magenta(35),
    LightMagenta(1, 35),
    Cyan(36),
    LightCyan(1, 36),
    White(37),
    LightGray(1, 37),

    // Background text colors
    BlackBg(40),
    RedBg(41),
    GreenBg(42),
    YellowBg(43),
    BlueBg(44),
    MagentaBg(45),
    CyanBg(46),
    WhiteBg(47),

    // Reset for fore/background & Highlight
    Fg(38),
    FgEnd(39),
    Bg(48),
    BgEnd(49),
    HlEnd(22, 27, 39),

    // Foreground Hi-Intensity text colors
    HiBlack(90),
    HiRed(91),
    HiGreen(92),
    HiYellow(93),
    HiBlue(94),
    HiMagenta(95),
    HiCyan(96),
    HiWhite(97),

    // Background Hi-Intensity text colors
    HiBlackBg(100),
    HiRedBg(101),
    HiGreenBg(102),
    HiYellowBg(103),
    HiBlueBg(104),
    HiMagentaBg(105),
    HiCyanBg(106),
    HiWhiteBg(107);

    /**
     * Returns ANSI escape unicode for the color.
     */
    val esc get() = codes.esc
}

typealias AnsiEsc = String

/**
 * Returns true if you are running in a terminal.
 */
val isTTY = System.console() != null

/**
 * Escape unicode in hex.
 */
const val ESC: AnsiEsc = "\u001B"

/**
 * Returns an ANSI escape unicode of the string.
 */
inline val String.esc: AnsiEsc get() = "$ESC[${this}m"

/**
 * Returns an ANSI escape unicode of the integer.
 */
inline val Int.esc get() = toString().esc

/**
 * 0 is reset for all
 */
val ESC_END = 0.esc

/**
 * Returns an ANSI escape unicode of the int array.
 */
inline val IntArray.esc get() = joinToString(";").esc

/**
 * Returns an ANSI escape unicode from a list of integers.
 */
inline val Array<Int>.esc get() = joinToString(";").esc

/**
 * Returns an ANSI escape unicode from a list of integers.
 */
fun esc(vararg codes: Int) = codes.esc

/**
 * Returns formatted string with given ANSI color codes.
 */
fun String.color(vararg codes: Int) = "${codes.esc}$this$ESC_END"

/**
 * Returns formatted string with given ANSI color.
 */
fun String.color(color: AnsiColor) = color(*color.codes)

/**
 * Returns 256-color extended color set (From 0 to 255) formatted string.
 *
 * @param code color code in the range 0 to 255.
 * @param fg [true] for foreground color, otherwise background. Default is [true]
 */
fun String.color256(code: Int, fg: Boolean = true): String {
    val esc = code % 255
    return when (fg) {
        true -> color(38, 5, esc)
        else -> color(48, 5, esc)
    }
}

/**
 * Returns foreground colored string with given color code.
 */
fun String.fg256(code: Int) = color256(code, true)

/**
 * Returns foreground colored string with random color.
 */
fun String.fg256() = color256(RAND.nextInt(256), true)

/**
 * Returns background colored string with given color code.
 */
fun String.bg256(code: Int) = color256(code, false)

/**
 * Returns background colored string with random color.
 */
fun String.bg256() = color256(RAND.nextInt(256), true)

/**
 * Returns 8-bit gray scale (From 232 to 256 in color set) formatted string.
 *
 * @param code color code in the range 0 to (256-232)
 * @param fg [true] for foreground color, otherwise background. Default is [true]
 */
fun String.grayScale(code: Int, fg: Boolean = true): String {
    val start = 232
    val end = 256
    val esc = start + (code % (end - start))
    return when (fg) {
        true -> color(38, 5, esc)
        else -> color(48, 5, esc)
    }
}

/**
 * Returns foreground grayscale formatted string.
 */
fun String.fgGrayScale(code: Int) = grayScale(code, true)

/**
 * Returns background grayscale formatted string.
 */
fun String.bgGrayScale(code: Int) = grayScale(code, false)

/**
 * 8-bit color functions.
 */
inline val String.bold get() = color(1)
inline val String.italic get() = color(3)
inline val String.underline get() = color(4)
inline val String.blink get() = color(5)
inline val String.reversed get() = color(7)
inline val String.strike get() = color(9)
inline val String.black get() = color(30)
inline val String.red get() = color(31)
inline val String.green get() = color(32)
inline val String.yellow get() = color(33)
inline val String.blue get() = color(34)
inline val String.magenta get() = color(35)
inline val String.cyan get() = color(36)
inline val String.gray get() = color(37)

/**
 * Make the string bold and underline.
 */
inline val String.bu get() = bold.underline

/**
 * Make the string bold, underline & italics
 */
inline val String.bui get() = bold.underline.italic

/**
 * Success string
 */
inline val String.sux get() = "\u2713 $this".cyan

/**
 * Error string
 */
inline val String.err get() = "\u2717 $this".red

/**
 * Warn string
 */
inline val String.warn get() = "\u27A4 $this".yellow

/**
 * High voltage string
 */
inline val String.highvolt get() = "\u26A1 $this".yellow

/**
 * Completed (Beer Glass) string.
 */
inline val String.done get() = "\uD83C\uDF7A  $this".green

fun main(args: Array<String>) {
    "Kotlin".bold.underline.blue.p
    "Java".yellow.bu.p
    "Dart".cyan.bold.p
    "Golang".magenta.p
    "Scala".red.underline.bold.p
    "Python".green.bold.p
    "Clojure".magenta.bold.p
    "Ruby".err.bold.p

    for (i in 1..50) {
        "Hello $i".fgGrayScale(i).p
    }

    for (i in 1..50) {
        "Hello $i".bgGrayScale(i).p
    }

    for (i in 1..300) {
        "Hello FG $i".fg256(i).p
    }

    for (i in 1..300) {
        "Hello BG $i".bg256(i).p
    }

    AnsiColor.values().forEach {
        "AnsiColor ${it.name} (${it.codes.joinToString(",")})".color(it).p
    }
}