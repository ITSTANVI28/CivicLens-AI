package com.example.civiclensai.utils;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import androidx.activity.result.ActivityResultLauncher;

import java.util.Locale;

public class VoiceTriageHelper {

    /**
     * Launches the native Android Speech Recognition Intent supporting English, Hindi, and Marathi dictation.
     */
    public static void launchVoiceDictation(ActivityResultLauncher<Intent> speechLauncher, Context context) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "🎙️ Dictate Civic Issue Report (Marathi, Hindi, English)");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        try {
            speechLauncher.launch(intent);
        } catch (Exception e) {
            // Speech recognition not supported on device
        }
    }
}
