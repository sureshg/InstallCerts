package io.sureshg

import io.airlift.airline.Help
import io.airlift.airline.SingleCommand
import io.sureshg.cmd.Install


/**
 *
 *
 * @author  Suresh
 */


fun main(args: Array<String>) {

    args.forEach { println( ">>>> $it") }
    val cmd = SingleCommand.singleCommand(Install::class.java)
    try {

        val ping =   cmd.parse(*args)


      //  Help.help(ping.helpOption?.commandMetadata)
        if (ping.helpOption!!.showHelpIfRequested()) {
            return
        }

        ping.run()
    }catch (e: Exception) {
        Help.help(cmd.commandMetadata)
        println(e.message)
    }
}
