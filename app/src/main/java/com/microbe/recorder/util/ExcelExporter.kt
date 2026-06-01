package com.microbe.recorder.util

import android.content.Context
import com.microbe.recorder.database.RecordEntity
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExcelExporter {

    /**
     * 导出记录到 Excel 文件
     */
    fun exportToExcel(context: Context, records: List<RecordEntity>): File? {
        return try {
            val workbook = XSSFWorkbook()

            // 创建标题样式
            val headerStyle = createHeaderStyle(workbook)

            // 创建数据样式
            val dataStyle = createDataStyle(workbook)

            // 创建工作表
            val sheet = workbook.createSheet("微生物实验记录")

            // 创建标题行
            val headerRow = sheet.createRow(0)
            val headers = arrayOf(
                "序号", "实验编号", "样品名称", "培养时间",
                "观察结果", "备注", "实验描述", "创建时间"
            )

            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            // 填充数据
            records.forEachIndexed { index, record ->
                val row = sheet.createRow(index + 1)

                row.createCell(0).apply {
                    setCellValue((index + 1).toDouble())
                    cellStyle = dataStyle
                }

                row.createCell(1).apply {
                    setCellValue(record.experimentNumber)
                    cellStyle = dataStyle
                }

                row.createCell(2).apply {
                    setCellValue(record.sampleName)
                    cellStyle = dataStyle
                }

                row.createCell(3).apply {
                    setCellValue(record.cultureTime)
                    cellStyle = dataStyle
                }

                row.createCell(4).apply {
                    setCellValue(record.observationResult)
                    cellStyle = dataStyle
                }

                row.createCell(5).apply {
                    setCellValue(record.notes)
                    cellStyle = dataStyle
                }

                row.createCell(6).apply {
                    setCellValue(record.description)
                    cellStyle = dataStyle
                }

                row.createCell(7).apply {
                    setCellValue(FileHelper.formatDateTime(record.createdAt))
                    cellStyle = dataStyle
                }
            }

            // 自动调整列宽
            headers.indices.forEach { sheet.setColumnWidth(it, 5000) }
            sheet.setColumnWidth(4, 8000) // 观察结果列宽一些
            sheet.setColumnWidth(6, 8000) // 描述列宽一些

            // 保存文件
            val exportDir = FileHelper.getExportDir(context)
            val fileName = FileHelper.generateExportFileName("微生物实验记录", "xlsx")
            val file = File(exportDir, fileName)

            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }

            workbook.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 创建标题样式
     */
    private fun createHeaderStyle(workbook: XSSFWorkbook): CellStyle {
        val style = workbook.createCellStyle()

        // 背景色
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index)
        style.fillPattern = FillPatternType.SOLID_FOREGROUND

        // 边框
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN

        // 对齐
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER

        // 字体
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 12
        style.setFont(font)

        return style
    }

    /**
     * 创建数据样式
     */
    private fun createDataStyle(workbook: XSSFWorkbook): CellStyle {
        val style = workbook.createCellStyle()

        // 边框
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN

        // 对齐
        style.alignment = HorizontalAlignment.LEFT
        style.verticalAlignment = VerticalAlignment.CENTER

        // 自动换行
        style.wrapText = true

        // 字体
        val font = workbook.createFont()
        font.fontHeightInPoints = 11
        style.setFont(font)

        return style
    }
}
