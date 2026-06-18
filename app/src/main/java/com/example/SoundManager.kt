package com.example

import android.media.AudioManager
import android.media.ToneGenerator

object SoundManager {
    private var toneGenerator: ToneGenerator? = null
    private var enabled: Boolean = true

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEnabled(isEnabled: Boolean) {
        enabled = isEnabled
    }

    fun isEnabled(): Boolean = enabled

    fun playClick() {
        if (!enabled) return
        Thread {
            try {
                // Short, soft beep for clicks
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun playCorrect() {
        if (!enabled) return
        Thread {
            try {
                // Happy arpeggio: two quick rising tones
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_5, 100)
                Thread.sleep(80)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_9, 150)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun playWrong() {
        if (!enabled) return
        Thread {
            try {
                // Fat low frequency buzz
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 220)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun playCoin() {
        if (!enabled) return
        Thread {
            try {
                // Traditional retro coin sound
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_A, 80)
                Thread.sleep(70)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 120)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun playLevelUp() {
        if (!enabled) return
        Thread {
            try {
                // Quick rising triumphant scale
                val tones = listOf(
                    ToneGenerator.TONE_DTMF_1,
                    ToneGenerator.TONE_DTMF_4,
                    ToneGenerator.TONE_DTMF_7,
                    ToneGenerator.TONE_DTMF_C
                )
                for (t in tones) {
                    toneGenerator?.startTone(t, 80)
                    Thread.sleep(70)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
