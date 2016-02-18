package com.voicebaby.receiver;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;

public class CallReceiver extends AbsCallReceiver {

	private static final String TAG = "CallReceiver";

	@Override
	protected void onIncomingCallStarted(Context context, String number,
			Date start) {
		startTalking(context, number);
	}

	@Override
	protected void onIncomingCallOffhook(Context context, String number, Date hookDate) {
		stopTalking(context);
		startRecognition(context, number);
	}

	@Override
	protected void onIncomingCallEnded(Context context, String number,
			Date start, Date end) {
		stopTalking(context);
		stopRecognition(context);
	}

	@Override
	protected void onOutgoingCallOffhook(Context context, String number,
			Date start) {
		stopTalking(context);
		startRecognition(context, number);
	}

	@Override
	protected void onMissedCall(Context context, String number, Date start) {
	}

	@Override
	protected void onOutgoingCallEnded(Context context, String number,
			Date start, Date end) {
		stopRecognition(context);
	}

	private void startTalking(Context context, String number){
		Intent itn = new Intent("com.voicebaby.start_talking");
		itn.putExtra("number", number);
		context.startService(itn);
	}

	private void stopTalking(Context context) {
		Intent itn = new Intent("com.voicebaby.stop_talking");
		context.startService(itn);
	}

	private void startRecognition(Context context, String number){
		Intent itn = new Intent("com.voicebaby.start_recognition");
		itn.putExtra("number", number);
		context.startService(itn);
	}

	private void stopRecognition(Context context) {
		Intent itn = new Intent("com.voicebaby.stop_recognition");
		context.startService(itn);
	}

}
