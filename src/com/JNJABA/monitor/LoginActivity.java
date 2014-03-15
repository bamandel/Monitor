package com.JNJABA.monitor;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
