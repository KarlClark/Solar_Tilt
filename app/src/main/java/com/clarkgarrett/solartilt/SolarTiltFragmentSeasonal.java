package com.clarkgarrett.solartilt;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

	public class SolarTiltFragmentSeasonal extends Fragment implements LocationListener {
		private View mView;
		private TextView   mDegrees_TextView , mMinutes_TextView , mSeconds_TextView;
		private Button mSpring_Button, mSummer_Button, mFall_Button, mWinter_Button;
		boolean mFragmentStarted;
		private TextView mMessage_TextView;
		private Button mSeasonalEdit_Button;
		private Activity mActivity;
		private Callbacks mCallbacks;
		private double mLatitude;
		private LocationManager lm;
		private SolarTiltData mData;
		private boolean mTurningOnOffGPS=false;
		String TAG = "## My Info ##";
		
		@Override
		public void onAttach(Activity a){
			super.onAttach(a);
			mCallbacks=(Callbacks) a;
			mActivity=a;
		}

		@Override
		public void onResume(){
			super.onResume();
			mFragmentStarted=false;
		}
		
		@Override
		public void onDetach(){
			super.onDetach();
			mCallbacks=null;
		}
		
		@Override
		public void onStop(){
			super.onStop();
			mData.mSeasonalLatitudeWasShown=false;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);   //We will be using the icon button as an alternative back button
			setRetainInstance(true);   //Retain the fragment across activity recreation.
		}
		
		@TargetApi(11)
		@Override
		public View onCreateView(LayoutInflater inflater,ViewGroup parent,Bundle savedInstance) {
			mView = inflater.inflate(R.layout.fragment_solar_tilt_seasonal, parent,false);
			//If the device is new enough then set the icon up as a button.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
			}
			mData= SolarTiltData.get();  //A singleton object that will persist across rotations.
			lm= (LocationManager)mActivity.getSystemService(Context.LOCATION_SERVICE);
			getIDs();  // Get all the R.id's we will need.
			
			mSeasonalEdit_Button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v){
					mCallbacks.fragmentMessage(SolarTiltStaticEntities.mStartFragmentSeasonalEdit);
				}
			});
			
			mSpring_Button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v){
					SolarTiltStaticEntities.startAngleLevelActivity(mActivity, mSpring_Button, mFragmentStarted, mMessage_TextView);
				}
			});
			
			mSummer_Button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v){
					SolarTiltStaticEntities.startAngleLevelActivity(mActivity, mSummer_Button, mFragmentStarted, mMessage_TextView);
				}
			});
			
			mFall_Button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					SolarTiltStaticEntities.startAngleLevelActivity(mActivity, mFall_Button, mFragmentStarted, mMessage_TextView);
				}
			});
			
			mWinter_Button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					SolarTiltStaticEntities.startAngleLevelActivity(mActivity, mWinter_Button, mFragmentStarted, mMessage_TextView);
				}
			});
			
			mMessage_TextView.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent me) {
					// If user touches message view, he gets another chance to turn on or off the GPS.
					mTurningOnOffGPS = true;
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					return false;
				}
			});
			
			return mView;
		}
		
		
		@Override
		public void onStart(){
			super.onStart();
			startIt();
		}

		public void startIt(){
			checkGPS();  //Check is GPS is on etc.
			checkAndCalculate();  //Check for data error and if none calculate tilt angles.
		}
		
		private void checkAndCalculate(){
			if (! locationPermissionsGranted()) {
				mMessage_TextView.setText(getString(R.string.noPermission));
				return;
			}
			if( ! lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				if (mData.mSeasonalLatitudeWasShown){
					setMessage(getString(R.string.levelingTool) + "  " +getString(R.string.GPSnowCurrentOff));
					//mMessage_TextView.setText(getString(R.string.GPSnowCurrentOff));
					//double latitude = SolarTiltStaticEntities.convertLatitude(mDegrees_TextView, mMinutes_TextView, mSeconds_TextView);
					SolarTiltStaticEntities.calculateAngles(mSpring_Button, mSummer_Button, mFall_Button, mWinter_Button, mLatitude);
					return;
				}
				else{
					setMessage(getString(R.string.GPSnotOnSeasonal));
					return;
				}
			}
			if (mData.mWaitingForSeasonalGps){
				return;
			}
		setMessage(getString(R.string.levelingTool) + "  " + mMessage_TextView.getText().toString());
		SolarTiltStaticEntities.calculateAngles(mSpring_Button, mSummer_Button, mFall_Button, mWinter_Button, mLatitude);  // No errors so calculate tilt angles.
		}
		
		
		
		private void setMessage(String msg){
			blankOutFields();  // Old tilt angles no good if we have an error.
			mMessage_TextView.setText(msg);
		}
		
		
		private void checkGPS(){
			if (! locationPermissionsGranted()){
				mMessage_TextView.setText(getString(R.string.noPermission));
				return;
			}
			boolean GPSisOn=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (!GPSisOn && (mData.mLocation != null  && System.currentTimeMillis() - mData.mLocation.getTime() <= 15L  * 60L * 1000L)){
				mLatitude=mData.mLocation.getLatitude();  //GPS is off, but we have our own saved value.
				SolarTiltStaticEntities.setLatitude(mDegrees_TextView, mMinutes_TextView, mSeconds_TextView, mLatitude);
				mMessage_TextView.setText(getString(R.string.GPSnowCurrentOff));
				mData.mSeasonalLatitudeWasShown=true;
				return;
			}
			if(GPSisOn){ 
				getLatitude(); 
			}
			else{   //GPS is not on.
				if (mData.mSeasonalLatitudeWasShown){
					mMessage_TextView.setText(getString(R.string.GPSnowCurrentOff));	
				}
				else{
					mMessage_TextView.setText(getString(R.string.GPSnotOnSeasonal));  // Tell user to enter a latitude
				}
			}
		}
		
		private void getLatitude(){
			Location loc =lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (loc != null){  //We have a GPS location.
				if (System.currentTimeMillis() - loc.getTime() > 15L  * 60L * 1000L){
					waitingGPS();  //The location is more that 15 minutes old so wait for new update.
				}
				else{
					//The location is less than 15 minutes old.  This is good enough
					//because nobody can move far enough in 15 minutes to have a
					//significant effect on the tilt angle calculation.
					mLatitude =loc.getLatitude();
					SolarTiltStaticEntities.setLatitude(mDegrees_TextView, mMinutes_TextView, mSeconds_TextView, mLatitude);
					mMessage_TextView.setText(getString(R.string.GPSnowCurrentOn));
					mData.mLocation=loc;  //Save location in case user turns off GPS.
					mData.mSeasonalLatitudeWasShown =true;
				}
			}
			else{
				waitingGPS();
			}
		}
		
		private void waitingGPS(){
			lm.requestSingleUpdate(LocationManager.GPS_PROVIDER , this , null);  //We only need on fix for this app.
			mMessage_TextView.setText(getString(R.string.waitingGPS)); // Tell user we are waiting for GPS update.
			SolarTiltStaticEntities.blankLatitude(mDegrees_TextView, mMinutes_TextView, mSeconds_TextView);  // Blank out these fields since they 
			blankOutFields();  // are invalid without a current GPS fix.
			mData.mWaitingForSeasonalGps=true;             
		}
		
		private void blankOutFields(){
			mSpring_Button.setText("");
			mSummer_Button.setText("");
			mFall_Button.setText("");
			mWinter_Button.setText("");
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item){
			// There is only one possible MenuItem, the icon button.
			// So go back to date fragment.
			getActivity().onBackPressed();
			return true;
		}
		
		// Get all the IDs we will need.
		private void getIDs(){
			mSummer_Button = (Button)mView.findViewById(R.id.Button_SeasonalSummer);
			mWinter_Button = (Button)mView.findViewById(R.id.Button_SeasonalWinter);
			mSpring_Button = (Button)mView.findViewById(R.id.Button_SeasonalSpring);
			mFall_Button = (Button)mView.findViewById(R.id.Button_SeasonalFall);
			mMessage_TextView =(TextView)mView.findViewById(R.id.textView_SeasonalMessage);
			mDegrees_TextView=(TextView)mView.findViewById(R.id.textViewSeasonalDegrees);
			mMinutes_TextView=(TextView)mView.findViewById(R.id.textViewSeasonalMinutes);
			mSeconds_TextView=(TextView)mView.findViewById(R.id.textViewSeasonalSeconds);
			mSeasonalEdit_Button=(Button)mView.findViewById(R.id.buttonEditSeasonal);
		}
		
		public void onLocationChanged(Location loc){
			mData.mWaitingForSeasonalGps=false;
			if (isAdded()){  //We have a fresh GPS update.  But only process it if this
				mLatitude=loc.getLatitude();  // fragment is currently showing (isAdded) or
				SolarTiltStaticEntities.setLatitude(mDegrees_TextView, mMinutes_TextView, mSeconds_TextView, mLatitude); // we will crash.
				mMessage_TextView.setText(getString(R.string.GPSnowCurrentOn));
				mData.mSeasonalLatitudeWasShown =true;
				checkAndCalculate();
			}
		}
				
		public void onProviderDisabled(String provider){
			
			}
		
		public void onProviderEnabled(String provider){
			
		}
		
		public void onStatusChanged(String provider,int status, Bundle extras){
			
		}

		private boolean locationPermissionsGranted(){
			return
					(ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
							ContextCompat.checkSelfPermission(mActivity,	Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
		}
}
