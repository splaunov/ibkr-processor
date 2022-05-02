package me.splaunov.ibkrprocessor.cli

import io.micronaut.configuration.picocli.PicocliRunner
import me.splaunov.ibkrprocessor.exporter.Exporter
import me.splaunov.ibkrprocessor.reader.ActivityStatementReader
import me.splaunov.ibkrprocessor.reader.PnlCalculator
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import java.io.File
import jakarta.inject.Inject
import kotlin.system.exitProcess

@Command(name = "pnl")
class Application : Runnable {
    @Parameters(index = "0", description = ["A directory with IBKR activity reports in csv files."])
    lateinit var inCsvDir: File

    @Parameters(index = "1", description = ["An .xlsx file name for data export."])
    lateinit var outXlsxFile: File

    @Inject
    lateinit var reader: ActivityStatementReader

    @Inject
    lateinit var pnlCalculator: PnlCalculator

    override fun run() {
        println(inCsvDir.absolutePath)
        if (inCsvDir.isFile) {
            println("First parameter should be a directory")
            exitProcess(1)
        }
        val sellDetails = pnlCalculator.getSellDetails(reader.readTrades(inCsvDir))
        Exporter().export(sellDetails, outXlsxFile)
        val pnl = pnlCalculator.calculateRealizedPnlRub(sellDetails)
        println("PnL: $pnl")
    }

}

fun main(args: Array<String>): Unit = PicocliRunner.run(Application::class.java, *args)


