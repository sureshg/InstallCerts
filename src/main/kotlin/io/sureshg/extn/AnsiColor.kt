package io.sureshg.extn

/**
 * ANSI color/graphics escape sequences.
 *
 * Black       0;30     Dark Gray     1;30
 * Red         0;31     Light Red     1;31
 * Green       0;32     Light Green   1;32
 * Brown       0;33     Yellow        1;33
 * Blue        0;34     Light Blue    1;34
 * Purple      0;35     Light Purple  1;35
 * Cyan        0;36     Light Cyan    1;36
 * Light Gray  0;37     White         1;37
 *
 *
 * @author Suresh G
 *
 * @see [TLDP](http://www.tldp.org/HOWTO/Bash-Prompt-HOWTO/x329.html)
 * @see [CusrorMovement](http://www.tldp.org/HOWTO/Bash-Prompt-HOWTO/x361.html)
 * @see [EscapeSeq](http://www.isthe.com/chongo/tech/comp/ansi_escapes.html)
 */
enum class AnsiColor(val eSeq: String) {

    BOLD("\u001B[1m"),
    FAINT("\u001B[2m"),
    NORMAL("\u001B[22m"),
    STANDOUT("\u001B[3m"),
    NO_STANDOUT("\u001B[23m"),
    UNDERLINE("\u001B[4m"),
    NO_UNDERLINE("\u001B[24m"),
    BLINK("\u001B[5m"),
    NO_BLINK("\u001B[25m"),
    CONCEALED_ON("\u001B[8m"),

    NO_COLOR("\u001B[0m"),
    BLACK("\u001B[0;30m"),
    RED("\u001B[0;31m"),
    GREEN("\u001B[0;32m"),
    BROWN("\u001B[0;33m"),
    BLUE("\u001B[0;34m"),
    PURPLE("\u001B[0;35m"),
    CYAN("\u001B[0;36m"),
    LIGHT_GRAY("\u001B[0;37m"),
    DEFAULT_FG("\u001B[0;39m"),

    DARK_GRAY("\u001B[1;30m"),
    LIGHT_RED("\u001B[1;31m"),
    LIGHT_GREEN("\u001B[1;32m"),
    YELLOW("\u001B[1;33m"),
    LIGHT_BLUE("\u001B[1;34m"),
    LIGHT_PURPLE("\u001B[1;35m"),
    LIGHT_CYAN("\u001B[1;36m"),
    WHITE("\u001B[1;37m"),

    BLACK_BG("\u001B[40m"),
    RED_BG("\u001B[41m"),
    GREEN_BG("\u001B[42m"),
    YELLOW_BG("\u001B[43m"),
    BLUE_BG("\u001B[44m"),
    PURPLE_BG("\u001B[45m"),
    CYAN_BG("\u001B[66m"),
    WHITE_BG("\u001B[47m"),
    DEFAULT_BG("\u001B[49m")
}



