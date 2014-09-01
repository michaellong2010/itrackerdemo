package com.example.demo;

import java.io.File;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import ar.com.daidalos.afiledialog.FileChooserActivity;
import ar.com.daidalos.afiledialog.FileChooserCore;

/*20131213 added by michael
 * List iTracker meta data log files in folder /sdcard/iTracker/*.txt */
public class LogFileChooserActivity extends FileChooserActivity {
	public final String Tag = "LogFileChooserActivity";
	
	/**20140819 added by michael
	 * setting child activity orientation
	 */
	public static final String INPUT_ACTIVITY_ORIENTATION = "input_activity_orientation";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	Log.d(Tag, Boolean.toString(getWindow().hasFeature(Window.FEATURE_ACTION_BAR)));
    	Log.d(Tag, Boolean.toString(getWindow().hasFeature((Window.FEATURE_NO_TITLE))));
    	super.onCreate(savedInstanceState);
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	
    	/*20140819 added by michael
    	 * set the activity orientation*/
    	Bundle extras = this.getIntent().getExtras();
    	if (extras.containsKey(INPUT_ACTIVITY_ORIENTATION)) {
    		if (getRequestedOrientation() != extras.getInt(INPUT_ACTIVITY_ORIENTATION))
    			setRequestedOrientation(extras.getInt(INPUT_ACTIVITY_ORIENTATION));
    	}
    	
    	ActionBar abr;
    	abr = this.getActionBar();
    	//abr.setTitle("knight");
    	//Log.d(Tag, (String) abr.getTitle());
    	
        /*20131214 added by michael
         * register file select listener */
    	
    	getFileChooserCore().addListener(new FileChooserCore.OnFileSelectedListener() {

			@Override
			public void onFileSelected(File file) {
				// TODO Auto-generated method stub
				//select a log file then open the txt content
				Intent intent;
				intent = new Intent(LogFileChooserActivity.this, LogFileDisplayActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra(OUTPUT_FILE_OBJECT, file);
				startActivity(intent);
			}

			@Override
			public void onFileSelected(File folder, String name) {
				// TODO Auto-generated method stub
				//create a new file
			}
        	
        });
        
    }
    
	@Override
    protected void onDestroy() {
		super.onDestroy();
	}
}
