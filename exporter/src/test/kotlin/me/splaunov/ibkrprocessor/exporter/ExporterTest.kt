package me.splaunov.ibkrprocessor.exporter

import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import me.splaunov.ibkrprocessor.data.PurchaseOperationDetails
import me.splaunov.ibkrprocessor.data.SellOperationDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.LocalDate

@MicronautTest
class ExporterTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun export() {
        val file = tempDir.resolve("export.xlsx").toFile()
        val sellOps = listOf(
            SellOperationDetails(
                TradeOrder(
                    "AAPL", "USD",
                    LocalDate.parse("2020-08-21"), -2, 490.54f, -1.0f
                ), 73.7711f,
                listOf(
                    PurchaseOperationDetails(
                        TradeOrder(
                            "AAPL", "USD",
                            LocalDate.parse("2020-08-20"), 4, 550.55f, -1.1f
                        ), 75f, 2
                    )
                )
            )
        )

        Exporter().export(sellOps, file)

        file.exists() shouldBe true
        val sheet = XSSFWorkbook(file).getSheet("trades")
        sheet.getRow(1)?.getCell(0)?.stringCellValue shouldBe "AAPL"
        sheet.getRow(4)?.getCell(0)?.stringCellValue shouldBe "Итого"
    }
}