package com.JNJABA.monitor;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/*
 * The way to store data is by calling these functions
 * SharedPreferences settings = getApplicationContext().getSharedPreferences(getResources().getString(R.string.monitor_data), MODE_PRIVATE);
 * SharedPreferences.Editor editor = settings.edit();
 * 
 * editor.putString("name", "Brian");
 * editor.commit();
 * 
 * The way to get data from our project(though I don't think you will need to in this activity) is
 * 
 * String userName = settings.getString(getResources().getString(R.string.user_username), false);
 */

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		Button login = (Button) findViewById(R.id.bLogin);
		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
			}
			
		});
		
		//Mock data values for testing right now
		SharedPreferences settings = getApplicationContext().getSharedPreferences(getResources().getString(R.string.monitor_data), MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString(getResources().getString(R.string.user_first_name), "Brian");
		editor.putString(getResources().getString(R.string.user_last_name), "Mandel");
		editor.putString(getResources().getString(R.string.user_walking_speed), "5");
		editor.putString(getResources().getString(R.string.user_fall_status), "true");
		editor.putString(getResources().getString(R.string.user_heart_rate), "87");
		editor.putString(getResources().getString(R.string.user_overall_health), "7");
		if(editor.commit())
			Toast.makeText(this, "Data has been commited to file", Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show();
		
	}
	
/*	@Override
	protected void onResume() {
		super.onResume();
		
		settings = getApplicationContext().getSharedPreferences("Monitor", MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString("name", "Brian");
	}*/
	
	@Override
	protected void onStop() {
		finish();
		
		super.onStop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
