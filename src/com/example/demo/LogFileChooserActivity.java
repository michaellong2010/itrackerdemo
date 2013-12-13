package com.example.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import ar.com.daidalos.afiledialog.FileChooserActivity;

/*20131213 added by michael
 * List iTracker meta data log files in folder /sdcard/iTracker/*.txt */
public class LogFileChooserActivity extends FileChooserActivity {
	public final String Tag = "LogFileChooserActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	this.setTheme(R.style.AppTheme_title);
    	Log.d(Tag, Boolean.toString(getWindow().requestFeature(Window.FEATURE_ACTION_BAR)));
    	Log.d(Tag, Boolean.toString(getWindow().requestFeature(Window.FEATURE_NO_TITLE)));
    	super.onCreate(savedInstanceState);
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
