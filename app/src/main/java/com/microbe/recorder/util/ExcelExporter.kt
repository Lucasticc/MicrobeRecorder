package com.microbe.recorder.util

import android.content.Context
import com.microbe.recorder.database.RecordEntity
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExcelExporter {

    fun exportToExcel(context: Context, records: List<RecordEntity>): File? {
        return try {
            val workbook = XSSFWorkbook()

            val headerStyle = createHeaderStyle(workbook)
            val dataStyle = createDataStyle(workbook)

            val sheet = workbook.createSheet("菌类实验记录")

            // 标题行
            val headers = arrayOf(
                "序号", "实验编号", "样品名称", "培养时间",
                "观察结果", "实验描述", "备注", "创建时间", "照片"
            )

            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { i, h ->
                headerRow.createCell(i).apply {
                    setCellValue(h)
                    cellStyle = headerStyle
                }
            }

            // 用于嵌入图片
            val drawing = sheet.createDrawingPatriarch()

            // 数据行
            records.forEachIndexed { index, record ->
                val row = sheet.createRow(index + 1)

                row.createCell(0).apply { setCellValue((index + 1).toDouble()); cellStyle = dataStyle }
                row.createCell(1).apply { setCellValue(record.experimentNumber); cellStyle = dataStyle }
                row.createCell(2).apply { setCellValue(record.sampleName); cellStyle = dataStyle }
                row.createCell(3).apply { setCellValue(record.cultureTime); cellStyle = dataStyle }
                row.createCell(4).apply { setCellValue(record.observationResult); cellStyle = dataStyle }
                row.createCell(5).apply { setCellValue(record.description); cellStyle = dataStyle }
                row.createCell(6).apply { setCellValue(record.notes); cellStyle = dataStyle }
                row.createCell(7).apply { setCellValue(FileHelper.formatDateTime(record.createdAt)); cellStyle = dataStyle }

                // 嵌入照片
                if (record.photoPaths.isNotEmpty()) {
                    val paths = record.photoPaths.split(",")
                    var colOffset = 0f
                    paths.forEach { photoPath ->
                        val photoFile = File(photoPath)
                        if (photoFile.exists()) {
                            try {
                                val bytes = photoFile.readBytes()
                                val pictureIdx = workbook.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_JPEG)

                                val anchor = drawing.createAnchor(0, 0, 0, 0, 8, index + 1, 11, index + 1 + 6)

                                drawing.createPicture(anchor, pictureIdx)
                            } catch (_: Exception) { }
                        }
                    }
                }

                // 行高（如果有照片就加大）
                if (record.photoPaths.isNotEmpty()) {
                    row.heightInPoints = 120f
                }
            }

            // 列宽
            sheet.setColumnWidth(0, 2000)
            sheet.setColumnWidth(1, 4000)
            sheet.setColumnWidth(2, 4000)
            sheet.setColumnWidth(3, 3000)
            sheet.setColumnWidth(4, 6000)
            sheet.setColumnWidth(5, 6000)
            sheet.setColumnWidth(6, 4000)
            sheet.setColumnWidth(7, 5000)
            sheet.setColumnWidth(8, 12000) // 照片列

            // 保存文件
            val exportDir = FileHelper.getExportDir(context)
            val fileName = FileHelper.generateExportFileName("菌类实验记录", "xlsx")
            val file = File(exportDir, fileName)

            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index)
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 12
        style.setFont(font)
        return style
    }

    private fun createDataStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        style.borderBottom = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        style.alignment = HorizontalAlignment.LEFT
        style.verticalAlignment = VerticalAlignment.CENTER
        style.wrapText = true
        val font = workbook.createFont()
        font.fontHeightInPoints = 11
        style.setFont(font)
        return style
    }
}
