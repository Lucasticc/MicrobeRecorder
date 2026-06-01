package com.microbe.recorder.util

import android.content.Context
import com.microbe.recorder.database.RecordEntity
import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.apache.poi.xwpf.usermodel.XWPFTableCell
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object WordExporter {

    /**
     * 导出记录到 Word 文件
     */
    fun exportToWord(context: Context, records: List<RecordEntity>): File? {
        return try {
            val document = XWPFDocument()

            // 创建标题
            val titleParagraph = document.createParagraph()
            titleParagraph.alignment = ParagraphAlignment.CENTER
            val titleRun = titleParagraph.createRun()
            titleRun.setText("微生物实验记录报告")
            titleRun.bold = true
            titleRun.fontSize = 22
            titleRun.fontFamily = "宋体"

            // 创建日期段落
            val dateParagraph = document.createParagraph()
            dateParagraph.alignment = ParagraphAlignment.CENTER
            val dateRun = dateParagraph.createRun()
            dateRun.setText("导出时间：${FileHelper.formatDateTime(System.currentTimeMillis())}")
            dateRun.fontSize = 12
            dateRun.fontFamily = "宋体"
            dateRun.color = "666666"

            // 空行
            document.createParagraph()

            // 为每条记录创建详细信息
            records.forEachIndexed { index, record ->
                // 记录标题
                val recordTitleParagraph = document.createParagraph()
                val recordTitleRun = recordTitleParagraph.createRun()
                recordTitleRun.setText("记录 ${index + 1}：${record.experimentNumber}")
                recordTitleRun.bold = true
                recordTitleRun.fontSize = 16
                recordTitleRun.fontFamily = "宋体"

                // 创建信息表格
                val table = document.createTable(7, 2)

                // 设置表格内容
                fillTableRow(table, 0, "实验编号", record.experimentNumber)
                fillTableRow(table, 1, "样品名称", record.sampleName)
                fillTableRow(table, 2, "培养时间", record.cultureTime)
                fillTableRow(table, 3, "观察结果", record.observationResult)
                fillTableRow(table, 4, "备注", record.notes)
                fillTableRow(table, 5, "实验描述", record.description)
                fillTableRow(table, 6, "创建时间", FileHelper.formatDateTime(record.createdAt))

                // 检查是否有照片
                if (record.photoPaths.isNotEmpty()) {
                    val photoParagraph = document.createParagraph()
                    val photoRun = photoParagraph.createRun()
                    photoRun.setText("实验照片：")
                    photoRun.bold = true
                    photoRun.fontSize = 12
                    photoRun.fontFamily = "宋体"

                    // 插入照片
                    val photoPaths = record.photoPaths.split(",")
                    photoPaths.forEach { photoPath ->
                        val photoFile = File(photoPath)
                        if (photoFile.exists()) {
                            try {
                                val photoParagraph2 = document.createParagraph()
                                photoParagraph2.alignment = ParagraphAlignment.CENTER
                                val photoRun2 = photoParagraph2.createRun()
                                val inputStream = FileInputStream(photoFile)
                                photoRun2.addPicture(
                                    inputStream,
                                    XWPFDocument.PICTURE_TYPE_JPEG,
                                    photoFile.name,
                                    Units.toEMU(300.0),
                                    Units.toEMU(225.0)
                                )
                                inputStream.close()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                // 检查是否有录音
                if (record.audioPath.isNotEmpty()) {
                    val audioParagraph = document.createParagraph()
                    val audioRun = audioParagraph.createRun()
                    audioRun.setText("录音文件：${File(record.audioPath).name}")
                    audioRun.fontSize = 12
                    audioRun.fontFamily = "宋体"
                    audioRun.color = "666666"
                }

                // 分隔线
                if (index < records.size - 1) {
                    val separatorParagraph = document.createParagraph()
                    separatorParagraph.alignment = ParagraphAlignment.CENTER
                    val separatorRun = separatorParagraph.createRun()
                    separatorRun.setText("────────────────────────────────────")
                    separatorRun.color = "CCCCCC"
                }

                document.createParagraph()
            }

            // 保存文件
            val exportDir = FileHelper.getExportDir(context)
            val fileName = FileHelper.generateExportFileName("微生物实验记录", "docx")
            val file = File(exportDir, fileName)

            FileOutputStream(file).use { outputStream ->
                document.write(outputStream)
            }

            document.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 填充表格行
     */
    private fun fillTableRow(table: XWPFTable, rowIndex: Int, label: String, value: String) {
        val row = table.getRow(rowIndex)

        // 标签单元格
        val labelCell = row.getCell(0)
        labelCell.text = label
        val labelParagraph = labelCell.paragraphs[0]
        val labelRun = labelParagraph.createRun()
        labelRun.bold = true
        labelRun.fontSize = 11
        labelRun.fontFamily = "宋体"

        // 值单元格
        val valueCell = row.getCell(1)
        valueCell.text = value
        val valueParagraph = valueCell.paragraphs[0]
        val valueRun = valueParagraph.createRun()
        valueRun.fontSize = 11
        valueRun.fontFamily = "宋体"
    }
}
