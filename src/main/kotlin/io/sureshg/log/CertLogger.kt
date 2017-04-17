package io.sureshg.log

import io.sureshg.extn.*
import java.io.PrintWriter
import java.io.StringWriter
import java.text.MessageFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter
import java.util.logging.Level.*

/**
 * Sample Logger. Used to log messages to both file and console.
 * Console message are specially formatted to show it's intent.
 *
 * @author Suresh
 */
object CertLogger {

    /**
     * Initialize the logger.
     */
    fun init() {
        val fileHandler = FileHandler("CertInstall-%g.log", true).apply {
            level = ALL
            formatter = BriefLogFormatter()
        }

        // Configure the Root logger
        Logger.getLogger("").apply {
            useParentHandlers = false
            level = ALL
            val console = handlers[0]
            console.formatter = ConsoleFormatter()
            console.level = level
            addHandler(fileHandler)
        }
    }
}

/**
 * Console just needs the message
 */
class ConsoleFormatter : Formatter() {

    override fun format(logRec: LogRecord): String {
        val msg = logRec.message
        val fmtMsg = when (logRec.level) {
            INFO -> msg
            SEVERE -> msg.err
            WARNING -> msg.warn
            FINE -> msg.sux
            FINER -> msg.done
            FINEST -> msg.highvolt
            else -> msg
        }
        return "$fmtMsg \n"
    }
}

/**
 * A Java logging formatter that writes more compact output
 * than the default. Taken from https://git.io/vrJfG
 */
class BriefLogFormatter : Formatter() {

    companion object {
        private val messageFormat = MessageFormat("{4,date,HH:mm:ss} {0} {1}{2}.{3}: {5}\n{6}")
    }

    override fun format(logRecord: LogRecord): String {
        val arguments = arrayOfNulls<Any>(7)
        arguments[0] = logRecord.threadID
        arguments[1] = when (logRecord.level) {
            SEVERE -> " **ERROR** "
            WARNING -> " (warning) "
            else -> ""
        }
        val fullClassName = logRecord.sourceClassName
        val dollarIndex = fullClassName.indexOf('$')
        val className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1, if (dollarIndex == -1) fullClassName.length else dollarIndex)
        arguments[2] = className
        arguments[3] = logRecord.sourceMethodName
        arguments[4] = Date(logRecord.millis)
        arguments[5] = if (logRecord.parameters != null) MessageFormat.format(logRecord.message, *logRecord.parameters) else logRecord.message
        if (logRecord.thrown != null) {
            val result = StringWriter()
            logRecord.thrown.printStackTrace(PrintWriter(result))
            arguments[6] = result.toString()
        } else {
            arguments[6] = ""
        }
        return messageFormat.format(arguments)
    }
}