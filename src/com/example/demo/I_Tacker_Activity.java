package com.example.demo;

import gif.decoder.GifRun;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
	static InternalHandler  mHandler;

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
						/*20140730 added by michael
						 * when disconnect i-track device then reset mItrack_dev device fw & hw info*/
						mItracker_dev.Reset_Device_Info();
						Refresh_About_Dialog();
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
							/*20140731 added by michael
							 * User granted usb operation permission¡Ai-track device online and */
							Running_Firmware_MD5_checksum();
							mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_FW_HEADER, 0, 1, dataBytes, 1);
							Refresh_About_Dialog();
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
			mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 0, 0, null, 1);
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
	ThreadPoolExecutor polling_data_executor, general_task_executor;
	/*20131212 added by michael
	 * output log file to record information*/
	File sdcard = Environment.getExternalStorageDirectory();
	final String iTracker_Data_Dir = sdcard.getPath() + "/iTracker"; 
	File iTracker_MetaData = new File(iTracker_Data_Dir);
	File iTracker_logfile;
	static BufferedWriter log_file_buf;
	/*20140211 added by michael
	 * preference dialog*/
	public Dialog preference_dialog = null, about_dialog = null;
	LinearLayout preference_dialog_layout, about_dialog_layout;
	Spinner spinner = null;
	/*20140303 added by michael
	 * current pipetting detection mode selection*/
	int Pipetting_Mode = -1, Cur_Pipetting_Mode;
	boolean Adjust_Detection_Sensitivity = false, Cur_Adjust_Detection_Sensitivity;
	int Pipetting_Sensitivity_Level = -1, Cur_Detection_Sensitivity_Level = -1;
	/*20140605 added by michael*/
	ImageView running_status_v, connection_status_v;
	/*20140729 added by michael*/
	public URL url;
	private ArrayList<URL> url_list = new ArrayList<URL>();
	private Iterator URL_list_itrator;
	final String Http_Repo_Host = "https://googledrive.com/host/0By-Tp-CAFbFyc042TGZmeWFfZWs/";
	private String MD5_list_filename = "iTrack_md5_list.txt"; 
	public String files_MD5_list = Http_Repo_Host + MD5_list_filename;
	public String app_filename = "ItrackerDemo-20131125-google-repo.apk";
	public String firmware_filename = "ads7953.release";
	//final String iTrack_Cache_Dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "//";
	public String iTrack_Cache_Dir;
	public String DL_fileExtenstion, DL_filename;
	/*20140813 added by michael
	 * dataBytes used by UI thread¡AdataBytes1 used by worker thread */
	byte[] dataBytes = new byte[1024];
	byte[] dataBytes1 = new byte[1024];
	FileInputStream fis;
	FileOutputStream fos;
	public static final String PREFS_NAME = "MyPrefsFile";
	private SharedPreferences preference;
	private Editor preference_editor;
	private int versionCode;
	private String versionName;
	private String [] parsed_version;
	private static final int Msg_Refresh_About_Dlg = 0x10;
	private static final int Msg_Upgrade_App = 0x11;
	private static final int Msg_Upgrade_Firmware = 0x12;
	private static final int Msg_Show_Upgrade_Progress = 0x13;
	private static final int Msg_Upgrade_Error = 0x14;
	private static final int Msg_Next_Download = 0x15;
	private static final int Msg_Cancel_Dlg =0x16;
	public ProgressBar inderterminate_progressbar;
	public boolean app_up_to_date = false, firmware_up_to_date = false, force_upgrade = false;
	public String Upgrade_Error_Message;
	public TextView about_status_msg = null;
	/*20140822 added by michael*/
	iTrack_Properties app_properties;
	/*20140918 added by michael*/
	ConnectivityReceiver network_status_receiver;
	ConnectivityReceiver.OnNetworkAvailableListener network_available_listener;
	DownloadFilesTask current_download;
	int download_phase = -1;
	double menu_height_mm, menu_item_pixels, menu_icon_pixels;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	app_properties = new iTrack_Properties();
    	app_properties.load_property();
		super.onCreate(savedInstanceState);
		mTmpTextView = new TextView(this);
		mLayout_Content = (FrameLayout) this.findViewById(android.R.id.content);
		mLayout_Content.addView(mTmpTextView);
		//timerStart();
		mTimer = new Timer();
		mHandler= new InternalHandler();
		//iTracker_polling_thread = new Thread(iTracker_DataRefreshTask);
		polling_data_executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		general_task_executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
		
		/*20140728 added by michael*/
		mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_NONE);
		
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
					if (Connect_Itracker(true)) {
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
					mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 0, 0, null, 1);
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
		
		/*20140730 added by michael*/
		preference = this.getSharedPreferences(PREFS_NAME, 0);
		preference_editor = preference.edit();
		String md5_checksum = Running_App_MD5_checksum();
		Log.d(Tag, "running app md5 checksum: " + md5_checksum);
		preference_editor.putString("local app checksum", md5_checksum);
		preference_editor.commit();
		//Running_App_MD5_checksum();
		
    	PackageManager pm = getPackageManager();
    	PackageInfo pkginfo =null;
    	ApplicationInfo App_Info =null;
    	
    	try {
    		pkginfo = pm.getPackageInfo(getPackageName(), 0);
			//App_Info = pm.getApplicationInfo(this.getPackageName(), 0);
    		App_Info = pkginfo.applicationInfo;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	versionCode = pkginfo.versionCode;
    	versionName = pkginfo.versionName;
    	parsed_version = versionName.split(Pattern.quote("."));
    	MD5_list_filename = "iTrack_md5_list.txt";
    	MD5_list_filename = MD5_list_filename.substring(0, MD5_list_filename.indexOf("."));
    	MD5_list_filename = MD5_list_filename + "_app_ver_" + parsed_version[0] + ".txt";
    	files_MD5_list = Http_Repo_Host + MD5_list_filename;
    	
    	//Log.d(Tag, "usb host feature:" + Boolean.toString(getPackageManager().hasSystemFeature("android.hardware.usb.host")));
    	//Log.d(Tag, "usb accessory feature:" + Boolean.toString(getPackageManager().hasSystemFeature("android.hardware.usb.accessory")));
    	//app_properties = new iTrack_Properties();
    	//app_properties.load_property();
    	this.setRequestedOrientation(Integer.valueOf(app_properties.getProperty(iTrack_Properties.prop_portrait, "1")));
    	menu_height_mm = Double.valueOf(app_properties.getProperty(iTrack_Properties.prop_viewable_height)); 
    	
    	//Log.d(Tag, "cpu cores: " + Integer.toString(this.getNumCores()) );
    	//Log.d(Tag, "cpu processors: " + Integer.toString(Runtime.getRuntime().availableProcessors()) );
    	
    	/*20140918 added by michael*/
    	network_status_receiver = new ConnectivityReceiver ( this );
    	network_available_listener = new ConnectivityReceiver.OnNetworkAvailableListener() {
    		Message message;
    		Button dlgbtn_update;

			@Override
			public void onNetworkAvailable() {
				// TODO Auto-generated method stub
				if ( url_list.size() > 0 && current_download != null && url_list.get(0) == current_download.url ) {
					if (current_download.isTaskFinish) {
						url_list.remove(url_list.get(0));
						Next_Download();
					}
				}
				else
					Next_Download();
				if (about_dialog != null && about_dialog.isShowing()) {
					message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Upgrade_Error, "status: network connection");
					message.sendToTarget();
				}
				if (download_phase==1 && about_dialog != null && about_dialog.isShowing()) {
					dlgbtn_update = (Button)about_dialog_layout.findViewById(R.id.update_btn);
					dlgbtn_update.setEnabled(true);
				}
			}

			@Override
			public void onNetworkUnavailable() {
				// TODO Auto-generated method stub
				if (current_download != null) {
					current_download.cancel(true);
					current_download = null;
					if (about_dialog != null && about_dialog.isShowing()) {
						inderterminate_progressbar.setVisibility(View.INVISIBLE);
						about_dialog.setCancelable( true );
					}
				}
				if (about_dialog != null && about_dialog.isShowing()) {
					message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Upgrade_Error, "status: network disconnection");
					message.sendToTarget();
				}
				if (download_phase==1 && about_dialog != null && about_dialog.isShowing()) {
					dlgbtn_update = (Button)about_dialog_layout.findViewById(R.id.update_btn);
					dlgbtn_update.setEnabled(false);
				}
			}
    		
    	};
    	network_status_receiver.setOnNetworkAvailableListener(network_available_listener);
    	//Log.d(Tag, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() );
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
	    	result = mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_DATA, 0, 0, null, 0);
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
				
				if (mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_DATA, 0, 0, null, 0))
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
		/*20141022 added by michael
		 * add the option to allow reverse the file list ording*/
		intent.putExtra(FileChooserActivity.INPUT_REVERSE_FILELIST_ORDER, true);
		/*20140819 added by michael
		 * add the additional extra param INPUT_ACTIVITY_ORIENTATION to handle the activity screen orientation¡Athese values(SCREEN_ORIENTATION_SENSOR_PORTRAIT¡BSCREEN_ORIENTATION_REVERSE_PORTRAIT...etc) define in  class ActivityInfo*/
		intent.putExtra(LogFileChooserActivity.INPUT_ACTIVITY_ORIENTATION, getRequestedOrientation());
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
		/*20140805 added by michael
		 * Restarting App after upgrade app*/
		if (requestCode==1000) {
			Log.d(Tag, "ResultCode: " + Integer.toString(resultCode));
			/*intent = getIntent();
			finish();
			startActivity(intent);*/
			Refresh_About_Dialog();
		}		
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
		if (Well_Selection==mItracker_dev.Well_96) {
			//Well_View.setWell(I_Tracker_Well_Plate_View.Wells_96);
			Well_View.setWell(I_Tracker_Well_Plate_View.Wells_96, app_properties, false);
		}
		else {
			//Well_View.setWell(I_Tracker_Well_Plate_View.Wells_384);
			Well_View.setWell(I_Tracker_Well_Plate_View.Wells_384, app_properties, false);
		}
		mLayout_Content.removeAllViews();
		mLayout_Content.addView(Well_View);
		mLayout_Content.addView(mIndicators_Layout);
		//setContentView(Well_View);
		//EnumerationDevice(getIntent());
		mItracker_dev.Well_Plate_Mode = Well_Selection;
		Well_View.DrawBitmap(false);

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
	static int running_count = 0;
    public boolean Connect_Itracker(boolean detect_led_sensor) {
    	//mItracker_dev.setInterface();
/*    	Log.d(Tag, "cmd type size:"+Integer.toString(I_Tracker_Device.CMD_T.SZ_CMD_T));*/
    	int nRetry = 0;
		if (mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_SETTING, 0, 0, null, 1)
				&& mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_START, 0, 0, null, 1)) {
			if ( detect_led_sensor == true ) {
			/*read i-track data once to retrive Led & Sensor failure info */
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(Tag, "i-track running: " + Integer.toString(running_count++));
			while ( mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_DATA, 0, 0, null, 0) && mItracker_dev.failure_detect_ready == 0 ) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				nRetry++;
			}
			Well_View.set_led_sensor_failure(mItracker_dev.X_Led_failure, mItracker_dev.X_Sensor_failure, mItracker_dev.Y_Led_failure, mItracker_dev.Y_Sensor_failure);
			Well_View.DrawBitmap(true);
			}
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
    		//p = Runtime.getRuntime().exec("/system/xbin/su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			DataInputStream is = new DataInputStream(p.getInputStream());
			os.writeBytes("service call activity 42 s16 com.android.systemui\n");
			try { //Thread.sleep(5000);
				os.flush();
				p.wait(100);
			} 
			catch(Exception ex) {
			}
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
    	network_status_receiver.bind(this);
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
    		//p = Runtime.getRuntime().exec("/system/xbin/su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			DataInputStream is = new DataInputStream(p.getInputStream());
			os.writeBytes("am startservice -n com.android.systemui/.SystemUIService\n");
			try { 
				//Thread.sleep(5000);
				os.flush();
				p.wait(100);
			} catch(Exception ex) {}
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
    	network_status_receiver.unbind(this);
    }


    @Override
    protected void onPause() {
    	super.onPause();
    	turn_on_wifi();
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
		turn_off_wifi();
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
				/*20140731 added by michael
				 * read i-track device firmware md5 checksum immediately when i-track device attach*/
				Running_Firmware_MD5_checksum();
				mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_FW_HEADER, 0, 1, dataBytes, 1);
				Refresh_About_Dialog();
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
    				Itracker_MI_State = 1 << Itracker_MI_Start;
    				if ((mItrackerState &( 1 << Itracker_State_isRunning))==1) {
    					Itracker_MI_State ^= 1 << Itracker_MI_Start;
    					Itracker_MI_State ^= 1 << Itracker_MI_Pause;
    					mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 0, 0, null, 1);
    					if (Connect_Itracker(true)) {
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
    						Start_Refresh_iTracker_Data_Thread();
    					}
    				}
    				else {
    					//Itracker_MI_State = 1 << Itracker_MI_Start;
    					mItrackerState = 0;
    				}
    				Show_Toast_Msg(I_Tracker_Device_Conn);
    				mItrackerState |= 1 << Itracker_State_isConnect;
    				
    				//if (mGif != null)
    					//mGif.Resume_thread();
    				/*20140731 added by michael
    				 * read i-track device firmware md5 checksum immediately when i-track device attach*/
    				Running_Firmware_MD5_checksum();
    				mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_FW_HEADER, 0, 1, dataBytes, 1);
    				Refresh_About_Dialog();
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
    
    /*20140729 added by michael*/
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /*20140729 added by michael*/
    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    
    /*20140729 added by michael*/
    private class DownloadFilesTask extends AsyncTask<URL, Integer, Integer> {
    	public boolean isDownloadSuccess, isTaskFinish;
    	URL url;

    	DownloadFilesTask( URL... urls ) {
    		url = urls[0];
    	}
		@Override
		protected Integer doInBackground(URL... urls) {
			// TODO Auto-generated method stub			
			HttpURLConnection connection = null;
			int fileLength = -1, nRead_Bytes;
			InputStream input = null;
			OutputStream output = null;
			long totalSize = 0;
			Message message;
			
			isDownloadSuccess = false;
			isTaskFinish = false;
			try {
				connection = (HttpURLConnection) urls[0].openConnection();
				connection .setRequestProperty("Accept-Encoding", "identity");
				connection.setInstanceFollowRedirects(true);
				connection.connect();

				DL_fileExtenstion = MimeTypeMap.getFileExtensionFromUrl(urls[0].toString());
				DL_filename = URLUtil.guessFileName(urls[0].toString(), null, DL_fileExtenstion);
				Log.d (Tag, DL_filename);
				iTrack_Cache_Dir = I_Tacker_Activity.this.getCacheDir().getPath() + "//";
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
	                 Log.d(Tag, "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
	                 I_Tacker_Activity.this.Upgrade_Error_Message = DL_filename + ", " + "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
	     			 message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Upgrade_Error, Upgrade_Error_Message);
					 message.sendToTarget();
	                 
	                 if (connection.getResponseCode() == 301) {
	                	 connection.setInstanceFollowRedirects(false);
	                	 String redirect_link = connection.getHeaderField("Location");
	                	 Log.d(Tag, redirect_link);
	                	 connection.connect();
	                 }
	                 else {
	                 }
				}
				
				/*DL_fileExtenstion = MimeTypeMap.getFileExtensionFromUrl(urls[0].toString());
				DL_filename = URLUtil.guessFileName(urls[0].toString(), null, DL_fileExtenstion);
				iTrack_Cache_Dir = I_Tacker_Activity.this.getCacheDir().getPath() + "//";*/
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Show_Upgrade_Progress);
		            message.sendToTarget();					
					fileLength = connection.getContentLength();
					input = connection.getInputStream();
					/*DL_fileExtenstion = MimeTypeMap.getFileExtensionFromUrl(urls[0].toString());
					DL_filename = URLUtil.guessFileName(urls[0].toString(), null, DL_fileExtenstion);
					iTrack_Cache_Dir = I_Tacker_Activity.this.getCacheDir().getPath() + "//";*/
					//iTrack_Cache_Dir = "/cache//";
					Log.d(Tag, iTrack_Cache_Dir + DL_filename + DL_fileExtenstion);
					output = new FileOutputStream(iTrack_Cache_Dir + DL_filename);
					
		             while ((nRead_Bytes = input.read(dataBytes1)) != -1) {
		                 totalSize += nRead_Bytes;
		                 // publishing the progress....
		                 if (fileLength > 0) // only if total length is known
		                     publishProgress((int) (totalSize * 100 / fileLength));
		                 output.write(dataBytes1, 0, nRead_Bytes);
		             }
					isDownloadSuccess = true;
				}
				else {
					isDownloadSuccess = false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isDownloadSuccess = false;
			}
			finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (connection != null)
					connection.disconnect();
	         }
			
			isTaskFinish = true;
            
			/*20140731 added by michael
			 * download finish */
			//Log.d (Tag, DL_filename);
			if (DL_filename!=null && DL_filename.equals(MD5_list_filename)) {
				download_phase = 1;
				message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Upgrade_Error, "status: dowloand  " + MD5_list_filename + "  finish");
				message.sendToTarget();
				message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Refresh_About_Dlg);
	            message.sendToTarget();
			}
			else
				if (DL_filename!=null && DL_filename.equals(app_filename)) {
					download_phase = 2;
					message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Upgrade_Error, "status: dowloand  " + app_filename + "  finish");
					message.sendToTarget();
					message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Upgrade_App);
		            message.sendToTarget();					
				}
				else
					if (DL_filename!=null && DL_filename.equals(firmware_filename)) {
						download_phase = 3;
						message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Upgrade_Error, "status: dowloand  " + firmware_filename + "  finish");
						message.sendToTarget();
						message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Upgrade_Firmware);
			            message.sendToTarget();
					}

			if (isDownloadSuccess==false)
			  return -1;
			else {
				return 0;
			}
		}    	
    }
    
    /*20140730 added by michael
     * validation a given string is the md5 checksum*/
    public boolean isValidMD5(String s) {
    	if (s!=null)
    		return s.matches("[a-fA-F0-9]{32}");
    	else
    		return false;
    }
    
    /*20140730 added by michael*/
    private class InternalHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
        	  case Msg_Upgrade_Error:
        		  if (about_dialog_layout == null)
        			  about_dialog_layout = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_about, null);
        		  about_status_msg = (TextView)about_dialog_layout.findViewById(R.id.status);
        		  if (about_status_msg != null) {
        			  Log.d (Tag, (String)msg.obj);
        			  about_status_msg.setText((String) msg.obj);
        		  }
        		  break;
        	  case Msg_Show_Upgrade_Progress:
				  if (about_dialog != null && about_dialog.isShowing()) {
					  inderterminate_progressbar.setVisibility(View.INVISIBLE);
					  about_dialog.setCancelable( false );
				  }
        		  //inderterminate_progressbar.setVisibility(View.VISIBLE);        	      
        	      break;
        	  case Msg_Refresh_About_Dlg:
        		  CheckBox checkbox1 = null;
        		  if ( url_list.size() > 0 )
        			  url_list.remove(url_list.get(0));
        		  checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
        		  checkbox1.setEnabled( true );
        		  Refresh_About_Dialog();
        		  inderterminate_progressbar.setVisibility(View.INVISIBLE);
        		  about_dialog.setCancelable(true);  				
        		  break;
        	  case Msg_Upgrade_App:
        		  if ( url_list.size() > 0 )
        			  url_list.remove(url_list.get(0));
        		  general_task_executor.execute(upgrade_app_runnable);
        		  break;

        	  case Msg_Upgrade_Firmware:
        		  if ( url_list.size() > 0 )
        			  url_list.remove(url_list.get(0));
        		  general_task_executor.execute(upgrade_firmware_runnable);
        		  break;
        	  case Msg_Cancel_Dlg:
        		  turn_off_wifi();
        		  break;
        	}
        }
    }
    
    /*20140730 added by michael*/
    private String Running_App_MD5_checksum() {
		PackageManager pm = getPackageManager();
		ApplicationInfo App_Info =null;
		MessageDigest md =null;
		//FileInputStream fis = null;
		int nReadBytes = 0;
		StringBuffer sb = new StringBuffer("");

		Log.d(Tag, "Running package: " + this.getPackageName());
		try {
			App_Info = pm.getApplicationInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (App_Info != null) {
			Log.d(Tag, "Source dir: " + App_Info.sourceDir);
			Log.d(Tag, "Source dir: " + App_Info.publicSourceDir);

		    try {
		    	md = MessageDigest.getInstance("MD5");
				fis = new FileInputStream(App_Info.sourceDir);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    
		    try {
				while ((nReadBytes = fis.read(dataBytes)) != -1) {
				    md.update(dataBytes, 0, nReadBytes);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
		   
		    byte[] mdbytes = md.digest();
		  //convert the byte to hex format
		    for (int i = 0; i < mdbytes.length; i++) {
		    	Log.d(Tag, "integer: " + Integer.toHexString((mdbytes[i])));
		    	Log.d(Tag, "integer1: " + Integer.toString(((mdbytes[i] & 0xff) + 0x100), 16).substring(1));
		    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
		    Log.d(Tag, "MD5 checksum: " + sb.toString());
		}

    	return sb.toString();
    }
    
    /*20140731 added by michael*/
    private String Running_Firmware_MD5_checksum() {
    	return null;
    }
    
    /*20140731 added by michael
     * get current installed i-track app brief version description*/
    public String getAppDesc() {
    	//return "versionCode=" + Integer.toString(versionCode) + "  " + "versionName=" + versionName;
    	return Integer.toString(versionCode) + "---"  + versionName;
    }
    
    public String getFirmwareDesc() {
    	//return "versionCode=" + mItracker_dev.Fw_Version_Code + "  " + "versionName=" + mItracker_dev.Fw_Version_Name;
    	return Integer.toString(mItracker_dev.Fw_Version_Code) + "---" +  mItracker_dev.Fw_Version_Name;
    }
    
    void Refresh_About_Dialog() {
		TextView iTrack_firmware_ver_desc = null, iTrack_app_ver_desc = null;
		CheckBox checkbox1;
	    Button dlgbtn_update;
	    File f;

    	if (about_dialog!=null && about_dialog.isShowing()) {
			iTrack_firmware_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.firmware_info);
			iTrack_app_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.app_info);
			checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
			force_upgrade = checkbox1.isChecked();
			dlgbtn_update = (Button)about_dialog_layout.findViewById(R.id.update_btn);
			dlgbtn_update.setEnabled(false);
			
			Properties defaultProps = new Properties();
			String server_app_md5, server_firmware_md5, local_app_md5, local_firmware_md5;
			Color color;
			local_app_md5 = preference.getString("local app checksum", "");
			local_firmware_md5 = mItracker_dev.Fw_md5_checksum;
			try {
				f = new File(iTrack_Cache_Dir + this.MD5_list_filename);
				if (f.exists()) {
					fis = new FileInputStream(f);
					defaultProps.load(fis);
					fis.close();
				}
				server_app_md5 = defaultProps.getProperty("app", "");
				server_firmware_md5 = defaultProps.getProperty("firmware", "");
				
				Log.d(Tag, "app= " + server_app_md5); 
				Log.d(Tag, "app Md5 found: " + Boolean.toString(isValidMD5(server_app_md5)));
				if (isValidMD5(local_app_md5)) {
					if (isValidMD5(server_app_md5)) {
						if (server_app_md5.equalsIgnoreCase(local_app_md5)) {
							iTrack_app_ver_desc.setText("iTrack App ver.:  " + getAppDesc() + "(up-to-date)");
							if (force_upgrade && is_internet_available())
								dlgbtn_update.setEnabled(true);
							else
								dlgbtn_update.setEnabled(false);
						}
						else {
							iTrack_app_ver_desc.setText("iTrack App ver.:  " + getAppDesc() + "(out-of-date)");
							if (is_internet_available())
								dlgbtn_update.setEnabled(true);
						}										
					}
					else
						iTrack_app_ver_desc.setText("iTrack App ver.:  " + getAppDesc());
				}
				else {
					iTrack_app_ver_desc.setText("iTrack App ver.:  ");
				}
					
				Log.d(Tag, "firmware= " + server_firmware_md5);
				Log.d(Tag, "firmware Md5 found: " + Boolean.toString(isValidMD5(server_firmware_md5)));
				if (isValidMD5(local_firmware_md5)) {
					if (isValidMD5(server_firmware_md5)) {
						if (server_firmware_md5.equalsIgnoreCase(local_firmware_md5)) {
							iTrack_firmware_ver_desc.setText("iTrack Firmware ver.:  " + getFirmwareDesc() + "(up-to-date)");
							if (force_upgrade && is_internet_available())
								dlgbtn_update.setEnabled(true);
							else
								if (dlgbtn_update.isEnabled()==false)
									dlgbtn_update.setEnabled(false);
						}
						else {
							iTrack_firmware_ver_desc.setText("iTrack Firmware ver.:  " + getFirmwareDesc() + "(out-of-date)");
							if (is_internet_available())
								dlgbtn_update.setEnabled(true);
						}
					}
					else {
						iTrack_firmware_ver_desc.setText("iTrack Firmware ver.:  ");
					}
				}
				else {
					iTrack_firmware_ver_desc.setText("iTrack Firmware ver.:  ");
				}				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    }
    
    /*20140805 added by michael*/
    String get_upgrade_app_filename() {
    	//return  "ItrackerDemo-20131125-google-repo" + ".apk";
    	app_filename = "ItrackerDemo-20131125-google-repo.apk";
    	app_filename = app_filename.substring(0, app_filename.indexOf("."));
    	app_filename = app_filename + "_app_ver_" + parsed_version[0] + ".apk";
    	return app_filename;
    } 

    String get_upgrade_firmware_filename() {
    	//return  "ads7953" + ".release";
    	return firmware_filename;
    } 
    
    /*20140811 added by michael
     * clear app cache */
    public static void trimCache(Context context) {
    	try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
            	deleteDir(dir);
            }
         } catch (Exception e) {
            // TODO: handle exception
         }	
    }
    
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
           String[] children = dir.list();
           for (int i = 0; i < children.length; i++) {
              boolean success = deleteDir(new File(dir, children[i]));
              if (!success) {
                 return false;
              }
           }
        }

        // The directory is now empty so delete it
        return dir.delete();
     }
    
    public View.OnClickListener Upgrade_listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			CheckBox checkbox1;
			
			v.setEnabled(false);
			checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
			//checkbox1.setChecked(false);
			checkbox1.setEnabled(false);
			//inderterminate_progressbar.setVisibility(View.VISIBLE);
			Upgrade_System(v);
			about_dialog.setCancelable(false);
		}
    	
    };
    public void Upgrade_System(View v) {
    	TextView iTrack_firmware_ver_desc = null, iTrack_app_ver_desc = null;
    	//DownloadFilesTask downloadTask, downloadTask1;
    	
		iTrack_firmware_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.firmware_info);
		iTrack_app_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.app_info);
		about_status_msg = (TextView)about_dialog_layout.findViewById(R.id.status);
		about_status_msg.setText("");
		url_list.clear();

		//downloadTask = new DownloadFilesTask();
		try {
			if ((iTrack_firmware_ver_desc != null && iTrack_firmware_ver_desc.getText().toString().contains("out-of-date")) || force_upgrade==true) {
				firmware_up_to_date = false;
				url = new URL(Http_Repo_Host + get_upgrade_firmware_filename());
				url_list.add(url);
				//downloadTask.execute(url);
			}
			else
				firmware_up_to_date = true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//downloadTask1 = new DownloadFilesTask();
		try {
			if ((iTrack_app_ver_desc != null && iTrack_app_ver_desc.getText().toString().contains("out-of-date")) || force_upgrade==true) {
				app_up_to_date = false;
				url = new URL(Http_Repo_Host + get_upgrade_app_filename());
				url_list.add(url);
				//downloadTask1.execute(url);
			}
			else
				app_up_to_date = true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//URL_list_itrator = url_list.iterator();
		Next_Download();
    }
    
    /*20140814 added by michael*/
    void Next_Download() {
    	DownloadFilesTask downloadTask;
    	
    	URL_list_itrator = url_list.iterator();
		if (URL_list_itrator.hasNext()) {
			url = (URL)URL_list_itrator.next();
	    	downloadTask = new DownloadFilesTask(url);
	    	current_download = downloadTask;			
			downloadTask.execute(url);
		}
    }

    CompoundButton.OnCheckedChangeListener force_upgrade_listener = new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			//force_upgrade = isChecked;
			Refresh_About_Dialog();
		}
    	
    };
    
    /*20140814 added by michael*/
	Runnable upgrade_app_runnable = new Runnable() {
		File f, f1;
		java.lang.Process p;
		int nReadBytes;
		String result = "";
		byte[] b = new byte[256];
		byte[] b1 = new byte[256];
		//CheckBox checkbox1;
		String download_dir;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
        	f = new File(iTrack_Cache_Dir + DL_filename);
			if (f.exists()) {
				try {
					p = Runtime.getRuntime().exec("/system/xbin/su-new");
					DataOutputStream os = new DataOutputStream(p.getOutputStream());
					DataInputStream is = new DataInputStream(p.getInputStream());
					//Log.d(Tag, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() );
					download_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
					f1 = new File ( download_dir );
					if ( !f1.exists() ) {
						f1.mkdirs();
					}
					os.writeBytes("cp " + iTrack_Cache_Dir + DL_filename + " " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "//" + DL_filename + "\n");
					try {
						// Thread.sleep(100);
						os.flush();
						p.wait(100);
					} catch (Exception ex) {
					}
					while (is.available() > 0) {
						nReadBytes = is.read(dataBytes);
						if (nReadBytes <= 0)
							break;
						String seg = new String(dataBytes, 0, nReadBytes);
						result = result + seg; // result is a string to show
												// in textview
					}
					Log.d(Tag, "shell exit code: " + result);

					/*
					 * 20140814 added by michael
					 * install a package silently
					 */
					os.writeBytes("pm install -r "
							+ Environment.getExternalStoragePublicDirectory(
									Environment.DIRECTORY_DOWNLOADS).getPath()
							+ "//" + DL_filename + "\n");
					try {
						// Thread.sleep(500);
						os.flush();
						p.wait(100);
					} catch (Exception ex) {
					}
					while (is.available() > 0) {
						nReadBytes = is.read(dataBytes);
						if (nReadBytes <= 0)
							break;
						String seg = new String(dataBytes, 0, nReadBytes);
						result = result + seg; // result is a string to show
												// in textview
					}
					Log.d(Tag, "shell exit code: " + result);
					os.flush();
					os.close();
					is.close();
					p.waitFor();
					p.destroy();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				/*Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "//" + DL_filename)), "application/vnd.android.package-archive");
				Intent intent = new Intent(Intent.ACTION_DELETE,
				Uri.fromParts("package", Auto_updater_test.this.getPackageName(), null));
				startActivity(intent);*/
				
				/*PackageInfo info = I_Tacker_Activity.this.getPackageManager().getPackageArchiveInfo(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "//" + DL_filename, 0); 
				if ((info != null && info.versionCode >=I_Tacker_Activity.this.versionCode) || force_upgrade) {
					Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
					intent.setData(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "//" + DL_filename)));
					intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
					intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
					startActivityForResult(intent, 1000); 
				}*/

				/*Intent it = new Intent(Intent.ACTION_MAIN);
				it.setComponent(new ComponentName(I_Tacker_Activity.this.getPackageName(), I_Tacker_Activity.this.getPackageName() + ".App_Updater_Activity"));
				it.putExtra("Apk_filename", Environment.getExternalStoragePublicDirectory(Environment. DIRECTORY_DOWNLOADS).getPath() + "//" + DL_filename); 
				Log.d(Tag, "Msg_Upgrade_App process thread id" + Integer.toString(Process.myTid())); startActivity(it);*/
				//this.startActivityForResult(it, 0);
				//this.setResult(resultCode, data);
				//Intent intent = new Intent(this,
				//LogFileDisplayActivity.class);

			}
			app_up_to_date = true;
			Message message = null;
			if (app_up_to_date && firmware_up_to_date) {
				/*inderterminate_progressbar.setVisibility(View.INVISIBLE);
				about_dialog.setCancelable(true);
				checkbox1.setEnabled(true);
				Refresh_About_Dialog();*/				
				message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Refresh_About_Dlg);
				message.sendToTarget();
				message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Upgrade_Error, "status: upgrade complete");
				message.sendToTarget();
			}
			Next_Download();
		}
	};

	/*20140814 added by michael*/
	Runnable upgrade_firmware_runnable = new Runnable() {
    	File f;
    	int nReadBytes, nPage;
    	byte [] b = new byte [256];
    	byte [] b1 = new byte [256];
    	byte [] byte_array = new byte [1024];
    	//CheckBox checkbox1;
    	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
        	f = new File(iTrack_Cache_Dir + DL_filename);
			if (f.exists()) {
				for (int j = 0; j < 1; j++) {
					Log.d(Tag, "flash programming test iteration: " + Integer.toString(j));
					try {
						fis = new FileInputStream(f);
						nReadBytes = fis.read(byte_array, 0, 256);
						System.arraycopy(byte_array, 0, b, 0, 256);
						ByteBuffer byte_buf;
						byte_buf = ByteBuffer.allocate(256);
						byte_buf = ByteBuffer.wrap(b);
						byte_buf.order(ByteOrder.LITTLE_ENDIAN);
						byte_buf.position(56);
						/* copy original factory Hw rev. */
						byte_buf.putInt(mItracker_dev.Hw_Version_Code);
						byte_buf.position(0);
						int Fw_Version_Code = byte_buf.getInt();
						if ((Fw_Version_Code >= mItracker_dev.Fw_Version_Code) || force_upgrade) {
							/*force i-track device data flash region dirty*/
							//if (force_upgrade) {
								//mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_FW_HEADER, 0, 1, b1, 1);
							    System.arraycopy(mItracker_dev.fw_header_bytes, 0, b1, 0, 256);
								byte_buf.position(255);
								byte_buf.put((byte) (b1[255] + 1));
								Log.d(Tag, "dirty byte: " + Integer.toString(b[255], 16));
							//}
							nPage = 1;
							while ((nReadBytes = fis.read(byte_array, 0, 256)) != -1) {
								if (nReadBytes == 256)
									mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_FW_UPGRADE, nPage, 1, byte_array, 0);
								else {
									Arrays.fill(byte_array, nReadBytes, byte_array.length, (byte) 0);
									mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_FW_UPGRADE, nPage, 1, byte_array, 0);
								}
								nPage = nPage + 1;
								/*
								 * important insert sufficient delay time to
								 * avoid device buffer overrun
								 */
								try {
									Thread.sleep(5);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							try {
								Thread.sleep(5);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							nPage = 0;
							if (mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_FW_UPGRADE, nPage, 1, b, 0)) {
								Log.d(Tag, "write firmware bin data complete");
							}
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (fis != null)
							try {
								fis.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
				}
			}
			firmware_up_to_date = true;
			Message message = null;
			if (app_up_to_date && firmware_up_to_date) {
				/*inderterminate_progressbar.setVisibility(View.INVISIBLE);
				about_dialog.setCancelable(true);
				checkbox1.setEnabled(true);
				Refresh_About_Dialog();*/
				message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Refresh_About_Dlg);
				message.sendToTarget();
				message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Upgrade_Error, "status: upgrade complete");
				message.sendToTarget();
			}
			Next_Download();
		}
	};
	
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
					if (Connect_Itracker(true)) {
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
    					mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 0, 0, null, 1);
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
    				/*20141023 added by michael
    				 *  comfirm to exit current pipetting transaction*/
					Builder builder = new AlertDialog.Builder(I_Tacker_Activity.this);
					AlertDialog dialog = builder.create();
					dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
					dialog.setTitle("Do you want to end current pipetting session?");
					/*20131217 modified by michael*/
					dialog.setMessage("Warning: the current pipetting session will be terminated!");
					dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Itracker_MI_State = 1 << Itracker_MI_Start;
		                    Reset_App();
		                    Show_Toast_Msg(I_Tracker_Device_Reset);
							if (Well_View.Show_focus_coord)
								Well_View.blink_last_well();

							write_logfile_msg("End pipetting session");
							flush_close_logfile();
						}						
					});
					dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}						
					});
					dialog.getWindow().setGravity(Gravity.TOP);
					dialog.setIcon(R.drawable.ic_launcher1);
					dialog.setCancelable(false);
					dialog.show();
    				 
    				//ending the current task, clear device states & records, infos
    				//Itracker_MI_State = 1 << Itracker_MI_Start;
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
                    //Reset_App();
                    //Show_Toast_Msg(I_Tracker_Device_Reset);

                    /*20130327 added by michael*/
					//Show_focus_coord==true then call I_Tracker_Well_Plate.blink_last_well() again
					//if (Well_View.Show_focus_coord)
						//Well_View.blink_last_well();
					
					/*20131213 added by michael*/
					//write_logfile_msg("End pipetting session");
					//flush_close_logfile();
					//break;*/

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
					dialog.setTitle("Do you want to back to well plate selection?");
					/*20131217 modified by michael*/
					dialog.setMessage("Warning: the current pipetting session will be terminated!");
					dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", listenerAccept);
					dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", listenerDoesNotAccept);
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
    		    if (preference_dialog==null) {
				  preference_dialog = new Dialog(this, R.style.CenterDialog);
				  preference_dialog_layout = (LinearLayout) LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.dialog_preference, null);
				  spinner = (Spinner) preference_dialog_layout.findViewById(R.id.spinner1);
				  ArrayList<String> spinner_items = new ArrayList<String>();
				  //spinner_items.add("Auto-channel Pipet");
				  spinner_items.add("Single-channel Pipet");
				  spinner_items.add("8-channel Pipet");
				  spinner_items.add("12-channel Pipet");
				  ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_spinner_item, spinner_items);
				  adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
				  spinner.setAdapter(adapter);
    		    }
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
						/*20141023 modified by michael
						 * remove the auto pipetting mode¡Aremapped mode index*/
						mItracker_dev.set_pipetting_detection_mode(Pipetting_Mode+1);
						mItracker_dev.set_pipetting_detection_sensitivity_level(Adjust_Detection_Sensitivity, Pipetting_Sensitivity_Level);
						if ((mItrackerState & (1 << Itracker_State_isRunning)) == 1) {
							mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_STOP, 0, 0, null, 1);
							if (Connect_Itracker(false)) {
								
							}
							else {
								if (I_Tacker_Activity.mDebug_Itracker==true)
									Toast.makeText(getApplicationContext(), "Can't changing preference", Toast.LENGTH_LONG).show();
							}
						}
						else {
							mItracker_dev.Itracker_IOCTL(I_Tracker_Device.CMD_T.HID_CMD_ITRACKER_SETTING, 0, 0, null, 1);							
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
    			
    		case (6):
    			download_phase = -1;
    			turn_on_wifi();
    			TextView iTrack_firmware_ver_desc = null, iTrack_app_ver_desc = null;

    		    Button dlgbtn_update;
    		    //DownloadFilesTask downloadTask;
    		    Message message;
    			if (tv.getText()=="About") {
    				trimCache(this);
        			mMenuDrawer.toggleMenu();
        			if (about_dialog==null) {
        				about_dialog = new Dialog(this, R.style.CenterDialog);
        				if (about_dialog_layout == null) 
        					about_dialog_layout = (LinearLayout) LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.dialog_about, null);
        				about_dialog.getWindow().setGravity(Gravity.TOP);
        				about_dialog.setContentView(about_dialog_layout);
        				about_dialog.setTitle("About");
        				about_dialog.setCancelable(true);
        				about_dialog.setCancelMessage(mHandler.obtainMessage(Msg_Cancel_Dlg));
        			}
        			if (about_dialog_layout != null) {
        				iTrack_firmware_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.firmware_info);
        				iTrack_app_ver_desc = (TextView)about_dialog_layout.findViewById(R.id.app_info);
        				checkbox1 = (CheckBox) about_dialog_layout.findViewById(R.id.force_upgrade_checkBox1);
        				checkbox1.setEnabled( false );
        				dlgbtn_update = (Button)about_dialog_layout.findViewById(R.id.update_btn);
        				checkbox1.setChecked(false);
        				checkbox1.setOnCheckedChangeListener(force_upgrade_listener);
        				about_status_msg = (TextView)about_dialog_layout.findViewById(R.id.status);
        				about_status_msg.setTextColor(Color.YELLOW);
        				if ( this.is_internet_available() )
        					about_status_msg.setText("status¡G  network connection");
        				else
        					about_status_msg.setText("status¡G  network disconnection");
        				inderterminate_progressbar = (ProgressBar) about_dialog_layout.findViewById(R.id.progressBar1);
        				inderterminate_progressbar.setVisibility(View.INVISIBLE);
        				dlgbtn_update.setOnClickListener(Upgrade_listener);
        				dlgbtn_update.setEnabled(false);
        				/*20140916 added by michael*/
        				/*WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        				if (wifi.isWifiEnabled()){
        				//wifi is enabled
        					checkbox1.setEnabled( true );
        				}
        				else
        					checkbox1.setEnabled( false );*/
        				//is_internet_available();

        				/*20140729 added by michael
        				 * compare MD-5 checksums of i-track app & device firmware on server with those on local
        				 * if there are different between server & local then enable update or disable update 
        				 * */
        				//downloadTask = new DownloadFilesTask();
        				/*//if ( this.is_internet_available() ) {*/
							try {
								url = new URL(files_MD5_list);
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	        				if ( this.url_list.isEmpty() ){
	        					url_list.add(url);
	        				}
							/*downloadTask.execute(url);*/
        				//}
        				//else {
        					//message = mHandler.obtainMessage(I_Tacker_Activity.this.Msg_Refresh_About_Dlg);
        		            //message.sendToTarget();        					
        				//}

        				/*20140730 added by michael
        				 * wait for worker thread finish*/
        				/*why the follow code cause UI thread dead lock*/
        				/*do {
        					try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
        				} while(downloadTask.getStatus()!=AsyncTask.Status.FINISHED);*/
        				/*do {
        					try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}        				
        				} while (downloadTask.isTaskFinish==false);*/
        				about_dialog.show();
						//if (downloadTask.isDownloadSuccess==true) {
							//Refresh_About_Dialog();
			                 //message = mHandler.obtainMessage(this.Msg_Refresh_About_Dlg);
			                 //message.sendToTarget();
						//}
        			}


    				preference_editor.putLong("Version Code", this.versionCode);
    				preference_editor.putString("Version Name", this.versionName);
    				preference_editor.commit();
    				
    				Log.d("Environment.getDownloadCacheDirectory()", Environment.getDownloadCacheDirectory().getPath());
    				Log.d("Environment.getExternalStorageDirectory()", Environment.getExternalStorageDirectory().getPath());
    				Log.d("Environment.getExternalStoragePublicDirectory()", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
    				
    				/*20140731 added by michael
    				 * refer sizeof(struct FW_Header)*/
    				/*mItracker_dev.Itracker_IOCTL(CMD_T.HID_CMD_ITRACKER_FW_HEADER, 0, 1, dataBytes, 1);
    				
    				byte[] mdbytes = new byte[16];
    				System.arraycopy(dataBytes, 4+48+4, mdbytes, 0, 16);
    				StringBuffer sb = new StringBuffer("");
					// convert the byte to hex format
					for (int i = 0; i < mdbytes.length; i++) {
						sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
					}
					Log.d(Tag, "firmware MD5 checksum: " + sb.toString());
					Log.d(Tag, "firmware MD5 checksum: " + this.mItracker_dev.Fw_md5_checksum);*/
    			}
    		  //File f = new File("/data/data/com.example.demo/cache/ItrackerDemo-20131125-google-repo.apk");
    			//File f = new File("/mnt/sdcard/Downloads/ItrackerDemo-20131125-google-repo.apk");
    			//File f = new File("/cache/ItrackerDemo-20131125-google-repo.apk");
    			//File f = new File("/cache/test.apk");
    			/*File f = new File("/mnt/sdcard/Downloads/test.apk");
  			  Intent intent = new Intent(Intent.ACTION_VIEW);
  			  intent.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
			  //Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", Auto_updater_test.this.getPackageName(), null));
  			  startActivity(intent);*/
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
		menu_height_mm = Double.valueOf(app_properties.getProperty (  iTrack_Properties.prop_viewable_height, "61"  ));
		menu_item_pixels = convert_mm2pixel ( menu_height_mm / 7 );
		menu_icon_pixels = ((int) ( menu_item_pixels / 8 - 1 )) * 8;
        My_StateListDrawable d1;
        //Bitmap newbmp = Bitmap.createBitmap(36, 36, Bitmap.Config.ARGB_8888);
        Bitmap newbmp = Bitmap.createBitmap((int)menu_icon_pixels, (int)menu_icon_pixels, Bitmap.Config.ARGB_8888);
        BitmapDrawable d3 = new BitmapDrawable(getResources(), newbmp);

        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.start_en), 0xFF, (int)menu_icon_pixels, (int)menu_icon_pixels);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.start_en), 0x40, (int)menu_icon_pixels, (int)menu_icon_pixels);
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
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.restart), 0xFF, (int)menu_icon_pixels, (int)menu_icon_pixels);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.restart), 0x40, (int)menu_icon_pixels, (int)menu_icon_pixels);
        items.add(new Item("End", d1));
        //d1 = null;
        
        d1 = new My_StateListDrawable(this);
        //d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.previous_en), 0xFF);
        //d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.previous_en), 0x40);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.undo), 0xFF, (int)menu_icon_pixels, (int)menu_icon_pixels);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.undo), 0x40, (int)menu_icon_pixels, (int)menu_icon_pixels);
        items.add(new Item("Undo", d1));
        //d1 = null;
        
        d1 = new My_StateListDrawable(this);
        //d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.next_en), 0xFF);
        //d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.next_en), 0x40);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.redo), 0xFF, (int)menu_icon_pixels, (int)menu_icon_pixels);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.redo), 0x40, (int)menu_icon_pixels, (int)menu_icon_pixels);
        items.add(new Item("Redo", d1));
        //d1 = null;

        //items.add(new Item("Well selection", (BitmapDrawable)d3));
        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.home), 0xFF, (int)menu_icon_pixels, (int)menu_icon_pixels);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.home), 0x40, (int)menu_icon_pixels, (int)menu_icon_pixels);
        items.add(new Item("Home", d1));
        
        /*20140207 added by michael
         * use spinner to implement combobox for selection of pipetting scanning methods*/
        Item_spinner item_spinner;
        item_spinner = new Item_spinner(new String [] {"Single-well", "Multiple-well", "Auto"}, Pipetting_mode_selection);
        //items.add(item_spinner);
        
        //R.layout.dialog_preference
        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.preference), 0xFF, (int)menu_icon_pixels, (int)menu_icon_pixels);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.preference), 0x40, (int)menu_icon_pixels, (int)menu_icon_pixels);
        items.add(new Item("Preference", d1));
        
        /*20140725 added by michael
         * Adding a menu item "About"¡Athere are i-track hardware¡Bfirmware¡Bapp released version info on the dialog
         * Click the button "update" to upgrade the i-track firmware and app¡Ait's enable/disable reflect there exist latest i-track firmware or app 
         * */
        items.add(new Item("About", d3));
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
	static Bitmap newbmp;// = Bitmap.createBitmap(36, 36, Bitmap.Config.ARGB_8888);
	static BitmapDrawable d3 = null;
	int last_item_state = -1, change_item_state = 0;
	@Override
	protected void update_item_state() {
		// TODO Auto-generated method stub
		int i, j, start, item_state;
		View v;

		//My_StateListDrawable d1, d2;
		//Bitmap newbmp = Bitmap.createBitmap(36, 36, Bitmap.Config.ARGB_8888);
		//Bitmap newbmp = Bitmap.createBitmap((int)menu_icon_pixels, (int)menu_icon_pixels, Bitmap.Config.ARGB_8888);
		//BitmapDrawable d3;
		if (d3 == null) {
        //My_StateListDrawable d1, d2;
        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.run1), 0xFF, (int)menu_icon_pixels, (int)menu_icon_pixels);
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.run1), 0x40, (int)menu_icon_pixels, (int)menu_icon_pixels);
        d2 = new My_StateListDrawable(this);
        d2.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.pause1), 0xFF, (int)menu_icon_pixels, (int)menu_icon_pixels);
        d2.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.pause1), 0x40, (int)menu_icon_pixels, (int)menu_icon_pixels);
        //Bitmap newbmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888); 
        //BitmapDrawable d3 = new BitmapDrawable(getResources(), newbmp);
        newbmp = Bitmap.createBitmap((int)menu_icon_pixels, (int)menu_icon_pixels, Bitmap.Config.ARGB_8888);
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
				/*20140806 added by michael
				 * "About" menu item only enable when mItrackerState claim the system is running */
				/*case (6):
					if ((mItrackerState & (1 << Itracker_State_isRunning)) == 1) {
						v.setEnabled(true);
					}
					else
						v.setEnabled(false);
					break;*/
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
		/*case (6):
			if ((mItrackerState & (1 << Itracker_State_isRunning)) == 1)
				return true;
			else
				return false;*/
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
	
	/**
	 * Gets the number of cores available in this device, across all processors.
	 * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
	 * @return The number of cores, or 1 if failed to get result
	 */
	private int getNumCores() {
	    //Private Class to display only CPU devices in the directory listing
	    class CpuFilter implements FileFilter {
	        @Override
	        public boolean accept(File pathname) {
	            //Check if filename is "cpu", followed by a single digit number
	            if(Pattern.matches("cpu[0-9]", pathname.getName())) {
	                return true;
	            }
	            return false;
	        }      
	    }

	    try {
	        //Get directory containing CPU info
	    	File dir = new File("/sys/devices/system/cpu/");
	    	//Filter to only list the devices we care about
	        File[] files = dir.listFiles(new CpuFilter());
	        Log.d(Tag, "CPU Count: "+files.length);
	        //Return the number of cores (virtual CPU devices)
	        return files.length;
	    } catch(Exception e) {
	        //Print exception
	        Log.d(Tag, "CPU Count: Failed.");
	        e.printStackTrace();
	        //Default to return 1 core
	        return 1;
	    }
	}
	
	/*20140917 added by michael*/
	boolean is_internet_available() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		/*WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifi_info;
		if ( wifi.isWifiEnabled() == true ) {
			wifi_info = wifi.getConnectionInfo();
			wifi.disconnect();
			wifi_info = wifi.getConnectionInfo();
		}

		Log.d( Tag, "network preference:" + cm.getNetworkPreference() );
		NetworkInfo[] info = cm.getAllNetworkInfo();
		Log.d( Tag, "network interface count:" + info.length );
        for (int i = 0; i < info.length; i++) {
        	//cm.stopUsingNetworkFeature(networkType, feature)
        	Log.d(Tag, info[i].getTypeName() + " " + info[i].getSubtypeName() + " " + info[i].isConnected() );
            if (info[i].getState() == NetworkInfo.State.CONNECTED) {
            }
        }*/		
		
		NetworkInfo activeNetwork, wifi_network, mobile_network;
		wifi_network = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		mobile_network = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		
		return isConnected;
	}
	//boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
	
	void turn_off_wifi () {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		
		NetworkInfo activeNetwork;
		activeNetwork = cm.getActiveNetworkInfo();		
		boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();		
		boolean isWiFi = activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
		
		wifi.disconnect();
		wifi.setWifiEnabled(false);
	}
	
	void turn_on_wifi () {
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		
		wifi.setWifiEnabled(true);
		wifi.reassociate();
	}
	
	/**
	 * Check the hardware if there are connected to Internet or not.
	 * This method gets the list of all available networks and if any one of 
	 * them is connected returns true. If none is connected returns false.
	 * 
	 * @param context {@link Context} of the app.
	 * @return <code>true</code> if connected or <code>false</code> if not
	 */
	public static boolean isNetworkAvailable(Context context) {
	    boolean available = false;
	    try {
	        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

	        if (connectivity != null) {
	            NetworkInfo[] info = connectivity.getAllNetworkInfo();
	            if (info != null) {
	                for (int i = 0; i < info.length; i++) {
	                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
	                        available = true;
	                    }
	                }
	            }
	        }
	        if (available == false) {
	            NetworkInfo wiMax = connectivity.getNetworkInfo(6);

	            if (wiMax != null && wiMax.isConnected()) {
	                available = true;
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return available;
	}
	
	DisplayMetrics metrics = new DisplayMetrics();
	public double convert_mm2pixel(double value) {
		((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics((metrics));
		return (int) (value * (1.0f/25.4f) * ((2.54 * metrics.widthPixels) / Double.valueOf((app_properties.getProperty(iTrack_Properties.prop_screen_short_edge_width)))));
	}
}
