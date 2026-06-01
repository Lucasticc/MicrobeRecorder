package com.microbe.recorder.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioFile: File? = null
    private var isRecording = false

    /**
     * 开始录音
     */
    fun startRecording(): String? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFileName = "AUDIO_${timeStamp}.m4a"

        val storageDir = File(context.getExternalFilesDir(null), "audio")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        currentAudioFile = File(storageDir, audioFileName)

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        return try {
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(currentAudioFile?.absolutePath)
                prepare()
                start()
                isRecording = true
            }
            currentAudioFile?.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 停止录音
     */
    fun stopRecording(): String? {
        return try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
                currentAudioFile?.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            null
        }
    }

    /**
     * 取消录音
     */
    fun cancelRecording() {
        try {
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                currentAudioFile?.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            isRecording = false
            currentAudioFile = null
        }
    }

    /**
     * 是否正在录音
     */
    fun isCurrentlyRecording(): Boolean = isRecording

    /**
     * 获取当前录音文件路径
     */
    fun getCurrentAudioPath(): String? = currentAudioFile?.absolutePath

    /**
     * 删除录音文件
     */
    fun deleteAudioFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
