package me.splaunov.ibkrprocessor.exporter

import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import me.splaunov.ibkrprocessor.data.InstrumentInformation
import me.splaunov.ibkrprocessor.data.PurchaseDetails
import me.splaunov.ibkrprocessor.data.SellingDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import me.splaunov.ibkrprocessor.reader.CurrencyRatesProvider
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Instant

@MicronautTest
class ExporterTest {
    @TempDir
    lateinit var tempDir: Path

    private val currencyRatesProvider = mockk<CurrencyRatesProvider>().apply {
        every { getRate(any(), any()) } returns 70f
    }

    @Test
    fun export() {
        val file = tempDir.resolve("export.xlsx").toFile()
        val instrumentInformation = mapOf("AAPL" to InstrumentInformation("AAPL", "001"))
        val sellOps = listOf(
            SellingDetails(
                TradeOrder(
                    "AAPL", "USD",
                    Instant.parse("2021-08-21T00:00:00Z"), -2F, 490.54f, -1.0f
                ),
                listOf(
                    PurchaseDetails(
                        TradeOrder(
                            "AAPL", "USD",
                            Instant.parse("2020-08-20T00:00:00Z"), 4F, 550.55f, -1.1f
                        ), 2F
                    )
                )
            )
        )

        Exporter(currencyRatesProvider).export(sellOps, file, instrumentInformation)

        file.exists() shouldBe true
        val sheet = XSSFWorkbook(file).getSheet("2021")
        sheet.getRow(1)?.getCell(0)?.stringCellValue shouldBe "AAPL"
        sheet.getRow(2)?.getCell(0)?.stringCellValue shouldBe "AAPL"
    }
}