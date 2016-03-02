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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SolarTiltFragmentDate extends Fragment implements DialogClickListener, LocationListener {
	
	private LocationManager mLm;
	private Callbacks mCallbacks;
	private Activity mActivity;
	private double mLatitude;
	private TextView mTextView_MM, mTextView_DD, mTextView_Message, mTextView_Degrees,
	                 mTextView_Minutes, mTextView_Seconds;
	private Button mButton_TiltAngle;
	private DialogFragment mDialog;
	private SolarTiltData mData;
	private Button mButton_Seasonal, mButton_EditDate;
	private boolean mFragmentStarted=false;
	private static final String TAG="## My Info ##";
	
	@Override
	public void onAttach(Activity a){
		super.onAttach(a);
		//Get our activity to listen for our signal.  The activity implements the Callbacks
		//interface.  Also store the activity in a variable so we don't have to
		//call getActivity() all the time. 
		mCallbacks=(Callbacks) a;
		mActivity=a;
	}
	
	@Override
	public void onDetach(){
		super.onDetach();
		mCallbacks=null;
	}
	
	@Override
	public void onStop(){
		super.onStop();
		mData.mDateLatitudeWasShown=false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//Retain the fragment across activity recreation.
		setRetainInstance(true);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mFragmentStarted=false;
	}

	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup parent,Bundle savedInstance) {
		View v = inflater.inflate(R.layout.fragment_solar_tilt_date, parent,false);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		}
		
		
		mLm= (LocationManager)mActivity.getSystemService(Context.LOCATION_SERVICE);
		mData= SolarTiltData.get();
		mTextView_MM = (TextView)v.findViewById(R.id.textViewMM);
		mTextView_DD = (TextView)v.findViewById(R.id.textViewDD);
		mTextView_Message=(TextView)v.findViewById(R.id.textViewDateMessage);
		mTextView_Degrees=(TextView)v.findViewById(R.id.textViewDateDegrees);
		mTextView_Minutes=(TextView)v.findViewById(R.id.textViewDateMinutes);
		mTextView_Seconds=(TextView)v.findViewById(R.id.textViewDateSeconds);
		
		mButton_Seasonal= (Button)v.findViewById(R.id.buttonSeasonal);
		mButton_Seasonal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){ //Tell the activity to display the seasonal fragment.
				mCallbacks.fragmentMessage(SolarTiltStaticEntities.mStartFragmentSeasonal);
			}
		});
		
		mButton_EditDate= (Button)v.findViewById(R.id.buttonEditDate);
		mButton_EditDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){  //Tell the activity to display the date edit fragment.
				mCallbacks.fragmentMessage(SolarTiltStaticEntities.mStartFragmentDateEdit);
			}
		});
		
		mButton_TiltAngle=(Button)v.findViewById(R.id.ButtonDateTiltAngle);
		mButton_TiltAngle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){  // Start the leveling tool activity.
				SolarTiltStaticEntities.startAngleLevelActivity(mActivity, mButton_TiltAngle, mFragmentStarted, mTextView_Message);
			}
		});
		
		mTextView_Message.setOnTouchListener(new View.OnTouchListener(){
			 public boolean onTouch(View v, MotionEvent me){
				 // If user touches message view, he gets another chance to turn on or off the GPS.
				 startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				 return false;
			 }
		 });
		
		return v;
	}
	
	@Override
	public void onStart(){
		super.onStart();
		startIt();
	}

	public void startIt(){
		checkGPS();
		SolarTiltStaticEntities.setDate(mTextView_MM, mTextView_DD);
		checkAndCalculate();
	}
	
	
	private void getLatitude(){
		Location loc =mLm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (loc != null){  //we have a location
			if (System.currentTimeMillis() - loc.getTime() > 15L  * 60L * 1000L){ //Location is more than 15 minutes old.
				waitingGPS();  //So wait for new GPS update.
			}
			else{  //Location is less than 15 minutes old.  This is good enough since no one can move far enough in
				mLatitude =loc.getLatitude();  //15 minutes to have a significant effect on the tilt angle.
				String s =Location.convert(mLatitude,Location.FORMAT_SECONDS);
				SolarTiltStaticEntities.setLatitude(mTextView_Degrees, mTextView_Minutes, mTextView_Seconds, mLatitude);
				mData.mLocation=loc;  //Save location in case user turns off GPS.
				mData.mDateLatitudeWasShown=true;
				showMessage(getString(R.string.GPSnowCurrentOn));
			}
		}
		else{
			waitingGPS();  //No past location, so we have to wait for GPS update.
		}
	}
	
	private void waitingGPS(){
		mLm.requestSingleUpdate(LocationManager.GPS_PROVIDER , this , null);  //We only need one update for this app 
		showMessage(getString(R.string.waitingGPS));  //tell user we are waiting for a GPS update.
		SolarTiltStaticEntities.blankLatitude(mTextView_Degrees, mTextView_Minutes, mTextView_Seconds);  // Blank out these fields since their values are invalid without
		mButton_TiltAngle.setText(""); // fresh GPS info.
		mData.mWaitingForDateGps=true;
	}
	
	private void showMessage(String msg){
		if (! mData.mDialogShowing){
			mTextView_Message.setText(msg);
		}
	}
	
	private void checkGPS(){
		if (! locationPermissionsGranted()){
			mTextView_Message.setText(getString(R.string.noPermission));
			return;
		}
		boolean GPSisOn=mLm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if  (! GPSisOn && (mData.mLocation != null  && System.currentTimeMillis() - mData.mLocation.getTime() <= 15L  * 60L * 1000L)){
			mLatitude=mData.mLocation.getLatitude();  //GPS is off but we have our own saved location that is less than 15 minutes old.
			SolarTiltStaticEntities.setLatitude(mTextView_Degrees, mTextView_Minutes, mTextView_Seconds, mLatitude);
			mData.mDateLatitudeWasShown=true;
			mTextView_Message.setText(getString(R.string.GPSnowCurrentOff));
			return;
		}
		
		if(GPSisOn){  //GPS service is on
			getLatitude();
			mData.mYesClicked=false;
			mData.mDialogShown=true;
		}
		else{
			if (! mData.mDialogShown || mData.mYesClicked){  //GPS service is not on. Show dialog asking user if he wants to
				mData.mYesClicked=false;  // turn on the GPS.  If he answers yes
				mDialog = new SolarTiltFragmentDialog();  // and then doesn't actually turn
                mDialog.setTargetFragment(this,0); //it on when given the chance then
                mDialog.setCancelable(false);  //then display the dialog again.  Insist on an actual no answer
                mDialog.show(getFragmentManager(), "tag");  // before letting user proceed without turning on GPS.
                mData.mDialogShown=true;
                mData.mDialogShowing = true;
			}
			else{
				if (mData.mDateLatitudeWasShown){
					showMessage(getString(R.string.GPSnowCurrentOff));
				}
			}
		}
	}
	
	private void checkAndCalculate(){
		if (! locationPermissionsGranted()) {
			mTextView_Message.setText(getString(R.string.noPermission));
			return;
		}
		/* 
		 * If GPS is off and the latitude was never displayed (GPS never was on) then
		 * we have an error and can't proceed. If GPS is on we can proceed. If the latitude
		 * was displayed we can proceed even if GPS is off.  It just means the user turned
		 * off the GPS after the latitude was displayed.
		 */
		if( ! mLm.isProviderEnabled(LocationManager.GPS_PROVIDER)  && ! mData.mDateLatitudeWasShown){
			showMessage(getString(R.string.GPSnotOn));
			return;
		}
		// If we are waiting for the GPS to return data, we can't proceed.
		if (mData.mWaitingForDateGps){
			return;
		}
		// Calculate tilt angle and display it along with message about leveling tool.
		SolarTiltStaticEntities.calculateTiltAngle(mTextView_MM, mTextView_DD, mLatitude, mButton_TiltAngle);
		showMessage(getString(R.string.levelingTool) + "  " + mTextView_Message.getText().toString());
	}
	
	public void onYesClick(){
		// Call back from dialog fragment.  User said yes. So start a settings activity
		// where he can turn on the GPS.
		mData.mYesClicked =true;
		mData.mDialogShowing=false;
		startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	}
	
	public void onNoClick(){
		// Call back from dialog fragment.  User said no to turning on GPS.
		// So tell user to enter latitude manually.
		mData.mDialogShowing=false;
		showMessage(getString(R.string.GPSnotOn));
	}

	@Override
	public void onLocationChanged(Location loc){
		mData.mWaitingForDateGps=false;
		//We got a fresh location. So process the information,  but only if we 
		//are in the isAdded state i.e. not in the backstack or we will crash.
		if (isAdded()){
			mLatitude=loc.getLatitude();
			SolarTiltStaticEntities.setLatitude(mTextView_Degrees, mTextView_Minutes, mTextView_Seconds, mLatitude);
			mData.mLocation=loc;
			mData.mDateLatitudeWasShown=true;
			showMessage(getString(R.string.GPSnowCurrentOn));
			checkAndCalculate();
		}
	}
	
	@Override
	public void onProviderDisabled(String provider){
		
	}
	
	@Override
	public void onProviderEnabled(String provider){
		
	}
	
	@Override
	public void onStatusChanged(String provider,int status, Bundle extras){
		
	}

	private boolean locationPermissionsGranted(){
		return
		(ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(mActivity,	Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
	}
}
