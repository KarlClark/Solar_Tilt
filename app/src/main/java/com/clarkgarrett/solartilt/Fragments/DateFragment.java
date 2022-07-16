package com.clarkgarrett.solartilt.Fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.clarkgarrett.solartilt.DataSingleton;
import com.clarkgarrett.solartilt.Listeners.DialogClickListener;
import com.clarkgarrett.solartilt.Listeners.FragmentCallback;
import com.clarkgarrett.solartilt.R;
import com.clarkgarrett.solartilt.Utility;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class DateFragment extends  Fragment implements  LocationListener {

	private LocationManager mLm;
	private FragmentCallback mFragmentCallback;
	private Context mContext;
	private double mLatitude;
	private TextView mTextView_MM, mTextView_DD, mTextView_Message, mTextView_Degrees,
	                 mTextView_Minutes, mTextView_Seconds;
	private Button mButton_TiltAngle;
	private DialogFragment mDialog;
	private DataSingleton mData;
	private Button mButton_Seasonal, mButton_EditDate;
	private boolean mFragmentStarted=false, mLocationPermissionDenied = false;
	private static final String TAG="## My Info ##";

	@Override
	public void onAttach(Context c){
		super.onAttach(c);
		//Get our activity to listen for our signal.  The activity implements the Callbacks
		//interface.  Also store the activity in a variable so we don't have to
		//call getActivity() all the time. 
		mFragmentCallback =(FragmentCallback) c;
		mContext = c;
	}

	@Override
	public void onDetach(){
		super.onDetach();
		mFragmentCallback =null;
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


		mLm= (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
		mData= DataSingleton.get();
		//mData.mDialogShown=false;
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
				mFragmentCallback.fragmentMessage(Utility.START_SEASONAL_FRAGMENT);
			}
		});

		mButton_EditDate= (Button)v.findViewById(R.id.buttonEditDate);
		mButton_EditDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){  //Tell the activity to display the date edit fragment.
				mFragmentCallback.fragmentMessage(Utility.START_DATE_EDIT_FRAGMENT);
			}
		});

		mButton_TiltAngle=(Button)v.findViewById(R.id.ButtonDateTiltAngle);
		mButton_TiltAngle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){  // Start the leveling tool activity.
				Utility.startAngleLevelActivity(mContext, mButton_TiltAngle, mFragmentStarted, mTextView_Message);
			}
		});

		mTextView_Message.setOnTouchListener(new View.OnTouchListener(){
			 public boolean onTouch(View v, MotionEvent me){
				 if (mLocationPermissionDenied){  //User wants to allow location permission.
					 mLocationPermissionDenied = false;
					requestLocationPermission();
				 }else {
					 // User wants another chance to turn on or off the GPS.
					 startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				 }
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

	private void startIt(){
		checkGPS();
		Utility.setDate(mTextView_MM, mTextView_DD);
		checkAndCalculate();
	}


	private void getLatitude(){
		Location loc = null;
		try {
			loc = mLm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}catch(SecurityException e){
			// Should be impossible to get here since we already checked for location
			// permission.  But Android Studio complains if this isn't in a try/catch block.
			mTextView_Message.setText(getString(R.string.noPermission));
			mLocationPermissionDenied = true;
			return;
		}
		if (loc != null){  //we have a location
			if (System.currentTimeMillis() - loc.getTime() > 15L  * 60L * 1000L){ //Location is more than 15 minutes old.
				waitingGPS();  //So wait for new GPS update.
			}
			else{  //Location is less than 15 minutes old.  This is good enough since no one can move far enough in
				mLatitude =loc.getLatitude();  //15 minutes to have a significant effect on the tilt angle.
				String s =Location.convert(mLatitude,Location.FORMAT_SECONDS);
				Utility.setLatitude(mTextView_Degrees, mTextView_Minutes, mTextView_Seconds, mLatitude);
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
		try {
			mLm.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);  //We only need one update for this app
		}catch(SecurityException e){
			// Should be impossible to get here since we already checked for location
			// permission.  But Android Studio complains if this isn't in a try/catch block.
			Utility.blankLatitude(mTextView_Degrees, mTextView_Minutes, mTextView_Seconds);  // Blank out these fields since their values are invalid without
			mButton_TiltAngle.setText(""); // fresh GPS info.
			mTextView_Message.setText(getString(R.string.noPermission));
			mLocationPermissionDenied = true;
			return;
		}
		showMessage(getString(R.string.waitingGPS));  //tell user we are waiting for a GPS update.
		Utility.blankLatitude(mTextView_Degrees, mTextView_Minutes, mTextView_Seconds);  // Blank out these fields since their values are invalid without
		mButton_TiltAngle.setText(""); // fresh GPS info.
		mData.mWaitingForDateGps=true;
	}
	
	private void showMessage(String msg){
		if (! mData.mDialogShowing){
			mTextView_Message.setText(msg);
		}
	}
	
	private void checkGPS(){

		// Must check location permission every time because user can
		// turn off permission anytime he wants
		if (! locationPermissionsGranted()){
			requestLocationPermission();
			return;
		}
		boolean GPSisOn=mLm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if  (! GPSisOn && (mData.mLocation != null  && System.currentTimeMillis() - mData.mLocation.getTime() <= 15L  * 60L * 1000L)){
			mLatitude=mData.mLocation.getLatitude();  //GPS is off but we have our own saved location that is less than 15 minutes old.
			Utility.setLatitude(mTextView_Degrees, mTextView_Minutes, mTextView_Seconds, mLatitude);
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
			if (! mData.mDialogShown || mData.mYesClicked || mData.mDialogShowing){
				// GPS service is not on. Show dialog asking user if he wants to
				// turn on the GPS.  If he answers yes and then doesn't actually turn
				// it on when given the chance, then display the dialog again.  Insist
				// on an actual no answer before letting the user proceed without
				// turning on GPS.
				mData.mYesClicked=false;
				Utility.getAlertDialog(getActivity(), mTextView_Message).show();
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
		Utility.calculateTiltAngle(mTextView_MM, mTextView_DD, mLatitude, mButton_TiltAngle);
		showMessage(getString(R.string.levelingTool) + "  " + mTextView_Message.getText().toString());
	}



	@Override
	public void onLocationChanged(Location loc){
		mData.mWaitingForDateGps=false;
		//We got a fresh location. So process the information,  but only if we 
		//are in the isAdded state i.e. not in the backstack or we will crash.
		if (isAdded()){
			mLatitude=loc.getLatitude();
			Utility.setLatitude(mTextView_Degrees, mTextView_Minutes, mTextView_Seconds, mLatitude);
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

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		// Called after user responds to the get permissions dialog.
		if (grantResults.length > 0) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				// User granted location permission.  We want to run the startIt() method again, but we don't want
				// to do it from inside this method, which is already running through the activity onRequestPermissionResult()
				// method.  In particular, if the GPS is turned off the app will try to start an AlertDialogFragment
				// to advise the user of this fact, and this will cause an IllegalStateException. So I use a
				// Handler to post the startIt() method to the message queue, and now this method will exit normally.
				Handler handler = new Handler();
				handler.post(new Runnable() {
					@Override
					public void run() {
						startIt();
					}
				});
			} else {
				// The user denied location permission.  We want to show different messages
				// depending on whether he also checked the 'Never ask again' box.
				if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
					// User didn't check 'Never ask again' box.
					mTextView_Message.setText(getString(R.string.noPermission));
				}else{
					// User checked 'Never ask again' box.
					mTextView_Message.setText(getString(R.string.noPermissionDone));
				}
				mLocationPermissionDenied = true;
			}
		}
		// If grantResults.length = 0 then it means the permissions dialog was interrupted for some reason
		// like screen rotation. If so the activity will just restart this fragment and the whole process
		// will start over automatically. So we don't need an 'else' clause to do anything here.
	}


	private boolean locationPermissionsGranted(){
		// Check if location permissions are granted.  Return true if they are or false if they aren't.
		return	(ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(mContext,	Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
	}

	private void requestLocationPermission(){
		// This method will display the dialog asking the user to grant location permissions. After the user makes his
		// choice, the system will call onRequestPermissionResult();
		String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION ,Manifest.permission.ACCESS_COARSE_LOCATION};
		requestPermissions( permissions, 0);
	}


}
