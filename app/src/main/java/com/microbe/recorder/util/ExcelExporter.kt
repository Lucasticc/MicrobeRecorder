package com.microbe.recorder.util

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.microbe.recorder.database.RecordEntity
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFClientAnchor
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.min

object ExcelExporter {

    var lastError: String = ""
    private const val TAG = "ExcelExporter"

    data class ExportResult(val fileName: String, val shareUri: String? = null)
    /** 记录 → 其照片文件列表的映射，用于按记录维度报告导出进度 */
    private data class RecordPhotos(val recordIndex: Int, val photos: List<File>)

    private const val MAX_IMAGE_FILE_SIZE = 1 * 1024 * 1024L  // 1MB: 原图直接嵌入
    private const val MAX_IMAGE_DIMENSION = 1500               // 超过则缩放

    /**
     * 导出 Excel（图片嵌入单元格）
     * 1. POI 创建工作簿 + 用 createPictureAnchor 将图片锚定到单元格
     * 2. 流式写入 ZIP（先写 XML，再逐张流式注入原图）
     */
    fun exportToExcel(
        context: Context,
        records: List<RecordEntity>,
        customName: String? = null,
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ): ExportResult? {
        Log.d(TAG, "开始导出Excel，记录数: ${records.size}")

        val xlsxName = if (!customName.isNullOrBlank()) {
            if (customName.endsWith(".xlsx")) customName else "$customName.xlsx"
        } else FileHelper.generateExportFileName("菌类实验记录", "xlsx")

        try {
            // 1. 用 POI 创建工作簿，图片通过 createPictureAnchor 嵌入到单元格
            val (workbookEntries, recordPhotosList) = createWorkbookWithPictures(records)

            val streamResult = FileHelper.openDownloadsStream(context, xlsxName,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            if (streamResult == null) {
                lastError = "无法创建下载文件"
                return null
            }

            streamResult.first.use { os ->
                ZipOutputStream(os).use { zos ->
                    // 写入 XML 条目（含 drawing 结构，不含媒体文件）
                    for ((name, bytes) in workbookEntries) {
                        zos.putNextEntry(ZipEntry(name))
                        zos.write(bytes)
                        zos.closeEntry()
                    }

                    // 流式写入真实原图（按记录分组，每处理完一条记录报告一次进度）
                    val buffer = ByteArray(16384)
                    var imageIdx = 0
                    for ((recordIdx, rp) in recordPhotosList.withIndex()) {
                        for (photo in rp.photos) {
                            zos.putNextEntry(ZipEntry("xl/media/image${imageIdx + 1}.png"))
                            FileInputStream(photo).use { fis ->
                                var n: Int
                                while (fis.read(buffer).also { n = it } != -1) {
                                    zos.write(buffer, 0, n)
                                }
                            }
                            zos.closeEntry()
                            imageIdx++
                        }
                        // 每条记录的所有图片写完后报告一次进度
                        onProgress?.invoke(recordIdx + 1, records.size)
                    }
                    if (recordPhotosList.isEmpty()) {
                        onProgress?.invoke(records.size, records.size)
                    }
                }
            }

            Log.d(TAG, "导出完成: ${streamResult.second}")
            return ExportResult(xlsxName, streamResult.second)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace(); lastError = "记录太多导致内存不足"; return null
        } catch (e: Exception) {
            e.printStackTrace(); Log.e(TAG, "导出异常", e); lastError = "导出失败: ${e.message}"; return null
        }
    }

    /**
     * 用 POI 创建工作簿，通过 createPictureAnchor 将图片锚定到单元格。
     * 返回 (ZIP 条目列表, 每条记录对应的照片文件列表)
     */
    private fun createWorkbookWithPictures(
        records: List<RecordEntity>
    ): Pair<List<Pair<String, ByteArray>>, List<RecordPhotos>> {
        val entries = mutableListOf<Pair<String, ByteArray>>()
        val recordPhotosList = mutableListOf<RecordPhotos>()

        XSSFWorkbook().use { wb ->
            val hs = createHeaderStyle(wb); val dsLarge = createDataStyleLarge(wb); val dsSmall = createDataStyleSmall(wb)
            val sheet = wb.createSheet("菌类实验记录")

            val hdrs = arrayOf("种植日期", "记录日期", "处理组", "观察记录", "照片1", "照片2", "照片3", "照片4", "照片5", "备注/调整")
            val hr = sheet.createRow(0)
            hdrs.forEachIndexed { i, h -> hr.createCell(i).apply { setCellValue(h); cellStyle = hs } }

            // 创建 drawing patriarch（所有图片都锚定到这个 drawing）
            val patriarch = sheet.createDrawingPatriarch()

            records.forEachIndexed { idx, r ->
                val row = sheet.createRow(idx + 1)
                row.createCell(0).apply { setCellValue(r.plantingDate); cellStyle = dsLarge }
                row.createCell(1).apply { setCellValue(FileHelper.formatDate(r.createdAt)); cellStyle = dsLarge }
                row.createCell(2).apply { setCellValue(r.treatmentGroup); cellStyle = dsLarge }
                // 观察记录：根据文字长度自适应字号
                row.createCell(3).apply { setCellValue(r.observationResult); cellStyle = adaptiveStyle(r.observationResult, dsLarge, dsSmall) }
                val pl = if (r.photoPaths.isNotEmpty()) r.photoPaths.split(",") else emptyList()
                val currentRecordPhotos = mutableListOf<File>()
                for (i in 0 until 5) {
                    val c = row.createCell(4 + i); c.cellStyle = dsLarge
                    if (i < pl.size) {
                        val f = File(pl[i].trim())
                        if (f.exists()) {
                            // 读取图片，大图自动压缩以控制内存
                            val imageBytes = compressImageIfNeeded(f)
                            val pictureIdx = wb.addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG)

                            // XSSFClientAnchor: 锚定到 (row, col) 起点
                            // dx1/dy1=0: 从单元格左上角开始
                            // dx2/dy2=0: 图片占满整个单元格（随列宽行高联动）
                            val anchor = XSSFClientAnchor(
                                0, 0, 0, 0,                                // dx1, dy1, dx2, dy2 (EMU)
                                (4 + i), (idx + 1),                         // 起始列、起始行
                                (4 + i + 1), (idx + 2)                      // 终止列、终止行
                            )
                            patriarch.createPicture(anchor, pictureIdx)

                            currentRecordPhotos.add(f)
                        }
                    }
                }
                if (currentRecordPhotos.isNotEmpty()) {
                    recordPhotosList.add(RecordPhotos(idx, currentRecordPhotos))
                }
                row.createCell(9).apply { setCellValue(r.notes); cellStyle = adaptiveStyle(r.notes, dsLarge, dsSmall) }
            }
            sheet.setColumnWidth(0, 3500); sheet.setColumnWidth(1, 3500)
            sheet.setColumnWidth(2, 3500); sheet.setColumnWidth(3, 8000)
            for (i in 4..8) sheet.setColumnWidth(i, 4500); sheet.setColumnWidth(9, 5000)
            for (idx in records.indices) {
                sheet.getRow(idx + 1)?.height = (40 * 20).toShort()
            }

            // 写入临时文件，提取 ZIP 条目
            val tmp = File.createTempFile("poi_", ".xlsx")
            try {
                FileOutputStream(tmp).use { wb.write(it) }
                ZipInputStream(FileInputStream(tmp)).use { zis ->
                    var e = zis.nextEntry
                    while (e != null) {
                        if (!e.isDirectory) {
                            entries.add(e.name to zis.readBytes())
                        }
                        e = zis.nextEntry
                    }
                }
            } finally { tmp.delete() }
        }
        return entries to recordPhotosList
    }

    /**
     * 压缩图片以控制内存。小于 1MB 直接读取，大于 1MB 缩放后 JPEG 压缩。
     */
    private fun compressImageIfNeeded(file: File): ByteArray {
        if (file.length() <= MAX_IMAGE_FILE_SIZE) {
            return file.readBytes()
        }
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        var inSampleSize = 1
        while (options.outWidth / inSampleSize > MAX_IMAGE_DIMENSION || options.outHeight / inSampleSize > MAX_IMAGE_DIMENSION) {
            inSampleSize *= 2
        }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath,
            BitmapFactory.Options().apply { this.inSampleSize = inSampleSize })
            ?: return file.readBytes()
        val os = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, os)
        bitmap.recycle()
        return os.toByteArray()
    }

    // ===== 照片 ZIP 导出（分块读写，按记录数进度）=====
    fun exportPhotos(
        context: Context,
        records: List<RecordEntity>,
        customName: String? = null,
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ): ExportResult? {
        Log.d(TAG, "开始导出照片，记录数: ${records.size}")
        return try {
            val zipName = if (!customName.isNullOrBlank()) {
                if (customName.endsWith(".zip")) customName else "$customName.zip"
            } else FileHelper.generateExportFileName("菌类实验记录_照片", "zip")

            val streamResult = FileHelper.openDownloadsStream(context, zipName, "application/zip")
            if (streamResult == null) { lastError = "无法创建下载文件"; return null }

            val totalRecords = records.size
            val buffer = ByteArray(8192)

            streamResult.first.use { os ->
                ZipOutputStream(os).use { zip ->
                    val usedNames = mutableSetOf<String>()
                    records.forEachIndexed { recordIdx, record ->
                        if (record.photoPaths.isNotEmpty()) {
                            val rd = FileHelper.formatDate(record.createdAt).replace("-", "")
                            val g = record.treatmentGroup.ifEmpty { "未分组" }
                            val pl = record.plantingDate.replace("-", "").ifEmpty { "未知日期" }
                            record.photoPaths.split(",").forEachIndexed { i, path ->
                                val pf = File(path.trim())
                                if (pf.exists()) {
                                    val ext = pf.extension.ifEmpty { "jpg" }
                                    var pn = "${rd}_${g}_${pl}_${i + 1}.$ext"; var c = 2
                                    while (usedNames.contains(pn)) { pn = "${rd}_${g}_${pl}_${i + 1}_$c.$ext"; c++ }
                                    usedNames.add(pn)
                                    zip.putNextEntry(ZipEntry(pn))
                                    pf.inputStream().use { inp -> var n: Int; while (inp.read(buffer).also { n = it } != -1) zip.write(buffer, 0, n) }
                                    zip.closeEntry()
                                }
                            }
                        }
                        onProgress?.invoke(recordIdx + 1, totalRecords)
                    }
                }
            }
            ExportResult(zipName, streamResult.second)
        } catch (e: OutOfMemoryError) { e.printStackTrace(); lastError = "照片太多导致内存不足"; null
        } catch (e: Exception) { e.printStackTrace(); lastError = "导出照片失败: ${e.message}"; null }
    }

    /**
     * 根据文字长度自适应字号：短文字用大字号，长文字用小字号
     * 阈值：20个字符（约一行）
     */
    private fun adaptiveStyle(text: String, large: XSSFCellStyle, small: XSSFCellStyle): XSSFCellStyle {
        return if (text.length <= 20) large else small
    }

    private fun createHeaderStyle(wb: XSSFWorkbook): XSSFCellStyle {
        val s = wb.createCellStyle()
        s.fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index; s.fillPattern = FillPatternType.SOLID_FOREGROUND
        s.borderBottom = BorderStyle.THIN; s.borderTop = BorderStyle.THIN; s.borderLeft = BorderStyle.THIN; s.borderRight = BorderStyle.THIN
        s.alignment = HorizontalAlignment.CENTER; s.verticalAlignment = VerticalAlignment.CENTER
        s.setFont(wb.createFont().apply { bold = true; fontHeightInPoints = 11 }); return s
    }

    /** 短文字样式：大字号（12pt） */
    private fun createDataStyleLarge(wb: XSSFWorkbook): XSSFCellStyle {
        val s = wb.createCellStyle()
        s.borderBottom = BorderStyle.THIN; s.borderTop = BorderStyle.THIN; s.borderLeft = BorderStyle.THIN; s.borderRight = BorderStyle.THIN
        s.alignment = HorizontalAlignment.LEFT; s.verticalAlignment = VerticalAlignment.CENTER; s.wrapText = true
        s.setFont(wb.createFont().apply { fontHeightInPoints = 12 }); return s
    }

    /** 长文字样式：小字号（9pt），适配两行 */
    private fun createDataStyleSmall(wb: XSSFWorkbook): XSSFCellStyle {
        val s = wb.createCellStyle()
        s.borderBottom = BorderStyle.THIN; s.borderTop = BorderStyle.THIN; s.borderLeft = BorderStyle.THIN; s.borderRight = BorderStyle.THIN
        s.alignment = HorizontalAlignment.LEFT; s.verticalAlignment = VerticalAlignment.CENTER; s.wrapText = true
        s.setFont(wb.createFont().apply { fontHeightInPoints = 9 }); return s
    }
}
