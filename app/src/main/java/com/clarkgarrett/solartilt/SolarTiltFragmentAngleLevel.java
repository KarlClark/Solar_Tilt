package com.clarkgarrett.solartilt;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Timer;
import java.util.TimerTask;

public class SolarTiltFragmentAngleLevel extends Fragment {
	
	private float[] mAccelerometerValues,mMagneticFieldValues;
	private float[] mOrientationValues = new float[3];
	private float[] mRotationMatrix = new float[9];
	private float mFilteredPitch , k=0.9f, mAngle=0;
	private boolean mFirstValue;
	private boolean mHasMagneticFieldSensor;
	private Activity mMyActivity;
	private AngleLevelView mAngleLevelView;
	private SensorManager mSensorManager;
	private Timer mUpdateTimer;
	private static final int PITCH = 1;
	private static final String ANGLE = "angle";
	private static final String TAG="## My Info ##";
	private static final String TAG2="## My Info2 ##";
	
	// Convenience method that does the work of creating a fragment
	// with a bundle containing the angle.
	public static SolarTiltFragmentAngleLevel newInstance(float angle){
		Bundle args = new Bundle();
		args.putFloat(ANGLE, angle);
		SolarTiltFragmentAngleLevel fragment = new SolarTiltFragmentAngleLevel();
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onAttach(Activity a){
		super.onAttach(a);
		mMyActivity= a;
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mSensorManager.unregisterListener(myAccelerometerListener);
		mSensorManager.unregisterListener(myMagneticFieldListener);
		if (mUpdateTimer != null){
			mUpdateTimer.cancel();
			mUpdateTimer = null;
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		//create accelerometer and magnetic field sensors and register listeners for them.
		Sensor aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor mfSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mHasMagneticFieldSensor = mfSensor != null;
		mSensorManager.registerListener(myAccelerometerListener, aSensor, SensorManager.SENSOR_DELAY_UI);
		if (mHasMagneticFieldSensor) {
			mSensorManager.registerListener(myMagneticFieldListener, mfSensor, SensorManager.SENSOR_DELAY_UI);
		}
		if ( mUpdateTimer == null){
			mFirstValue=true;
			mUpdateTimer = new Timer("timer tag");
			mUpdateTimer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					updateGUI();
				}
			}, 0, 50);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();  // Get the angle from
		mAngle = args.getFloat(ANGLE); // the attached bundle.
		setHasOptionsMenu(true);   //We will be using the icon button as an alternative back button
	}
	
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_solar_tilt_angle_level, parent,false);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		mAngleLevelView =(AngleLevelView)v.findViewById(R.id.angleLevelView);
		mSensorManager = (SensorManager)mMyActivity.getSystemService(Context.SENSOR_SERVICE);
		
		mAngleLevelView.setTargetAngle(mAngle);
		//mAngleLevelView.setColor(getResources().getColor(R.color.blue));
		return v;
	}
	
	 @Override
	  public void onPause(){
		 super.onPause();
		 mSensorManager.unregisterListener(myAccelerometerListener);
		 mSensorManager.unregisterListener(myMagneticFieldListener);
		 mUpdateTimer.cancel();
		 mUpdateTimer = null;
	  }
	 
	 @Override
		public boolean onOptionsItemSelected(MenuItem item){
			// There is only one possible MenuItem, the icon button.
			// So go back to previous fragment.
			mMyActivity.onBackPressed();
			return true;
		}
	 
	 final SensorEventListener myAccelerometerListener = new SensorEventListener() {
		 @Override
		 public void onSensorChanged(SensorEvent sensorEvent) {
			 //Log.i(TAG,"myAccelermeterListener sensor type = " + sensorEvent.sensor.getType() );
			 if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
				 mAccelerometerValues = sensorEvent.values.clone(); // Store accelerometer values when they change.
		 }
		 @Override
		 public void onAccuracyChanged(Sensor sensor, int accuracy){}
	 };
	 
	 final SensorEventListener myMagneticFieldListener = new SensorEventListener() {
		 @Override
		 public void onSensorChanged(SensorEvent sensorEvent) {
			 //Log.i(TAG,"myMagneticFieldListener sensor type = " + sensorEvent.sensor.getType() );
			 if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
				 mMagneticFieldValues = sensorEvent.values.clone();  //Store magnetic sensor values when they change.
		 }
		 @Override
		 public void onAccuracyChanged(Sensor sensor, int accuracy){}
	 };
	 
	 /*
	  * Form the Rotation matrix from our accelerometer and magnetic field values.  Use
	  * this to get the device orientation and use the orientation to get the pitch angle.
	  * Run the pitch angle through a low pass filter to smooth out the movement of the
	  * GUI, and pass the filtered angle to the AngleLevelView view so it can re=draw the
	  * GUI with the new angle.
	  */
	 private void updateGUI() {
		 //Log.i(TAG,"updateGUI called, mAccelermeterVlaues= " + mAccelerometerValues + " mMagneticFIeldValues= " + mMagneticFieldValues);
		 mMyActivity.runOnUiThread(new Runnable(){
			 public void run() {
				 float angle = 0;
				 boolean haveAngle = false;

				 if (mHasMagneticFieldSensor) {
					 if (mAccelerometerValues != null && mMagneticFieldValues != null) {
						 SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerValues, mMagneticFieldValues);
						 SensorManager.getOrientation(mRotationMatrix, mOrientationValues);
						 angle = (float) Math.toDegrees(mOrientationValues[PITCH]);
						 haveAngle=true;
					 }
				 } else{
					 if  (mAccelerometerValues != null) {
						 float[] av = new float[3];
						 av = mAccelerometerValues.clone();
						 double norm_Of_av = Math.sqrt(av[0] * av[0] + av[1] * av[1] + av[2] * av[2]);
						 angle = -(float) Math.toDegrees(Math.asin(av[1] / norm_Of_av));
						 haveAngle = true;
					 }
				 }

				 if(haveAngle) {
					 if (mFirstValue) {
						 mFilteredPitch = angle;
						 mFirstValue = false;
					 } else {
						 mFilteredPitch = k * mFilteredPitch + ((1.0f) - k) * angle;
					 }
					 mAngleLevelView.setAngle(mFilteredPitch);
				 }
			 }
		 });
	 }
}
 