package com.tudor.drivesmart;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class TrafficSignSoundManager {
    private TextToSpeech tts;
    private final Handler ttsHandler;
    private final Context context;
    private final Queue<String> queue = new LinkedList<>();
    private boolean isSpeaking = false;

    public TrafficSignSoundManager(Context context) {
        this.context = context;
        HandlerThread handlerThread = new HandlerThread("TtsThread");
        handlerThread.start();
        ttsHandler = new Handler(handlerThread.getLooper());
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale locale = Locale.getDefault();
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
                processNextSign();
            }

            @Override
            public void onError(String utteranceId) {
                isSpeaking = false;
                processNextSign();
            }
        });
    }

    private void processNextSign() {
        ttsHandler.post(() -> {
            if (!isSpeaking && !queue.isEmpty()) {
                String sign = queue.poll();
                tts.speak(getSignAnnouncement(sign), TextToSpeech.QUEUE_FLUSH, null, Integer.toString(sign.hashCode()));
            }
        });
    }

    public void announceSign(String sign) {
        ttsHandler.post(() -> {
            queue.offer(sign);
            if (!isSpeaking) {
                processNextSign();
            }
        });
    }

    private String getSignAnnouncement(String sign) {
        switch (sign) {
            case "pedestrian_crossing":
                return context.getString(R.string.pedestrian_crossing);
            case "pedestrian_crossing_warning":
                return context.getString(R.string.pedestrian_crossing_warning);
            case "give_way":
                return context.getString(R.string.give_way);
            case "stop":
                return context.getString(R.string.stop);
            case "priority_road":
                return context.getString(R.string.priority_road);
            case "roundabout":
                return context.getString(R.string.roundabout);
            case "roundabout_warning":
                return context.getString(R.string.roundabout_warning);
            case "speed_limit_50":
                return context.getString(R.string.speed_limit_50);
            case "speed_limit_60":
                return context.getString(R.string.speed_limit_60);
            case "speed_limit_70":
                return context.getString(R.string.speed_limit_70);
            case "speed_limit_80":
                return context.getString(R.string.speed_limit_80);
            case "speed_limit_90":
                return context.getString(R.string.speed_limit_90);
            case "dangerous_curve_left":
                return context.getString(R.string.dangerous_curve_left);
            case "dangerous_curve_right":
                return context.getString(R.string.dangerous_curve_right);
            case "right_bend":
                return context.getString(R.string.right_bend);
            case "left_bend":
                return context.getString(R.string.left_bend);
            case "double_curve":
                return context.getString(R.string.double_curve);
            case "road_narrows":
                return context.getString(R.string.road_narrows);
            case "road_narrows_left":
                return context.getString(R.string.road_narrows_left);
            case "road_narrows_right":
                return context.getString(R.string.road_narrows_right);
            default:
                return context.getString(R.string.undefined);
        }
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
