package com.clarkgarrett.solartilt.Fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.DialogFragment;

import com.clarkgarrett.solartilt.DataSingleton;
import com.clarkgarrett.solartilt.Listeners.DialogClickListener;
import com.clarkgarrett.solartilt.Listeners.FragmentCallback;
import com.clarkgarrett.solartilt.R;
import com.clarkgarrett.solartilt.Utility;

public class SeasonalFragment extends Fragment implements LocationListener,DialogClickListener {
	private View mView;
	private TextView   mDegrees_TextView , mMinutes_TextView , mSeconds_TextView;
	private Button mSpring_Button, mSummer_Button, mFall_Button, mWinter_Button;
	boolean mFragmentStarted;
	private TextView mMessage_TextView;
	private Button mSeasonalEdit_Button;
	private Context mContext;
	private FragmentCallback mFragmentCallback;
	private double mLatitude;
	private LocationManager lm;
	private DialogFragment mDialog;
	private DataSingleton mData;
	private boolean mLocationPermissionDenied = false;
	String TAG = "## My Info ##";

	@Override
	public void onAttach(Context c){
		super.onAttach(c);
		mFragmentCallback =(FragmentCallback) c;
		mContext = c;
	}

	@Override
	public void onResume(){
		super.onResume();
		mFragmentStarted=false;
	}

	@Override
	public void onDetach(){
		super.onDetach();
		mFragmentCallback =null;
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
		mData= DataSingleton.get();  //A singleton object that will persist across rotations.
		lm= (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
		getIDs();  // Get all the R.id's we will need.

		mSeasonalEdit_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				mFragmentCallback.fragmentMessage(Utility.START_SEASONAL_EDIT_FRAGMENT);
			}
		});

		mSpring_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				Utility.startAngleLevelActivity(mContext, mSpring_Button, mFragmentStarted, mMessage_TextView);
			}
		});

		mSummer_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				Utility.startAngleLevelActivity(mContext, mSummer_Button, mFragmentStarted, mMessage_TextView);
			}
		});

		mFall_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utility.startAngleLevelActivity(mContext, mFall_Button, mFragmentStarted, mMessage_TextView);
			}
		});

		mWinter_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utility.startAngleLevelActivity(mContext, mWinter_Button, mFragmentStarted, mMessage_TextView);
			}
		});

		mMessage_TextView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent me) {
				if (mLocationPermissionDenied){  //User wants to allow location permission.
					mLocationPermissionDenied = false;
					requestLocationPermission();
				}else {
					// User gets another chance to turn on or off the GPS.
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
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

	private void startIt(){
		checkGPS();  //Check is GPS is on etc.
		checkAndCalculate();  //Check for data error and if none calculate tilt angles.
	}

	private void checkAndCalculate(){
		if (! locationPermissionsGranted()) {
			// The user must have declined location permissions in the DateFragment. So show
			// different messages depending on whether he also checked the 'Never ask again' box.
			if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
				// User didn't check 'Never ask again' box.
				mMessage_TextView.setText(getString(R.string.noPermission));
			}else{
				// User checked 'Never ask again' box.
				mMessage_TextView.setText(getString(R.string.noPermissionDone));
			}
			mLocationPermissionDenied = true;
			return;
		}
		if( ! lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			if (mData.mSeasonalLatitudeWasShown){
				setMessage(getString(R.string.levelingTool) + "  " +getString(R.string.GPSnowCurrentOff));
				//mMessage_TextView.setText(getString(R.string.GPSnowCurrentOff));
				//double latitude = SolarTiltStaticEntities.convertLatitude(mDegrees_TextView, mMinutes_TextView, mSeconds_TextView);
				Utility.calculateAngles(mSpring_Button, mSummer_Button, mFall_Button, mWinter_Button, mLatitude);
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
		Utility.calculateAngles(mSpring_Button, mSummer_Button, mFall_Button, mWinter_Button, mLatitude);  // No errors so calculate tilt angles.
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

	private void setMessage(String msg){
		blankOutFields();  // Old tilt angles no good if we have an error.
		showMessage(msg);
	}


	private void checkGPS(){
		Log.i(TAG, "mDialogShown= " + mData.mDialogShown +"  mYesClicked= " + mData.mYesClicked);
		if (! locationPermissionsGranted()){
			// Since the app starts with the DateFragment, the user must have already
			// seen the permissions dialog.  So don't make him look at it again here.
			// Just return.
			return;
		}
		boolean GPSisOn=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!GPSisOn && (mData.mLocation != null  && System.currentTimeMillis() - mData.mLocation.getTime() <= 15L  * 60L * 1000L)){
			mLatitude=mData.mLocation.getLatitude();  //GPS is off, but we have our own saved value.
			Utility.setLatitude(mDegrees_TextView, mMinutes_TextView, mSeconds_TextView, mLatitude);
			mMessage_TextView.setText(getString(R.string.GPSnowCurrentOff));
			mData.mSeasonalLatitudeWasShown=true;
			return;
		}
		if(GPSisOn){
			getLatitude();
			mData.mYesClicked=false;
			mData.mDialogShown=true;
		}
		else{   //GPS is not on.
			if (! mData.mDialogShown || mData.mYesClicked || mData.mDialogShowing){
                // GPS service is not on. Show dialog asking user if he wants to
                // turn on the GPS.  If he answers yes and then doesn't actually turn
                // it on when given the chance, then display the dialog again.  Insist
                // on an actual no answer before letting the user proceed without
                // turning on GPS.
				mData.mYesClicked=false;
                Utility.getAlertDialog(getActivity(), mMessage_TextView).show();
				mData.mDialogShown=true;
				mData.mDialogShowing = true;
			}
			else{
				if (mData.mSeasonalLatitudeWasShown){
					showMessage(getString(R.string.GPSnowCurrentOff));
				}
			}
		}
	}

	private void getLatitude(){
		Location loc = null;
		try {
			loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}catch (SecurityException e){
			// Should be impossible to get here since we already checked for location
			// permission.  But Android Studio complains if this isn't in a try/catch block.
			showMessage(getString(R.string.noPermission));
			mLocationPermissionDenied = true;
			return;
		}
		if (loc != null){  //We have a GPS location.
			if (System.currentTimeMillis() - loc.getTime() > 15L  * 60L * 1000L){
				waitingGPS();  //The location is more that 15 minutes old so wait for new update.
			}
			else{
				//The location is less than 15 minutes old.  This is good enough
				//because nobody can move far enough in 15 minutes to have a
				//significant effect on the tilt angle calculation.
				mLatitude =loc.getLatitude();
				Utility.setLatitude(mDegrees_TextView, mMinutes_TextView, mSeconds_TextView, mLatitude);
				showMessage(getString(R.string.GPSnowCurrentOn));
				mData.mLocation=loc;  //Save location in case user turns off GPS.
				mData.mSeasonalLatitudeWasShown =true;
			}
		}
		else{
			waitingGPS();
		}
	}

	private void waitingGPS(){
		try {
			lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);  //We only need on fix for this app.
		}catch(SecurityException e){
			// Should be impossible to get here since we already checked for location
			// permission.  But Android Studio complains if this isn't in a try/catch block.
			Utility.blankLatitude(mDegrees_TextView, mMinutes_TextView, mSeconds_TextView);  // Blank out these fields since they
			blankOutFields();  // are invalid without a current GPS fix.
			showMessage(getString(R.string.noPermission));
			mLocationPermissionDenied = true;
			return;
		}
		showMessage(getString(R.string.waitingGPS)); // Tell user we are waiting for GPS update.
		Utility.blankLatitude(mDegrees_TextView, mMinutes_TextView, mSeconds_TextView);  // Blank out these fields since they
		blankOutFields();  // are invalid without a current GPS fix.
		mData.mWaitingForSeasonalGps=true;
	}

	private void blankOutFields(){
		mSpring_Button.setText("");
		mSummer_Button.setText("");
		mFall_Button.setText("");
		mWinter_Button.setText("");
	}

	private void showMessage(String msg){
		if (! mData.mDialogShowing){
			mMessage_TextView.setText(msg);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// There is only one possible MenuItem, the icon button.
		// So go back to date fragment.
		getActivity().onBackPressed();
		return true;
	}

	// Get all the Views we will need.
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

	@Override
	public void onLocationChanged(Location loc){
		mData.mWaitingForSeasonalGps=false;
		if (isAdded()){  //We have a fresh GPS update.  But only process it if this
			mLatitude=loc.getLatitude();  // fragment is currently showing (isAdded) or
			Utility.setLatitude(mDegrees_TextView, mMinutes_TextView, mSeconds_TextView, mLatitude); // we will crash.
			showMessage(getString(R.string.GPSnowCurrentOn));
			mData.mSeasonalLatitudeWasShown =true;
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
					mMessage_TextView.setText(getString(R.string.noPermission));
				} else {
					// User checked 'Never ask again' box.
					mMessage_TextView.setText(getString(R.string.noPermissionDone));
				}
				mLocationPermissionDenied = true;
			}
		}
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
