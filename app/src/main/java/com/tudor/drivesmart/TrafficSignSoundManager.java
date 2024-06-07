package com.tudor.drivesmart;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class TrafficSignSoundManager {

    private TextToSpeech tts;
    private final Handler ttsHandler;
    private final Queue<String> queue = new LinkedList<>();
    private boolean isSpeaking = false;
    SharedPreferences sharedPreferences;

    public TrafficSignSoundManager(Context context) {
        sharedPreferences = context.getSharedPreferences("saveData", Context.MODE_PRIVATE);

        HandlerThread handlerThread = new HandlerThread("TtsThread");
        handlerThread.start();
        ttsHandler = new Handler(handlerThread.getLooper());

        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                String lang = sharedPreferences.getString("app_lang", "en");
                Locale locale = new Locale(lang);

                int result = tts.setLanguage(locale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.US);
                }
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                isSpeaking = true;
            }

            @Override
            public void onDone(String utteranceId) {
                isSpeaking = false;
                processNextAnno();
            }

            @Override
            public void onError(String utteranceId) {
                isSpeaking = false;
                processNextAnno();
            }
        });
    }

    private void processNextAnno() {
        ttsHandler.post(() -> {
            if (!isSpeaking && !queue.isEmpty()) {
                String anno = queue.poll();
                assert anno != null;
                tts.speak(anno, TextToSpeech.QUEUE_FLUSH, null, Integer.toString(anno.hashCode()));
            }
        });
    }

    public void announce(String anno) {
        ttsHandler.post(() -> {
            queue.offer(anno);
            if (!isSpeaking) {
                processNextAnno();
            }
        });
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (ttsHandler != null) {
            ttsHandler.getLooper().quitSafely();
        }
    }
}
