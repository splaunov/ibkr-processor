package me.splaunov.ibkrprocessor.exporter

import jakarta.inject.Singleton
import me.splaunov.ibkrprocessor.data.InstrumentInformation
import me.splaunov.ibkrprocessor.data.PurchaseDetails
import me.splaunov.ibkrprocessor.data.SellingDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import me.splaunov.ibkrprocessor.data.toLocalDate
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.COMMISSION
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.COST
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.CURRENCYRATE
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.DATE
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.EXPENSES
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.PNL
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.PRICE
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.QUANTITY
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.SALEEXPENSES
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.SALEPROCEEDS
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.SALEPROCEEDS_USD
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.SECURITY_ID
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.SYMBOL
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.TAX
import me.splaunov.ibkrprocessor.reader.CurrencyRatesProvider
import mu.KotlinLogging
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

@Singleton
class Exporter(
    private val currencyRatesProvider: CurrencyRatesProvider,
) {
    private val logger = KotlinLogging.logger { }

    fun export(
        saleOperations: List<SellingDetails>,
        file: File,
        instrumentInformation: Map<String, InstrumentInformation>,
    ) {
        val template = TemplateHandler(currencyRatesProvider)
        saleOperations.sortedBy { it.sellOrder.symbol }.forEach { saleOp ->
            logger.debug { "Adding sale $saleOp" }
            val securityId = instrumentInformation[saleOp.sellOrder.symbol]?.securityId
                ?: error("Instrument information not found: ${saleOp.sellOrder.symbol}")
            saleOp.purchases.forEach { template.addPurchase(saleOp.sellOrder.date.toLocalDate().year, it, securityId) }
            template.addSale(saleOp, securityId)
        }
        template.addSummary()
        template.write(file)
    }

    private class TemplateHandler(private val currencyRatesProvider: CurrencyRatesProvider) {
        private val workbook: XSSFWorkbook
        private val formulaEvaluator: XSSFFormulaEvaluator
        private var currentRowNum = arrayOf(1, 1, 1, 1)
        private var firstPurchaseRowNum = arrayOf(1, 1, 1, 1)
        private val summaryRowCaption: String
        private val dataColumnsStyles: Array<XSSFCellStyle?>
        private val summaryColumnsStyles: Array<XSSFCellStyle?>

        init {
            val inStream = Exporter::class.java.getResourceAsStream("/template.xlsx")
                ?: error("Template not found.")
            inStream.use { workbook = XSSFWorkbook(inStream) }

            formulaEvaluator = workbook.creationHelper.createFormulaEvaluator()

            val dataRow = workbook.getSheetAt(0).getRow(1)
            dataColumnsStyles = arrayOfNulls(dataRow.lastCellNum + 1)
            dataRow.cellIterator().forEach { cell ->
                dataColumnsStyles[cell.columnIndex] = cell.cellStyle as XSSFCellStyle?
            }

            val summaryRow = workbook.getSheetAt(0).getRow(2)
            val summaryRowCaptionCell = summaryRow.getCell(0)
            summaryRowCaption = summaryRowCaptionCell.stringCellValue
            summaryColumnsStyles = arrayOfNulls(summaryRow.lastCellNum + 1)
            summaryRow.cellIterator().forEach { cell ->
                summaryColumnsStyles[cell.columnIndex] = cell.cellStyle as XSSFCellStyle?
            }
        }

        private fun getSheet(name: String): XSSFSheet {
            return workbook.getSheet(name)
                ?: error("Sheet with name '$name' not found in the template.")
        }

        fun addPurchase(sellYear: Int, purchase: PurchaseDetails, securityId: String) {
            val sheet = getSheet(sellYear.toString())
            val row = sheet.createRow(currentRowNum[workbook.indexOf(sheet)]++)
            createCells(row, purchase.purchaseOrder, securityId)

            val commissionCell = row.getCell(COMMISSION)
            commissionCell.cellFormula =
                "${purchase.purchaseOrder.commission}*${purchase.quantitySold}/${purchase.purchaseOrder.quantity}"
            row.getCell(QUANTITY).setCellValue(purchase.quantitySold.toDouble())
            row.getCell(CURRENCYRATE).setCellValue(
                currencyRatesProvider.getRate(
                    purchase.purchaseOrder.date.toLocalDate(),
                    purchase.purchaseOrder.currency
                ).toDouble()
            )

            val r = row.rowNum
            val expensesCell = row.getCell(EXPENSES)
            expensesCell.cellFormula = "(${COST[r]} + ${COMMISSION[r]})*${CURRENCYRATE[r]}"
        }

        fun addSale(sale: SellingDetails, securityId: String) {
            val sheet = getSheet(sale.sellOrder.date.toLocalDate().year.toString())
            val sheetIndex = workbook.indexOf(sheet)
            val row = sheet.createRow(currentRowNum[sheetIndex]++)
            createCells(row, sale.sellOrder, securityId)
            row.getCell(CURRENCYRATE).setCellValue(
                currencyRatesProvider.getRate(sale.sellOrder.date.toLocalDate(), sale.sellOrder.currency).toDouble()
            )

            val r = row.rowNum

            val saleProceedsUsdCell = row.getCell(SALEPROCEEDS_USD)
            saleProceedsUsdCell.cellFormula = COST[r]

            val saleProceedsCell = row.getCell(SALEPROCEEDS)
            saleProceedsCell.cellFormula = "${COST[r]}*${CURRENCYRATE[r]}"

            val saleExpensesCell = row.getCell(SALEEXPENSES)
            saleExpensesCell.cellFormula = "SUM(${EXPENSES[firstPurchaseRowNum[sheetIndex]]}:${EXPENSES[r - 1]}) + " +
                "${COMMISSION[r]}*${CURRENCYRATE[r]}"

            val pnlCell = row.getCell(PNL)
            pnlCell.cellFormula = "${SALEPROCEEDS[r]} + ${SALEEXPENSES[r]}"

            val taxCell = row.getCell(TAX)
            taxCell.cellFormula = "${PNL[r]}*13%"

            sheet.createRow(currentRowNum[sheetIndex]++)
            firstPurchaseRowNum[sheetIndex] = currentRowNum[sheetIndex]
        }

        fun addSummary() {
            for (sheetIndex in (0 until workbook.numberOfSheets)) {
                val sheet = workbook.getSheetAt(sheetIndex)
                val summaryRow = sheet.createRow(sheet.lastRowNum + 1)
                summaryColumnsStyles.forEachIndexed { i, style ->
                    summaryRow.createCell(i).cellStyle = style
                }

                val summaryCaptionCell = summaryRow.getCell(0)
                summaryCaptionCell.setCellValue(summaryRowCaption)

                val r = summaryRow.rowNum

                val saleProceedsUsdCell = summaryRow.getCell(SALEPROCEEDS_USD)
                saleProceedsUsdCell.cellFormula = "SUM(${SALEPROCEEDS_USD[1]}:${SALEPROCEEDS_USD[r - 1]})"

                val saleProceedsCell = summaryRow.getCell(SALEPROCEEDS)
                saleProceedsCell.cellFormula = "SUM(${SALEPROCEEDS[1]}:${SALEPROCEEDS[r - 1]})"

                val saleExpensesCell = summaryRow.getCell(SALEEXPENSES)
                saleExpensesCell.cellFormula = "SUM(${SALEEXPENSES[1]}:${SALEEXPENSES[r - 1]})"

                val pnlCell = summaryRow.getCell(PNL)
                pnlCell.cellFormula = "SUM(${PNL[1]}:${PNL[r - 1]})"

                val taxCell = summaryRow.getCell(TAX)
                taxCell.cellFormula = "SUM(${TAX[1]}:${TAX[r - 1]})"

                formulaEvaluator.evaluateAll()
            }
        }

        fun write(file: File) {
            FileOutputStream(file).use { workbook.write(it) }
        }

        private fun createCells(row: XSSFRow, trade: TradeOrder, securityId: String) {
            dataColumnsStyles.forEachIndexed { i, style ->
                row.createCell(i).cellStyle = style
            }
            row.getCell(SYMBOL).setCellValue(trade.symbol)
            row.getCell(SECURITY_ID).setCellValue(securityId)
            row.getCell(DATE).setCellValue(trade.date.toLocalDate())
            row.getCell(QUANTITY).setCellValue(trade.quantity.toDouble())
            row.getCell(PRICE).setCellValue(trade.price.toDouble())

            val r = row.rowNum
            val costCell = row.getCell(COST)
            costCell.cellFormula = "-${QUANTITY[r]}*${PRICE[r]}"

            row.getCell(COMMISSION).setCellValue(trade.commission.toDouble())
        }

        private enum class Column(val index: Int, val symbol: String) {
            SYMBOL(0, "A"),
            SECURITY_ID(1, "B"),
            DATE(2, "C"),
            QUANTITY(3, "D"),
            PRICE(4, "E"),
            COST(5, "F"),
            COMMISSION(6, "G"),
            CURRENCYRATE(7, "H"),
            EXPENSES(8, "I"),
            SALEPROCEEDS_USD(9, "J"),
            SALEPROCEEDS(10, "K"),
            SALEEXPENSES(11, "L"),
            PNL(12, "M"),
            TAX(13, "N");

            fun coordinates(rowNum: Int): String = "$symbol${rowNum + 1}"
            operator fun get(rowNum: Int): String = coordinates(rowNum)
        }

        //        private fun XSSFRow.createCell(column: Column): XSSFCell = createCell(column.index)
        private fun XSSFRow.getCell(column: Column): XSSFCell = getCell(column.index)
    }
}
