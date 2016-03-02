package com.clarkgarrett.solartilt;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;

public class SolarTiltActivity extends FragmentActivity implements Callbacks {

	private FragmentManager fm = getSupportFragmentManager();
	private String TAG = "## My Info ##";
	private SolarTiltFragmentDate mDate_Fragment;
	private SolarTiltFragmentDateEdit mDateEdit_Fragment;
	private SolarTiltFragmentSeasonal mSeason_Fragment;
	private SolarTiltFragmentSeasonalEdit mSeasonalEdit_Fragment;
	private boolean mFirstTime = true;
	private SolarTiltData mSolarTiltData = SolarTiltData.get();
	private int PERMISSIONS_FLAG = 1;
	private final String DATE_FRAGMENT_TAG = "DFT";
	private final String SEASONAL_FRAGMENT_TAG = "SFT";
	private final String DATE_EDIT_FRAGMENT_TAG = "DEFT";
	private final String SEASONAL_EDIT_FRAGMENT_TAG = "SEFT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solar_tilt);
		mFirstTime = ! mSolarTiltData.mFragmentShown;
	}

	@Override
	public void onBackPressed(){
		mSolarTiltData.mFragmentShown = false;
		super.onBackPressed();
	}

	@Override
	public void onResumeFragments() {
		//If there is no fragment being shown, we are probably starting from scratch so
		//create and display the date fragment.
		checkLocationPermissions();
	}

	@Override
	public void fragmentMessage(int id) {
		switch (id) {
			case SolarTiltStaticEntities.mStartFragmentSeasonal:
				if (mSeason_Fragment == null) {
					mSeason_Fragment = new SolarTiltFragmentSeasonal();
				}
				fm.beginTransaction().replace(R.id.fragmentContainer, mSeason_Fragment, SEASONAL_FRAGMENT_TAG).addToBackStack("").commit();
				return;
			case SolarTiltStaticEntities.mStartFragmentSeasonalEdit:
				if (mSeasonalEdit_Fragment == null) {
					mSeasonalEdit_Fragment = new SolarTiltFragmentSeasonalEdit();
				}
				fm.beginTransaction().replace(R.id.fragmentContainer, mSeasonalEdit_Fragment, SEASONAL_EDIT_FRAGMENT_TAG).addToBackStack("").commit();
				return;
			case SolarTiltStaticEntities.mStartFragmentDateEdit:
				if (mDateEdit_Fragment == null) {
					mDateEdit_Fragment = new SolarTiltFragmentDateEdit();
				}
				fm.beginTransaction().replace(R.id.fragmentContainer, mDateEdit_Fragment, DATE_EDIT_FRAGMENT_TAG).addToBackStack("").commit();
				return;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		for(int i = 0; i < grantResults.length; i++){
			if (grantResults[i] == PackageManager.PERMISSION_DENIED){
				return;
			}
		}

		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if (fragment instanceof SolarTiltFragmentDate){
			((SolarTiltFragmentDate)fragment).startIt();
		}else{
			if(fragment instanceof SolarTiltFragmentSeasonal){
				((SolarTiltFragmentSeasonal)fragment).startIt();
			}
		}
	}

	private void checkLocationPermissions(){

		if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
				|| !mFirstTime) {
			if (fm.findFragmentById(R.id.fragmentContainer) == null) {
				mDate_Fragment = new SolarTiltFragmentDate();
				fm.beginTransaction().add(R.id.fragmentContainer, mDate_Fragment, DATE_FRAGMENT_TAG).commit();
				mSolarTiltData.mFragmentShown = true;
			}
		} else {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
					PERMISSIONS_FLAG);
		}
		mFirstTime = false;
	}

}
