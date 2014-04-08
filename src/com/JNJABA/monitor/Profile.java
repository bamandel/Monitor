package com.JNJABA.monitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

public class Profile {
	private String address2;
	private Bitmap proPic;
	private EmergencyContact emergencyContact;

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private static Context context;

	public Profile(Context appContext) {
		context = appContext;
		settings = context.getApplicationContext().getSharedPreferences(
				context.getResources().getString(R.string.monitor_data),
				Context.MODE_PRIVATE);
		editor = settings.edit();
	}

	public static class Holder {
		private static final Profile INSTANCE = new Profile(context);
	}

	public static Profile getInstance() {
		return Holder.INSTANCE;
	}
	
	public void updatePreferences() {editor.apply();}

	public void addProPic(Bitmap pic) {proPic = pic;}
	public Bitmap getProPic() {return proPic;}
	public void addAddress2(String mAddress2) {address2 = mAddress2;}
	public String getAddress2() {return address2;}
	public void addEmergencyContact(EmergencyContact contact) {emergencyContact = contact;}
	public EmergencyContact getEmergencyContact() {return emergencyContact;}
	
	public String toString() {
		return "";
	}

	public String getFirstName() {
		return settings.getString(context.getResources().getString(R.string.user_first_name), "Unknown");
	}

	public void setFirstName(String mFirstName) {
		editor.putString(context.getResources().getString(R.string.user_first_name), mFirstName);
	}

	public String getLastName() {
		return settings.getString(context.getResources().getString(R.string.user_last_name), "Unknown");
	}

	public void setLastName(String mLastName) {
		editor.putString(context.getResources().getString(R.string.user_last_name), mLastName);
	}

	public int getAge() {
		return Integer.parseInt(settings.getString(context.getResources().getString(R.string.user_age), "-1"));
	}

	public void setAge(int mAge) {
		editor.putString(context.getResources().getString(R.string.user_age), String.valueOf(mAge));
	}

	public String getSex() {
		return settings.getString(context.getResources().getString(R.string.user_sex), "Unknown");
	}

	public void setSex(String mSex) {
		editor.putString(context.getResources().getString(R.string.user_sex), mSex);
	}

	public int getWeight() {
		return Integer.parseInt(settings.getString(context.getResources().getString(R.string.user_weight), "-1"));
	}

	public void setWeight(int mWeight) {
		editor.putString(context.getResources().getString(R.string.user_weight), String.valueOf(mWeight));
	}

	public int getHeight() {
		return Integer.parseInt(settings.getString(context.getResources().getString(R.string.user_height), "-1"));
	}

	public void setHeight(int mHeight) {
		editor.putString(context.getResources().getString(R.string.user_height), String.valueOf(mHeight));
	}

	public String getEmail() {
		return settings.getString(context.getResources().getString(R.string.user_email), "Unknown");
	}

	public void setEmail(String mEmail) {
		editor.putString(context.getResources().getString(R.string.user_email), mEmail);
	}

	public String getAddress() {
		return settings.getString(context.getResources().getString(R.string.user_address), "Unknown");
	}

	public void setAddress(String mAddress1) {
		editor.putString(context.getResources().getString(R.string.user_address), mAddress1);
	}

	public double getBmi() {
		return Double.parseDouble(settings.getString(context.getResources().getString(R.string.user_BMI), "-1"));
	}

	public void setBmi(double mBmi) {
		editor.putString(context.getResources().getString(R.string.user_BMI), String.valueOf(mBmi));
	}

	public int getActivityLevel() {
		return Integer.parseInt(settings.getString(context.getResources().getString(R.string.user_overall_health), "-1"));
	}

	public void setActivityLevel(int mActivityLevel) {
		editor.putString(context.getResources().getString(R.string.user_overall_health), String.valueOf(mActivityLevel));
	}
}
