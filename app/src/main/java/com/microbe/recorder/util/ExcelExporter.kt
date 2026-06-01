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

    var lastError: String = ""

    fun exportToExcel(context: Context, records: List<RecordEntity>): File? {
        return try {
            val workbook = XSSFWorkbook()
            val headerStyle = createHeaderStyle(workbook)
            val dataStyle = createDataStyle(workbook)
            val sheet = workbook.createSheet("菌类实验记录")
            val drawing = sheet.createDrawingPatriarch()

            // 标题行：种植日期 | 记录日期 | 处理组 | 观察记录 | 照片1 | 照片2 | 照片3 | 照片4 | 照片5 | 备注
            val headers = arrayOf("种植日期", "记录日期", "处理组", "观察记录", "照片1", "照片2", "照片3", "照片4", "照片5", "备注/调整")
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { i, h ->
                headerRow.createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
            }

            // 数据行
            records.forEachIndexed { index, record ->
                val row = sheet.createRow(index + 1)
                row.heightInPoints = 100f // 固定行高给照片

                row.createCell(0).apply { setCellValue(record.plantingDate); cellStyle = dataStyle }
                row.createCell(1).apply { setCellValue(FileHelper.formatDate(record.createdAt)); cellStyle = dataStyle }
                row.createCell(2).apply { setCellValue(record.treatmentGroup); cellStyle = dataStyle }
                row.createCell(3).apply { setCellValue(record.observationResult); cellStyle = dataStyle }

                // 5个照片单元格
                val photoList = if (record.photoPaths.isNotEmpty()) record.photoPaths.split(",") else emptyList()
                for (i in 0 until 5) {
                    val cell = row.createCell(4 + i)
                    cell.cellStyle = dataStyle
                    if (i < photoList.size) {
                        val photoFile = File(photoList[i])
                        if (photoFile.exists()) {
                            try {
                                val bytes = photoFile.readBytes()
                                val pictureIdx = workbook.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_JPEG)
                                val anchor = drawing.createAnchor(0, 0, 0, 0, 4 + i, index + 1, 5 + i, index + 2)
                                drawing.createPicture(anchor, pictureIdx)
                                cell.setCellValue("照片${i + 1}")
                            } catch (_: Exception) { cell.setCellValue("") }
                        }
                    }
                }

                row.createCell(9).apply { setCellValue(record.notes); cellStyle = dataStyle }
            }

            // 列宽
            sheet.setColumnWidth(0, 3500) // 种植日期
            sheet.setColumnWidth(1, 3500) // 记录日期
            sheet.setColumnWidth(2, 3500) // 处理组
            sheet.setColumnWidth(3, 8000) // 观察记录
            for (i in 4..8) sheet.setColumnWidth(i, 4000) // 照片列
            sheet.setColumnWidth(9, 5000) // 备注

            val exportDir = FileHelper.getExportDir(context)

            // 先写 xlsx 到临时文件
            val xlsxName = FileHelper.generateExportFileName("菌类实验记录", "xlsx")
            val xlsxFile = File(exportDir, xlsxName)
            FileOutputStream(xlsxFile).use { workbook.write(it) }
            workbook.close()

            // 创建 ZIP 包含 xlsx + 照片文件夹
            val zipName = FileHelper.generateExportFileName("菌类实验记录", "zip")
            val zipFile = File(exportDir, zipName)

            java.util.zip.ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
                // 写入 xlsx
                zip.putNextEntry(java.util.zip.ZipEntry(xlsxName))
                zip.write(xlsxFile.readBytes())
                zip.closeEntry()

                // 写入照片文件夹（全部平铺，命名：记录日期_处理组名称_种植日期_序号.jpg）
                val usedNames = mutableSetOf<String>()
                records.forEach { record ->
                    if (record.photoPaths.isNotEmpty()) {
                        val recordDate = FileHelper.formatDate(record.createdAt)
                        val group = record.treatmentGroup.ifEmpty { "未分组" }
                        val planting = record.plantingDate.ifEmpty { "未知日期" }
                        record.photoPaths.split(",").forEachIndexed { i, path ->
                            val photoFile = java.io.File(path)
                            if (photoFile.exists()) {
                                val ext = photoFile.extension.ifEmpty { "jpg" }
                                var photoName = "${recordDate}_${group}_${planting}_${i + 1}.$ext"
                                // 去重：同名则加序号
                                var counter = 2
                                while (usedNames.contains(photoName)) {
                                    photoName = "${recordDate}_${group}_${planting}_${i + 1}_$counter.$ext"
                                    counter++
                                }
                                usedNames.add(photoName)
                                zip.putNextEntry(java.util.zip.ZipEntry("photos/$photoName"))
                                zip.write(photoFile.readBytes())
                                zip.closeEntry()
                            }
                        }
                    }
                }
            }

            // 删除临时 xlsx
            xlsxFile.delete()

            zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            lastError = e.message ?: "未知错误"
            null
        }
    }

    private fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index)
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.borderBottom = BorderStyle.THIN; style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN; style.borderRight = BorderStyle.THIN
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        val font = workbook.createFont()
        font.bold = true; font.fontHeightInPoints = 12
        style.setFont(font)
        return style
    }

    private fun createDataStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        style.borderBottom = BorderStyle.THIN; style.borderTop = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN; style.borderRight = BorderStyle.THIN
        style.alignment = HorizontalAlignment.LEFT
        style.verticalAlignment = VerticalAlignment.CENTER
        style.wrapText = true
        val font = workbook.createFont()
        font.fontHeightInPoints = 11
        style.setFont(font)
        return style
    }
}
