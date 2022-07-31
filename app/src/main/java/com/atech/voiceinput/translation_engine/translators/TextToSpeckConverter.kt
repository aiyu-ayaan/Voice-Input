package com.atech.voiceinput.translation_engine.translators

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.atech.voiceinput.translation_engine.ConversionCallback
import com.atech.voiceinput.translation_engine.TranslatorFactory
import java.util.*

class TextToSpeckConverter(private val conversionCallaBack: ConversionCallback) :
    TranslatorFactory.IConverter {
    private val TAG = SpeechToTextConverter::class.java.name
    private var textToSpeech: TextToSpeech? = null

    override fun initialize(message: String, appContext: Activity): TranslatorFactory.IConverter {
        textToSpeech = TextToSpeech(appContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.getDefault()
                textToSpeech!!.setPitch(1.3f)
                textToSpeech!!.setSpeechRate(1f)
                textToSpeech!!.language = Locale.UK

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ttsGreater21(message)
                } else {
                    ttsUnder20(message)
                }
            } else {
                conversionCallaBack.onErrorOccurred("Failed to initialize TTS engine")
            }
        })
        return this
    }

    private fun finish() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }
    }

    private fun ttsUnder20(text: String) {
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MessageId"

        textToSpeech!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

            override fun onStart(utteranceId: String) {
                Log.d(TAG, "started listening")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                super.onError(utteranceId, errorCode)
                conversionCallaBack.onErrorOccurred("Some Error Occurred " + getErrorText(errorCode))

            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String) {
                conversionCallaBack.onErrorOccurred("Some Error Occurred $utteranceId")
            }

            override fun onDone(utteranceId: String) {
                //do some work here
                conversionCallaBack.onCompletion()
                finish()
            }
        })

        textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, map)

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun ttsGreater21(text: String) {
        val utteranceId = this.hashCode().toString() + ""
        textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        while (textToSpeech!!.isSpeaking) {
            Log.d(TAG, "Speaking")
        }
        conversionCallaBack.onCompletion()
    }


    override fun getErrorText(errorCode: Int): String {
        val message: String = when (errorCode) {
            TextToSpeech.ERROR -> "Generic error"
            TextToSpeech.ERROR_INVALID_REQUEST -> "Client side error, invalid request"
            TextToSpeech.ERROR_NOT_INSTALLED_YET -> "Insufficient download of the voice data"
            TextToSpeech.ERROR_NETWORK -> "Network error"
            TextToSpeech.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            TextToSpeech.ERROR_OUTPUT -> "Failure in to the output (audio device or a file)"
            TextToSpeech.ERROR_SYNTHESIS -> "Failure of a TTS engine to synthesize the given input."
            TextToSpeech.ERROR_SERVICE -> "error from server"
            else -> "Didn't understand, please try again."
        }
        return message
    }


}