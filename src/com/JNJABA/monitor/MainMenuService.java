package com.JNJABA.monitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class MainMenuService extends Service implements LocationListener, SensorEventListener {
	private static final String TAG = "Monitor-Service";
	private static final int NOTIFICATION_ID = 1;
	private static final String CLOUD_WEBPAGE = "I do no know yet";
	
	private static final long SECOND = 1000;
	private static final long MINUTE = 60 * SECOND;
	private static final long FAST_DELAY = 5 * SECOND;
	private static final long SLOW_DELAY = 15 * MINUTE;
	
	private static Location lastLocation;
	private static LocationManager locationManager;
	
	//Booleans used to determine which GPS speed to use
	private static boolean fast = false;
	private boolean repeat = true;
	
	//Storage method to Share data with whole application
	private static SharedPreferences settings;
	private static SharedPreferences.Editor editor;
	
	//Update times for 
	private final int UPDATE_TIME = 900000; // 15 min
	private final int UPDATE_DISTANCE = 8046; // 5 miles
	
	//Fall detection stuff
	
	private static final String tag = "FallDetection.SensorService";
	private static final boolean debug = true;
	private static final float LFT_MAX = (float) 6.6700; //RSS <= LFT_MAX (.679 g)
	private static final float UFT_MIN = (float) 19.62; //RSS >= UTF_MIN (i.e., 2g) Used in SensorService2.
	private static final long TRE_MAX = 366000;
	private static final long TRE_MIN = 102000;
	private static KVector curr_vector;
	private static KVector prev_vector;
	private static float curr_ori;
	private static float prev_ori;
	private static int inactivityCounter2;
	private static int inactivityCounter1;
	private static boolean inactivityDetected = false;
	private static long inactivity_timestamp;
	private static long LFT_timestamp;
	private static long UFT_timestamp;
	private static long timestamp_fall_detected;
	private static Vector<Long> lft_timestamps;
	private static boolean isRunning = false;
	private static boolean detectedFall = false;
	private static boolean LFT_exceeded = false;
	private static SensorManager sensorManager;
	private static Sensor accelerometer;
	private static LinkedList <KVector> vectorList=new LinkedList<KVector>();
	private static boolean ADLTypeAB=false;
	private static boolean ADLTypeC=false;
	private static boolean newPotentialFallForADLs=false;
	private static boolean checkedADLs=false;
	private static final float AAMV_MIN=2.646f;
	private static final int FFI_MAX=100;
	private static final double FFAAM_MIN=0.5*SensorManager.GRAVITY_EARTH;
	public static final double MICRO_TO_MILLI= 0.001;
	private KVector vectorFirst=new KVector();
	private KVector vectorSecond=new KVector();
	private KVector interpolatedVector=new KVector();
	
	private WakeLock wakeLock = null;
	
	public MainMenuService() {
		super();
		
		if(!isRunning)
			isRunning = true;
	}
	
	public void onCreate() {
		curr_vector = new KVector();
		prev_vector = new KVector();
		
		inactivityCounter1 = inactivityCounter2 = 0;
		curr_ori = prev_ori = 0;
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = (Sensor) sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		settings = getApplicationContext().getSharedPreferences(getResources().getString(R.string.monitor_data), MODE_PRIVATE);
		editor = settings.edit();
		
		callGPS(false);
		
		Intent notificationIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		Notification notification = new Notification.Builder(
				getApplicationContext())
				.setSmallIcon(android.R.drawable.ic_media_play)
				.setOngoing(true).setContentTitle("Monitoring")
				.setContentText("Click to access Monitoring app")
				.setContentIntent(pendingIntent).build();
		
		startForeground(NOTIFICATION_ID, notification);
		
		final Handler locationHandler = new Handler();
		locationHandler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				fast = settings.getBoolean(getResources().getString(R.string.in_health_activity), false);
				
				if(fast) 
					GPSBuffer(true);
				else 
					GPSBuffer(false);
				
				lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				updatePreferences();
				
				if(settings.getBoolean(getResources().getString(R.string.in_health_activity), false) == true)
					locationHandler.postDelayed(this, FAST_DELAY);
				else
					locationHandler.postDelayed(this, SLOW_DELAY);
			}
		});
	}
	
	// The intent will contain any data the service needs to use
	public int onStartCommand(Intent intent, int flags, int startId) {
		registerListener();
		wakeLock.acquire();
		
		return START_STICKY;
	}
	
	//Makes sure callGPS is called only when needed
	private void GPSBuffer(boolean running) {
		if(running && repeat) {
			callGPS(fast);
			repeat = false;
			return;
		}
		if(!running && !repeat) {
			repeat = true;
			callGPS(fast);
		}
	}
	
	// Get GPS location
	private void callGPS(boolean fastest) {

		// updates GPS every 15 minutes or when someone moves across the span of
		// the US
		// not meant to be updated based on area traveled.
		
		//removes previous updates preparing for new method
		locationManager.removeUpdates(this);
		
		//fastest GPS updates in case of emergency or real time viewing
		if(fastest) {
			log("Running in fastest");
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
		//Casual monitoring speed
		else {
			log("Running in slowest");
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_TIME, UPDATE_DISTANCE,this);
		}
	}
	
	//Stores all the data acquired by this Service
	private void updatePreferences() {
		editor.putString(getResources().getString(R.string.user_location_latitude), String.valueOf(lastLocation.getLatitude()));
		editor.putString(getResources().getString(R.string.user_location_longitude),String.valueOf(lastLocation.getLongitude()));
		editor.putString(getResources().getString(R.string.user_fall_status), Boolean.toString(fast));
		
		log("Lat: " + settings.getString(getResources().getString(R.string.user_location_latitude), "No Lat Value"));
		log("Lon: " + settings.getString(getResources().getString(R.string.user_location_longitude), "No Lon Value"));
		log("fall: " + settings.getString(getResources().getString(R.string.user_fall_status), "No Fall Value"));
		
		//Using apply to allowing for separate thread processing
		editor.apply();
	}
	
	// Sends data to Server periodically or on Emergency
	private void storeData() {
		// Will most likely have to start a new thread
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost(CLOUD_WEBPAGE);
		
		try {
			List<NameValuePair> values = new ArrayList<NameValuePair>();
			//Send my data
			post.setEntity(new UrlEncodedFormEntity(values));
			
			HttpResponse response = httpClient.execute(post);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Start Monitoring for fall
	private void handleActionMonitor() {
		log("HandleActionMonitor");
		
		fast = true;
		GPSBuffer(fast);
		updatePreferences();
		//storeData();
		sendWarning();
		fast = false;
	}

	// Starts the activity to warn of fall. last GPS location is sent
	private void sendWarning() {
		Intent emergency = new Intent(MainMenuService.this, EmergencyActivity.class);
		emergency.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		emergency.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

		emergency.putExtra("latitude", lastLocation.getLatitude());
		emergency.putExtra("longitude", lastLocation.getLongitude());
		emergency.putExtra("emergency number", settings.getString(getResources().getString(R.string.emergency_phone_number), "tel:555"));

		startActivity(emergency);
	}

	@Override
	public IBinder onBind(Intent intent) {return null;}
	
	@Override
	public void onLocationChanged(Location currentLocation) {
		lastLocation = currentLocation;
	}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
		
		unregisterReceiver(receiver);
		unregisterListener();
		wakeLock.release();
		stopForeground(true);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
	private void registerListener() {
		sensorManager.registerListener(MainMenuService.this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	private void unregisterListener() {
		sensorManager.unregisterListener(this);
	}
	
	//Receiver bypass used to allow Sensor detection when screen is out
	public BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(!intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
				return;
			
			Runnable runnable = new Runnable() {
				public void run() {
					unregisterListener();
					registerListener();
				}
			};
			new Handler().postDelayed(runnable, 500);
		}
	};
	
	@Override
	public void onSensorChanged(final SensorEvent se) {
		long timestamp = (long) se.timestamp / 1000;
		
		if (se.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
			if(debug)
				Log.e(tag, "Sensor not accelerometer: " + se.sensor.getType());
		}
		
		if (se.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
			//save the last vector
			vectorFirst.set(vectorSecond);
												
			/**
			 * All values are in SI units (m/s^2)
			 * 	values[0]: Acceleration minus Gx on the x-axis
			 * 	values[1]: Acceleration minus Gy on the y-axis
			 * 	values[2]: Acceleration minus Gz on the z-axis 
			*/
			vectorSecond.set(se.values[0], se.values[1], se.values[2]);
			vectorSecond.setTimeStamp(timestamp);
			
			
			if(vectorFirst.getTimeStamp() <= 0) {
				vectorFirst.setTimeStamp(timestamp);
				onNewReading(vectorSecond);
			}
			
			else{
				interpolatedVector=new KVector();
				float interpolatedX=vectorFirst.getX() + (vectorSecond.getX() - vectorFirst.getX())/2;
				float interpolatedY=vectorFirst.getY() + (vectorSecond.getY() - vectorFirst.getY())/2;
				float interpolatedZ=vectorFirst.getZ() + (vectorSecond.getZ() - vectorFirst.getZ())/2;
				
				interpolatedVector.set(interpolatedX, interpolatedY, interpolatedZ);
				interpolatedVector.setTimeStamp(vectorFirst.getTimeStamp() + (vectorSecond.getTimeStamp()-vectorFirst.getTimeStamp())/2);
				onNewReading(interpolatedVector);
				onNewReading(vectorSecond);
			}
			
		}
	}
	
	private void onNewReading(KVector newVector) {
		//se.timestamp returns a time in nanoseconds. Why divide it by 1000? The timestamps ought to be in nanoseconds, not microseconds...
		
		//save the last vector
		prev_vector.set(curr_vector);
				
		/**
		 * All values are in SI units (m/s^2)
		 * 	values[0]: Acceleration minus Gx on the x-axis
		 * 	values[1]: Acceleration minus Gy on the y-axis
		 * 	values[2]: Acceleration minus Gz on the z-axis 
		*/
		curr_vector.set(newVector);
		
		if(prev_vector.getTimeStamp() <= 0) 
			prev_vector.setTimeStamp(curr_vector.getTimeStamp());
		
		prev_ori = curr_ori;
		//Orientation between position at timestamp x and timestamp (x-1)
		//Note: The first iteration, vectorFirst will be 0;
		curr_ori = curr_vector.orientation(prev_vector);
		
		if(debug) {
			//Log.d(tag + ".var_info", "START: " + se.timestamp);
			//Log.i(tag + ".var_info", "Curr Vector: " + curr_vector.toString() + "::Orientation: " + curr_ori);
			//Log.i(tag + ".var_info", "Prev Vector: " + prev_vector.toString() + "::Orientation: " + prev_ori);
		}
		
		//if a possible fall has been detected, we check for inactivity.
		if (detectedFall) {
			determineInactivityPeriod();
		}
		if ((inactivityCounter1>=167) && (!inactivityDetected)) {
			inactivityCounter2=0;
			inactivityCounter1=0;
			detectedFall=false;
		}
		
		
		checkADLs();
		
		if (inactivityDetected && detectedFall && checkedADLs) /*(timestamp - timestamp_fall_detected) > 520000) */ {
			if((!ADLTypeAB)&&(!ADLTypeC)) {		
				if (debug) {
					Log.i(tag + "SensorListener", "Fall Dectected!!!!! -> Alert Class Intent Started:" + (curr_vector.getTimeStamp() - timestamp_fall_detected));
				}
				editor.putString(this.getResources().getString(R.string.user_fall_status), "true");
				handleActionMonitor();
			}
			editor.putString(this.getResources().getString(R.string.user_fall_status), "false");
			editor.apply();
			
			inactivityCounter1=0;
			inactivityCounter2=0;
			timestamp_fall_detected=0;
			inactivityDetected=false;
			detectedFall = false;
			//Do we need a debug message here?
			
		}
	checkLFT();
	checkUFT();
	}
	
	private void checkLFT() {
		if (curr_vector.getRSS() <= LFT_MAX) {
			if (debug) {
				Log.v(tag, "Lower fall threshold exceeded");
			}
			
			if (!LFT_exceeded) {
				lft_timestamps = new Vector<Long>(1, 1);
				LFT_timestamp = curr_vector.getTimeStamp();
			}
			lft_timestamps.add(curr_vector.getTimeStamp());
			LFT_exceeded = true;
			
			if (debug) {
				Log.i(tag, "curr_ori: " + curr_ori + "::curr_vector: " + curr_vector.getRSS() + "::timestamp: " + curr_vector.getTimeStamp());
			}
		}
	}
	private void checkUFT(){		
		if ( curr_vector.getRSS() >= UFT_MIN) {
			UFT_timestamp = curr_vector.getTimeStamp();
									
			//if the LFT was not exceeded, there's no point in checking for a new fall.
			if(!LFT_exceeded)
				return;
			
				Iterator<Long> it = lft_timestamps.iterator();
				while( it.hasNext() ) {
					Long tmp = (Long) it.next();
					//check for bad timestamps
					if(tmp <= 0) {
						if(debug) {
							Log.e(tag, "Bad Timestamp: " + tmp);
							//logMsg+="Bad Timestamp: " + tmp+"\n";
						}
						continue;
					}
					
					tmp = UFT_timestamp - tmp;
				
					//check to see if (UFT_timestamp - tmp) is below the min Rising-Edge Threshold for all the timestamps
					if(tmp < TRE_MIN) { continue; }
					//check to see if (UFT_timestamp - tmp) is above the max Rising-Edge Threshold for all the timestamps 
					if(tmp > TRE_MAX) { continue; }
				
					timestamp_fall_detected = curr_vector.getTimeStamp();
					if (debug) {
						Log.v(tag, "Fall Detected: UFT-LFT = " + tmp + "::Time Detected: " + timestamp_fall_detected);
					}
				
					detectedFall = true;
				
					//updating variables to be used by method checkADLS()...
					newPotentialFallForADLs=true;
					checkedADLs=false;
					//Note: I added this statement here. There is no reason to keep checking the timestamps when a fall is already detected.
					break;
				}
				
			
				LFT_exceeded = false;
			
				if (debug) {
					Log.i(tag, "UFT:"+(curr_vector.getTimeStamp())+":"+curr_ori+"::"+curr_vector.getRSS()+":::"+(UFT_timestamp-LFT_timestamp));
				}
			}
		
	}
	
	private void checkADLs() {
		//If a new possible fall hasn't been detected, we only need to save the last 640ms worth of data.
		if(!newPotentialFallForADLs) {
			//adding the current vector to the end of the list.
			vectorList.add(new KVector(curr_vector));
		   
		   /* Since we're saving only the last 640ms worth of data, while the timestamp of the last (and most recent) vector in the vectorList is more than 640ms later than 
		    * the timestamp of the first (and oldest) vector in the list, we remove the first vector in the vectorLlist. 
		    * (getTimeStamp() returns a time in microseconds; we need a time in milliseconds. So we multiply by MICRO_TO_MILLI to convert to milliseconds.)
		    */
		   while((vectorList.getLast().getTimeStamp()*MICRO_TO_MILLI - vectorList.getFirst().getTimeStamp()*MICRO_TO_MILLI) > 640) {
		      vectorList.removeFirst();
		   }
		}

		//If a new possible fall has been detected, we need to save the 640ms worth of data before the UFT was exceeded until 540ms after the UFT was exceeded.
		else {
			
		   /* While the timestamp for when the UFT was exceeded is more than 640ms later than the timestamp of the first (and oldest) vector in the list, 
		    * we remove the first vector in the vectorList. 
			* (getTimeStamp() returns a time in microseconds; we need a time in milliseconds. So we multiply by MICRO_TO_MILLI to convert to milliseconds.)
			*/
		   while((UFT_timestamp*MICRO_TO_MILLI - vectorList.getFirst().getTimeStamp()*MICRO_TO_MILLI) > 640) {
		      vectorList.removeFirst();
		   }
		   
		   //If the difference between the UFT_timestamp and the timestamp of the most recent vector stored in the vectorList is less than or equal to 540ms, 
		   //then we haven't yet stored all the data in the time window plus one reading past the window, and hence we cannot yet check for ADLS.
		   //We add the current vector to the list.
		   if((vectorList.getLast().getTimeStamp()*MICRO_TO_MILLI - UFT_timestamp*MICRO_TO_MILLI)<=540) {
		      vectorList.add(new KVector(curr_vector));
		   }

		   
		   //Else, we have stored all info necessary to calculate AAMV, FFI, and FFAAM, and so we can now test for class A, B, and C ADLs.
		   else {
			   double acc1=0;
			   double acc2=0;
			 
			   //diffTotal will hold the total of all acceleration magnitude variations between the vectors in the vectorList.
			   double diffTotal=0;
		       ListIterator<KVector> itr=vectorList.listIterator();
		       if(itr.hasNext()){acc2=itr.next().getRSS();}
		       
		       while(itr.hasNext()) {
		    	   acc1=acc2;
		    	   acc2=itr.next().getRSS();
		    	   diffTotal+=Math.abs(acc2-acc1);
		       }
		       
		       /* The average acceleration magnitude variation is equal to the total of the acceleration magnitude variations 
		        * divided by the number of vectors within the time window. 
		        * (We subtract one from the vectorList's size because the vectorList includes one vector beyond the time window.)
		        */
			   double AAMV=diffTotal / (vectorList.size()-1);
		   
		      if(AAMV < AAMV_MIN){
		         ADLTypeAB=true;
		         if (debug){
					//Toast.makeText(this, "Calculated AAMV. Detected a class A or B ADL.", 1).show();
					Log.i(tag, "AAMV = " + AAMV +"::Time detected: " + curr_vector.getTimeStamp());
				 }
		      }
		      else	{
		         ADLTypeAB=false;
		         if (debug)	{
					//Toast.makeText(this, "Calculated AAMV. Did not detect a class A or B ADL.", 1).show();
					Log.i(tag, "AAMV = " + AAMV +"::Time calculated: " + curr_vector.getTimeStamp());
				 }
		      }
		      checkTypeC();
		      checkedADLs=true;
		      newPotentialFallForADLs=false;
		      
		      vectorList.add(new KVector(curr_vector));
		   }
		}
	}
	
	private void checkTypeC()
	{
		//the landingStart is estimated to be 80ms prior to the time the UFT was last exceeded.
		long landingStart=(long)(UFT_timestamp*MICRO_TO_MILLI)-80;
		//we must now search backwards in time for the leap end, the time at which to begin the search is 20ms before the landingStart.
		long startSearchTime=landingStart-20;
		
		ListIterator<KVector> itr=vectorList.listIterator();

		//after this while loop, the call itr.previous() will return one element past the startSearchTime...
		while((itr.hasNext())&&((itr.next().getTimeStamp()*MICRO_TO_MILLI)<=startSearchTime)) {}

		itr.previous();
		
		while((itr.hasPrevious())&&(itr.previous().getRSS()<SensorManager.GRAVITY_EARTH)) {}

		KVector leapEnd=itr.next();

		//The Free Fall Interval is the difference between the landing start and leap end.
		long FFI=landingStart-(long)(leapEnd.getTimeStamp()*MICRO_TO_MILLI);
		//totalAcc will keep a running total of all the RSS values for the vectors in the FFI
		double totalAcc=leapEnd.getRSS();
		//accCount will keep track of the number of vectors in the free fall interval.
		int accCount=1;
		KVector currVector;
		
		while((itr.hasNext())&&(((currVector=itr.next()).getTimeStamp()*MICRO_TO_MILLI)<=landingStart)) {
		   totalAcc+=currVector.getRSS();
		   accCount++;
		}

		//The Free Fall Average Acceleration Magnitude equals the total of the RSS values of all vectors in the FFI
		//divided by the number of vectors in the free fall interval.
		double FFAAM=totalAcc/accCount;

		if((FFI>(long)FFI_MAX)&&(FFAAM<FFAAM_MIN)) {
		   ADLTypeC=true;
		   if (debug) {
		      Log.i(tag, "FFI = " + FFI +"::FFAAM = "+FFAAM+"::Time detected: " + curr_vector.getTimeStamp());
		   }
		}
		else
		{
		   ADLTypeC=false;
		   if (debug) {
		      Log.i(tag, "FFI = " + FFI +"::FFAAM = "+FFAAM+"::Time calculated: " + curr_vector.getTimeStamp());
		   }
		}
	}
	
	private void determineInactivityPeriod() {
		inactivityCounter1++;
		Log.i(tag, "determineInactivityPeriod called. Current Count: " + inactivityCounter2 + "::Total Time: " + ((float)inactivity_timestamp/1000000) + " seconds");
		
		if ((curr_ori < 3) && ( prev_ori < 3)) {
			inactivityCounter2++;
			if(inactivityCounter2>=17) {
				inactivity_timestamp += curr_vector.getTimeStamp() - prev_vector.getTimeStamp();
			
				inactivityDetected = true;
				//this is to reset the LFT value because it is possible for the LFT to be exceeded, then inactivity, then UFT exceeded.
				LFT_exceeded = false;
				
				if(debug) {
					Log.i(tag, "Inactivity Period Detected! Current Count: " + inactivityCounter2 + "::Timestamp is: " + curr_vector.getTimeStamp());
				}
			}
		}
	
		else {
			inactivityCounter2=0;
			Log.i(tag, "Inactivity Period reset. Current Count: " + inactivityCounter2);
		}
	}
	
	public class MyBinder extends Binder {
		public MainMenuService getService() {
			return MainMenuService.this;
		}
	}
	
	private void log(String msg) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.i(TAG, msg);
	}
	
}
