package com.voicebaby.service;

import com.baidu.speechsynthesizer.SpeechSynthesizer;
import com.baidu.speechsynthesizer.SpeechSynthesizerListener;
import com.baidu.speechsynthesizer.publicutility.DataInfoUtils;
import com.baidu.speechsynthesizer.publicutility.SpeechError;
import com.baidu.speechsynthesizer.publicutility.SpeechLogger;
import com.voicebaby.tools.QLog;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

public class TalkingService extends IntentService implements SpeechSynthesizerListener {

	protected static final String TAG = "TalkingService";
    private static SpeechSynthesizer speechSynthesizer;
    private AudioManager mAudioManager;

	public TalkingService() {
		super("TalkingService");
	}
	
	@Override
	public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initBaiduTTS();
        QLog.logi(TAG, "onCreate......");
    }

    private void initBaiduTTS(){
        if(speechSynthesizer != null){
            return;
        }

        System.loadLibrary("gnustl_shared");
        // 部分版本不需要BDSpeechDecoder_V1
        try {
            System.loadLibrary("BDSpeechDecoder_V1");
        } catch (UnsatisfiedLinkError e) {
            SpeechLogger.logD("load BDSpeechDecoder_V1 failed, ignore");
        }
        System.loadLibrary("bd_etts");
        System.loadLibrary("bds");

        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            speechSynthesizer =
                    SpeechSynthesizer.newInstance(SpeechSynthesizer.SYNTHESIZER_AUTO, getApplicationContext(), "holder",
                            this);
            // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
            speechSynthesizer.setApiKey(appInfo.metaData.getString("com.baidu.speech.API_KEY"), appInfo.metaData.getString("com.baidu.speech.SECRET_KEY"));
            // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
            speechSynthesizer.setAppId(appInfo.metaData.getString("com.baidu.speech.APP_ID"));
            // 设置授权文件路径
//        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, LICENCE_FILE_NAME);
            // TTS所需的资源文件，可以放在任意可读目录，可以任意改名
            String ttsTextModelFilePath =
                    getApplicationContext().getApplicationInfo().dataDir + "/lib/libbd_etts_text.dat.so";
            String ttsSpeechModelFilePath =
                    getApplicationContext().getApplicationInfo().dataDir + "/lib/libbd_etts_speech_female.dat.so";
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, ttsTextModelFilePath);
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, ttsSpeechModelFilePath);
            DataInfoUtils.verifyDataFile(ttsTextModelFilePath);
            DataInfoUtils.getDataFileParam(ttsTextModelFilePath, DataInfoUtils.TTS_DATA_PARAM_DATE);
            DataInfoUtils.getDataFileParam(ttsTextModelFilePath, DataInfoUtils.TTS_DATA_PARAM_SPEAKER);
            DataInfoUtils.getDataFileParam(ttsTextModelFilePath, DataInfoUtils.TTS_DATA_PARAM_GENDER);
            DataInfoUtils.getDataFileParam(ttsTextModelFilePath, DataInfoUtils.TTS_DATA_PARAM_CATEGORY);
            DataInfoUtils.getDataFileParam(ttsTextModelFilePath, DataInfoUtils.TTS_DATA_PARAM_LANGUAGE);
            speechSynthesizer.initEngine();
        } catch (PackageManager.NameNotFoundException e) {
           return;
        }
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if ("com.voicebaby.start_talking".equals(action)) {
//			String text = getContactByNumber(getApplicationContext(),
//					intent.getStringExtra("number"))
            String text = intent.getStringExtra("number")
					+ "，来电话了";
			text = text + "……" + text;
			say(text);
		} else if ("com.voicebaby.stop_talking".equals(action)) {
			stop();
		}

        QLog.logi(TAG, "onHandleIntent......");
	}

	private void say(final String text) {
		setRingVolume(this, 0);
        setMusicVolume(this, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                speechSynthesizer.speak(text);
            }
        }, 100);
    }

	private void stop() {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                speechSynthesizer.cancel();
            }
        }, 100);
    }

	private void clearRingtone(Context context) {
		Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context,
				RingtoneManager.TYPE_RINGTONE);
		RingtoneManager.setActualDefaultRingtoneUri(context,
				RingtoneManager.TYPE_RINGTONE, null);
		Log.d(TAG, "onIncomingCallStarted set ringtone = null");
	}

	private void restoreRingtone(Context context, Uri uri) {
		RingtoneManager.setActualDefaultRingtoneUri(context,
				RingtoneManager.TYPE_RINGTONE, uri);
		Log.d(TAG, "onIncomingCallStarted set ringtone = " + uri.toString());
	}

	private void setRingVolume(Context context, int v) {
		AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_RING, v, 0);
        am.getStreamVolume(AudioManager.STREAM_RING);
	}

	private void setMusicVolume(Context context, int v) {
		AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, v, 0);
	}

	private String getContactByNumber(Context context, String number) {
		// define the columns I want the query to return
		String[] projection = new String[] {
				ContactsContract.PhoneLookup.DISPLAY_NAME,
				ContactsContract.PhoneLookup._ID };

		// encode the phone number and build the filter URI
		Uri contactUri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));

		// query time
		Cursor cursor = context.getContentResolver().query(contactUri,
				projection, null, null, null);

		if (cursor.moveToFirst()) {
			// Get values from contacts database:
			String name = cursor.getString(cursor
					.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			return name;
		} else {
			return number;
		}
	}


    @Override
    public void onStartWorking(SpeechSynthesizer arg0) {
//        QLog.log("开始工作，请等待数据...");
    }

    @Override
    public void onSpeechStart(SpeechSynthesizer synthesizer) {
//        QLog.log("朗读开始");
    }

    @Override
    public void onSpeechResume(SpeechSynthesizer synthesizer) {
//        QLog.log("朗读继续");
    }

    @Override
    public void onSpeechProgressChanged(SpeechSynthesizer synthesizer, int progress) {
//        QLog.log("朗读进度" + progress);
    }

    @Override
    public void onSpeechPause(SpeechSynthesizer synthesizer) {
        QLog.log("朗读已暂停");
    }

    @Override
    public void onSpeechFinish(SpeechSynthesizer synthesizer) {
//        QLog.log("朗读已停止");
//        setRingVolume(TalkingService.this, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
        stop();
    }

    @Override
    public void onNewDataArrive(SpeechSynthesizer synthesizer, byte[] audioData, boolean isLastData) {
//        QLog.log("新的音频数据：" + audioData.length + (isLastData ? ("end") : ""));
    }

    @Override
    public void onError(SpeechSynthesizer synthesizer, SpeechError error) {
//        QLog.log("发生错误：" + error);
    }

    @Override
    public void onCancel(SpeechSynthesizer synthesizer) {
//        QLog.log("已取消");
    }

    @Override
    public void onBufferProgressChanged(SpeechSynthesizer synthesizer, int progress) {
//        QLog.log("缓冲进度" + progress);
    }

    @Override
    public void onSynthesizeFinish(SpeechSynthesizer arg0) {
        // TODO Auto-generated method stub
//        QLog.log("合成已完成");
    }

}
