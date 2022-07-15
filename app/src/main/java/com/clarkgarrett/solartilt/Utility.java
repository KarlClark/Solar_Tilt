package com.clarkgarrett.solartilt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.clarkgarrett.solartilt.Activities.AngleLevelActivity;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

public class Utility {

	private static String privacyPolicyString = null;
	public static final int START_DATE_EDIT_FRAGMENT = 1;
	public static final int START_SEASONAL_FRAGMENT = 2;
	public static final int START_SEASONAL_EDIT_FRAGMENT = 3;
	public static final double INVALID_LATITUDE =1000;
	private static final String TAG = "## My Info ##";

	public static AlertDialog getAlertDialog(final Context context, final TextView view){
		final DataSingleton mData = DataSingleton.get();

		TextView myMsg = new TextView(context);
		myMsg.setText(R.string.gpsoff);
		myMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
		myMsg.setGravity(Gravity.CENTER);
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
		myMsg.setPadding(px,px,px,px);

		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab
			.setCancelable(false)
			.setView(myMsg)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mData.mYesClicked =true;
					mData.mDialogShowing=false;
					context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mData.mDialogShowing=false;
					view.setText(R.string.GPSnotOn);
				}
			});

		return ab.create();
	}
	
	public static int getDaysFromEquinox(int mm, int dd, double latitude) {
		Calendar equinoxDate = Calendar.getInstance();
		Calendar ourDate = Calendar.getInstance();
		int year = ourDate.get(Calendar.YEAR);
		equinoxDate.clear();
		ourDate.clear();//negative latitude, southern hemisphere, spring is in September
		if (latitude < 0){  
			equinoxDate.set(year, Calendar.SEPTEMBER,21);
		}
		else{
			equinoxDate.set(year,Calendar.MARCH,21);
		}
		ourDate.set(year, mm-1, dd); //Month is 0 relative
		if (ourDate.getTimeInMillis() < equinoxDate.getTimeInMillis()){
			equinoxDate.set(Calendar.YEAR, year-1);  //equinox date was in the previous year.
		}
		return (int)((ourDate.getTimeInMillis() - equinoxDate.getTimeInMillis())/(24 * 60 *60 *1000));
	}
	
	public static void calculateTiltAngle(TextView textView_MM, TextView textView_DD , double latitude, Button button_TiltAngle){
			
			if (textView_MM.getText().length() == 0  || textView_DD.getText().length() == 0  ||  latitude == INVALID_LATITUDE){
				button_TiltAngle.setText("");
                button_TiltAngle.setTag(null);
				return;
			}
			
			double angle1Degrees = (getDaysFromEquinox(Integer.parseInt(textView_MM.getText().toString()),
					                Integer.parseInt(textView_DD.getText().toString()) , latitude)/365.25)*360;
			double angle1Radians = (angle1Degrees * 2 * Math.PI)/360;
			double tiltAngle = 23.5 * Math.sin(angle1Radians);
			DecimalFormat round1 = new DecimalFormat("#.#");
			button_TiltAngle.setText("" + round1.format(Math.abs(latitude) - tiltAngle) + "\u00b0");
            button_TiltAngle.setTag(Math.abs(latitude)-tiltAngle);
		}
	
	public static void setDate(TextView textView_MM, TextView textView_DD){
		//Get current date and fill in month and day views.
		Date date=new Date();
		textView_MM.setText(DateFormat.format("MM",date).toString());
		textView_DD.setText(DateFormat.format("dd", date).toString());
	}
	
	public static void calculateAngles(Button springB, Button summerB, Button fallB, Button winterB, double latitude){
		if(latitude == INVALID_LATITUDE){
			springB.setText("");
            springB.setTag(null);

			summerB.setText("");
            summerB.setTag(null);

			fallB.setText("");
            fallB.setTag(null);

			winterB.setText("");
            winterB.setTag(null);
			return;
		}
		DecimalFormat mRound1 = new DecimalFormat("#.#");
		double alat=Math.abs(latitude);
        double tiltAngle;

        tiltAngle=0.98*alat-2.3;
		springB.setText(""+mRound1.format(tiltAngle) + "\u00b0");
        springB.setTag(tiltAngle);

        tiltAngle= 0.92*alat-24.3;
		summerB.setText(""+mRound1.format(tiltAngle) + "\u00b0");
        summerB.setTag(tiltAngle);

        tiltAngle=0.98*alat-2.3;
		fallB.setText(""+mRound1.format(tiltAngle) + "\u00b0");
        fallB.setTag(tiltAngle);

        tiltAngle=0.89*alat+24;
		winterB.setText(""+mRound1.format(tiltAngle) + "\u00b0");
        winterB.setTag(tiltAngle);
	}
	
	public static void setLatitude(TextView degreesTv, TextView minutesTv, TextView secondsTv, double latitude ){
		
		String degMinSec = Location.convert(latitude, Location.FORMAT_SECONDS);
		int index1 = degMinSec.indexOf(":");
		int index2 = degMinSec.lastIndexOf(":");
		int index3 = degMinSec.indexOf(".");
        if(index3 == -1){
            index3=degMinSec.indexOf(",");
        }
		if (index3 == -1){
			index3=degMinSec.length();
		}
		degreesTv.setText(degMinSec.substring(0,index1));
		minutesTv.setText(degMinSec.substring(index1+1,index2));
		secondsTv.setText(degMinSec.substring(index2 + 1, index3));
	}
	
	public static double convertLatitude(TextView degreesEt, TextView minutesEt, TextView secondsEt) {
		String degminsec = degreesEt.getText().toString() + ":" +
	                       minutesEt.getText().toString() + ":" +
	                       secondsEt.getText().toString();
		double loc;
		try {
			loc = Location.convert(degminsec);
		}catch(IllegalArgumentException e){
			IllegalArgumentException iae = new IllegalArgumentException("Attempt to pass #" + degminsec +"# to Location.convert().  " + e.getMessage());
			iae.setStackTrace(e.getStackTrace());
			throw iae;
		}
		return loc;
	}
	
	public static boolean latitudeIsOk(EditText degreesEt, EditText minutesEt, EditText secondsEt, TextView messageTv){
        String intString;

		messageTv.setText("");
		if (degreesEt.getText().length() == 0) {
			messageTv.setText(R.string.blankDegrees);
			return false;
		}
		
		if (minutesEt.getText().length() == 0) {
			messageTv.setText(R.string.blankMinutes);
			return false;
		}
		
		if (secondsEt.getText().length() == 0) {
			messageTv.setText(R.string.blankSeconds);
			return false;
		}

        intString = degreesEt.getText().toString();
		Log.i(TAG,"inString=#" + intString +"#" +"  Matches= " + intString.matches("-?[0-9]+"));
		if (! intString.matches("-?[0-9]+")){
			messageTv.setText(R.string.badDegrees);
			return false;
		}
		int degrees = 0;
		try {
			degrees = Integer.parseInt(intString);
		}catch(NumberFormatException e){
			messageTv.setText(R.string.badDegrees);
			return false;
		}
		if (degrees < -90 || degrees > 90){
			messageTv.setText(R.string.badDegrees);
			return false;
		}

        intString= minutesEt.getText().toString();
		if (! intString.matches("[0-9]+")){
			messageTv.setText(R.string.badMinutes);
			return false;
		}
		int minutes = 0;
		try {
			minutes = Integer.parseInt(intString);
		}catch(NumberFormatException e){
			messageTv.setText(R.string.badMinutes);
			return false;
		}
		if (minutes < 0 || minutes > 59){
			messageTv.setText(R.string.badMinutes);
			return false;
		}

        intString=secondsEt.getText().toString();
		if (! intString.matches("[0-9]+")){
			messageTv.setText(R.string.badSeconds);
			return false;
		}
		int seconds = 0;
		try {
			seconds = Integer.parseInt(intString);
		}catch(NumberFormatException e){
			messageTv.setText(R.string.badSeconds);
			return false;
		}
		if (seconds < 0 || seconds > 59){
			messageTv.setText(R.string.badSeconds);
			return false;
		}
		
		double lat = convertLatitude(degreesEt, minutesEt, secondsEt);
		if (lat < -90.0 || lat > 90.0){
			messageTv.setText(R.string.badLatitude);
			return false;
		}
		
		return true;
	}
	
	public static void blankLatitude(TextView degreesTv, TextView minutesTv, TextView secondsTv){
		degreesTv.setText("");
		minutesTv.setText("");
		secondsTv.setText("");
	}
	
	public static void startAngleLevelActivity(Context context, Button tiltAngle, boolean mFragmentStarted, TextView messageTv){
		if (tiltAngle.getText().length() == 0){
			messageTv.setText(R.string.badTiltAngle);
			return;
		}
		if (! mFragmentStarted) {
			 Intent i = new Intent(context, AngleLevelActivity.class);
             double  dAngle = (Double)tiltAngle.getTag();
             float angle = (float)dAngle;
			 i.putExtra("angle", angle);
			 context.startActivity(i);
			mFragmentStarted=true;
		}
	}
}
