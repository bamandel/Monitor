package com.JNJABA.monitor;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class EmergencyActivity extends Activity {
	private static final int ONE_SEC = 1000;
	private static final int TEN_SEC = 10 * ONE_SEC;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emergency);
		
		Button bOK = (Button) findViewById(R.id.bOK);
		Button bHELP = (Button) findViewById(R.id.bHELP);
		final ImageView ivCountdownFrame = (ImageView) findViewById(R.id.ivCountdownFrame);
		
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
		AnimationDrawable countdownAnimation = (AnimationDrawable) ivCountdownFrame.getBackground();
		
		countdownAnimation.start();
		
		new Timer().schedule(new TimerTask() {
		    @Override
		    public void run() {
		    	getHelp();
		    }
		}, TEN_SEC);
		
	}
	
	protected void getHelp() {
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.emergency, menu);
		return true;
	}

}