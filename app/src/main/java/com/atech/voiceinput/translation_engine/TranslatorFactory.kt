

package com.atech.voiceinput.translation_engine

import android.app.Activity
import com.atech.voiceinput.translation_engine.translators.TextToSpeckConverter
import com.atech.voiceinput.translation_engine.translators.SpeechToTextConverter

class TranslatorFactory private constructor() {

    enum class TRANSLATORS {
        TEXT_TO_SPEECH,
        SPEECH_TO_TEXT
    }

    interface IConverter {
        fun initialize(message: String, appContext: Activity): IConverter

        fun getErrorText(errorCode: Int): String
    }


    fun with(TRANSLATORS: TRANSLATORS, conversionCallback: ConversionCallback): IConverter {
        return when (TRANSLATORS) {
            TranslatorFactory.TRANSLATORS.TEXT_TO_SPEECH ->
                //Get Text to speech translator
                TextToSpeckConverter(conversionCallback)

            TranslatorFactory.TRANSLATORS.SPEECH_TO_TEXT ->

                //Get speech to text translator
                SpeechToTextConverter(conversionCallback)
        }
    }

    companion object {
        val instance = TranslatorFactory()
    }
}
