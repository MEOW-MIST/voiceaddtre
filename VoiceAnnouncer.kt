package com.roly.eldersdesktop.launcher

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAnnouncer(private val context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.CHINA)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.setLanguage(Locale.US)
                }
                isInitialized = true
            }
        }
    }

    fun announce(text: String) {
        if (!isInitialized) {
            return
        }

        textToSpeech?.apply {
            stop()
            speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }

    fun setEnabled(enabled: Boolean) {
        if (!enabled) {
            textToSpeech?.stop()
        }
    }

    companion object {
        /**
         * 将时间转换为中文语音播报格式
         * 例如：14:05 -> "十四点零五分"
         */
        fun formatTimeForSpeech(hour: Int, minute: Int): String {
            val hourText = when (hour) {
                0 -> "零点"
                1 -> "一点"
                2 -> "两点"
                3 -> "三点"
                4 -> "四点"
                5 -> "五点"
                6 -> "六点"
                7 -> "七点"
                8 -> "八点"
                9 -> "九点"
                10 -> "十点"
                11 -> "十一点"
                12 -> "十二点"
                13 -> "十三点"
                14 -> "十四点"
                15 -> "十五点"
                16 -> "十六点"
                17 -> "十七点"
                18 -> "十八点"
                19 -> "十九点"
                20 -> "二十点"
                21 -> "二十一点"
                22 -> "二十二点"
                23 -> "二十三点"
                else -> "${hour}点"
            }

            val minuteText = when (minute) {
                0 -> "整"
                in 1..9 -> "零${digitToChinese(minute)}分"
                else -> "${twoDigitToChinese(minute)}分"
            }

            return "$hourText$minuteText"
        }

        private fun digitToChinese(digit: Int): String {
            return when (digit) {
                0 -> "零"
                1 -> "一"
                2 -> "二"
                3 -> "三"
                4 -> "四"
                5 -> "五"
                6 -> "六"
                7 -> "七"
                8 -> "八"
                9 -> "九"
                else -> digit.toString()
            }
        }

        private fun twoDigitToChinese(number: Int): String {
            if (number < 10) return digitToChinese(number)
            
            val tens = number / 10
            val units = number % 10
            
            return when (tens) {
                1 -> "十${if (units > 0) digitToChinese(units) else ""}"
                else -> "${digitToChinese(tens)}十${if (units > 0) digitToChinese(units) else ""}"
            }
        }
    }
}
