package com.example.demo;

import gif.decoder.GifRun;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.xmlpull.v1.XmlPullParser;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserActivity;
import ar.com.daidalos.afiledialog.FileChooserDialog;

import com.example.demo.I_Tracker_Device.CMD_T;


@SuppressLint("InlinedApi")
public class I_Tacker_Activity extends BaseListSample implements OnCheckedChangeListener, OnTouchListener, MenuAdapter.OnRetrieveItemEnable {
	
	FrameLayout mLayout_Content;
/*20131129 added by michael*/
	RelativeLayout mIndicators_Layout;
	//Calendar c = Calendar.getInstance();
	//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
	static SimpleDateFormat df1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Handler  mHandler;

	public final String Tag = "I_Tracker_Activity";
	Timer    mTimer;
	TextView mTmpTextView;
	Button tmp_Button;
	RadioButton tmp_Radio_button;
	RadioGroup myRadiogroup;
	LinearLayout myWellPlateSelection;
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
		@SuppressWarnings("static-access")
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()) {
			//case R.id.ID_well_plate_view:
			case R.id.ID_StatusIndicator:
				/*I_Tracker_Well_Plate_View v1 = (I_Tracker_Well_Plate_View) v;
				if (mTouchPositionX < v1.mMaxTouchablePosX
						&& mTouchPositionY < v1.mMaxTouchablePosY) {*/
					if ((mItrackerState & (1 << I_Tacker_Activity.this.Itracker_State_isConnect)) == 0) {
						Show_Toast_Msg(I_Tracker_Device_DisCon);
					} else if ((mItrackerState & (1 << Itracker_State_isRunning)) == 1) {
						Show_Toast_Msg(I_Tracker_Device_Running);
					}
					else
						Show_Toast_Msg(I_Tacker_Activity.this.I_Tracker_Device_Stop);
				//}
				break;

			case R.id.ID_OverflowMenuButton:
                mMenuDrawer.toggleMenu();
				break;
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
    /*20131213 added by michael*/
    int Itracker_MI_State_Before_Offline = 1 << Itracker_MI_Start;
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
						/*20131208 modified by michael
						 * The following two lines order are very important¡Ait should terminated worker thread to halt operation USB connection objects then garbage recycle connection */
						Stop_Refresh_iTracker_Data_Thread();  //first stop worker thread to avoid USB access error exception
						mItracker_dev.DeviceOffline();

						/*20131213 modified by michael*/
						//mItrackerState = 0;
						/*20140605 note by michael
						 * if you want to resume the device running status before usb disconnection¡Ait should comment out the line and keep 'mItrackerState' unchanged */
						mItrackerState &= ~(1 << Itracker_State_isConnect); 

						/*20131213 modified by michael
						 * Accident disconnection cause iTracker device off-line¡Ashould backup the last all of the menu item states */
						Itracker_MI_State_Before_Offline = Itracker_MI_State;
						Itracker_MI_State = 0;
						UpdateActionMenuItem();
						Log.d(Tag, "onReceive process thread id" + Integer.toString(Process.myTid()));
						mItracker_dev.show_debug(Tag+"onReceive process thread id" + Integer.toString(Process.myTid())+"\n");
						/*20131208 modified by michael*/
						//timerTaskPause();
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
							mItrackerState |= 1 << Itracker_State_isConnect;
							//mGif.Resume_thread();
							//UpdateActionMenuItem();
						}
					} else {
						Log.d(Tag, "permission denied for device " + device);
						mItracker_dev.show_debug(Tag+"permission denied for device "+device+"\n");
						Itracker_MI_State = 0;
						//mItrackerState = 0;
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
/*20131129 added by michael*/
    public static final int Itracker_State_isConnect = 3;
/*20131213 added by michael*/
    public static final int Itracker_State_isResumable = 4;
    public int mItrackerState = 0;
/*20130317 added by michael*/    
    private static final String ACTION_USB_PERMISSION = "com.example.demo.USB_PERMISSION";
    UsbManager mUsbManager;
    PendingIntent mPermissionIntent;
	TheTimerTask timertask;
/*20130318 added by michael*/
	I_Tracker_Well_Plate_View Well_View;
	public int UI_invalid = 0, UI_invalid_pipetting = 0;

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
		/*20131208 modified by michael*/
		//timerTaskPause();
		Stop_Refresh_iTracker_Data_Thread();
		if ((mItrackerState & (1<<Itracker_State_isRunning)) != 0) {
			mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
			mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 1);
		}					
		/*20130318 added by michael*/
		//timer thread has stop, so below is UI thread running only
		mItracker_dev.reset();
		Well_View.ResetWell();
        if ((mItrackerState & (1 << Itracker_State_isConnect)) != 0) {
    		//mItrackerState = 0;
            mItrackerState = 1 << Itracker_State_isConnect;        	
        }
        else {
        	mItrackerState = 0;
        }
		//Well_View.invalidate();		
	}
	
	DialogInterface.OnClickListener listenerAccept = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			/*20130408 added by michael*/
			if ((Itracker_MI_State & 1 << Itracker_MI_Stop) != 0 || (Itracker_MI_State & 1 << Itracker_MI_Pause) != 0 || (Itracker_MI_State & 1 << Itracker_MI_Start) != 0) {
				Itracker_MI_State = 1 << Itracker_MI_Start;
			}
			else
				Itracker_MI_State = 0;
			Reset_App();
			mLayout_Content.removeAllViews();
			/*20131211 modified by michael
			 * new well plate selection page*/
			//mLayout_Content.addView(myRadiogroup);
			mLayout_Content.addView(myWellPlateSelection);

			//Toast.makeText(I_Tacker_Activity.this, "Great! Welcome.", Toast.LENGTH_SHORT).show();
			/*20131124 added by michael*/
			mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_NONE);
			
			/*20131213 added by michael
			 * close the current session */
			write_logfile_msg("End pipetting session & back to home");
			flush_close_logfile();
		}
	};
	
	DialogInterface.OnClickListener listenerDoesNotAccept = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			//Toast.makeText(I_Tacker_Activity.this, "You can't use this app then.", Toast.LENGTH_SHORT).show();
		}
	};

/*20131124 added by michael
use menudrawer implement fly-in menu¡Amoving the action mode menu items*/
	//protected MenuDrawer mMenuDrawer;
	/*20131128 added by michael*/
	OverflowMenuButton OveflaowBtn;
	/*20131202 added by michael*/
	GifRun mGif;
	/*20131208 added by michael
	 * To enhance performance¡Ainstead of using timertask on timer thread¡Ausing the pure worker thread to handle polling iTracker data*/
	Thread iTracker_polling_thread;
	//Runnable iTracker_DataRefreshTask;
	boolean AllowRefresh_iTrackerData = false;
	/*20131226 added by michael*/
	ThreadPoolExecutor polling_data_executor;
	/*20131212 added by michael
	 * output log file to record information*/
	File sdcard = Environment.getExternalStorageDirectory();
	final String iTracker_Data_Dir = sdcard.getPath() + "/iTracker"; 
	File iTracker_MetaData = new File(iTracker_Data_Dir);
	File iTracker_logfile;
	static BufferedWriter log_file_buf;
	/*20140211 added by michael
	 * preference dialog*/
	public Dialog preference_dialog;
	LinearLayout preference_dialog_layout;
	/*20140303 added by michael
	 * current pipetting detection mode selection*/
	int Pipetting_Mode = -1, Cur_Pipetting_Mode;
	boolean Adjust_Detection_Sensitivity = false, Cur_Adjust_Detection_Sensitivity;
	int Pipetting_Sensitivity_Level = -1, Cur_Detection_Sensitivity_Level;
	/*20140605 added by michael*/
	ImageView running_status_v, connection_status_v;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTmpTextView = new TextView(this);
		mLayout_Content = (FrameLayout) this.findViewById(android.R.id.content);
		mLayout_Content.addView(mTmpTextView);
		//timerStart();
		mTimer = new Timer();
		mHandler= new Handler();
		//iTracker_polling_thread = new Thread(iTracker_DataRefreshTask);
		polling_data_executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//disable android sleep mode to avoid when system awake up again will yield the USB attach event once again 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
/*20130306 added by michael
radio group to let user to choice well plate for i-tacker*/
		//Tag = getPackageName();
		/*setContentView(R.layout.radiogroup_well_selection);
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
		Drawable d1 = getResources().getDrawable(resID);*/
		//tmp_Button.setBackgroundDrawable(d1);

        /*20131211 added by michael*/
		setContentView(R.layout.well_plate_selection);
		myWellPlateSelection = (LinearLayout) findViewById(R.id.ID_well_plate_selection);
		
		/*XmlPullParser parser = getResources().getXml(R.layout.well_plate_selection);
		AttributeSet attributes = Xml.asAttributeSet(parser);
		TypedValue a = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.buttonStyle, a, true);
		//getTheme().obtainStyledAttributes(android.R.)
        //TypedArray a = obtainStyledAttributes(attributes, android.R.style., defStyle, 0);
		tmp_Button = (Button) findViewById(R.id.ID_btn_logfile_itracker);
		Drawable d = tmp_Button.getBackground(); 
		tmp_Button = (Button) findViewById(R.id.ID_btn_96_well_plate);
		d = tmp_Button.getBackground();*/
		
		Well_View = new I_Tracker_Well_Plate_View(this, I_Tracker_Well_Plate_View.Wells_384);
		/*20140605 added by michael
		 * create Well_View background drawable via calling setBackgroundColor()*/
		//Well_View.setBackgroundColor(0xFF000000);
		//mMenuDrawer.setBackgroundColor(0xFF000000);
		Well_View.setId(R.id.ID_well_plate_view);
		FrameLayout.LayoutParams lp =  new FrameLayout.LayoutParams(Well_View.screen_width_pixel(), Well_View.screen_height_pixel());
		Well_View.setLayoutParams(lp);
		mTmpTextView.setTextColor(getResources().getColor(android.R.color.holo_green_light));
		
		//mLayout_Content.addView(Well_View);
		//mLayout_Content.removeAllViews();
		//20130308 added by michael
		 //*long click a view and display contextual button
		 
		//Well_View.setOnLongClickListener(listener);
		//Well_View.setOnClickListener(listener1);
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
						/*20131213 added by michael
						 * judge if it is start a new pipetting session or resume a last session according to the `End` button rnable/disable state */
						if (log_file_buf == null) {
							try {
								create_logfile(generate_logfilename());
								write_logfile_msg("Start a new pipetting session");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else {
							write_logfile_msg("Resume");
						}
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
						//Well_View.set_focus_coord(mItracker_dev.get_focus_coord());
						/*20131208 modified by michael
						 * replace timerTaskStart() with Start_Refresh_iTracker_Data_Thread()*/
						//timerTaskStart();
						Start_Refresh_iTracker_Data_Thread();
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
					/*20131208 modified by michael*/
					//timerTaskPause();
					Stop_Refresh_iTracker_Data_Thread();
					mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
					mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 1);
					mItrackerState &= ~(1 << Itracker_State_isRunning);
					Show_Toast_Msg(I_Tracker_Device_Tracking_Off);
					
					/*20130327 added by michael*/
					//Show_focus_coord==true then call I_Tracker_Well_Plate.blink_last_well() again
					if (Well_View.Show_focus_coord)
						Well_View.blink_last_well();
					
					
					/*20131213 added by michael*/
					write_logfile_msg("Pause");
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
					
					/*20131213 added by michael*/
					write_logfile_msg("End pipetting session");
					flush_close_logfile();
					break;
				case R.id.ID_MI_previous_trans:
					/*if (!isBackable())
						mMenu_item_state ^= 1 << Itracker_MI_Previos_Tran;*/
					//synchronized (TheDelegatedTimerTask) {
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
							//Well_View.decrese_SingleWellColor(mItracker_dev.get_reverse_undo_coord());
							Well_View.setWellColor(mItracker_dev.Valid_Coord_Histogram);
							Well_View.set_focus_coord(mItracker_dev.get_focus_coord());
							//Well_View.DrawBitmap();
							//Well_View.invalidate();
							//Toast mToastMsg = Toast.makeText(getApplicationContext(), "previous", Toast.LENGTH_LONG);
						}
					//}
						
					/*20131213 added by michael*/
					write_logfile_msg("Undo");
					break;
				case R.id.ID_MI_next_trans:
					/*if (!isForwardable())
						mMenu_item_state ^= 1 << Itracker_MI_Next_Tran;*/
					//synchronized (TheDelegatedTimerTask) {
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
							//Well_View.increase_SingleWellColor(mItracker_dev.get_reverse_redo_coord());
							Well_View.setWellColor(mItracker_dev.Valid_Coord_Histogram);
							Well_View.set_focus_coord(mItracker_dev.get_focus_coord());
							//Well_View.DrawBitmap();
							//Well_View.invalidate();
						}
					//}

					/*20131213 added by michael*/
					write_logfile_msg("Redo");
					break;

				case R.id.ID_MI_well_selection:
					//mLayout_Content.removeAllViews();
					//mLayout_Content.addView(myRadiogroup);
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
				//Update_Menu_Item_Enable(menu, mMenu_item_state);
				update_item_state();
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
				
				/*20131127 added by michael*/
				update_item_state();
				//if (mGif != null)
					//mGif.Resume_thread();
			}
		};
	    //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		//ActionBar Abr = this.getActionBar();
		//this.setContentView(mLayout_Content);
		mReentrance = 0;
		mItracker_dev = new I_Tracker_Device(this);

		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		
		IntentFilter mIntentFilter;
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		mIntentFilter.addAction(ACTION_USB_PERMISSION);
		registerReceiver(mReceiver, mIntentFilter);

		/*20140624 added by michael*/
		//back up the original wall paper¡Athe action should be done before stop the system service com.android.systemui/SystemUI
		mOrigWallpaper = WallpaperManager.getInstance(this);
		mOrigWallpaper_drawable = mOrigWallpaper.getDrawable();
		if (mOrigWallpaper_drawable instanceof BitmapDrawable) {
			mOrigWallpaper_bitmap = ((BitmapDrawable)mOrigWallpaper_drawable).getBitmap();
		}
		else
			mOrigWallpaper_bitmap = null;

		/*20130317 added by michael*/
		mRequest_USB_permission = false;
		EnumerationDevice(getIntent());
		//timertask = new TheTimerTask();
		
		/*20130319 added by michael*/
		SystemUI_intent.setComponent(new ComponentName(
                "com.android.systemui",
                "com.android.systemui.SystemUIService"));
		/*20130320 added by michael*/
		//back up the original wall paper
		/*mOrigWallpaper = WallpaperManager.getInstance(this);
		mOrigWallpaper_drawable = mOrigWallpaper.getDrawable();
		if (mOrigWallpaper_drawable instanceof BitmapDrawable) {
			mOrigWallpaper_bitmap = ((BitmapDrawable)mOrigWallpaper_drawable).getBitmap(); 
		}*/
		//Bitmap icon = BitmapFactory.
		getResources();
		
		/*20131124 added by michael
		create the menudrawer instance*/
		//mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND, getDrawerPosition(), getDragMode());
		/*20131127 added by michael*/
		this.mAdapter.setOnRetrieveItemEnable(this);
				
		/*20131128 added by michael
		 * add a menu overflow button*/
		//FrameLayout.LayoutParams lp1;
		RelativeLayout.LayoutParams lp2;
		mIndicators_Layout = new RelativeLayout(this);
		lp = new FrameLayout.LayoutParams(40, (int)Well_View.mMaxTouchablePosY, Gravity.END|Gravity.TOP);
		mIndicators_Layout.setLayoutParams(lp);
		
		OveflaowBtn = new OverflowMenuButton(this, null, R.attr.customOverflowMenuButtonStyle);
		lp2 = new RelativeLayout.LayoutParams(32, ViewGroup.LayoutParams.WRAP_CONTENT);
		//lp1.setMargins((int)(Well_View.mMaxTouchablePosX-32), (int)(Well_View.mMaxTouchablePosY-48), 32, 48);
		//lp1.setMargins(Well_View.mMaxTouchablePosX-32, Well_View.mMaxTouchablePosY-48, 32, 48);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		mIndicators_Layout.addView(OveflaowBtn, lp2);
		//lp1 = (FrameLayout.LayoutParams)mLayout_Content.getLayoutParams();
		//lp1.setMargins((int)(Well_View.mMaxTouchablePosX-32), (int)(Well_View.mMaxTouchablePosY-48), 0, 0);
		//mLayout_Content.setLayoutParams(lp1);
		//OveflaowBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
		OveflaowBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.overflowmwnu_button_selector_holo_dark));
		//OveflaowBtn.setScaleX((float) 0.75);
		OveflaowBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_moreoverflow_normal_holo_dark));		
		OveflaowBtn.setId(R.id.ID_OverflowMenuButton);
		OveflaowBtn.setOnClickListener(this.listener1);
		//OveflaowBtn.setScaleY((float) 0.6);

		
		/*20131129 added by michael
		 * status indicator to notify iTracker is running or pause*/
		SurfaceView surf_v = new SurfaceView(this);
		surf_v.setId(R.id.ID_StatusIndicator);
		lp2 = new RelativeLayout.LayoutParams(32, 32);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		//lp2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		//lp2.addRule(RelativeLayout.BELOW, R.id.ID_OverflowMenuButton);
		lp2.setMargins(0, (int)(Well_View.mMaxTouchablePosY-32)/2, 0, 0);
		//mIndicators_Layout.addView(surf_v, lp2);
		surf_v.setOnClickListener(listener1);
		mGif = new  GifRun();
		mGif.LoadGiff(surf_v, this, R.drawable.status_32x32);
		
		/*20140605 added by michael*/
		running_status_v = new ImageView(this);
		lp2 = new RelativeLayout.LayoutParams(32, 32);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		lp2.setMargins(0, (int)(Well_View.mMaxTouchablePosY-32)/2, 0, 0);
		mIndicators_Layout.addView(running_status_v, lp2);
		
		connection_status_v = new ImageView(this);
		lp2 = new RelativeLayout.LayoutParams(32, 32);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		lp2.setMargins(0, (int)(Well_View.mMaxTouchablePosY-32), 0, 0);
		mIndicators_Layout.addView(connection_status_v, lp2);
	}

	@Override
    protected void onDestroy() {
		super.onDestroy();
		//timerStop();
		Stop_Refresh_iTracker_Data_Thread();
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
		boolean result = false;
	    @Override public void run() {
/*	        mCount++; 
	        mTextView.setText("Count="+mCount); // WRONG
	        Toast.makeText(getApplicationContext(), "10 secs escape!", Toast.LENGTH_LONG).show();

	        String formattedDate = df.format(c.getTime());
	        mTmpView.setText(formattedDate);*/
	    	/*****  Run in Timer thread  *****/
	    	result = false;
	    	result = mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_DATA, 0);
	    	/*20130318 added by michael*/
	    	//deal with the following Itracker data
	    	/*mItracker_dev.coord_index;
	    	mItracker_dev.Valid_Coord_Buf;
	    	mItracker_dev.Valid_Coord_Histogram;
	    	mItracker_dev.Valid_Coord_Buf_Seq;
	    	mItracker_dev.Valid_Coord_Seq_Index;
	    	mItracker_dev.Valid_Coord_Back_For;*/
			//synchronized (TheDelegatedTimerTask) {
	    	UI_invalid_pipetting = 0;
	    	if (result)
	    		UI_invalid_pipetting = mItracker_dev.Process_Itracker_Data();
			//}
	    	//Well_View.setWellColor();
	    	//Well_View.setWellColor(mItracker_dev.Valid_Coord_Histogram);
	    	/*****  Run in Timer thread  *****/
	    	
	    	//queue a Runnable task into UI thread
		    //synchronized (TheDelegatedTimerTask) {
	    	if (UI_invalid_pipetting != 0) {
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

	/*20131208 added by michael*/
	Runnable iTracker_DataRefreshTask = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (AllowRefresh_iTrackerData) {
				/*****  Run in worker thread  *****/
				/*update the iTracker data by polling continuously
				 */
				/*****  Run in worker thread  *****/
				
				if (mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_DATA, 0))
				  UI_invalid_pipetting = mItracker_dev.Process_Itracker_Data();
                else
				   UI_invalid_pipetting = 0;
				
		    	//queue a Runnable task into UI thread
			    //synchronized (TheDelegatedTimerTask) {
		    	if (UI_invalid_pipetting != 0) {
		    		//Well_View.setWellColor(mItracker_dev.Valid_Coord_Histogram);
						mHandler.post(TheDelegatedTimerTask);
						mItrackerState |= 1 << Itracker_State_isBackable;
						mItrackerState &= ~(1 << Itracker_State_isForwardable);
						//if ((mItrackerState & (1 << Itracker_State_isBackable)) != 0)
							Itracker_MI_State |= 1 << Itracker_MI_Previos_Tran;

						//if ((mItrackerState & (1 << Itracker_State_isForwardable)) == 0)
							Itracker_MI_State &= ~(1 << Itracker_MI_Next_Tran);
					//}
				    		try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		    	}
		    	else {
		    		/*20130327 added by michael*/
		    		mHandler.post(BlinkWellTimerTask);
		    		try {
						Thread.sleep(650);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
			}
		}
		
	};

	protected void timerTaskStart() {
	    //mTimer = new Timer();
	    if (timertask==null) {
	    	timertask = new TheTimerTask();
	    	mTimer.schedule( timertask, 0, 1000); // 100 milli seconds
	    }
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

	/*20131208 added by michael
	 * instead of using timer thread to request iTracker data continuously¡Acreate a newly worker thread to  retrieve the iTracker data*/
	protected void Start_Refresh_iTracker_Data_Thread() {
/*		if (iTracker_polling_thread.getState()==Thread.State.TERMINATED) {
			AllowRefresh_iTrackerData = true;
			iTracker_polling_thread = new Thread(iTracker_DataRefreshTask);
			20131224 added by michael
			 * before starting a thread¡Ait usually should set the adaptive worker thread priority less than main thread¡A such as Process.THREAD_PRIORITY_BACKGROUND 
			iTracker_polling_thread.setPriority(Thread.currentThread().getPriority() - 1);
			iTracker_polling_thread.start();
		}
		else
			if (iTracker_polling_thread.getState()==Thread.State.NEW) {
				AllowRefresh_iTrackerData = true;
				20131224 added by michael
				 * before starting a thread¡Ait usually should set the adaptive worker thread priority less than main thread¡A such as Process.THREAD_PRIORITY_BACKGROUND 
				iTracker_polling_thread.setPriority(Thread.currentThread().getPriority() - 1);
				iTracker_polling_thread.start();
			}*/
		AllowRefresh_iTrackerData = true;
		this.polling_data_executor.execute(this.iTracker_DataRefreshTask);
	}

	protected void Stop_Refresh_iTracker_Data_Thread() {
		//if the thread state is New then exit the loop runnable by setting the variable AllowRefresh_iTrackerData=false
		AllowRefresh_iTrackerData = false;
		//if (iTracker_polling_thread.getState() == Thread.State.NEW) {
/*			boolean retry = true;
			while (retry) {
				try {
					iTracker_polling_thread.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}*/
		//}
		try {
			this.polling_data_executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	/*20131213 added by michael*/
	public void flush_close_logfile() {
		try {
			if (log_file_buf != null) {
				log_file_buf.flush();
				log_file_buf.close();
				Flush_Log_File = "log file saved: " + iTracker_logfile.getPath(); 
				Show_Toast_Msg(Flush_Log_File);
				log_file_buf = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*20131213 added by michael*/
	public static void write_logfile_msg(String line) {
		if (log_file_buf != null) {
			String line_text = df1.format(new Date()) + "  " + line;
			try {
				log_file_buf.write(line_text, 0, line_text.length());
				log_file_buf.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*20131213 added by michael
	 * the log file file naming format logyyyymmdd-hhmmss.txt*/
	public String generate_logfilename() {
		String filename;
		filename = "log_" + df.format(new Date()) + ".txt";
		Log.d(Tag, filename);
		return filename;
	}
	
	/*20131212 added by michael*/
	public void create_logfile(String filename) throws IOException {
		if (sdcard.exists()) {
			if (!iTracker_MetaData.exists()) {
				iTracker_MetaData.mkdirs();
				Log.d(Tag, "sdcard directory exist:" + Boolean.toString(iTracker_MetaData.exists()));
			}
			
			if (iTracker_MetaData.exists()) {
			  iTracker_logfile = new File(iTracker_MetaData, filename);
			  log_file_buf = new BufferedWriter(new FileWriter(iTracker_logfile, false));
			}
			else {
				log_file_buf = null;
				Log.d(Tag, "Can't create the log file");
			}
		}
		else {
			log_file_buf = null;
			Log.d(Tag, "Can't found external sdcard ");
		}		
	}

	static int semaphore = 0;
	/*20131211 added by michael*/
	public void OnBnClickLogFileItracker(View v) {
		/*Intent it = new Intent(Intent.ACTION_MAIN);
		it.setComponent(new ComponentName("com.example.hello_android", "com.example.hello_android.MainActivity"));
		startActivity(it);
		this.startActivityForResult(it, 0);*/
		//this.setResult(resultCode, data);
		//Intent intent = new Intent(this, LogFileDisplayActivity.class);
		Intent intent = new Intent(this, LogFileChooserActivity.class);
		//intent.putExtra(FileChooserActivity.INPUT_FOLDER_MODE, true);
		intent.putExtra(FileChooserActivity.INPUT_SHOW_FULL_PATH_IN_TITLE, true);
		intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, iTracker_Data_Dir);
		startActivity(intent);
	
		/*RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.example_appwidget_layout);
		views.setTextViewText(R.id.textView1, "samephore: " + Integer.toString(semaphore));
		ComponentName thisWidget = new ComponentName( this.getApplicationContext(), ExampleAppWidgetProvider.class );
		AppWidgetManager.getInstance( this.getApplicationContext() ).updateAppWidget( thisWidget, views );
		semaphore++;*/
		/*Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
		startActivity(browserIntent);*/
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}
	
    public void OnBnClick_96_Well_Plate(View v) throws IOException {
    	Well_Selection = I_Tracker_Device.Well_96;
    	this.mItracker_dev.set_well_plate(Well_Selection);
    	OnBnClickEnterItracker(v);
    	/*20131212 added by michael*/
    	//create_logfile();
    }
    
    public void OnBnClick_384_Well_Plate(View v) throws IOException {
    	Well_Selection = I_Tracker_Device.Well_384;
    	this.mItracker_dev.set_well_plate(Well_Selection);
    	OnBnClickEnterItracker(v);
    	/*20131212 added by michael*/
    	//create_logfile();
    }
    
//Enter i-tracker single well demostration
	public void OnBnClickEnterItracker(View v) {
		if (Well_Selection==mItracker_dev.Well_96)
			Well_View.setWell(I_Tracker_Well_Plate_View.Wells_96);
		else
			Well_View.setWell(I_Tracker_Well_Plate_View.Wells_384);
		mLayout_Content.removeAllViews();
		mLayout_Content.addView(Well_View);
		mLayout_Content.addView(mIndicators_Layout);
		//setContentView(Well_View);
		//EnumerationDevice(getIntent());
		mItracker_dev.Well_Plate_Mode = Well_Selection;
		Well_View.DrawBitmap();

		/*20131124 added by michael
		enable menudrawer and configure it's child view mMenuContainer dimension to fit the adapted UI region*/
		ViewGroup.LayoutParams lp1;
		ViewGroup vg;
		mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_FULLSCREEN);
		vg = (ViewGroup) this.findViewById(R.id.md__menu);
		lp1 = (ViewGroup.LayoutParams)vg.getLayoutParams();
		Log.d("menudrawer (width, height)", Integer.toString(vg.getWidth())+" ,"+Integer.toString(vg.getHeight()));
		lp1.height = (int)Well_View.mMaxTouchablePosY + 5;
		//lp.width = LayoutParams.WRAP_CONTENT;
		vg.setLayoutParams(lp1);
		Log.d("menudrawer (width, height)", Integer.toString(vg.getWidth())+" ,"+Integer.toString(vg.getHeight()));
		Log.d("first menu item position", Integer.toString(mList.getFirstVisiblePosition()));
		Log.d("last menu item position", Integer.toString(mList.getLastVisiblePosition()));
		//android.R.drawable.divider_horizontal_bright;
		//android.R.drawable.divider_horizontal_dark;
		//android.R.drawable.divider_horizontal_dim_dark;
		//android.R.drawable.divider_horizontal_textfield;
		update_item_state();
		
		/*20131202 added by michael*/
		//mGif.Resume_thread();
	}

//Exit the i-tracker demo activity
	public void OnBnClickExitItracker(View v) {
		/*20140624 added by michael
		 * restore the original wallpaper
		 * */
		try {
			if (mOrigWallpaper_bitmap != null)
				mOrigWallpaper.setBitmap(mOrigWallpaper_bitmap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		   Log.d(Tag, "isdrawable:" + Boolean.toString(mLayout_Content.isDrawingCacheEnabled()));
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
				&& mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_START, 1)) {
			return true;
		}
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
    	byte [] buff;
    	int readed;
    	String result;
    	
    	result = "kkk";
    	buff = new byte [100];
    	try {
			p = Runtime.getRuntime().exec("/system/xbin/su-new");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			DataInputStream is = new DataInputStream(p.getInputStream());
			os.writeBytes("service call activity 42 s16 com.android.systemui\n");
			try { Thread.sleep(5000); } catch(Exception ex) {}
            while( is.available() > 0) {
                readed = is.read(buff);
                if ( readed <= 0 ) break;
                String seg = new String(buff,0,readed);   
                result = result + seg; //result is a string to show in textview
            }
            Log.d(Tag, "shell exit code: " + result);
			os.flush();
			os.close();
			is.close();
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
    	byte [] buff;
    	int readed;
    	String result;
    	
    	result = "kkk";
    	buff = new byte [100];    	
    	try {
			/*p = Runtime.getRuntime().exec("/system/xbin/su-new am startservice -n com.android.systemui/.SystemUIService\n");
			p.waitFor();
			p.destroy();*/
			p = Runtime.getRuntime().exec("/system/xbin/su-new");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			DataInputStream is = new DataInputStream(p.getInputStream());
			os.writeBytes("am startservice -n com.android.systemui/.SystemUIService\n");
			try { Thread.sleep(5000); } catch(Exception ex) {}
            while( is.available() > 0) {
                readed = is.read(buff);
                if ( readed <= 0 ) break;
                String seg = new String(buff,0,readed);   
                result = result + seg; //result is a string to show in textview
            }
            Log.d(Tag, "shell exit code: " + result);
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
				mItrackerState |= 1 << Itracker_State_isConnect;
				//if (mGif != null)
					//mGif.Resume_thread();
			} else {
//if isDeviceOnline() return true, send a permission request to communicate with device
				if (mItracker_dev.isDeviceOnline()) {
					mRequest_USB_permission = true;
					mUsbManager.requestPermission(mItracker_dev.getDevice(), mPermissionIntent);
				}
				else {
					Itracker_MI_State = 0;
					mItrackerState = 0;
				}
			}
			mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
		}
    	else
    		if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
    			UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    			if (mItracker_dev.Enumeration(device)) {
    				/*20140605 modified by michael
    				 * when device is running and unplug the usb cable will cause disconnection¡Athe device still running
    				 * when reconnect the cable and enumeration device success¡Aapp resume the last running status of device */
    				if ((mItrackerState &( 1 << Itracker_State_isConnect))==1) {
    					Itracker_MI_State ^= 1 << Itracker_MI_Pause;
    					mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 1);
    					if (Connect_Itracker()) {
    						if (log_file_buf == null) {
    							try {
    								create_logfile(generate_logfilename());
    								write_logfile_msg("Start a new pipetting session");
    							} catch (IOException e) {
    								// TODO Auto-generated catch block
    								e.printStackTrace();
    							}
    						}
    						else {
    							write_logfile_msg("Resume");
    						}
    						Itracker_MI_State |= 1 << Itracker_MI_Stop;
    						if ((mItrackerState & (1<<Itracker_State_isBackable)) != 0)
    							Itracker_MI_State |= 1 << Itracker_MI_Previos_Tran;
    						
    						if ((mItrackerState & (1<<Itracker_State_isForwardable)) != 0)
    							Itracker_MI_State |= 1 << Itracker_MI_Next_Tran;
    						
    					}
    				}
    				else
    					Itracker_MI_State = 1 << Itracker_MI_Start;
    				Show_Toast_Msg(I_Tracker_Device_Conn);
    				mItrackerState |= 1 << Itracker_State_isConnect;
    				
    				//if (mGif != null)
    					//mGif.Resume_thread();
    			}
    			else {
    				Itracker_MI_State = 0;
    				mItrackerState = 0;
    			}    			
    		}
    	UpdateActionMenuItem();
    }
    
    public void UpdateActionMenuItem() {		
    	if (mMode != null) {
    		mMode.invalidate();
    	}
    	else {
    		update_item_state();
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
    /*20131217 added by  michael*/
    public static String Flush_Log_File = ""; 
    
    public void Show_Toast_Msg(String msg ) {
		Toast mToastMsg;
		
		if (msg.contains("flush log file: "))
			mToastMsg = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
		else
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
    
    /*20131124 added by michael
    getDragMode() & getDrawerPosition()*/
    @Override
    protected int getDragMode() {
        //return MenuDrawer.MENU_DRAG_CONTENT;
    	return MenuDrawer.MENU_DRAG_WINDOW;
    }

    @Override
    protected Position getDrawerPosition() {
        return Position.START;
    }

    /*20131126 add by michael
     *interactive with user then execute same correspond action with onActionItemClicked(), then update menu item state*/
    @Override
	protected void onMenuItemClicked(int position, Item item) {
		// TODO Auto-generated method stub
    	View v;
    	TextView tv;
    	int start;
    	
    	start = mList.getFirstVisiblePosition();
    	v = mList.getChildAt(position-start);
    	
    	if (mAdapter.getItemViewType(position)==0) {
    		tv = (TextView) v; 
    		switch (position) {
    		case Itracker_MI_Start:
    			if (tv.getText()=="Run") {
					if (Connect_Itracker()) {
						mMenuDrawer.toggleMenu();
						Show_Toast_Msg(I_Tracker_Device_Tracking_On);
						
						mItrackerState |= 1 << Itracker_State_isRunning;
						Itracker_MI_State ^= 1 << Itracker_MI_Start;
						Itracker_MI_State ^= 1 << Itracker_MI_Pause;
						//mMenu_item_state ^= 1 << Itracker_MI_Stop;
						/*20131213 added by michael
						 * judge if it is start a new pipetting session or resume a last session according to the `End` button rnable/disable state */
						if (log_file_buf == null) {
							try {
								create_logfile(generate_logfilename());
								write_logfile_msg("Start a new pipetting session");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else {
							write_logfile_msg("Resume");
						}
						Itracker_MI_State |= 1 << Itracker_MI_Stop;
						if ((mItrackerState & (1<<Itracker_State_isBackable)) != 0)
							Itracker_MI_State |= 1 << Itracker_MI_Previos_Tran;
						
						if ((mItrackerState & (1<<Itracker_State_isForwardable)) != 0)
							Itracker_MI_State |= 1 << Itracker_MI_Next_Tran;
						/*20130317 preparing polling device*/
						//create a new thread to receive the device data continuously
						//implement this task via timer & timertask, using timer implicitly run task in a new thread
/*						mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_SETTING, 1);
						mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
						mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_START, 1);
						mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");*/
						
						/*20130327 added by michael*/
						//Well_View.set_focus_coord(mItracker_dev.get_focus_coord());
						/*20131208 modified by michael
						 * replace timerTaskStart() with Start_Refresh_iTracker_Data_Thread()*/
						//timerTaskStart();
						Start_Refresh_iTracker_Data_Thread();
					}
					else {
//Device may be something wrong cause we can't send commands(HID_CMD_ITRACKER_SETTING¡BHID_CMD_ITRACKER_START)
						if (I_Tacker_Activity.mDebug_Itracker==true)
							Toast.makeText(getApplicationContext(), "Can't satrt Itracker device", Toast.LENGTH_LONG).show();
					}
					//mMenuDrawer.toggleMenu();
					//Show_Toast_Msg(I_Tracker_Device_Tracking_On);
    			}
    			else
    				if (tv.getText()=="Pause") {
    					//pause the current task
    					Itracker_MI_State ^= 1 << Itracker_MI_Start;
    					Itracker_MI_State ^= 1 << Itracker_MI_Pause;
    					/*20131208 modified by michael*/
    					//timerTaskPause();
    					Stop_Refresh_iTracker_Data_Thread();
    					mItracker_dev.show_debug(Tag+"The line number is " + new Exception().getStackTrace()[0].getLineNumber()+"\n");
    					mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 1);
    					mItrackerState &= ~(1 << Itracker_State_isRunning);
    					Show_Toast_Msg(I_Tracker_Device_Tracking_Off);
    					
    					/*20130327 added by michael*/
    					//Show_focus_coord==true then call I_Tracker_Well_Plate.blink_last_well() again
    					if (Well_View.Show_focus_coord)
    						Well_View.blink_last_well();
    					
    					
    					/*20131213 added by michael*/
    					write_logfile_msg("Pause");
    					break;    					
    				}
    			break;
    			
    		case (Itracker_MI_Stop-1):
    			if (tv.getText()=="End") {
    				//ending the current task, clear device states & records, infos
    				Itracker_MI_State = 1 << Itracker_MI_Start;
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
					
					/*20131213 added by michael*/
					write_logfile_msg("End pipetting session");
					flush_close_logfile();
					break;

    			}
    			break;
    			
    		case (Itracker_MI_Previos_Tran-1):
    			if (tv.getText()=="Undo") {
					/*if (!isBackable())
					mMenu_item_state ^= 1 << Itracker_MI_Previos_Tran;*/
				//synchronized (TheDelegatedTimerTask) {
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
						//Well_View.decrese_SingleWellColor(mItracker_dev.get_reverse_undo_coord());
						Well_View.setWellColor(mItracker_dev.Valid_Coord_Histogram);
						Well_View.set_focus_coord(mItracker_dev.get_focus_coord());
						//Well_View.DrawBitmap();
						//Well_View.invalidate();
						//Toast mToastMsg = Toast.makeText(getApplicationContext(), "previous", Toast.LENGTH_LONG);
					}
				//}
					
				/*20131213 added by michael*/
				write_logfile_msg("Undo");
    			}
    			break;
    			
    		case (Itracker_MI_Next_Tran-1):
    			if (tv.getText()=="Redo") {
					/*if (!isForwardable())
					mMenu_item_state ^= 1 << Itracker_MI_Next_Tran;*/
				//synchronized (TheDelegatedTimerTask) {
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
						//Well_View.increase_SingleWellColor(mItracker_dev.get_reverse_redo_coord());
						Well_View.setWellColor(mItracker_dev.Valid_Coord_Histogram);
						Well_View.set_focus_coord(mItracker_dev.get_focus_coord());
						//Well_View.DrawBitmap();
						//Well_View.invalidate();
					}
				//}

				/*20131213 added by michael*/
				write_logfile_msg("Redo");    				
    			}
    			break;
    			
    		case (4):
    			if (tv.getText()=="Home") {
					//mLayout_Content.removeAllViews();
					//mLayout_Content.addView(myRadiogroup);
					//setContentView(myRadiogroup);
					Builder builder = new AlertDialog.Builder(I_Tacker_Activity.this);
					AlertDialog dialog = builder.create();
					dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
					dialog.setTitle("Do you back to well plate selection?");
					/*20131217 modified by michael*/
					dialog.setMessage("Warning: the current pipetting session will be terminated!");
					dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Yes", listenerAccept);
					dialog.setButton(DialogInterface.BUTTON_POSITIVE, "No", listenerDoesNotAccept);
					dialog.getWindow().setGravity(Gravity.TOP);
					dialog.setIcon(R.drawable.ic_launcher1);
					dialog.setCancelable(false);
					dialog.show();
					mMenuDrawer.toggleMenu();
    			}
    			break;
    		
    		/*20140221 added by michael
    		 * preference setting */
    		case (5):
    			mMenuDrawer.toggleMenu();
				preference_dialog = new Dialog(this, R.style.CenterDialog);
				preference_dialog_layout = (LinearLayout) LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.dialog_preference, null);
				Spinner spinner = (Spinner) preference_dialog_layout.findViewById(R.id.spinner1);
				ArrayList<String> spinner_items = new ArrayList<String>();
				spinner_items.add("Auto-channel Pipet");
				spinner_items.add("Single-channel Pipet");
				spinner_items.add("8-channel Pipet");
				spinner_items.add("12-channel Pipet");
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, spinner_items);
				adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapter);
				spinner.setOnItemSelectedListener(Pipetting_mode_selection);
				if (Pipetting_Mode == -1) {
				  int spinner_adapter_item_count = spinner.getCount();
				  if (spinner_adapter_item_count > 0) {
					  spinner.setSelection(0);
					  Pipetting_Mode = 0;
				  }
				}
				else
					spinner.setSelection(Pipetting_Mode);
				
				Button dlgbtn_cancel, dlgbtn_ok;
				dlgbtn_cancel = (Button) preference_dialog_layout.findViewById(R.id.button2_cancel);
				// dlgbtn_cancel.setOnClickListener(null);
				dlgbtn_cancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) { // TODO
						preference_dialog.dismiss();
					}
				});
				dlgbtn_ok = (Button) preference_dialog_layout.findViewById(R.id.button1_ok);
				dlgbtn_ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) { // TODO
						Pipetting_Mode = Cur_Pipetting_Mode;
						Adjust_Detection_Sensitivity = Cur_Adjust_Detection_Sensitivity;
						Pipetting_Sensitivity_Level = Cur_Detection_Sensitivity_Level;
						preference_dialog.dismiss();
						
						/*20140306 added by michael*/
						mItracker_dev.set_pipetting_detection_mode(Pipetting_Mode);
						mItracker_dev.set_pipetting_detection_sensitivity_level(Adjust_Detection_Sensitivity, Pipetting_Sensitivity_Level);
						if ((mItrackerState & (1 << Itracker_State_isRunning)) == 1) {
							mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 1);
							if (Connect_Itracker()) {
								
							}
							else {
								if (I_Tacker_Activity.mDebug_Itracker==true)
									Toast.makeText(getApplicationContext(), "Can't changing preference", Toast.LENGTH_LONG).show();
							}
						}
						else {
							mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_SETTING, 1);							
						}
					}
				});
				
				CheckBox checkbox1;
				checkbox1 = (CheckBox) preference_dialog_layout.findViewById(R.id.checkBox1);
				checkbox1.setChecked(Adjust_Detection_Sensitivity);
				SeekBar seekbar1;
				seekbar1 = (SeekBar) preference_dialog_layout.findViewById(R.id.seekBar1);
 			    seekbar1.setEnabled(Adjust_Detection_Sensitivity);
				checkbox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						// TODO Auto-generated method stub
						Cur_Adjust_Detection_Sensitivity = isChecked;
						SeekBar seekbar1;
						seekbar1 = (SeekBar) preference_dialog_layout.findViewById(R.id.seekBar1);
						seekbar1.setEnabled(isChecked);
						
						/*20140312 added by michael*/
						TextView seekbar_value;
						seekbar_value = (TextView) preference_dialog_layout.findViewById(R.id.textView2);
						if (isChecked==true) {
							seekbar_value.setVisibility(View.VISIBLE);
							if (Cur_Detection_Sensitivity_Level==-1)
								Cur_Detection_Sensitivity_Level = seekbar1.getProgress();
							seekbar_value.setText(Integer.toString(Cur_Detection_Sensitivity_Level) + "%"); 
						}
						else
							seekbar_value.setVisibility(View.INVISIBLE);
					}
				});
				if (Pipetting_Sensitivity_Level==-1)
					Pipetting_Sensitivity_Level = seekbar1.getProgress();
				else
					seekbar1.setProgress(Pipetting_Sensitivity_Level);
				seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						// TODO Auto-generated method stub
						Cur_Detection_Sensitivity_Level = progress;
						TextView seekbar_value;
						seekbar_value = (TextView) preference_dialog_layout.findViewById(R.id.textView2);
						seekbar_value.setText(Integer.toString(Cur_Detection_Sensitivity_Level) + "%");
					}
				});

				/*20140311 added by michael*/
				TextView seekbar_value;
				seekbar_value = (TextView) preference_dialog_layout.findViewById(R.id.textView2);
				if (Adjust_Detection_Sensitivity==true) {
					seekbar_value.setVisibility(View.VISIBLE);
					if (Pipetting_Sensitivity_Level==-1)
						Pipetting_Sensitivity_Level = seekbar1.getProgress();
					seekbar_value.setText(Integer.toString(Pipetting_Sensitivity_Level) + "%"); 
				}
				else
					seekbar_value.setVisibility(View.INVISIBLE);
				//preference_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				preference_dialog.getWindow().setGravity(Gravity.TOP);
				//preference_dialog.setContentView(R.layout.dialog_preference);
				preference_dialog.setContentView(preference_dialog_layout);
				preference_dialog.setTitle("Preference");
				preference_dialog.setCancelable(true);

				/*20140303 added by michael
				 * change the dialog 
				 * dialog construct hierarchical like activity¡Ait's framework with a window¡BdecorView*/
				TextView dlg_title;
				dlg_title = (TextView) preference_dialog.getWindow().getDecorView().findViewById(android.R.id.title);
				//dlg_title.setText("Change Dialog");
    			preference_dialog.show();
    			break;
    		}
    	}
    	Log.d(this.getComponentName().toShortString().toString(), item.mTitle);
		//update_item_state();
		UpdateActionMenuItem();
	}

    /*20131126 add by michael
     * assign the menu items meta-data
     * *(non-Javadoc)
     * @see com.example.demo.BaseListSample#setItemsData(java.util.List)
     */
	@Override
	protected void setItemsData(List<Object> items) {
		// TODO Auto-generated method stub
        My_StateListDrawable d1;
        Bitmap newbmp = Bitmap.createBitmap(36, 36, Bitmap.Config.ARGB_8888); 
        BitmapDrawable d3 = new BitmapDrawable(getResources(), newbmp);

        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.start_en), 0xFF);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.start_en), 0x40);
        items.add(new Item("Start", d1));
        //d1 = null;
        
        /*d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.pause_en));
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.pause_dis));
        items.add(new Item("Pause", d1));*/
        //d1 = null;
        
        d1 = new My_StateListDrawable(this);
        //d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.stop_en), 0xFF);
        //d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.stop_en), 0x40);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.restart), 0xFF);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.restart), 0x40);
        items.add(new Item("End", d1));
        //d1 = null;
        
        d1 = new My_StateListDrawable(this);
        //d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.previous_en), 0xFF);
        //d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.previous_en), 0x40);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.undo), 0xFF);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.undo), 0x40);
        items.add(new Item("Undo", d1));
        //d1 = null;
        
        d1 = new My_StateListDrawable(this);
        //d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.next_en), 0xFF);
        //d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.next_en), 0x40);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.redo), 0xFF);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.redo), 0x40);
        items.add(new Item("Redo", d1));
        //d1 = null;

        //items.add(new Item("Well selection", (BitmapDrawable)d3));
        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.home), 0xFF);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.home), 0x40);
        items.add(new Item("Home", d1));
        
        /*20140207 added by michael
         * use spinner to implement combobox for selection of pipetting scanning methods*/
        Item_spinner item_spinner;
        item_spinner = new Item_spinner(new String [] {"Single-well", "Multiple-well", "Auto"}, Pipetting_mode_selection);
        //items.add(item_spinner);
        
        //R.layout.dialog_preference
        items.add(new Item("Preference", d3));
	}

	public void Preference_Dialog_Cancel(View v) {
		return;
	}
	/*20131126 add by michael
	 * take View.setEnabled() to update the menu item view state enabled/disabled textColor & compoundDrawable appearance in ListView
	 * (non-Javadoc)
	 * @see com.example.demo.BaseListSample#update_item_state()
	 */
	/*20131217 modified by michael*/
	static My_StateListDrawable d1, d2;
	static Bitmap newbmp = Bitmap.createBitmap(36, 36, Bitmap.Config.ARGB_8888);
	static BitmapDrawable d3;
	int last_item_state = -1, change_item_state = 0;
	@Override
	protected void update_item_state() {
		// TODO Auto-generated method stub
		int i, j, start, item_state;
		View v;
		if (d3 == null) {
        //My_StateListDrawable d1, d2;
        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.run1), 0xFF);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.run1), 0x40);
        d2 = new My_StateListDrawable(this);
        d2.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.pause1), 0xFF);
        d2.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.pause1), 0x40);
        //Bitmap newbmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888); 
        //BitmapDrawable d3 = new BitmapDrawable(getResources(), newbmp);
        d3 = new BitmapDrawable(getResources(), newbmp);
		}
		
		item_state = Itracker_MI_State & Itracker_MI_MASK;

		if (last_item_state == item_state)
			return;
		else
			if (mList.getChildCount() != 0) {
				if (last_item_state != -1) {
					change_item_state = last_item_state ^ item_state;
					//last_item_state = item_state;
				}
				else {
					//last_item_state = item_state;
					change_item_state = 0x1f;
				}
				last_item_state = item_state;

			}
		/*if (item_state==0x1f)
			return;*/
		for (start = i = mList.getFirstVisiblePosition(), j = mList.getLastVisiblePosition(); i <= j; i++) {
			//if (mAdapter.getItemViewType(i)==0 && (change_item_state & (1 << i)) != 0) {
			if (mAdapter.getItemViewType(i)==0) {
				v = mList.getChildAt(i-start);
				switch (i) {
				/*20131203 added by michael
				 * integrate start and pause into a single compound button¡Acustomer define this function is `Start/Pause`*/
				case Itracker_MI_Start:
					if ((item_state & (1 << Itracker_MI_Start)) == (1 << Itracker_MI_Start)) {
						this.items.set(0, new Item("Run", d1));
						((TextView) v).setText("Run");
						v.setEnabled(true);
					}
					else
						if ((item_state & (1 << Itracker_MI_Pause)) == (1 << Itracker_MI_Pause)) {
							this.items.set(0, new Item("Pause", d2));
							((TextView) v).setText("Pause");
							v.setEnabled(true);
						}
						else {
							this.items.set(0, new Item("Start/Pause", d3));
							((TextView) v).setText("Start/Pause");
							v.setEnabled(false);
						}
					mAdapter.syncItems(items);
		            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
		                ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(((Item) items.get(0)).mIconRes, 0, 0, 0);
		            } else {*/
		                //tv.setCompoundDrawablesWithIntrinsicBounds(((Item) item).mIconRes, 0, 0, 0);
		            	((TextView) v).setCompoundDrawablesWithIntrinsicBounds(((Item) items.get(0)).mDrawable, null, null, null);
		            //}
					break;

				/*20131204 modified by michael
				 * customer define the function is `End`*/
				case (Itracker_MI_Stop-1):
					if ((item_state & (1 << Itracker_MI_Stop)) == (1 << Itracker_MI_Stop))
						v.setEnabled(true);
					else
						v.setEnabled(false);
					break;

				/*case Itracker_MI_Pause:
					if ((item_state & (1 << Itracker_MI_Pause)) == (1 << Itracker_MI_Pause))
						v.setEnabled(true);
					else
						v.setEnabled(false);
					break;*/

				/*20131204 modified by michael
				 * customer define the function is `Undo`*/
				case (Itracker_MI_Previos_Tran-1):
					if ((item_state & (1 << Itracker_MI_Previos_Tran)) == (1 << Itracker_MI_Previos_Tran))
						v.setEnabled(true);
					else
						v.setEnabled(false);
					break;
					
				/*20131204 modified by michael
				 * customer define the function is `Redo`*/
				case (Itracker_MI_Next_Tran-1):
					if ((item_state & (1 << Itracker_MI_Next_Tran)) == (1 << Itracker_MI_Next_Tran))
						v.setEnabled(true);
					else
						v.setEnabled(false);
					break;
				}
			}
		}
		
		/*20131203 added by michael
		 * trigger a update event to update canvas on the SurfaceView for the status indicator*/
		/*20140604 modified by michael
		 * comment out source code¡Acustomer suggest using another static icons denote the usb connect/disconnect and device running/not running
		 * 3 profile mode denote by different alpha value
		 */
		//if (mGif != null)
			//mGif.Resume_thread();
		if (running_status_v != null) {
		if ((mItrackerState & (1 << Itracker_State_isConnect)) == 0) {
			running_status_v.setImageDrawable(getResources().getDrawable(R.drawable.red_circle));
			//running_status_v.setImageResource(R.drawable.pause1);
			connection_status_v.setImageResource(R.drawable.usb_disconnection);
			//Well_View.getBackground().setAlpha(255);
			Well_View.setAlpha(0.5f);
		}
		else {
			connection_status_v.setImageResource(R.drawable.usb_connection);
			if ((mItrackerState & (1 << Itracker_State_isRunning)) == 1) {
				running_status_v.setImageDrawable(getResources().getDrawable(R.drawable.green_circle));
				//running_status_v.setImageResource(R.drawable.pause1);
				//Well_View.getBackground().setAlpha(0);
				Well_View.setAlpha(1.0f);
			}
			else {
				running_status_v.setImageDrawable(getResources().getDrawable(R.drawable.red_circle));
				//running_status_v.setImageResource(R.drawable.run1);
				//Well_View.getBackground().setAlpha(255);
				Well_View.setAlpha(0.75f);
			}
		}
		}
	}

	/*20131127 added by michael
	 * according the software running state then modify Listview item enable/disable state
	 * the ListView item enable/disable mask the permission of OnItemClick() event on certain position
	 * */
	@Override
	public boolean getItemEnable(int position) {
		// TODO Auto-generated method stub
		int item_state;
		
		item_state = Itracker_MI_State & Itracker_MI_MASK;
		switch (position) {
		case Itracker_MI_Start:
			if ((item_state & (1 << Itracker_MI_Start)) == (1 << Itracker_MI_Start))
				return true;
			else
				if ((item_state & (1 << Itracker_MI_Pause)) == (1 << Itracker_MI_Pause))
					return true;
				else
					return false;
		case (Itracker_MI_Stop-1):
			if ((item_state & (1 << Itracker_MI_Stop)) == (1 << Itracker_MI_Stop))
				return true;
			else
				return false;
/*		case Itracker_MI_Pause:
			if ((item_state & (1 << Itracker_MI_Pause)) == (1 << Itracker_MI_Pause))
				return true;
			else
				return false;*/
		case (Itracker_MI_Previos_Tran-1):
			if ((item_state & (1 << Itracker_MI_Previos_Tran)) == (1 << Itracker_MI_Previos_Tran))
				return true;
			else
				return false;
		case (Itracker_MI_Next_Tran-1):
			if ((item_state & (1 << Itracker_MI_Next_Tran)) == (1 << Itracker_MI_Next_Tran))
				return true;
			else
				return false;
		}
		return true;
	}
	
	/*20131205 added by michael
	 * catch the HW back key button pressed event¡Alock system default function to finish the app¡Aprevent user touch the HW key carelessly*/
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK ) {
	        //do your stuff
	    }
	    return super.onKeyDown(keyCode, event);
	}*/
	public void onBackPressed() {
		//super.onBackPressed();
		/*File sdcard = Environment.getExternalStorageDirectory();
		final String iTracker_Data_Dir = sdcard + "/iTracker"; 
		File iTracker_MetaData = new File(iTracker_Data_Dir);
		File iTracker_logfile;
		if (sdcard.exists() && iTracker_MetaData.exists()) {
			
		}
		else {
			iTracker_MetaData.mkdirs();
			Log.d(this.toString(), "directory exist:"+Boolean.toString(iTracker_MetaData.exists()));
		}
		
		if (iTracker_MetaData.exists()) {
			iTracker_logfile = new File(iTracker_MetaData, "log.txt");
			try {
				BufferedWriter buf = new BufferedWriter(new FileWriter(iTracker_logfile));
				buf.write("knight", 0, "knight".length());
				buf.newLine();
				buf.flush();
				buf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

    }
	
	/*20140210 added by michael
	 *collapse the single-well¡Bmultiple-well mode¡Bauto mode selections into a spinner as one item within menudrawer listview menu*/
	AdapterView.OnItemSelectedListener Pipetting_mode_selection = new AdapterView.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			Log.d(Tag, "spinner selection");
			Cur_Pipetting_Mode = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			Log.d(Tag, "spinner no selection");
		}
		
	};
}
