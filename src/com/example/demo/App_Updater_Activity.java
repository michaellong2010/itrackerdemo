package com.example.demo;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;

public class App_Updater_Activity extends Activity {
	static String Tag = "com.example.demo.App_Updater_Activity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		String apk_filename;
		apk_filename = this.getIntent().getExtras().getString("Apk_filename");
		intent.setDataAndType(Uri.fromFile(new File(apk_filename)), "application/vnd.android.package-archive");
		// Intent intent = new Intent(Intent.ACTION_DELETE,
		// Uri.fromParts("package",
		// Auto_updater_test.this.getPackageName(), null));
		//startActivity(intent);
		//Log.d(Tag, "start activity process thread id" + Integer.toString(Process.myTid()));
		startActivity(intent);
	}
	
}
