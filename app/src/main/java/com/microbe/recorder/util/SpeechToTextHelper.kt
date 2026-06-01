package com.microbe.recorder.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class SpeechToTextHelper(
    private val context: Context,
    private val listener: SpeechToTextListener
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    interface SpeechToTextListener {
        fun onSpeechResult(text: String)
        fun onSpeechPartialResult(text: String)
        fun onSpeechError(error: String)
        fun onSpeechStarted()
        fun onSpeechEnded()
    }

    /**
     * 检查语音识别是否可用
     */
    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * 开始语音识别
     */
    fun startListening() {
        if (isListening) {
            stopListening()
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    listener.onSpeechStarted()
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    listener.onSpeechEnded()
                }

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "录音错误"
                        SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                        SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                        SpeechRecognizer.ERROR_NO_MATCH -> "未识别到语音"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别引擎忙"
                        SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音输入超时"
                        else -> "未知错误: $error"
                    }
                    listener.onSpeechError(errorMessage)
                    isListening = false
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        listener.onSpeechResult(matches[0])
                    }
                    isListening = false
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        listener.onSpeechPartialResult(matches[0])
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.CHINESE.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(intent)
        isListening = true
    }

    /**
     * 停止语音识别
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    /**
     * 取消语音识别
     */
    fun cancelListening() {
        speechRecognizer?.cancel()
        isListening = false
    }

    /**
     * 是否正在监听
     */
    fun isCurrentlyListening(): Boolean = isListening

    /**
     * 释放资源
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }
}
