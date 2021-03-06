package com.jarvis.jarvis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class TextSpeechUtils {

    private static SpeechRecognizer speechRecognizer;
    private static TextToSpeech speaker;
    private static TextSpeechUtilsCallback mCallback;
    private static final String TAG = "TextSpeechUtils";

    public static void init(Context context, final UtteranceProgressListener callback) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new VoiceInputListener());
        speaker = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                speaker.setOnUtteranceProgressListener(callback);
                speaker.setLanguage(Locale.getDefault());
            }
        });
    }

    public static void getVoiceInput(TextSpeechUtilsCallback callback) {
        mCallback = callback;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        speechRecognizer.startListening(intent);
    }

    public static void speakText(String text) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId");
        speaker.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }

    private static class VoiceInputListener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        public void onPartialResults(Bundle partialResults) {
            String str = "";
            Log.d(TAG, "onPartialResults " + partialResults);
            ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (data.size() > 0) {
                Log.d(TAG, "results: " + data.get(0));
                str = data.get(0);
            }
            if (mCallback != null) {
                mCallback.onPartialSpeech(str);
            }
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
            mCallback.onSpeechStart();
        }

        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }

        public void onError(int error) {
            Log.d(TAG, "error " + error);
        }

        public void onResults(Bundle results) {
            String str = "";
            Log.d(TAG, "onResults " + results);
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (data.size() > 0) {
                Log.d(TAG, "results: " + data.get(0));
                str = data.get(0);
            }
            if (mCallback != null) {
                mCallback.onSpeechInputComplete(str);
            }
        }

    }

    public interface TextSpeechUtilsCallback {
        public void onSpeechInputComplete(String input);
        public void onPartialSpeech(String input);
        public void onSpeechStart();
    }
}
