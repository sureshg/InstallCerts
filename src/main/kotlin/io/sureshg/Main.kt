package io.sureshg

import io.airlift.airline.SingleCommand
import io.sureshg.cmd.Install
import io.sureshg.extn.bold
import io.sureshg.extn.err

/**
 * Main method that runs InstallCerts commands.
 *
 * @author  Suresh
 */
fun main(args: Array<String>) {
    val cmd = SingleCommand.singleCommand(Install::class.java)
    var install: Install? = null
    try {
        install = cmd.parse(*args)
        if (install.helpOption.showHelpIfRequested()) {
            return
        }
        install.run()
    } catch (e: Throwable) {
        println("""|${e.message?.err}
                   |See ${"'installcerts --help'".bold}
                    """.trimMargin())
        install?.let {
            if (it.verbose) e.printStackTrace()
        }
    }
}
