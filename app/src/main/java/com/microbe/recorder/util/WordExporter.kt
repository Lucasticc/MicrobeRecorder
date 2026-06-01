package com.microbe.recorder.util

import android.content.Context
import android.util.Base64
import com.microbe.recorder.database.RecordEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object WordExporter {

    /**
     * 导出记录到 Word 文件（HTML 内嵌图片，Word 可直接打开显示图片）
     */
    fun exportToWord(context: Context, records: List<RecordEntity>): File? {
        return try {
            val exportDir = FileHelper.getExportDir(context)
            val fileName = FileHelper.generateExportFileName("菌类实验记录", "doc")
            val file = File(exportDir, fileName)

            OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8).use { writer ->
                writer.write("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                    <meta charset="utf-8">
                    <style>
                        body { font-family: "SimSun", "宋体", serif; font-size: 14px; color: #333; margin: 40px; }
                        h1 { text-align: center; color: #1B8A5A; font-size: 24px; margin-bottom: 5px; }
                        .date { text-align: center; color: #999; font-size: 12px; margin-bottom: 30px; }
                        .record { margin-bottom: 30px; page-break-inside: avoid; }
                        .record-title { font-size: 18px; font-weight: bold; color: #1B8A5A; border-bottom: 2px solid #1B8A5A; padding-bottom: 5px; margin-bottom: 15px; }
                        table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
                        td { border: 1px solid #ddd; padding: 8px 12px; vertical-align: top; }
                        .label { background: #f0f7f4; font-weight: bold; width: 100px; color: #555; }
                        .value { color: #333; }
                        .photo-section { margin-top: 10px; }
                        .photo-section b { color: #1B8A5A; }
                        .photo-grid { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 10px; }
                        .photo-grid img { max-width: 300px; max-height: 200px; border: 1px solid #ddd; border-radius: 4px; }
                        .audio-section { margin-top: 8px; color: #666; font-size: 12px; }
                        .separator { border: none; border-top: 1px dashed #ccc; margin: 20px 0; }
                    </style>
                    </head>
                    <body>
                    <h1>🦠 菌类实验记录报告</h1>
                    <div class="date">导出时间：${FileHelper.formatDateTime(System.currentTimeMillis())}</div>
                """.trimIndent())

                records.forEachIndexed { index, record ->
                    writer.write("""
                        <div class="record">
                        <div class="record-title">记录 ${index + 1}：${escapeHtml(record.experimentNumber)}</div>
                        <table>
                            <tr><td class="label">实验编号</td><td class="value">${escapeHtml(record.experimentNumber)}</td></tr>
                            <tr><td class="label">样品名称</td><td class="value">${escapeHtml(record.sampleName)}</td></tr>
                            <tr><td class="label">培养时间</td><td class="value">${escapeHtml(record.cultureTime)}</td></tr>
                            <tr><td class="label">观察结果</td><td class="value">${escapeHtml(record.observationResult)}</td></tr>
                            <tr><td class="label">实验描述</td><td class="value">${escapeHtml(record.description)}</td></tr>
                            <tr><td class="label">备注</td><td class="value">${escapeHtml(record.notes)}</td></tr>
                            <tr><td class="label">创建时间</td><td class="value">${escapeHtml(FileHelper.formatDateTime(record.createdAt))}</td></tr>
                        </table>
                    """.trimIndent())

                    // 内嵌照片（base64）
                    if (record.photoPaths.isNotEmpty()) {
                        val paths = record.photoPaths.split(",")
                        writer.write("""<div class="photo-section"><b>📷 实验照片：</b></div><div class="photo-grid">""")
                        paths.forEach { photoPath ->
                            val photoFile = File(photoPath)
                            if (photoFile.exists()) {
                                try {
                                    val bytes = photoFile.readBytes()
                                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                    val mimeType = when (photoFile.extension.lowercase()) {
                                        "png" -> "image/png"
                                        "webp" -> "image/webp"
                                        else -> "image/jpeg"
                                    }
                                    writer.write("""<img src="data:$mimeType;base64,$base64" />""")
                                } catch (_: Exception) { }
                            }
                        }
                        writer.write("</div>")
                    }

                    // 录音信息
                    if (record.audioPath.isNotEmpty()) {
                        writer.write("""<div class="audio-section">🎙️ 录音文件：${escapeHtml(File(record.audioPath).name)}</div>""")
                    }

                    writer.write("</div>")
                    if (index < records.size - 1) {
                        writer.write("<hr class='separator'>")
                    }
                }

                writer.write("</body></html>")
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("\n", "<br>")
    }
}
