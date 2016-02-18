package com.voicebaby.service;

import com.baidu.speech.VoiceRecognitionService;
import com.voicebaby.tools.QLog;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import java.util.ArrayList;

public class RingRecognitionService extends IntentService implements RecognitionListener {

	protected static final String TAG = "RecognitionService";

    private SpeechRecognizer speechRecognizer;
    private StringBuilder recoSb = new StringBuilder();

	public RingRecognitionService() {
		super("RecognitionService");
	}
	
	@Override
	public void onCreate() {
        super.onCreate();
        initRecognition();
        QLog.logi(TAG, "onCreate......");
    }

    private void initRecognition(){
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                speechRecognizer = SpeechRecognizer
                        .createSpeechRecognizer(RingRecognitionService.this, new ComponentName(RingRecognitionService.this, VoiceRecognitionService.class));
                speechRecognizer.setRecognitionListener(RingRecognitionService.this);
            }
        }, 100);
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if ("com.voicebaby.start_recognition".equals(action)) {
            start();
        } else if ("com.voicebaby.stop_recognition".equals(action)) {
            stop();
		}

        QLog.logi(TAG, "onHandleIntent......");
	}

    private void start(){
        if(speechRecognizer == null){
            initRecognition();
        }
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.putExtra("args", "");
                speechRecognizer.startListening(intent);
            }
        }, 100);
    }

    private void stop(){
        if(speechRecognizer != null){
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    speechRecognizer.stopListening();
                    speechRecognizer.destroy();

                    Toast.makeText(getApplicationContext(), recoSb.toString(), Toast.LENGTH_LONG).show();
                }
            }, 100);
        }
        recoSb = new StringBuilder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(speechRecognizer != null){
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    speechRecognizer.stopListening();
                    speechRecognizer.destroy();

                    Toast.makeText(getApplicationContext(), recoSb.toString(), Toast.LENGTH_LONG).show();
                }
            }, 100);
        }
        recoSb = new StringBuilder();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {
        start();
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(nbest != null && nbest.size() > 0){
            recoSb.append(nbest.get(0));
        }
        Toast.makeText(getApplicationContext(), recoSb.toString(), Toast.LENGTH_LONG).show();
        start();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
