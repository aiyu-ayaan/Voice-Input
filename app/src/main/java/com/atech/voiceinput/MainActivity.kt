package com.atech.voiceinput

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.atech.voiceinput.databinding.ActivityMainBinding
import com.atech.voiceinput.translation_engine.ConversionCallback
import com.atech.voiceinput.translation_engine.TranslatorFactory
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"


    private var permissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { permission ->
            permission[android.Manifest.permission.RECORD_AUDIO]?.let {
                if (!it) startActivity(
                    openInfoSettings()
                )
            }
        }


    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.apply {
            setSupportActionBar(toolbar)
            buttonStart.setOnClickListener {
                if (!checkRecordPermission()) {
                    askRecordFromMicPermission()
                } else {
                    textToSpeech(textViewShowQuestion.text.toString()) {
                        speechToText()
                    }
                }
            }
        }

    }


    private fun checkRecordPermission() = ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    private fun askRecordFromMicPermission() {
        val isHavingMicPermission = checkRecordPermission()
        if (!isHavingMicPermission)
            launchRecordSetting()
    }

    private fun launchRecordSetting() {
        val permissionToRequest = mutableListOf<String>()
        permissionToRequest.add(android.Manifest.permission.RECORD_AUDIO)
        if (permissionToRequest.isNotEmpty())
            permissionLauncher.launch(permissionToRequest.toTypedArray())
    }

    private fun textToSpeech(text: String, lambda: () -> Unit = {}) {
        TranslatorFactory
            .instance
            .with(TranslatorFactory.TRANSLATORS.TEXT_TO_SPEECH,
                object : ConversionCallback {
                    override fun onSuccess(result: String) {
                    }

                    override fun onCompletion() {
                        lambda.invoke()
                    }

                    override fun onErrorOccurred(errorMessage: String) {
                        Log.e(TAG, "Text2Speech Error: $errorMessage")
                    }

                })
            .initialize(text, this)

    }

    private fun speechToText() {
        Snackbar.make(binding.root, "Speak now, App is listening", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()

        TranslatorFactory
            .instance
            .with(TranslatorFactory.TRANSLATORS.SPEECH_TO_TEXT,
                object : ConversionCallback {
                    override fun onSuccess(result: String) {
                        checkResult(result)
                    }

                    override fun onCompletion() {
                    }

                    override fun onErrorOccurred(errorMessage: String) {
                        Log.e(TAG, "Speech2Text Error: $errorMessage")
                        Toast.makeText(
                            this@MainActivity,
                            "Can't recognize the voice . Try Again !!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }).initialize("Speak Now !!", this)
    }

    private fun checkResult(result: String) {
        Log.d(TAG, "checkResult: $result")
        if (result == "4" || result == "four")
            textToSpeech("Correct Answer")
        else
            textToSpeech("Incorrect")
    }

    private fun openInfoSettings() = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
}