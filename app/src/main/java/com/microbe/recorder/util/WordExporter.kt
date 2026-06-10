package com.microbe.recorder.util

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import com.microbe.recorder.database.RecordEntity
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.min
import kotlin.math.sqrt

object WordExporter {

    private const val MAX_IMAGE_FILE_SIZE = 1 * 1024 * 1024L  // 1MB: 原图直接嵌入
    private const val MAX_IMAGE_DIMENSION = 1500               // 超过则缩放

    var lastError: String = ""

    fun exportToWord(context: Context, records: List<RecordEntity>, customName: String? = null): ExcelExporter.ExportResult? {
        return try {
            val htmlBuilder = StringBuilder()

            htmlBuilder.append("""
                <!DOCTYPE html><html><head><meta charset="utf-8">
                <style>
                    body { font-family: "SimSun","宋体",serif; font-size:14px; color:#333; margin:40px; }
                    h1 { text-align:center; color:#1B8A5A; font-size:24px; margin-bottom:5px; }
                    .date { text-align:center; color:#999; font-size:12px; margin-bottom:30px; }
                    .record { margin-bottom:30px; page-break-inside:avoid; }
                    .record-title { font-size:16px; font-weight:bold; color:#1B8A5A; border-bottom:2px solid #1B8A5A; padding-bottom:5px; margin-bottom:10px; }
                    table { width:100%; border-collapse:collapse; margin-bottom:10px; }
                    td { border:1px solid #ddd; padding:8px 12px; vertical-align:top; }
                    .label { background:#f0f7f4; font-weight:bold; width:80px; color:#555; }
                    .photo-grid { display:flex; flex-wrap:wrap; gap:10px; margin-top:10px; }
                    .photo-grid img { max-width:250px; max-height:180px; border:1px solid #ddd; border-radius:4px; }
                    .separator { border:none; border-top:1px dashed #ccc; margin:20px 0; }
                </style></head><body>
                <h1>🦠 菌类实验记录报告</h1>
                <div class="date">导出时间：${FileHelper.formatDateTime(System.currentTimeMillis())}</div>
            """.trimIndent())

            records.forEachIndexed { index, record ->
                htmlBuilder.append("""<div class="record">
                    <div class="record-title">${escapeHtml(record.plantingDate)} - ${escapeHtml(record.treatmentGroup)}</div>
                    <table>
                        <tr><td class="label">种植日期</td><td>${escapeHtml(record.plantingDate)}</td></tr>
                        <tr><td class="label">记录日期</td><td>${escapeHtml(FileHelper.formatDate(record.createdAt))}</td></tr>
                        <tr><td class="label">处理组</td><td>${escapeHtml(record.treatmentGroup)}</td></tr>
                        <tr><td class="label">观察记录</td><td>${escapeHtml(record.observationResult)}</td></tr>
                        <tr><td class="label">备注</td><td>${escapeHtml(record.notes)}</td></tr>
                    </table>
                """.trimIndent())

                if (record.photoPaths.isNotEmpty()) {
                    htmlBuilder.append("""<div class="photo-grid">""")
                    record.photoPaths.split(",").forEach { path ->
                        val f = File(path.trim())
                        if (f.exists()) {
                            try {
                                val base64 = encodePhotoBase64(f)
                                val mime = when (f.extension.lowercase()) { "png" -> "image/png"; else -> "image/jpeg" }
                                htmlBuilder.append("""<img src="data:$mime;base64,$base64" />""")
                            } catch (_: Exception) {}
                        }
                    }
                    htmlBuilder.append("</div>")
                }

                htmlBuilder.append("</div>")
                if (index < records.size - 1) htmlBuilder.append("<hr class='separator'>")
            }

            htmlBuilder.append("</body></html>")

            val bytes = htmlBuilder.toString().toByteArray(Charsets.UTF_8)

            val fileName = if (!customName.isNullOrBlank()) {
                if (customName.endsWith(".doc")) customName else "$customName.doc"
            } else FileHelper.generateExportFileName("菌类实验记录", "doc")

            val path = FileHelper.saveToDownloads(context, fileName, bytes, "application/msword")
            if (path != null) {
                ExcelExporter.ExportResult(fileName, path)
            } else {
                lastError = "无法保存到下载目录"
                null
            }
        } catch (e: Exception) { e.printStackTrace(); lastError = e.message ?: "未知错误"; null }
    }

    /**
     * 编码照片为 Base64，自动处理大图压缩以避免 OOM。
     * - 小于 1MB 的图片直接读取原图
     * - 大于 1MB 或大分辨率的图片自动缩放后再编码
     */
    private fun encodePhotoBase64(file: File): String {
        if (file.length() <= MAX_IMAGE_FILE_SIZE) {
            // 小文件直接读取
            return Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
        }

        // 大文件：解码 → 缩放 → 重编码
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)

        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        while (width / inSampleSize > MAX_IMAGE_DIMENSION || height / inSampleSize > MAX_IMAGE_DIMENSION) {
            inSampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
            ?: return Base64.encodeToString(file.readBytes(), Base64.NO_WRAP) // 回退到原图

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
        bitmap.recycle()
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun escapeHtml(text: String): String =
        text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("\n","<br>")
}
