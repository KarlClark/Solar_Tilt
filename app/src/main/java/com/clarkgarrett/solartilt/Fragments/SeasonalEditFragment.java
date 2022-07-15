package com.clarkgarrett.solartilt.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import androidx.fragment.app.Fragment;

import com.clarkgarrett.solartilt.DataSingleton;
import com.clarkgarrett.solartilt.R;
import com.clarkgarrett.solartilt.Utility;

public class SeasonalEditFragment extends Fragment implements TextWatcher, View.OnTouchListener, OnEditorActionListener{
	
	private View mView;
	private Button  mSummer_Button , mWinter_Button , mSpring_Button , mFall_Button;
	private EditText  mDegrees_EditText , mMinutes_EditText , mSeconds_EditText;
	private TextView mMessage_TextView;
	private DataSingleton mData;
	private boolean mFragmentStarted;
	private InputMethodManager mImm;
	private boolean mLandscape;
	private int mScreenSize;
	private static final String TAG ="## My Info ##";
	
	// The next three methods implement TextWatcher
	@Override
	public void afterTextChanged(Editable s) {
		blankAngles();
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,int after){}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count){}
	
	// Implements View.OnTouchListener
	@Override
	public boolean onTouch(View v, MotionEvent me){
		blankAngles();
		((EditText)v).setText("");
		return false;
	}
	
	// Implements OnEditorActionListener
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent ke){
		if(actionId == EditorInfo.IME_ACTION_DONE  || actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
			if (Utility.latitudeIsOk(mDegrees_EditText, mMinutes_EditText, mSeconds_EditText, mMessage_TextView)){
				double latitude = Utility.convertLatitude(mDegrees_EditText, mMinutes_EditText, mSeconds_EditText);
				Utility.calculateAngles(mSpring_Button, mSummer_Button, mFall_Button, mWinter_Button, latitude);
				mMessage_TextView.setText(R.string.levelingTool);
			}else{
				if ((mScreenSize == Configuration.SCREENLAYOUT_SIZE_SMALL || 
						(mScreenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && mLandscape))){
						mImm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//Retain the fragment across activity recreation.
		setRetainInstance(true);
		setHasOptionsMenu(true);   //We will be using the icon button as an alternative back button
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mData.mSeasonalEditFragmentStarted=false;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mFragmentStarted=false;
	}
	
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup parent,Bundle savedInstance) {
		mView = inflater.inflate(R.layout.fragment_solar_tilt_seasonal_edit, parent,false);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		mScreenSize = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
		Display d = getActivity().getWindowManager().getDefaultDisplay();
		mLandscape = d.getWidth() > d.getHeight();
		
		mData= DataSingleton.get();
		
		getIDs();
		wireUpListeners();
		return mView;
	}
	
	private void getIDs(){
		mSummer_Button = (Button)mView.findViewById(R.id.Button_SummerEdit);
		mWinter_Button = (Button)mView.findViewById(R.id.Button_WinterEdit);
		mSpring_Button = (Button)mView.findViewById(R.id.Button_SpringEdit);
		mFall_Button = (Button)mView.findViewById(R.id.Button_FallEdit);
		mMessage_TextView =(TextView)mView.findViewById(R.id.textView_SeasonalMessageEdit);
		mDegrees_EditText=(EditText)mView.findViewById(R.id.editTextSeasonalDegrees);
		mMinutes_EditText=(EditText)mView.findViewById(R.id.editTextSeasonalMinutes);
		mSeconds_EditText=(EditText)mView.findViewById(R.id.editTextSeasonalSeconds);
	}
	
	@Override 
	public void onViewStateRestored(Bundle savedInstanceState){
		super.onViewStateRestored(savedInstanceState);
		double latitude;
		if( ! mData.mSeasonalEditFragmentStarted){
			
			//Entered fragment from seasonal fragment.
			if (mData.mLocation ==null){
				latitude=0.0;
			}
			else{
				latitude=mData.mLocation.getLatitude();
			}
			Utility.setLatitude(mDegrees_EditText, mMinutes_EditText, mSeconds_EditText, latitude);
			Utility.calculateAngles(mSpring_Button, mSummer_Button, mFall_Button, mWinter_Button, latitude);
			mMessage_TextView.setText(R.string.levelingTool);
		}
		else{
			//Entered fragment by returning from leveling tool fragment.
			if (Utility.latitudeIsOk(mDegrees_EditText, mMinutes_EditText, mSeconds_EditText, mMessage_TextView)){
				latitude= Utility.convertLatitude(mDegrees_EditText, mMinutes_EditText, mSeconds_EditText);
				Utility.calculateAngles(mSpring_Button, mSummer_Button, mFall_Button, mWinter_Button, latitude);
				mMessage_TextView.setText(R.string.levelingTool);
			}
		}
		//mMessage_TextView.setText(mData.mSeasonalEditMessage);
		mDegrees_EditText.requestFocus();
		mDegrees_EditText.setSelection(mDegrees_EditText.getText().length());
		mImm=(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
		mImm.showSoftInput(mDegrees_EditText, 0);
		mData.mSeasonalEditFragmentStarted=true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// There is only one possible MenuItem, the icon button.
		// So go back to date fragment.
		mImm.hideSoftInputFromWindow(mDegrees_EditText.getWindowToken(), 0);
		getActivity().onBackPressed();
		return true;
	}
	
	private void blankAngles(){
		mSpring_Button.setText("");
		mSummer_Button.setText("");
		mFall_Button.setText("");
		mWinter_Button.setText("");
		mMessage_TextView.setText("");
	}
	
	private void wireUpListeners(){
		
		mDegrees_EditText.setOnTouchListener(this);
		mMinutes_EditText.setOnTouchListener(this);
		mSeconds_EditText.setOnTouchListener(this);
		
		mDegrees_EditText.addTextChangedListener(this);
		mMinutes_EditText.addTextChangedListener(this);
		mSeconds_EditText.addTextChangedListener(this);
		
		mDegrees_EditText.setOnEditorActionListener(this);
		mMinutes_EditText.setOnEditorActionListener(this);
		mSeconds_EditText.setOnEditorActionListener(this);
		
		mSpring_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				Utility.startAngleLevelActivity(getActivity(), mSpring_Button, mFragmentStarted, mMessage_TextView);
			}
		});
		
		mSummer_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				Utility.startAngleLevelActivity(getActivity(), mSummer_Button, mFragmentStarted, mMessage_TextView);
			}
		});
		
		mFall_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				Utility.startAngleLevelActivity(getActivity(), mFall_Button, mFragmentStarted, mMessage_TextView);
			}
		});
		
		mWinter_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				Utility.startAngleLevelActivity(getActivity(), mWinter_Button, mFragmentStarted, mMessage_TextView);
			}
		});
	}

}
