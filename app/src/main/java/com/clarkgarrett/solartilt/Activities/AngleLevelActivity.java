package com.clarkgarrett.solartilt.Activities;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.clarkgarrett.solartilt.Fragments.AngleLevelFragment;
import com.clarkgarrett.solartilt.R;

public class AngleLevelActivity extends FragmentActivity {
	private FragmentManager fm=getSupportFragmentManager();

	//private FragmentManager getSupportFragmentManager() {
	//}

	private WakeLock mWakeLock;
	private static final String TAG2="## My Info2 ##";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		float angle = getIntent().getFloatExtra("angle", 0f);
		setContentView(R.layout.activity_solar_tilt);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "tag:tag");
		
		fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if (fragment == null){
			fragment= AngleLevelFragment.newInstance(angle);
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
