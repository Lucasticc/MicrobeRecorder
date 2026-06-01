package com.microbe.recorder.util

import android.content.Context
import android.util.Base64
import com.microbe.recorder.database.RecordEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object WordExporter {

    var lastError: String = ""

    fun exportToWord(context: Context, records: List<RecordEntity>): File? {
        return try {
            val exportDir = FileHelper.getExportDir(context)
            val fileName = FileHelper.generateExportFileName("菌类实验记录", "doc")
            val file = File(exportDir, fileName)

            OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8).use { writer ->
                writer.write("""
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
                    writer.write("""<div class="record">
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
                        writer.write("""<div class="photo-grid">""")
                        record.photoPaths.split(",").forEach { path ->
                            val f = File(path)
                            if (f.exists()) {
                                try {
                                    val base64 = Base64.encodeToString(f.readBytes(), Base64.NO_WRAP)
                                    val mime = when (f.extension.lowercase()) { "png" -> "image/png"; else -> "image/jpeg" }
                                    writer.write("""<img src="data:$mime;base64,$base64" />""")
                                } catch (_: Exception) {}
                            }
                        }
                        writer.write("</div>")
                    }

                    writer.write("</div>")
                    if (index < records.size - 1) writer.write("<hr class='separator'>")
                }

                writer.write("</body></html>")
            }

            file
        } catch (e: Exception) { e.printStackTrace(); lastError = e.message ?: "未知错误"; null }
    }

    private fun escapeHtml(text: String): String =
        text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("\n","<br>")
}
