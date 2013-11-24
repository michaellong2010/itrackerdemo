package com.example.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class I_Tacker_Receiver extends BroadcastReceiver {
	Toast mToast1;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (I_Tacker_Activity.mDebug_Itracker==true)
		  Toast.makeText(context, "knight2001", Toast.LENGTH_LONG).show();

	}

}
