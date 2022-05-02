package me.splaunov.ibkrprocessor.exporter

import me.splaunov.ibkrprocessor.data.PurchaseOperationDetails
import me.splaunov.ibkrprocessor.data.SellOperationDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import me.splaunov.ibkrprocessor.exporter.Exporter.TemplateHandler.Column.*
import org.apache.poi.xssf.usermodel.*
import java.io.File
import java.io.FileOutputStream
import jakarta.inject.Singleton

@Singleton
class Exporter {

    fun export(saleOperations: List<SellOperationDetails>, file: File) {
        val template = TemplateHandler()
        saleOperations.forEach { saleOp ->
            saleOp.purchases.forEach { template.addPurchase(saleOp.sellOrder.date.year, it) }
            template.addSale(saleOp)
        }
        template.addSummary()
        template.write(file)
    }

    private class TemplateHandler {
        private val workbook: XSSFWorkbook
        private val formulaEvaluator: XSSFFormulaEvaluator
        private var currentRowNum = arrayOf(1, 1)
        private var firstPurchaseRowNum = arrayOf(1, 1)
        private val summaryRowCaption: String
        private val dataColumnsStyles: Array<XSSFCellStyle?>
        private val summaryColumnsStyles: Array<XSSFCellStyle?>

        init {
            val inStream = Exporter::class.java.getResourceAsStream("/template.xlsx")
                ?: throw IllegalStateException("Template not found.")
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
            return workbook.getSheet(name) ?: throw IllegalStateException("Sheet with name '$name' not found in the template.")
        }

        fun addPurchase(sellYear: Int, purchase: PurchaseOperationDetails) {
            val sheet = getSheet(sellYear.toString())
            val row = sheet.createRow(currentRowNum[workbook.indexOf(sheet)]++)
            createCells(row, purchase.purchaseOrder)

            val commissionCell = row.getCell(COMMISSION)
            commissionCell.cellFormula =
                "${purchase.purchaseOrder.commission}*${purchase.quantitySold}/${purchase.purchaseOrder.quantity}"
            row.getCell(QUANTITY).setCellValue(purchase.quantitySold.toDouble())
            row.getCell(CURRENCYRATE).setCellValue(purchase.currencyRate.toDouble())

            val r = row.rowNum
            val expensesCell = row.getCell(EXPENSES)
            expensesCell.cellFormula = "(${COST[r]} + ${COMMISSION[r]})*${CURRENCYRATE[r]}"
        }

        fun addSale(sale: SellOperationDetails) {
            val sheet = getSheet(sale.sellOrder.date.year.toString())
            val sheetIndex = workbook.indexOf(sheet)
            val row = sheet.createRow(currentRowNum[sheetIndex]++)
            createCells(row, sale.sellOrder)
            row.getCell(CURRENCYRATE).setCellValue(sale.currencyRate.toDouble())

            val r = row.rowNum

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

        private fun createCells(row: XSSFRow, trade: TradeOrder) {
            dataColumnsStyles.forEachIndexed { i, style ->
                row.createCell(i).cellStyle = style
            }
            row.getCell(SYMBOL).setCellValue(trade.symbol)
            row.getCell(DATE).setCellValue(trade.date)
            row.getCell(QUANTITY).setCellValue(trade.quantity.toDouble())
            row.getCell(PRICE).setCellValue(trade.price.toDouble())

            val r = row.rowNum
            val costCell = row.getCell(COST)
            costCell.cellFormula = "-${QUANTITY[r]}*${PRICE[r]}"

            row.getCell(COMMISSION).setCellValue(trade.commission.toDouble())
        }

        private enum class Column(val index: Int, val symbol: String) {
            SYMBOL(0, "A"),
            DATE(1, "B"),
            QUANTITY(2, "C"),
            PRICE(3, "D"),
            COST(4, "E"),
            COMMISSION(5, "F"),
            CURRENCYRATE(6, "G"),
            EXPENSES(7, "H"),
            SALEPROCEEDS(8, "I"),
            SALEEXPENSES(9, "J"),
            PNL(10, "K"),
            TAX(11, "L");

            fun coordinates(rowNum: Int): String = "$symbol${rowNum + 1}"
            operator fun get(rowNum: Int): String = coordinates(rowNum)
        }

        //        private fun XSSFRow.createCell(column: Column): XSSFCell = createCell(column.index)
        private fun XSSFRow.getCell(column: Column): XSSFCell = getCell(column.index)
    }
}




