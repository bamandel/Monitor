package com.JNJABA.monitor;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.DragEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class OptionsActivity extends Activity {
	private SharedPreferences settings;
	private SharedPreferences.Editor edit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);
		
		settings = getApplicationContext().getSharedPreferences(getResources().getString(R.string.monitor_data), 0);
		edit = settings.edit();
		
		ToggleButton tbNotification = (ToggleButton) findViewById(R.id.tbNotificationToggle);
		tbNotification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				edit.putString(getResources().getString(R.string.notification_status), Boolean.toString(isChecked));
			}
		});
		ToggleButton tbGPS = (ToggleButton) findViewById(R.id.tbGPSToggle);
		tbGPS.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				edit.putString(getResources().getString(R.string.gps_status), Boolean.toString(isChecked));
			}
		});
		ToggleButton tbBlueTooth = (ToggleButton) findViewById(R.id.tbBlueToothToggle);
		tbBlueTooth.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				edit.putString(getResources().getString(R.string.bluetooth_status), Boolean.toString(isChecked));
			}
		});
		CheckBox cbMute = (CheckBox) findViewById(R.id.cbMute);
		cbMute.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				edit.putString(getResources().getString(R.string.mute_status), Boolean.toString(isChecked));
			}
		});
		CheckBox cbVibrate = (CheckBox) findViewById(R.id.cbVibrate);
		cbVibrate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				edit.putString(getResources().getString(R.string.vibrate_status), Boolean.toString(isChecked));
			}
		});
		SeekBar sbVolume = (SeekBar) findViewById(R.id.sbSound);
		sbVolume.setOnDragListener(new OnDragListener() {
			@Override
			public boolean onDrag(View v, DragEvent event) {
				// TODO Auto-generated method stub
				edit.putString(getResources().getString(R.string.volume_level), Float.toString(event.getY()));
				
				return false;
			}
		});
		RadioGroup fontSize = (RadioGroup) findViewById(R.id.rgTextSizes);
		fontSize.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		edit.apply();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.options, menu);
		return true;
	}

}
