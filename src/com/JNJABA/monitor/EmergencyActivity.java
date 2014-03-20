package com.JNJABA.monitor;

import java.util.Timer;
import java.util.TimerTask;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EmergencyActivity extends Activity {
	private static final int ONE_SEC = 1000;
	private static final int TEN_SEC = 10 * ONE_SEC;
	
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	
	private AnimationDrawable countdownAnimation;
	private ImageView ivCountdownFrame;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * 
	 * Maybe call the GPS and send data from this activity
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emergency);
		
		settings = getApplicationContext().getSharedPreferences(getResources().getString(R.string.monitor_data), MODE_PRIVATE);
		editor = settings.edit();
		
		Button bOK = (Button) findViewById(R.id.bOK);
		Button bHELP = (Button) findViewById(R.id.bHELP);
		ivCountdownFrame = (ImageView) findViewById(R.id.ivCountdownFrame);
		
		bOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		bHELP.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getHelp();
			}
		});
		
		ivCountdownFrame.setBackgroundResource(R.drawable.countdown_animation);
		countdownAnimation = (AnimationDrawable) ivCountdownFrame.getBackground();
		countdownAnimation.start();
	}
	
	protected void getHelp() {
		Intent call = new Intent(Intent.ACTION_CALL);
		call.setData(Uri.parse(settings.getString(getResources().getString(R.string.emergency_phone_number), "tel:16178164614")));
		//startActivity(call);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.emergency, menu);
		return true;
	}

}