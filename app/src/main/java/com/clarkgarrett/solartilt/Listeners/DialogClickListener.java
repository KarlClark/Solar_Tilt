package com.clarkgarrett.solartilt.Listeners;
// This interface is implemented by Solar_Tilt_Date_Fragment, which is
// the fragment that starts Solar_Tilt_Dialog_Fragment.  The dialog then
// calls back through it's target fragment.
public interface DialogClickListener {
	public void onYesClick();
	public void onNoClick();
}
