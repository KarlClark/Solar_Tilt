package com.clarkgarrett.solartilt;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class SolarTiltAngleLevelActivity extends FragmentActivity {
	private FragmentManager fm=getSupportFragmentManager();
	private WakeLock mWakeLock;
	private static final String TAG2="## My Info2 ##";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		float angle = getIntent().getFloatExtra("angle", 0f);
		setContentView(R.layout.activity_solar_tilt);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "tag");
		
		fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if (fragment == null){
			fragment= SolarTiltFragmentAngleLevel.newInstance(angle);
			fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		mWakeLock.acquire();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		if(mWakeLock.isHeld()){
			mWakeLock.release();
		}
	}
	
}
