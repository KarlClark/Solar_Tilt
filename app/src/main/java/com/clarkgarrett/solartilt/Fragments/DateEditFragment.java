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

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateEditFragment extends Fragment implements TextWatcher, View.OnTouchListener, OnEditorActionListener{
	
	private TextView mTextView_Message;
	private EditText mEditText_MM, mEditText_DD, mEditText_Degrees,
	                 mEditText_Minutes, mEditText_Seconds;
	Button mButton_TiltAngle;
	private int mScreenSize;
	private boolean mLandscape;
	private DataSingleton mData;
	private boolean mFragmentStarted= false;
	private int[] mMonthLengths = new int[] {31,28,31,30,31,30,31,31,30,31,30,31};
	private InputMethodManager mImm;
	private static final String TAG = "## My Info ##";

	//The next three methods implement TextWatcher
	@Override
	public void afterTextChanged(Editable s) {
		// New text in any field makes current tilt angle 
		// invalid, so blank it out.
		mButton_TiltAngle.setText("");
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,int after){}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count){}
	
	//Implements View.OnTouchListener
	@Override
	public boolean onTouch(View v, MotionEvent me){
		/*
		 * User touched one of the EditText fields.
		 * Blank out Tilt angle since its value is
		 * no longer valid because of incomplete data.
		 * Blank out message because message is no longer
		 * applicable. Blank out the EditText field so 
		 * user can enter data (also displays the hint).
		 * Put focus on EditText field so user is ready
		 * to type.
		*/
		mButton_TiltAngle.setText("");
		mTextView_Message.setText("");
		((EditText)v).setText("");
		((EditText)v).requestFocus();
		return false;
	}

	//Implements OnEditorActionListener
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent key){
		if(actionId == EditorInfo.IME_ACTION_DONE  || actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
			// The user pressed the Done key.
			checkAndCalculate();
			if (mTextView_Message.length() > 0  &&
				(mScreenSize == Configuration.SCREENLAYOUT_SIZE_SMALL || 
				(mScreenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && mLandscape))){
				// Lower soft keyboard to make all fields visible.
				mImm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
	public void onResume(){
		super.onResume();
		mFragmentStarted=false;
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mData.mDateEditFragmentStarted=false;
	}
	
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup parent,Bundle savedInstance) {
		View v = inflater.inflate(R.layout.fragment_solar_tilt_date_edit, parent,false);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		mScreenSize = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
		Display d = getActivity().getWindowManager().getDefaultDisplay();
		mLandscape = d.getWidth() > d.getHeight();
		
		mData= DataSingleton.get();
		
		mTextView_Message =(TextView)v.findViewById(R.id.textViewDateMessageEdit);
		mEditText_MM =(EditText)v.findViewById(R.id.editTextMM);
		mEditText_DD =(EditText)v.findViewById(R.id.editTextDD);
		mEditText_Degrees =(EditText)v.findViewById(R.id.editTextDateDegrees);
		mEditText_Minutes =(EditText)v.findViewById(R.id.editTextDateMinutes);
		mEditText_Seconds =(EditText)v.findViewById(R.id.editTextDateSeconds);
		mButton_TiltAngle=(Button)v.findViewById(R.id.ButtonTiltAngleDateEdit);
		
		wireUpListeners();
		
		return v;
	}
	
	@Override 
	public void onViewStateRestored(Bundle savedInstanceState){
		super.onViewStateRestored(savedInstanceState);
		double latitude;
		if(! mData.mDateEditFragmentStarted){
			// Entered fragment from date fragment.
			if (mData.mLocation ==null){
				latitude=0.0;
			}
			else{
				latitude=mData.mLocation.getLatitude();
			}
			Utility.setDate(mEditText_MM, mEditText_DD);
			Utility.setLatitude(mEditText_Degrees, mEditText_Minutes, mEditText_Seconds, latitude);
			Utility.calculateTiltAngle(mEditText_MM, mEditText_DD, latitude, mButton_TiltAngle);
			mTextView_Message.setText(R.string.levelingTool);
		}
		else{
			// Entered fragment by returning from leveling tool fragment.
			if(Utility.latitudeIsOk(mEditText_Degrees, mEditText_Minutes, mEditText_Seconds, mTextView_Message)){
				latitude = Utility.convertLatitude(mEditText_Degrees, mEditText_Minutes, mEditText_Seconds);
				Utility.calculateTiltAngle(mEditText_MM, mEditText_DD, latitude, mButton_TiltAngle);
				mTextView_Message.setText(R.string.levelingTool);
			}
		}
		//mTextView_Message.setText(mData.mDateEditMessage);
		mEditText_MM.requestFocus();
		mEditText_MM.setSelection(mEditText_MM.getText().length());
		mImm=(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
		mImm.showSoftInput(mEditText_MM, 0);
		mData.mDateEditFragmentStarted=true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// There is only one possible MenuItem, the icon button.
		// So go back to date fragment.
		//getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mImm.hideSoftInputFromWindow(mEditText_MM.getWindowToken(), 0);
		getActivity().onBackPressed();
		return true;
	}
	
	//Check our data and if no errors calculate tilt angle.
	private void checkAndCalculate(){
		int month;

		if (mEditText_MM.getText().length() ==0  ||  (month= Integer.parseInt(mEditText_MM.getText().toString())) > 12 || month==0){
			mTextView_Message.setText(R.string.badMonth);  //month view is blank or month value is not 1 to 12.
			return;
		}
		
		if (mEditText_DD.getText().length() ==0){   //Day view is blank.
			mTextView_Message.setText(R.string.badDay);
			return;
		}

		int day= Integer.parseInt(mEditText_DD.getText().toString());  // Check if day
		Calendar c = Calendar.getInstance();  // value is valid for the month value.
		GregorianCalendar gc = new GregorianCalendar();
		if (day > mMonthLengths[month-1] && ! (month==2 && day==29 && gc.isLeapYear(c.get(Calendar.YEAR)))){
			mTextView_Message.setText(R.string.badDay);
			return;
		}

		if (Utility.latitudeIsOk(mEditText_Degrees, mEditText_Minutes, mEditText_Seconds, mTextView_Message)){
			double latitude = Utility.convertLatitude(mEditText_Degrees, mEditText_Minutes, mEditText_Seconds);
			Utility.calculateTiltAngle(mEditText_MM, mEditText_DD, latitude, mButton_TiltAngle);
			mTextView_Message.setText(R.string.levelingTool);
		}
	}
	
	private void wireUpListeners(){
		
		mEditText_MM.setOnTouchListener(this);
		mEditText_DD.setOnTouchListener(this);
		mEditText_Degrees.setOnTouchListener(this);
		mEditText_Minutes.setOnTouchListener(this);
		mEditText_Seconds.setOnTouchListener(this);
		
		mEditText_MM.addTextChangedListener(this);
		mEditText_DD.addTextChangedListener(this);
		mEditText_Degrees.addTextChangedListener(this);
		mEditText_Minutes.addTextChangedListener(this);
		mEditText_Seconds.addTextChangedListener(this);
		
		mEditText_MM.setOnEditorActionListener(this);
		mEditText_DD.setOnEditorActionListener(this);
		mEditText_Degrees.setOnEditorActionListener(this);
		mEditText_Minutes.setOnEditorActionListener(this);
		mEditText_Seconds.setOnEditorActionListener(this);
		
		mButton_TiltAngle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				Utility.startAngleLevelActivity(getActivity(), mButton_TiltAngle, mFragmentStarted, mTextView_Message);
			}
		});
	}

}
