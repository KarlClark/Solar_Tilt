package com.clarkgarrett.solartilt.Activities;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.clarkgarrett.solartilt.DataSingleton;
import com.clarkgarrett.solartilt.Fragments.DateEditFragment;
import com.clarkgarrett.solartilt.Fragments.DateFragment;
import com.clarkgarrett.solartilt.Fragments.SeasonalEditFragment;
import com.clarkgarrett.solartilt.Fragments.SeasonalFragment;
import com.clarkgarrett.solartilt.Listeners.FragmentCallback;
import com.clarkgarrett.solartilt.R;
import com.clarkgarrett.solartilt.Utility;

public class SolarTiltActivity extends FragmentActivity implements FragmentCallback {

	private FragmentManager fm = getSupportFragmentManager();
	private String TAG = "## My Info ##";
	private DateFragment mDate_Fragment;
	private DateEditFragment mDateEdit_Fragment;
	private SeasonalFragment mSeason_Fragment;
	private SeasonalEditFragment mSeasonalEdit_Fragment;
	private DataSingleton mSolarTiltData = DataSingleton.get();
	private int PERMISSIONS_FLAG = 1;
	private final String DATE_FRAGMENT_TAG = "DFT";
	private final String SEASONAL_FRAGMENT_TAG = "SFT";
	private final String DATE_EDIT_FRAGMENT_TAG = "DEFT";
	private final String SEASONAL_EDIT_FRAGMENT_TAG = "SEFT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solar_tilt);
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

		if (fm.findFragmentById(R.id.fragmentContainer) == null) {
			mDate_Fragment = new DateFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, mDate_Fragment, DATE_FRAGMENT_TAG).commit();
			mSolarTiltData.mFragmentShown = true;
		}
	}

	@Override
	public void fragmentMessage(int id) {
		switch (id) {
			case Utility.START_SEASONAL_FRAGMENT:
				if (mSeason_Fragment == null) {
					mSeason_Fragment = new SeasonalFragment();
				}
				fm.beginTransaction().replace(R.id.fragmentContainer, mSeason_Fragment, SEASONAL_FRAGMENT_TAG).addToBackStack("").commit();
				return;
			case Utility.START_SEASONAL_EDIT_FRAGMENT:
				if (mSeasonalEdit_Fragment == null) {
					mSeasonalEdit_Fragment = new SeasonalEditFragment();
				}
				fm.beginTransaction().replace(R.id.fragmentContainer, mSeasonalEdit_Fragment, SEASONAL_EDIT_FRAGMENT_TAG).addToBackStack("").commit();
				return;
			case Utility.START_DATE_EDIT_FRAGMENT:
				if (mDateEdit_Fragment == null) {
					mDateEdit_Fragment = new DateEditFragment();
				}
				fm.beginTransaction().replace(R.id.fragmentContainer, mDateEdit_Fragment, DATE_EDIT_FRAGMENT_TAG).addToBackStack("").commit();
				return;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		//Required so that onRequestPermissionsResult will be called in the fragments.
		super.onRequestPermissionsResult(requestCode,permissions,grantResults);
	}

}
