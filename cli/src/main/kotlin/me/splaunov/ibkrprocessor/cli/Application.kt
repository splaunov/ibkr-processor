package me.splaunov.ibkrprocessor.cli

import io.micronaut.configuration.picocli.PicocliRunner
import me.splaunov.ibkrprocessor.exporter.Exporter
import me.splaunov.ibkrprocessor.reader.ActivityStatementReader
import me.splaunov.ibkrprocessor.reader.PnlCalculator
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import java.io.File
import jakarta.inject.Inject

@Command(name = "pnl")
class Application : Runnable {
    @Parameters(index = "0", description = ["A csv file with IBKR activity report."])
    lateinit var inCsvFile: File

    @Parameters(index = "1", description = ["An .xlsx file name for export data."])
    lateinit var outXlsxFile: File

    @Inject
    lateinit var reader: ActivityStatementReader

    @Inject
    lateinit var pnlCalculator: PnlCalculator

    override fun run() {
        println(inCsvFile.absolutePath)
        val sellDetails = pnlCalculator.getSellDetails(reader.readTrades(inCsvFile))
        Exporter().export(sellDetails, outXlsxFile)
        val pnl = pnlCalculator.calculateRealizedPnlRub(sellDetails)
        println("PnL: $pnl")
    }

}

fun main(args: Array<String>): Unit = PicocliRunner.run(Application::class.java, *args)


