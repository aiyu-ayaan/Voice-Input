
package com.atech.voiceinput.translation_engine


interface ConversionCallback {

    fun onSuccess(result: String)

    fun onCompletion()

    fun onErrorOccurred(errorMessage: String)

} 