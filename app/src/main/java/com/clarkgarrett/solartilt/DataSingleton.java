package com.clarkgarrett.solartilt;

import android.location.Location;

//This is singleton class for storing some data that needs to survive
//activity recreation.
public class DataSingleton {
	
	private static DataSingleton sSLD;
	public boolean mDialogShown,mDialogShowing,mYesClicked;
	public boolean mDateLatitudeWasShown=false;
	public boolean mSeasonalLatitudeWasShown=false;
	public boolean mDateEditFragmentStarted=false;
	public boolean mSeasonalEditFragmentStarted= false;
	public boolean mWaitingForDateGps = false;
	public boolean mWaitingForSeasonalGps=false;
	public boolean mFragmentShown = false;
	
	public Location mLocation;
	
	private DataSingleton() {};
	
	public static DataSingleton get() {
		if (sSLD == null) {
			sSLD = new DataSingleton();
		}
		return sSLD;
	}
}

	