package com.voicebaby.tools;

import com.voicebaby.constant.AppConstants;

import android.text.TextUtils;
import android.util.Log;

/**
 * Until class for debug
 * 
 */
public final class QLog {

	private final static String TAG = "voicebaby";

	public static <T> void loge(T text) {
		loge("", text);
	}

	public static <T> void log(T text) {
		log("", text);
	}

	public static <T> void logi(T text) {
		logi("", text);
	}

	/* 红色 */
	public static <T> void loge(String tag, T text) {
		if (AppConstants.DEBUG) {
			if (TextUtils.isEmpty(tag)) {
				tag = TAG;
			}
			Log.e(tag, String.valueOf(text));
		}
	}

	/* 绿色 */
	public static <T> void logi(String tag, T text) {
		if (AppConstants.DEBUG) {
			if (TextUtils.isEmpty(tag)) {
				tag = TAG;
			}
			Log.i(tag, String.valueOf(text));
		}
	}

	/* 黑色 */
	public static <T> void log(String tag, T text) {
		if (AppConstants.DEBUG) {
			if (TextUtils.isEmpty(tag)) {
				tag = TAG;
			}
			Log.v(tag, String.valueOf(text));
		}
	}

}
