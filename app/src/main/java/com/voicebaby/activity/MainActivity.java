package com.voicebaby.activity;

import com.voicebaby.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Intent itn1 = new Intent("com.voicebaby.stop_talking");
//        itn1.putExtra("number", "15210836210");
//        startService(itn1);
//
        Intent itn = new Intent("com.voicebaby.start_recognition");
        itn.putExtra("number", "15210836210");
        startService(itn);
    }

}
