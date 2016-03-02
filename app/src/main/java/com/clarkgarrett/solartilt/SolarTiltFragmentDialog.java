package com.clarkgarrett.solartilt;
/*
 * This dialog fragment displays a dialog asking the user if he wants
 * to turn on the GPS.  When we get the answer, we pass it back to our
 * target fragment through a basic callback methodology. 
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class SolarTiltFragmentDialog extends DialogFragment {
	private 
	DialogClickListener callback;
	String TAG="## My Info ##";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		callback=(DialogClickListener) getTargetFragment();  // target fragment implements DialogClickListener
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		
		View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog,null);
		TextView statement = (TextView)v.findViewById(R.id.textView_statement);
		TextView question = (TextView)v.findViewById(R.id.textView_question);
		statement.setText(R.string.GPSoff);
		question.setText(R.string.turn_on_GPS);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setView(v);
		
		alertDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which){
				callback.onYesClick();  //Pass yes answer back to target fragment.
			}
		});
		
		alertDialogBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which){
				callback.onNoClick();  //Pass no answer back to target fragment.
			}
		});
		
		return alertDialogBuilder.create();
	}
}
