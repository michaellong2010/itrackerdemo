package com.example.demo;

import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo.I_Tracker_Device.CMD_T;

public class I_Tacker_Activity extends Activity implements OnCheckedChangeListener, OnTouchListener {
	
	FrameLayout mLayout_Conten;
	//Calendar c = Calendar.getInstance();
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Handler  mHandler;

	public final String Tag = "I_Tracker_Activity";
	Timer    mTimer;
	TextView mTmpTextView;
	Button tmp_Button;
	RadioButton tmp_Radio_button;
	RadioGroup myRadiogroup;
	int Well_Selection;

/*20130308 added by michael
 contextual action mode & contextual menu*/
	ActionMode mMode;
	ActionMode.Callback mCallback;
	OnLongClickListener listener = new OnLongClickListener() {

		/*
		 * 20130311 added by michael check the touch coordinate the touch
		 * coordinate can be retrieved from MotionEvent so we need to listen the
		 * touch event
		 */
		@Override
		public boolean onLongClick(View v) {
			I_Tracker_Well_Plate_View v1 = (I_Tracker_Well_Plate_View) v;

			if (mTouchPositionX < v1.mMaxTouchablePosX
					&& mTouchPositionY < v1.mMaxTouchablePosY) {
				if (mMode != null)
					return false;
				else {
					mMode = startActionMode(mCallback);
					v.setSelected(true);
				}
			}
			return true;
		}
	};
	OnClickListener listener1 = new OnClickListener() {
        /*20130322 added by michael*/
		//click the display region, show the current connection of device
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if ((mItrackerState & (1 << Itracker_State_isRunning))==1) {
				Show_Toast_Msg(I_Tracker_Device_Running);
			}
			else {
				Show_Toast_Msg(I_Tracker_Device_Stop);
			}
			//
			//
		}	
	};
	
    public enum Touch_State {
    	IDLE, TOUCH, PINCH, 
    };
    Touch_State mTouch_state;
    float mTouchPositionX, mTouchPositionY;
/* Action mode menu item enable/disable state bit mask */
    public static final int Itracker_MI_MASK = 0x1f;
    public static final int Itracker_MI_Start = 0;
    public static final int Itracker_MI_Pause = 1;
    public static final int Itracker_MI_Stop = 2;
    public static final int Itracker_MI_Previos_Tran = 3;
    public static final int Itracker_MI_Next_Tran = 4;
    int Itracker_MI_State = 1 << Itracker_MI_Start;
/*20130312 added by michael*/
//indicate if the task in back stack
    int mReentrance;
    I_Tracker_Device mItracker_dev;
    //private AdbDevice mAdbDevice;
/*20130313 added by michael*/
//Create a broadcast receiver
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
				//UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (mItracker_dev != null && mItracker_dev.getDevice() != null) {
					if (device.getProductId() == mItracker_dev.getDevice().getProductId() && device.getVendorId() == mItracker_dev.getDevice().getVendorId()) {
						mItracker_dev.DeviceOffline();

						Itracker_MI_State = 0;
						UpdateActionMenuItem();
						Log.d(Tag, "onReceive process thread id" + Integer.toString(Process.myTid()));
						mItracker_dev.show_debug(Tag+"onReceive process thread id" + Integer.toString(Process.myTid())+"\n");
						timerTaskPause();
						Show_Toast_Msg(I_Tracker_Device_DisCon);
					}
				}
			}
			else {
				if (action.equals(ACTION_USB_PERMISSION)) {
					//UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							// call method to set up device communication
							mItracker_dev.show_debug(Tag+"permission allowed for device "+device+"\n");
							Itracker_MI_State = 1 << Itracker_MI_Start;
							Show_Toast_Msg(I_Tracker_Device_Conn);
							//UpdateActionMenuItem();
						}
					} else {
						Log.d(Tag, "permission denied for device " + device);
						mItracker_dev.show_debug(Tag+"permission denied for device "+device+"\n");
						Itracker_MI_State = 0;
						//UpdateActionMenuItem();
					}
					UpdateActionMenuItem();
					
					/*20130320 added by michael*/
					if (mRequest_USB_permission==true) {
						hide_system_bar();
						mRequest_USB_permission = false;
					}
				}
			}
		}
    	
    };
 /*20130313 added by michael*/
 //state flag for current i-tracker transaction
    public static final int Itracker_State_isRunning = 0;
    public static final int Itracker_State_isForwardable = 1;
    public static final int Itracker_State_isBackable = 2;
    public static int mItrackerState = 0;
/*20130317 added by michael*/    
    private static final String ACTION_USB_PERMISSION = "com.example.demo.USB_PERMISSION";
    UsbManager mUsbManager;
    PendingIntent mPermissionIntent;
	TheTimerTask timertask;
/*20130318 added by michael*/
	I_Tracker_Well_Plate_View Well_View;
	public int UI_invalid = 0;

/*20130319 added by michael*/
	Intent SystemUI_intent = new Intent();
	WallpaperManager mOrigWallpaper;
	Drawable mOrigWallpaper_drawable;
	Bitmap mOrigWallpaper_bitmap;
	boolean mRequest_USB_permission;
	public static boolean mDebug_Itracker = false;
	public static int mWell_View_Display_Mode = 1;
/*20130320 added by michael*/
//back to well plate selection dialog
	public void Reset_App() {
		timerTaskPause();
		if ((mItrackerState & (1<<Itracker_State_isRunning)) != 0) {
			mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
			mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 1);
		}					
		/*20130318 added by michael*/
		//timer thread has stop, so below is UI thread running only
		mItracker_dev.reset();
		Well_View.ResetWell();
		//Well_View.invalidate();
		mItrackerState = 0;		
	}
	
	DialogInterface.OnClickListener listenerAccept = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			/*20130408 added by michael*/
			Itracker_MI_State = 1 << Itracker_MI_Start;
			Reset_App();
			mLayout_Conten.removeAllViews();
			mLayout_Conten.addView(myRadiogroup);

			//Toast.makeText(I_Tacker_Activity.this, "Great! Welcome.", Toast.LENGTH_SHORT).show();
		}
	};
	
	DialogInterface.OnClickListener listenerDoesNotAccept = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			//Toast.makeText(I_Tacker_Activity.this, "You can't use this app then.", Toast.LENGTH_SHORT).show();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTmpTextView = new TextView(this);
		mLayout_Conten = (FrameLayout) this.findViewById(android.R.id.content);
		mLayout_Conten.addView(mTmpTextView);
		//timerStart();
		mTimer = new Timer();
		mHandler= new Handler();
//disable android sleep mode to avoid when system awake up again will yield the USB attach event once again 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
/*20130306 added by michael
radio group to let user to choice well plate for i-tacker*/
		//Tag = getPackageName();
		setContentView(R.layout.radiogroup_well_selection);
		tmp_Radio_button = (RadioButton) findViewById(R.id.ID_radioButton1);
		tmp_Radio_button.setOnCheckedChangeListener(this);
		tmp_Radio_button = (RadioButton) findViewById(R.id.ID_radioButton2);
		tmp_Radio_button.setOnCheckedChangeListener(this);
		
		myRadiogroup = (RadioGroup) findViewById(R.id.ID_plate_selection_radiogroup);
		//(RadioGroup.LayoutParams)
		if (myRadiogroup.getCheckedRadioButtonId() == R.id.ID_radioButton1)
			Well_Selection = I_Tracker_Device.Well_96;
		else
			Well_Selection = I_Tracker_Device.Well_384;
		//this.getResources().getDrawable(id)
		tmp_Button = (Button) findViewById(R.id.ID_btn_enter_itracker);
		Drawable d = tmp_Button.getBackground();
		int resID = getResources().getIdentifier("btn_default", "drawable", "android");
		String resName = getResources().getResourceName(resID);
		Drawable d1 = getResources().getDrawable(resID);
		//tmp_Button.setBackgroundDrawable(d1);


		Well_View = new I_Tracker_Well_Plate_View(this, I_Tracker_Well_Plate_View.Wells_384);
		Well_View.setId(R.id.ID_well_plate_view);
		FrameLayout.LayoutParams lp =  new FrameLayout.LayoutParams(600, 800);
		Well_View.setLayoutParams(lp);
		mTmpTextView.setTextColor(getResources().getColor(android.R.color.holo_green_light));
		
		//mLayout_Conten.addView(Well_View);
		//mLayout_Conten.removeView(v);
		//20130308 added by michael
		 //*long click a view and display contextual button
		 
		Well_View.setOnLongClickListener(listener);
		Well_View.setOnClickListener(listener1);
		mTouch_state = Touch_State.IDLE;
		Well_View.setOnTouchListener(this);
		mCallback = new ActionMode.Callback() {
            int mMenu_item_state;

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				mode.setTitle("Demo");
				getMenuInflater().inflate(R.menu.menu1, menu);
//20130311 added by michael
// inflater create menu item instance from the xml menu resource 
				
				mMenu_item_state = Itracker_MI_State;
				Update_Menu_Item_Enable(menu, mMenu_item_state);
/*				menu.findItem(R.id.ID_MI_next_trans).setEnabled(false);
				menu.findItem(R.id.ID_MI_pause_itracker).setEnabled(false);
				menu.findItem(R.id.ID_MI_previous_trans).setEnabled(false);
				menu.findItem(R.id.ID_MI_stop_itracker).setEnabled(false);*/
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				mMenu_item_state = Itracker_MI_State;
				Update_Menu_Item_Enable(menu, mMenu_item_state);
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// TODO Auto-generated method stub
				switch (item.getItemId()) {
				case R.id.ID_MI_start_itracker:
					if (Connect_Itracker()) {
						mItrackerState |= 1 << Itracker_State_isRunning;
						mMenu_item_state ^= 1 << Itracker_MI_Start;
						mMenu_item_state ^= 1 << Itracker_MI_Pause;
						//mMenu_item_state ^= 1 << Itracker_MI_Stop;
						mMenu_item_state |= 1 << Itracker_MI_Stop;
						if ((mItrackerState & (1<<Itracker_State_isBackable)) != 0)
						mMenu_item_state |= 1 << Itracker_MI_Previos_Tran;
						
						if ((mItrackerState & (1<<Itracker_State_isForwardable)) != 0)
						mMenu_item_state |= 1 << Itracker_MI_Next_Tran;
						/*20130317 preparing polling device*/
						//create a new thread to receive the device data continuously
						//implement this task via timer & timertask, using timer implicitly run task in a new thread
/*						mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_SETTING, 1);
						mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
						mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_START, 1);
						mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");*/
						
						/*20130327 added by michael*/
						Well_View.set_focus_coord(mItracker_dev.get_focus_coord());
						timerTaskStart();
					}
					else {
//Device may be something wrong cause we can't send commands(HID_CMD_ITRACKER_SETTING¡BHID_CMD_ITRACKER_START)
						if (I_Tacker_Activity.mDebug_Itracker==true)
							Toast.makeText(getApplicationContext(), "Can't satrt Itracker device", Toast.LENGTH_LONG).show();
					}
					Show_Toast_Msg(I_Tracker_Device_Tracking_On);
					break;
				case R.id.ID_MI_pause_itracker:
//pause the current task
					mMenu_item_state ^= 1 << Itracker_MI_Start;
					mMenu_item_state ^= 1 << Itracker_MI_Pause;
					timerTaskPause();
					mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
					mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 1);
					mItrackerState &= ~(1 << Itracker_State_isRunning);
					Show_Toast_Msg(I_Tracker_Device_Tracking_Off);
					
					/*20130327 added by michael*/
					//Show_focus_coord==true then call I_Tracker_Well_Plate.blink_last_well() again
					if (Well_View.Show_focus_coord)
						Well_View.blink_last_well();
					break;
				case R.id.ID_MI_stop_itracker:
//ending the current task, clear device states & records, infos
					mMenu_item_state = 1 << Itracker_MI_Start;
					/*timerTaskPause();
					if ((mItrackerState & (1<<Itracker_State_isRunning)) != 0) {
						mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
						mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 1);
					}*/					
					/*20130318 added by michael*/
					//timer thread has stop, so below is UI thread running only
					/*mItracker_dev.reset();
					Well_View.ResetWell();
					//Well_View.invalidate();
					mItrackerState = 0;*/
                    Reset_App();
                    Show_Toast_Msg(I_Tracker_Device_Reset);

                    /*20130327 added by michael*/
					//Show_focus_coord==true then call I_Tracker_Well_Plate.blink_last_well() again
					if (Well_View.Show_focus_coord)
						Well_View.blink_last_well();
					break;
				case R.id.ID_MI_previous_trans:
					/*if (!isBackable())
						mMenu_item_state ^= 1 << Itracker_MI_Previos_Tran;*/
					synchronized (TheDelegatedTimerTask) {
						UI_invalid = mItracker_dev.go_backward();
						if (mItracker_dev.Backwardable==0) {
							mItrackerState &= ~(1 << Itracker_State_isBackable);
							Itracker_MI_State &= ~(1 << Itracker_MI_Previos_Tran);
						}
						if (mItracker_dev.Forwardable==1) {
							mItrackerState |= 1 << Itracker_State_isForwardable;
							Itracker_MI_State |= 1 << Itracker_MI_Next_Tran;
						}

						UpdateActionMenuItem();
						if (UI_invalid == 1) {
							Well_View.setWellColor(mItracker_dev.Valid_Coord_Histogram);
							Well_View.set_focus_coord(mItracker_dev.get_focus_coord());
							//Well_View.DrawBitmap();
							//Well_View.invalidate();
							//Toast mToastMsg = Toast.makeText(getApplicationContext(), "previous", Toast.LENGTH_LONG);
						}
					}
					break;
				case R.id.ID_MI_next_trans:
					/*if (!isForwardable())
						mMenu_item_state ^= 1 << Itracker_MI_Next_Tran;*/
					synchronized (TheDelegatedTimerTask) {
						UI_invalid = mItracker_dev.go_forward();
						if (mItracker_dev.Backwardable==1) {
							mItrackerState |= 1 << Itracker_State_isBackable;
							Itracker_MI_State |= 1 << Itracker_MI_Previos_Tran;
						}
						if (mItracker_dev.Forwardable==0) {
							mItrackerState &= ~(1 << Itracker_State_isForwardable);
							Itracker_MI_State &= ~(1 << Itracker_MI_Next_Tran);
						}

						UpdateActionMenuItem();
						if (UI_invalid == 1) {
							Well_View.setWellColor(mItracker_dev.Valid_Coord_Histogram);
							Well_View.set_focus_coord(mItracker_dev.get_focus_coord());
							//Well_View.DrawBitmap();
							//Well_View.invalidate();
						}
					}
					break;

				case R.id.ID_MI_well_selection:
					//mLayout_Conten.removeAllViews();
					//mLayout_Conten.addView(myRadiogroup);
					//setContentView(myRadiogroup);
					Builder builder = new AlertDialog.Builder(I_Tacker_Activity.this);
					AlertDialog dialog = builder.create();
					dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
					dialog.setTitle("Do you back to well plate selection?");
					dialog.setMessage("Warning: the current task will be terminated!");
					dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Yes", listenerAccept);
					dialog.setButton(DialogInterface.BUTTON_POSITIVE, "No", listenerDoesNotAccept);
					dialog.getWindow().setGravity(Gravity.TOP);
					dialog.setIcon(R.drawable.ic_launcher1);
					dialog.setCancelable(false);
					dialog.show();
					break;
					

				case R.id.ID_MI_exit_app:
					I_Tacker_Activity.this.finish();
					break;
					
				default:
					return false;
				}
				Itracker_MI_State = mMenu_item_state;
				mode.finish();
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub
				mMode = null;
				if (I_Tacker_Activity.mDebug_Itracker==true) {
					/*Toast mToastMsg = Toast.makeText(getApplicationContext(), "Exit Action Mode!", Toast.LENGTH_LONG);
				
					mToastMsg.setGravity(Gravity.LEFT | Gravity.TOP, 300,100);
					mToastMsg.show();*/
				}
			}

			public void Update_Menu_Item_Enable(Menu menu, int item_state) {
				item_state &= Itracker_MI_MASK;

				if ((item_state & (1<<Itracker_MI_Start))==(1<<Itracker_MI_Start))
					menu.findItem(R.id.ID_MI_start_itracker).setEnabled(true);
				else
					menu.findItem(R.id.ID_MI_start_itracker).setEnabled(false);

				if ((item_state & (1<<Itracker_MI_Stop))==(1<<Itracker_MI_Stop))
					menu.findItem(R.id.ID_MI_stop_itracker).setEnabled(true);
				else
					menu.findItem(R.id.ID_MI_stop_itracker).setEnabled(false);

				if ((item_state & (1<<Itracker_MI_Pause))==(1<<Itracker_MI_Pause))
					menu.findItem(R.id.ID_MI_pause_itracker).setEnabled(true);
				else
					menu.findItem(R.id.ID_MI_pause_itracker).setEnabled(false);

				if ((item_state & (1<<Itracker_MI_Previos_Tran))==(1<<Itracker_MI_Previos_Tran))
					menu.findItem(R.id.ID_MI_previous_trans).setEnabled(true);
				else
					menu.findItem(R.id.ID_MI_previous_trans).setEnabled(false);
				
				if ((item_state & (1<<Itracker_MI_Next_Tran))==(1<<Itracker_MI_Next_Tran))
					menu.findItem(R.id.ID_MI_next_trans).setEnabled(true);
				else
					menu.findItem(R.id.ID_MI_next_trans).setEnabled(false);	
			}
		};
	    //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		//ActionBar Abr = this.getActionBar();
		//this.setContentView(mLayout_Conten);
		mReentrance = 0;
		mItracker_dev = new I_Tracker_Device(this);

		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		
		IntentFilter mIntentFilter;
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		mIntentFilter.addAction(ACTION_USB_PERMISSION);
		registerReceiver(mReceiver, mIntentFilter);
		
		/*20130317 added by michael*/
		mRequest_USB_permission = false;
		EnumerationDevice(getIntent());
		timertask = new TheTimerTask();
		
		/*20130319 added by michael*/
		SystemUI_intent.setComponent(new ComponentName(
                "com.android.systemui",
                "com.android.systemui.SystemUIService"));
		/*20130320 added by michael*/
		//back up the original wall paper
		mOrigWallpaper = WallpaperManager.getInstance(this);
		mOrigWallpaper_drawable = mOrigWallpaper.getDrawable();
		if (mOrigWallpaper_drawable instanceof BitmapDrawable) {
			mOrigWallpaper_bitmap = ((BitmapDrawable)mOrigWallpaper_drawable).getBitmap(); 
		}
		//Bitmap icon = BitmapFactory.
		getResources();
	}

	@Override
    protected void onDestroy() {
		super.onDestroy();
		timerStop();
		unregisterReceiver(mReceiver);
	}

	Runnable TheDelegatedTimerTask= new Runnable() {
	    @Override public void run() { 
	    	// function called by main thread when posted message is dequeued
/*	    	Calendar c = Calendar.getInstance();
	    	String formattedDate = df.format(c.getTime());
	    	mTmpTextView.setText(formattedDate);*/
	    	//Toast.makeText(getApplicationContext(), "10 secs escape!", Toast.LENGTH_SHORT).show();
	    	
	    	/*20130318 added by michael*/
	    	/*****  Run in main thread  *****/
	    	//Well_View.invalidate();
	    	//synchronized (TheDelegatedTimerTask) {
	    		Well_View.setWellColor(mItracker_dev.Valid_Coord_Histogram);
	    		Well_View.set_focus_coord(mItracker_dev.get_focus_coord());
	    	//}
	    	//Well_View.DrawBitmap();
	    	//Well_View.invalidate();
	    	UpdateActionMenuItem();
	    	//mTmpTextView.setText("dsll");
	    	//Toast.makeText(getApplicationContext(), "10 secs escape!", Toast.LENGTH_SHORT).show();
	    	/*****  Run in main thread  *****/
	    }
	};

	/*20130327 added by michael*/
	Runnable BlinkWellTimerTask = new Runnable() {
		@Override public void run() {
			Well_View.blink_last_well();
		}
	};
	
	protected class TheTimerTask extends TimerTask {
	    @Override public void run() {
/*	        mCount++; 
	        mTextView.setText("Count="+mCount); // WRONG
	        Toast.makeText(getApplicationContext(), "10 secs escape!", Toast.LENGTH_LONG).show();

	        String formattedDate = df.format(c.getTime());
	        mTmpView.setText(formattedDate);*/
	    	/*****  Run in Timer thread  *****/
	    	mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_DATA, 0);
	    	/*20130318 added by michael*/
	    	//deal with the following Itracker data
	    	/*mItracker_dev.coord_index;
	    	mItracker_dev.Valid_Coord_Buf;
	    	mItracker_dev.Valid_Coord_Histogram;
	    	mItracker_dev.Valid_Coord_Buf_Seq;
	    	mItracker_dev.Valid_Coord_Seq_Index;
	    	mItracker_dev.Valid_Coord_Back_For;*/
			//synchronized (TheDelegatedTimerTask) {
				UI_invalid = mItracker_dev.Process_Itracker_Data();
			//}
	    	//Well_View.setWellColor();
	    	//Well_View.setWellColor(mItracker_dev.Valid_Coord_Histogram);
	    	/*****  Run in Timer thread  *****/
	    	
	    	//queue a Runnable task into UI thread
		    //synchronized (TheDelegatedTimerTask) {
	    	if (UI_invalid != 0) {
				//synchronized (TheDelegatedTimerTask) {
	    		//Well_View.setWellColor(mItracker_dev.Valid_Coord_Histogram);
					mHandler.post(TheDelegatedTimerTask);
					mItrackerState |= 1 << Itracker_State_isBackable;
					mItrackerState &= ~(1 << Itracker_State_isForwardable);
					//if ((mItrackerState & (1 << Itracker_State_isBackable)) != 0)
						Itracker_MI_State |= 1 << Itracker_MI_Previos_Tran;

					//if ((mItrackerState & (1 << Itracker_State_isForwardable)) == 0)
						Itracker_MI_State &= ~(1 << Itracker_MI_Next_Tran);
				//}
	    	}
	    	else {
	    		/*20130327 added by michael*/
	    		mHandler.post(BlinkWellTimerTask);
	    	}
		    //}
	    }
	}

	protected void timerTaskStart() {
	    //mTimer = new Timer();
	    timertask = new TheTimerTask();
	    mTimer.schedule( timertask, 0, 1000); // 100 milli seconds
	}

	protected void timerTaskPause() {
		if (timertask != null) {
			timertask.cancel();
			timertask = null;
		}
	}
	
	protected void timerStop() {
	    //timertask.cancel();
		timerTaskPause();
	    mTimer.cancel();
	    //mTimer.purge();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		Log.d(Tag,
				Integer.toHexString(buttonView.getId()) + " is "
						+ Boolean.toString(buttonView.isChecked()));

		if (buttonView instanceof RadioButton) {
			if (buttonView.isChecked() == true) {
				switch (buttonView.getId()) {
				case R.id.ID_radioButton1:
					Well_Selection = I_Tracker_Device.Well_96;
					break;
				case R.id.ID_radioButton2:
					Well_Selection = I_Tracker_Device.Well_384;
					break;
				}
				buttonView.setTextColor(getResources().getColor(android.R.color.white));
			} else
				buttonView.setTextColor(getResources().getColor(android.R.color.tertiary_text_light));			
			
/*			switch (buttonView.getId()) {
			case R.id.ID_radioButton1:
				if (buttonView.isChecked())
					Well_Selection = I_Tracker_Device.Well_96;
				break;
			case R.id.ID_radioButton2:
				if (buttonView.isChecked())
					Well_Selection = I_Tracker_Device.Well_384;
				break;
			}*/
		}
	}

//Enter i-tracker single well demostration
	public void OnBnClickEnterItracker(View v) {
		if (Well_Selection==mItracker_dev.Well_96)
			Well_View.setWell(I_Tracker_Well_Plate_View.Wells_96);
		else
			Well_View.setWell(I_Tracker_Well_Plate_View.Wells_384);
		mLayout_Conten.removeAllViews();
		mLayout_Conten.addView(Well_View);
		//setContentView(Well_View);
		//EnumerationDevice(getIntent());
		mItracker_dev.Well_Plate_Mode = Well_Selection;
		Well_View.DrawBitmap();
	}

//Exit the i-tracker demo activity
	public void OnBnClickExitItracker(View v) {
		this.finish();
	}
	
	public class myCustomView extends View {
		public myCustomView(Context context) {
			super(context);		
		}
		
		@Override
        protected void onDraw(Canvas canvas) {
		   Log.d(Tag, "height: " + canvas.getHeight());
		   Log.d(Tag, "width: " + canvas.getWidth());
		   Log.d(Tag, "isdrawable:" + Boolean.toString(mLayout_Conten.isDrawingCacheEnabled()));
		}
	}

/*20130308 added by michael
inflate a menu.xml the menu_item with attribute android:showAsAction indicate the visible on Action bar 
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu1, menu);
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mTouch_state = Touch_State.TOUCH;
			mTouchPositionX = event.getX();
			mTouchPositionY = event.getY();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			mTouch_state = Touch_State.PINCH;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mTouch_state == Touch_State.TOUCH) {
				mTouchPositionX = event.getX();
				mTouchPositionY = event.getY();
			}
			break;
		case MotionEvent.ACTION_UP:
			mTouch_state = Touch_State.IDLE;
			break;
		}
		return false;
	}
	
    public boolean Connect_Itracker() {
    	//mItracker_dev.setInterface();
/*    	Log.d(Tag, "cmd type size:"+Integer.toString(I_Tracker_Device.CMD_T.SZ_CMD_T));*/
		if (mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_SETTING, 1)
				&& mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_START, 1))
			return true;
		return false;
    }
    
    public boolean isBackable() {
    	return true;
    }
    
    public boolean isForwardable() {
    	return true;
    }
    
    File systemUIapkFile = new File("/system/app/SystemUI.apk");
    public void hide_system_bar() {
    	java.lang.Process p;
    	
    	try {
			p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("service call activity 42 s16 com.android.systemui\n");
			os.flush();
			os.close();
			p.waitFor();
			p.destroy();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	if (mRequest_USB_permission==false)
    		hide_system_bar();
    }

    public void show_system_bar() {
    	java.lang.Process p;
    	
    	try {
			p = Runtime.getRuntime().exec("am startservice -n com.android.systemui/.SystemUIService\n");
			p.waitFor();
			p.destroy();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override
    protected void onStop() {
    	super.onStop();

/*20130320 added by michael*/
//when our activity become in-visible then resume the system bar
    	show_system_bar();
    }


    
    @Override
    protected void onResume() {
    	super.onResume();
    	/*mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
    	if (getIntent().getAction().equals(Intent.ACTION_MAIN)) {
    		mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
			if (mItracker_dev.Enumeration()) {
				mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
				Itracker_MI_State = 1 << Itracker_MI_Start;
			} else
				Itracker_MI_State = 0;
			mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
			//Toast.makeText(context, getIntent().getAction(), Toast.LENGTH_LONG).show();
		}
    	else
    		if (getIntent().getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
    			UsbDevice device = (UsbDevice) getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
    			if (mItracker_dev.Enumeration(device)) {
    				Itracker_MI_State = 1 << Itracker_MI_Start;
    			}
    			else {
    				Itracker_MI_State = 0;
    			}
    		}
    	mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
    	UpdateActionMenuItem();
        Log.d(Tag, "Action: " + getIntent().getAction());
    	mReentrance++;
    	mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
    	mItracker_dev.show_debug(getIntent().getAction());*/
    	
    	
    	//mItracker_dev.for_test();
    	//timerStart();
       	//mTimer.cancel();    		
    	//int k = mTimer.purge();
    	//mItracker_dev.for_test();
    	//this.getResources().get
		mItracker_dev.show_debug(Tag+"thread id" + Integer.toString(Process.myTid())+"\n");
    }
    
    public void EnumerationDevice(Intent intent) {
    	if (intent.getAction().equals(Intent.ACTION_MAIN)) {
    		mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
			if (mItracker_dev.Enumeration()) {
				mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
				Itracker_MI_State = 1 << Itracker_MI_Start;
				Show_Toast_Msg(I_Tracker_Device_Conn);
			} else {
//if isDeviceOnline() return true, send a permission request to communicate with device
				if (mItracker_dev.isDeviceOnline()) {
					mRequest_USB_permission = true;
					mUsbManager.requestPermission(mItracker_dev.getDevice(), mPermissionIntent);
				}
				else
					Itracker_MI_State = 0;
			}
			mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
		}
    	else
    		if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
    			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    			if (mItracker_dev.Enumeration(device)) {
    				Itracker_MI_State = 1 << Itracker_MI_Start;
    				Show_Toast_Msg(I_Tracker_Device_Conn);
    			}
    			else {
    				Itracker_MI_State = 0;
    			}    			
    		}
    	UpdateActionMenuItem();
    }
    
    public void UpdateActionMenuItem() {		
    	if (mMode != null) {
    		mMode.invalidate();
    	}
    }
    
    
    protected void onNewIntent(Intent intent) {
    	mItracker_dev.show_debug("New intent: "+intent.getAction()+"\n");
    	if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
    		EnumerationDevice(intent);
    }
    
    public static String I_Tracker_Device_Conn = "iTrack device connected!";
    public static String I_Tracker_Device_DisCon = "iTrack device disconnected!";
    public static String I_Tracker_Device_Running = "iTrack device running!";
    public static String I_Tracker_Device_Stop = "iTrack device stopped!";
    public static String I_Tracker_Device_Tracking_On = "iTrack device tracking on";
    public static String I_Tracker_Device_Tracking_Off = "iTrack device tracking off";
    public static String I_Tracker_Device_Reset = "itrack device reset";
    
    public void Show_Toast_Msg(String msg ) {
		Toast mToastMsg;
		
    	mToastMsg = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
    	TextView v = (TextView) mToastMsg.getView().findViewById(android.R.id.message);
    	v.setTextColor(Color.YELLOW);
    	v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
    	//LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
    	//v.setGravity(Gravity.CENTER);
		//mToastMsg.setGravity(Gravity.LEFT | Gravity.TOP, 300,100);
    	if (msg.equals(I_Tracker_Device_Conn) || msg.equals(I_Tracker_Device_DisCon))
    	  mToastMsg.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 250);
    	else
    		mToastMsg.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 125);
		mToastMsg.show();
    }
}
