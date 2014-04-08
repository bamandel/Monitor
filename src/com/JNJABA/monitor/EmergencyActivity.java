package com.JNJABA.monitor;

import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class EmergencyActivity extends Activity {
	private static final int ONE_SEC = 1000;
	private static final int TEN_SEC = 10 * ONE_SEC;
	
	private AnimationDrawable countdownAnimation;
	private ImageView ivCountdownFrame;
	
	private PowerManager.WakeLock wake;
	private AudioManager audio;
	private int volume = 0;
	private Ringtone ringtone;
	
	//Used to make sure phone doesnt end up calling
	private boolean hasRun = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EmergencyActivity");
		wake.acquire();
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		setContentView(R.layout.activity_emergency);
		
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		volume = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		audio.setStreamVolume(AudioManager.STREAM_ALARM, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		ringtone = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
		if(ringtone != null) {
			ringtone.setStreamType(AudioManager.STREAM_ALARM);
			ringtone.play();
		}
		
		Button bOK = (Button) findViewById(R.id.bOK);
		Button bHELP = (Button) findViewById(R.id.bHELP);
		ivCountdownFrame = (ImageView) findViewById(R.id.ivCountdownFrame);
		
		bOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ringtone.stop();
				hasRun = true;
				finish();
			}
		});
		
		bHELP.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				hasRun = true;
				getHelp();
			}
		});
		
		ivCountdownFrame.setBackgroundResource(R.drawable.countdown_animation);
		countdownAnimation = (AnimationDrawable) ivCountdownFrame.getBackground();
		countdownAnimation.start();
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!hasRun)
					getHelp();
			}
			
		}, TEN_SEC);
	}
	
	protected void getHelp() {
		ringtone.stop();
		hasRun = true;
		Intent call = new Intent(Intent.ACTION_CALL);
		call.setData(Uri.parse(getIntent().getStringExtra("emergency number")));
		startActivity(call);
		finish();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(wake.isHeld())
			wake.release();
	}
	

}