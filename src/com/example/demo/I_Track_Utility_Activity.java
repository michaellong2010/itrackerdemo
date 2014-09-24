package com.example.demo;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class I_Track_Utility_Activity extends Activity {
	/*20140819 added by michael
	 * the activity top level layout container*/
	public final String Tag = "I_Track_Utility_Activity";
	FrameLayout mLayout_Content;
	LinearLayout mLayout_Setting_Upper_Well, mLayout_Setting_Lower_Well;
	RelativeLayout mIndicators_Layout, mLayout_Draw_Upper_Well_Region, mLayout_Draw_Lower_Well_Region;
	I_Tracker_Well_Plate_View Well_View;
	EditText screen_width_cm, viewable_width_mm, viewable_height_mm;
	EditText well_pitch_x_mm, well_pitch_y_mm, wells_offset_x_mm, wells_offset_y_mm;
	AlertDialog.Builder alert_dialog_builder, save_exit_alert;
	iTrack_Properties property;
	ActionMode mMode;
	int [][] Valid_Coord_Histogram;
	Upper_Well_View upper_view;
	Lower_Well_View lower_view;
	OverflowMenuButton OveflaowBtn;
	ImageView running_status_v, connection_status_v;
	private int mWell_type;
	public static int X_holes, Y_holes;
	/*20140901 added by michael*/
	Spinner spinner = null;
	public int m_Current_Orientation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.setContentView(R.layout.upper_well_layout_param);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		getWindow().getDecorView().setBackgroundResource(android.R.color.background_dark);

		mMode = startActionMode(mCallback);
		alert_dialog_builder = new AlertDialog.Builder(this);
		property = new iTrack_Properties();
		property.load_property();
		/*ActionBar abr;
		abr = this.getActionBar();
		abr.setDisplayHomeAsUpEnabled(true);*/
		mLayout_Content = (FrameLayout) this.findViewById(android.R.id.content);
		mLayout_Setting_Upper_Well = (LinearLayout) LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.upper_well_layout_param, mLayout_Content, false);
		mLayout_Setting_Lower_Well = (LinearLayout) LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.lower_well_layout_param, mLayout_Content, false);
		mLayout_Content.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				mMode = startActionMode(mCallback);
				return true;
			}
		});
		
		mIndicators_Layout = new RelativeLayout(this);
		mIndicators_Layout.setId(100);
		Well_View = new I_Tracker_Well_Plate_View(this, I_Tracker_Well_Plate_View.Wells_384);
		//Well_View.setScaleType(ImageView.ScaleType.CENTER);
		Valid_Coord_Histogram = new int [24][16];
		upper_view = new Upper_Well_View(this);
		lower_view = new Lower_Well_View(this);
		//m_Current_Orientation = getRequestedOrientation();
		
		/*mLayout_Setting_Upper_Well = (LinearLayout) LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.upper_well_layout_param, mLayout_Content, false);
		mLayout_Content.addView(mLayout_Setting_Upper_Well);
		
		screen_width_mm = (EditText) this.findViewById(R.id.screen_short_edge);
		Log.d(Tag, property.getProperty(iTrack_Properties.prop_screen_short_edge_width));
		screen_width_mm.setText(property.getProperty(iTrack_Properties.prop_screen_short_edge_width));
		screen_width_mm.addTextChangedListener(mTextEditorWatcher);
		screen_width_mm.setFilters(apped_input_filter(screen_width_mm.getFilters(), new DecimalInputFilter(screen_width_mm, 2, 2, 11.9, 13, iTrack_Properties.def_prop_screen_short_edge_mm)));
		screen_width_mm.setOnEditorActionListener(ed_action_listener);
		
		viewable_width_mm = (EditText) this.findViewById(R.id.viewable_width);
		Log.d(Tag, property.getProperty(iTrack_Properties.prop_viewable_width));
		viewable_width_mm.setText(property.getProperty(iTrack_Properties.prop_viewable_width));
		viewable_width_mm.addTextChangedListener(mTextEditorWatcher);
		viewable_width_mm.setFilters(apped_input_filter(viewable_width_mm.getFilters(), new DecimalInputFilter(viewable_width_mm, 3, 0, 112, 128, iTrack_Properties.def_prop_viewable_width_mm)));
		viewable_width_mm.setOnEditorActionListener(ed_action_listener);
		
		viewable_height_mm = (EditText) this.findViewById(R.id.viewable_height);
		Log.d(Tag, property.getProperty(iTrack_Properties.prop_viewable_height));
		viewable_height_mm.setText(property.getProperty(iTrack_Properties.prop_viewable_height));
		viewable_height_mm.addTextChangedListener(mTextEditorWatcher);
		viewable_height_mm.setFilters(apped_input_filter(viewable_height_mm.getFilters(), new DecimalInputFilter(viewable_width_mm, 2, 0, 55, 65, iTrack_Properties.def_prop_viewable_height_mm)));
		viewable_height_mm.setOnEditorActionListener(ed_action_listener);*/
	}

	class Upper_Well_View extends View {
		public Upper_Well_View(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		public Upper_Well_View(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
		}

		public Upper_Well_View(Context context, AttributeSet attrs,
				int defStyle) {
			super(context, attrs, defStyle);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			BitmapDrawable d;
			Rect outRect;
			Paint paint;
			
			outRect = new Rect();
			this.getDrawingRect(outRect);
			d = (BitmapDrawable) Well_View.getDrawable();
			paint = d.getPaint();
			paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
			outRect.set(outRect.left, outRect.top, outRect.left + (outRect.right - outRect.left - mIndicators_Layout.getWidth()), outRect.bottom);
			canvas.drawRect(outRect, paint);
			paint.setXfermode(null);
			d.setBounds(outRect);
			//d.draw(canvas);
			canvas.drawBitmap(d.getBitmap(), d.getBounds(), outRect, d.getPaint());
			
			super.onDraw(canvas);
		}
	}
	
	public class Lower_Well_View extends View {

		public Lower_Well_View(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			Rect outRect, srcRect;
			BitmapDrawable d;
			Paint paint;
			
			outRect = new Rect();
			this.getDrawingRect(outRect);
			srcRect = new Rect(outRect);
			d = (BitmapDrawable) Well_View.getDrawable();
			paint = d.getPaint();
			paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
			canvas.drawRect(outRect, paint);
			paint.setXfermode(null);
			srcRect.set(srcRect.left, srcRect.top + mLayout_Setting_Lower_Well.getHeight() - mLayout_Draw_Lower_Well_Region.getHeight() + 150, srcRect.right, srcRect.top + mLayout_Setting_Lower_Well.getHeight());
			outRect.set(outRect.left, outRect.top + 150, outRect.right, outRect.bottom);
			//d.setBounds(0, 0, d.getIntrinsicHeight(), d.getIntrinsicWidth());
			canvas.drawBitmap(d.getBitmap(), srcRect, outRect, d.getPaint());
			super.onDraw(canvas);
		}		
	}
	
	/*20140901 added by michael*/	
	AdapterView.OnItemSelectedListener Portrait_mode_selection = new AdapterView.OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			if ( position == 0) {
				m_Current_Orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				setRequestedOrientation(m_Current_Orientation);
				property.setProperty(iTrack_Properties.prop_portrait, Integer.toString(m_Current_Orientation));
			}
			else {
				m_Current_Orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				setRequestedOrientation(m_Current_Orientation);
				property.setProperty(iTrack_Properties.prop_portrait, Integer.toString(m_Current_Orientation));
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
		}
		
	};
	
	ActionMode.Callback mCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.utility_menu, menu);

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			Button back_btn, draw_384_btn, draw_96_btn;
			
			switch (item.getItemId()) {
			case R.id.Setting_upper_well:
				hide_system_bar();
				if (mLayout_Setting_Upper_Well == null) {
					mLayout_Setting_Upper_Well = (LinearLayout) LayoutInflater.from(I_Track_Utility_Activity.this.getApplicationContext()).inflate(R.layout.upper_well_layout_param, mLayout_Content, false);
				}
				back_btn = (Button) mLayout_Setting_Upper_Well.findViewById(R.id.exit_setting);
				draw_384_btn = (Button) mLayout_Setting_Upper_Well.findViewById(R.id.button1);
				draw_96_btn = (Button) mLayout_Setting_Upper_Well.findViewById(R.id.button2);
				mLayout_Draw_Upper_Well_Region = (RelativeLayout) mLayout_Setting_Upper_Well.findViewById(R.id.draw_upper_well_region);
				/*20140901 added by michael
				 * create a spinner to select the portrait orientation */
				spinner = (Spinner) mLayout_Setting_Upper_Well.findViewById(R.id.spinner2);
				ArrayList<String> spinner_items = new ArrayList<String>();
				spinner_items.add("Portrait");
				spinner_items.add("Reverse Portrait");
				ArrayAdapter<String> adapter = new ArrayAdapter<String>( getApplicationContext(), R.layout.simple_spinner_item, spinner_items );
				adapter.setDropDownViewResource( R.layout.simple_spinner_dropdown_item );
				spinner.setAdapter(adapter);
				m_Current_Orientation = Integer.valueOf(property.getProperty(iTrack_Properties.prop_portrait));
				setRequestedOrientation(m_Current_Orientation);
				if ( m_Current_Orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ) {
					spinner.setSelection(0);
				}
				else
					spinner.setSelection(1);
				spinner.setOnItemSelectedListener(Portrait_mode_selection);
				
				back_btn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Exit_to_main(v);
						IME_hide();
					}
				});
				mLayout_Content.removeAllViews();
				mLayout_Content.addView(mLayout_Setting_Upper_Well);
				mLayout_Content.setLongClickable(false);
				draw_384_btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Clear_Well_View();
						Well_View.setWell(I_Tracker_Well_Plate_View.Wells_384, property, true); 
						Draw_Upper_Well();
						Well_View.DrawBitmap(false);
				    	Well_View.setWellColor(Valid_Coord_Histogram);
				    	upper_view.invalidate();
				    	IME_hide();
					}
					
				});
				draw_96_btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Clear_Well_View();
						Well_View.setWell(I_Tracker_Well_Plate_View.Wells_96, property, true);
						Draw_Upper_Well();
						Well_View.DrawBitmap(false);
				    	Well_View.setWellColor(Valid_Coord_Histogram);
				    	upper_view.invalidate();
				    	IME_hide();
					}
				});
				
				//mIndicators_Layout
				//RelativeLayout.LayoutParams lp2;
				//lp2 = new RelativeLayout.LayoutParams(40, (int)Well_View.mMaxTouchablePosY);
				screen_width_cm = (EditText) mLayout_Setting_Upper_Well.findViewById(R.id.screen_short_edge);
				Log.d(Tag, property.getProperty(iTrack_Properties.prop_screen_short_edge_width));
				screen_width_cm.setText(property.getProperty(iTrack_Properties.prop_screen_short_edge_width));
				screen_width_cm.addTextChangedListener(mTextEditorWatcher);
				screen_width_cm.setFilters(apped_input_filter(screen_width_cm.getFilters(), new DecimalInputFilter(screen_width_cm, 2, 2, 11.9, 13, iTrack_Properties.def_prop_screen_short_edge_cm)));
				screen_width_cm.setOnEditorActionListener(ed_action_listener);
				screen_width_cm.setOnFocusChangeListener(ed_focus_listener);
				
				viewable_width_mm = (EditText) mLayout_Setting_Upper_Well.findViewById(R.id.viewable_width);
				Log.d(Tag, property.getProperty(iTrack_Properties.prop_viewable_width));
				viewable_width_mm.setText(property.getProperty(iTrack_Properties.prop_viewable_width));
				viewable_width_mm.addTextChangedListener(mTextEditorWatcher);
				viewable_width_mm.setFilters(apped_input_filter(viewable_width_mm.getFilters(), new DecimalInputFilter(viewable_width_mm, 3, 0, 112, 128, iTrack_Properties.def_prop_viewable_width_mm)));
				viewable_width_mm.setOnEditorActionListener(ed_action_listener);
				viewable_width_mm.setOnFocusChangeListener(ed_focus_listener);
				
				viewable_height_mm = (EditText) mLayout_Setting_Upper_Well.findViewById(R.id.viewable_height);
				Log.d(Tag, property.getProperty(iTrack_Properties.prop_viewable_height));
				viewable_height_mm.setText(property.getProperty(iTrack_Properties.prop_viewable_height));
				viewable_height_mm.addTextChangedListener(mTextEditorWatcher);
				viewable_height_mm.setFilters(apped_input_filter(viewable_height_mm.getFilters(), new DecimalInputFilter(viewable_width_mm, 2, 0, 55, 65, iTrack_Properties.def_prop_viewable_height_mm)));
				viewable_height_mm.setOnEditorActionListener(ed_action_listener);
				viewable_height_mm.setOnFocusChangeListener(ed_focus_listener);
				
				if (mode != null) 
				 {
					mode.finish();
				 }
				return true;
				
			case R.id.Setting_lower_well:
				hide_system_bar();
				if (mLayout_Setting_Lower_Well == null) {
					mLayout_Setting_Lower_Well = (LinearLayout) LayoutInflater.from(I_Track_Utility_Activity.this.getApplicationContext()).inflate(R.layout.lower_well_layout_param, mLayout_Content, false);
				}
				back_btn = (Button) mLayout_Setting_Lower_Well.findViewById(R.id.exit_setting);
				draw_384_btn = (Button) mLayout_Setting_Lower_Well.findViewById(R.id.button1);
				draw_96_btn = (Button) mLayout_Setting_Lower_Well.findViewById(R.id.button2);
				mLayout_Draw_Lower_Well_Region = (RelativeLayout) mLayout_Setting_Lower_Well.findViewById(R.id.draw_lower_well_region);
				back_btn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Exit_to_main(v);
						IME_hide();
					}
				});
				mLayout_Content.removeAllViews();
				mLayout_Content.addView(mLayout_Setting_Lower_Well);
				mLayout_Content.setLongClickable(false);
				draw_384_btn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Clear_Well_View();
						Well_View.setWell(I_Tracker_Well_Plate_View.Wells_384, property, true);
						Draw_Lower_Well();
						Well_View.setWellColor(Valid_Coord_Histogram);
				    	lower_view.invalidate();
				    	IME_hide();
					}
				});
				draw_96_btn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Clear_Well_View();
						Well_View.setWell(I_Tracker_Well_Plate_View.Wells_96, property, true);
						Draw_Lower_Well();
						Well_View.setWellColor(Valid_Coord_Histogram);
				    	lower_view.invalidate();
				    	IME_hide();
					}
				});						

				
				well_pitch_x_mm = (EditText) mLayout_Setting_Lower_Well.findViewById(R.id.well_pitch_x);
				well_pitch_x_mm.setText(property.getProperty(iTrack_Properties.prop_well_pitch_x));
				well_pitch_x_mm.addTextChangedListener(mTextEditorWatcher);
				well_pitch_x_mm.setFilters(apped_input_filter(well_pitch_x_mm.getFilters(), new DecimalInputFilter(well_pitch_x_mm, 2, 3, 8.7, 9.9, iTrack_Properties.def_well_pitch_x_mm)));
				well_pitch_x_mm.setOnEditorActionListener(ed_action_listener);
				well_pitch_x_mm.setOnFocusChangeListener(ed_focus_listener);
				
				well_pitch_y_mm = (EditText) mLayout_Setting_Lower_Well.findViewById(R.id.well_pitch_y);
				well_pitch_y_mm.setText(property.getProperty(iTrack_Properties.prop_well_pitch_y));
				well_pitch_y_mm.addTextChangedListener(mTextEditorWatcher);
				well_pitch_y_mm.setFilters(apped_input_filter(well_pitch_y_mm.getFilters(), new DecimalInputFilter(well_pitch_y_mm, 2, 3, 8.7, 9.9, iTrack_Properties.def_well_pitch_y_mm)));
				well_pitch_y_mm.setOnEditorActionListener(ed_action_listener);
				well_pitch_y_mm.setOnFocusChangeListener(ed_focus_listener);
				
				wells_offset_x_mm = (EditText) mLayout_Setting_Lower_Well.findViewById(R.id.wells_offset_x);
				wells_offset_x_mm.setText(property.getProperty(iTrack_Properties.prop_wells_offset_x));
				wells_offset_x_mm.addTextChangedListener(mTextEditorWatcher);
				wells_offset_x_mm.setFilters(apped_input_filter(wells_offset_x_mm.getFilters(), new DecimalInputFilter(wells_offset_x_mm, 2, 2, 3, 9.9, iTrack_Properties.def_wells_offset_x_mm)));
				wells_offset_x_mm.setOnEditorActionListener(ed_action_listener);
				wells_offset_x_mm.setOnFocusChangeListener(ed_focus_listener);
				
				wells_offset_y_mm = (EditText) mLayout_Setting_Lower_Well.findViewById(R.id.wells_offset_y);
				wells_offset_y_mm.setText(property.getProperty(iTrack_Properties.prop_wells_offset_y));
				wells_offset_y_mm.addTextChangedListener(mTextEditorWatcher);
				wells_offset_y_mm.setFilters(apped_input_filter(wells_offset_y_mm.getFilters(), new DecimalInputFilter(wells_offset_y_mm, 2, 2, 80, 100, iTrack_Properties.def_wells_offset_y_mm)));
				wells_offset_y_mm.setOnEditorActionListener(ed_action_listener);
				wells_offset_y_mm.setOnFocusChangeListener(ed_focus_listener);
				if (mode != null) 
				 {
					mode.finish();
				 }
				return true;
			default:
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			mMode = null;
		}
		
	};
	
	void Draw_Lower_Well() {
		//mLayout_Draw_Lower_Well_Region.removeAllViews();
		
		if (mLayout_Draw_Lower_Well_Region.getChildCount() == 0) {
			RelativeLayout.LayoutParams lp2;
			lp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);		
			//lp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			lp2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
			lower_view.setLayoutParams(lp2);
			mLayout_Draw_Lower_Well_Region.addView(lower_view);
		}
		mLayout_Draw_Lower_Well_Region.invalidate();
		mLayout_Draw_Lower_Well_Region.requestLayout();
		
		Log.d("mLayout_Setting_Lower_Well", "height: " + mLayout_Setting_Lower_Well.getHeight() + "width: " + mLayout_Setting_Lower_Well.getWidth());
		Log.d("mLayout_Draw_Lower_Well_Region", "height: " + mLayout_Draw_Lower_Well_Region.getHeight() + "width: " + mLayout_Draw_Lower_Well_Region.getWidth());
		
		int i, j;
		Random r = new Random();
		int color_index = 1 + r.nextInt(8);
    	//for (i = 0; i < Valid_Coord_Histogram.length; i++)
    		//Arrays.fill(Valid_Coord_Histogram[i], 5);
    	for (i = 0; i < 24; i++)
    		for (j = 0; j < 16;) {
    			color_index = 1 + r.nextInt(8);
    			if (color_index==Valid_Coord_Histogram[i][j]) {
    				
    			}
    			else {
    				Valid_Coord_Histogram[i][j] = color_index;
    				j++;
    			}
    		}
	}
	
	void Draw_Upper_Well() {		
		mLayout_Draw_Upper_Well_Region.removeAllViews();
		RelativeLayout.LayoutParams lp2;
		lp2 = new RelativeLayout.LayoutParams(40, (int)Well_View.mMaxTouchablePosY);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		if (mIndicators_Layout.getLayoutParams() == null) {
			/*indicator container is a Relative layout*/
		}
		else {
		}
		mIndicators_Layout.setLayoutParams(lp2);
		//mIndicators_Layout.invalidate();
		
		if (mIndicators_Layout.getChildCount()==0) {
			
			OveflaowBtn = new OverflowMenuButton(this, null, R.attr.customOverflowMenuButtonStyle);
			OveflaowBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_moreoverflow_normal_holo_dark));
			lp2 = new RelativeLayout.LayoutParams(32, ViewGroup.LayoutParams.WRAP_CONTENT);
			lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
			mIndicators_Layout.addView(OveflaowBtn, lp2);
			
			running_status_v = new ImageView(this);
			lp2 = new RelativeLayout.LayoutParams(32, 32);
			lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			lp2.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			//lp2.setMargins(0, (int)(Well_View.mMaxTouchablePosY-32)/2, 0, 0);
			running_status_v.setImageDrawable(getResources().getDrawable(R.drawable.red_circle));
			mIndicators_Layout.addView(running_status_v, lp2);
			
			connection_status_v = new ImageView(this);
			lp2 = new RelativeLayout.LayoutParams(32, 32);
			lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			lp2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
			//lp2.setMargins(0, (int)(Well_View.mMaxTouchablePosY-32), 0, 0);
			connection_status_v.setImageResource(R.drawable.usb_disconnection);
			mIndicators_Layout.addView(connection_status_v, lp2);
		}
		else {
			//OveflaowBtn.requestLayout();
			//running_status_v.requestLayout();
			//connection_status_v.requestLayout();
		}
		
		/*attach mIndicators_Layout to it's parent*/
		Log.d(Tag, "height: " + mLayout_Draw_Upper_Well_Region.getHeight() + "width: " + mLayout_Draw_Upper_Well_Region.getWidth());
		mLayout_Draw_Upper_Well_Region.addView(mIndicators_Layout);
		Log.d(Tag, "height: " + mLayout_Draw_Upper_Well_Region.getHeight() + "width: " + mLayout_Draw_Upper_Well_Region.getWidth());
		
		lp2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);		
		lp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		upper_view.setLayoutParams(lp2);
		mLayout_Draw_Upper_Well_Region.addView(upper_view);
		
		int i, j;
		Random r = new Random();
		int color_index = 1 + r.nextInt(8);
    	//for (i = 0; i < Valid_Coord_Histogram.length; i++)
    		//Arrays.fill(Valid_Coord_Histogram[i], 5);
    	for (i = 0; i < 24; i++)
    		for (j = 0; j < 16;) {
    			color_index = 1 + r.nextInt(8);
    			if (color_index==Valid_Coord_Histogram[i][j]) {
    				
    			}
    			else {
    				Valid_Coord_Histogram[i][j] = color_index;
    				j++;
    			}
    		}
	}
	
	/*20140820 added by michael
	 * append the new input filter for edittext*/
	public InputFilter [] apped_input_filter(InputFilter []orig_filters, InputFilter filter) {
		int i;
		InputFilter [] new_filters = new InputFilter [orig_filters.length + 1];
		
		for (i = 0; i < orig_filters.length; i++) {
			new_filters[i] = orig_filters[i];
		}
		new_filters[i] = filter;
				
		return new_filters;
	}
	
	public class DecimalInputFilter implements InputFilter {

		Pattern mPattern;
		public EditText mEdit;
		public double Max, Min, default_val;
		CharSequence new_text;
		Matcher matcher;

		public DecimalInputFilter(EditText edit, int digitsBeforeDecimal,int digitsAfterDecimal, double min, double max, double def) {
		    //mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
			//mPattern = Pattern.compile("^[0-9]{0,1}?");
			if (digitsAfterDecimal == 0)
				mPattern = Pattern.compile("^[1-9][0-9]{0," + (digitsBeforeDecimal-1) +"}");
			else
				mPattern = Pattern.compile("^[1-9][0-9]{0," + (digitsBeforeDecimal-1) + "}(\\.[0-9]{0," + digitsAfterDecimal + "})?");
			//+([0-9]{1," + (digitsBeforeZero) + "})?+(\\.[0-9]{0," + (digitsAfterZero-1) + "})?");
			Max = max;
			Min = min;
			mEdit = edit;
			default_val = def;
		}

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

			//if (start < end) {
				//new_text = dest.subSequence(0, dstart).toString() + source.subSequence(start, end) + dest.subSequence(dend, dest.length());
			//}
			//else
				//if (start==end && end==0) {
					/*detect backspace to delete chars*/
					//new_text = dest.subSequence(0, dstart);
				//}

			new_text = dest.subSequence(0, dstart).toString() + source.subSequence(start, end) + dest.subSequence(dend, dest.length());
			matcher = mPattern.matcher(new_text);
			if (!new_text.toString().equals("") && !matcher.matches()) {
				/*Maybe backspace to delete chars and cause the regex is unmatched¡Atrim all chars after backspace char index*/
				if (start==end && end==0) {
					new_text = dest.subSequence(0, dstart);
					mEdit.setText(new_text);
				}
				return "";
			}
			return null;
		}

	}
	
	private final TextWatcher  mTextEditorWatcher = new TextWatcher() {
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			return;
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			return;
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub			
			return;
		}
		
	};
	
	   //edit_txt.setOnEditorActionListener
	EditText.OnEditorActionListener ed_action_listener = new EditText.OnEditorActionListener() {
		DecimalInputFilter mDecimalFilter;

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			int i;
			InputFilter [] filters;
			EditText mEdit;
			
			if (v instanceof EditText && actionId == EditorInfo.IME_ACTION_DONE) {
				mEdit = (EditText) v;
				filters = v.getFilters();
				mDecimalFilter = null;
				for (i = 0; i < filters.length; i++) {
					if (filters[i] instanceof DecimalInputFilter) {
						mDecimalFilter = (DecimalInputFilter)filters[i];
						String enteredValue = mEdit.getText().toString();
						if (enteredValue != null && !enteredValue.equals("")) {
							if (Double.parseDouble(enteredValue.trim()) < mDecimalFilter.Min
								|| Double.parseDouble(enteredValue.trim()) > mDecimalFilter.Max) {
								alert_dialog_builder.setMessage("Value range=(" + Double.toString(mDecimalFilter.Min) + ", " +  Double.toString(mDecimalFilter.Max) + ")");
								alert_dialog_builder.show();
								mEdit.setText(Double.toString(mDecimalFilter.default_val));
							}
							else {
								IME_toggle();
								/*sync & commit latest data into property*/
								sync_property(v);
							}
						}
						break;
					}
				}
				return true;
			}

			return false;
		}
	};
      
	public void IME_toggle(){
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm.isActive()){
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // hide
        } else {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY); // show
        }
    }//end method
	
	public void IME_hide() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
		//imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // hide
		imm.hideSoftInputFromInputMethod(screen_width_cm.getWindowToken(), 0);
	}// end method

	public void IME_show() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
		//imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY); // show
		imm.showSoftInput(this.screen_width_cm, InputMethodManager.SHOW_IMPLICIT);
	}// end method
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.utility_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.Setting_upper_well:
			hide_system_bar();
			if (mLayout_Setting_Upper_Well == null) {
				mLayout_Setting_Upper_Well = (LinearLayout) LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.upper_well_layout_param, mLayout_Content, false);
			}
			mLayout_Content.removeAllViews();
			mLayout_Content.addView(mLayout_Setting_Upper_Well);			
			return true;
		case R.id.Setting_lower_well:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
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

    public void Exit_to_main(View v) {    	
    	save_exit_alert = new AlertDialog.Builder(this);
    	save_exit_alert.setMessage("Save upper well parameters?")
		               .setCancelable(true);
		AlertDialog alert_dialog = save_exit_alert.create();		
		alert_dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
		    	mLayout_Content.removeAllViews();
		    	mLayout_Content.setLongClickable(true);
		    	show_system_bar();
		    	property.flush();
		    	mMode = startActionMode(mCallback);
		    	
		    	Clear_Well_View();
			}
			
		});
		alert_dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub				
		    	mLayout_Content.removeAllViews();
		    	mLayout_Content.setLongClickable(true);
		    	show_system_bar();
		    	mMode = startActionMode(mCallback);
		    	
		    	Clear_Well_View();
			}			
		});
		alert_dialog.show();

    }
    
    public void Clear_Well_View() {
    	/*clear the Well_View*/
    	BitmapDrawable d;
    	Paint paint;
    	Canvas canvas;
    	Bitmap bitmap;
    	
    	d = (BitmapDrawable) Well_View.getDrawable();
    	paint = d.getPaint();
    	paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
    	bitmap = d.getBitmap();
    	d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
    	canvas = new Canvas(d.getBitmap());
    	Log.d(Tag, d.getBounds().toString());
    	Log.d(Tag, "h: " + bitmap.getHeight() + "w: " + bitmap.getWidth());
    	Log.d(Tag, "h: " + d.getIntrinsicHeight() + "w: " + d.getIntrinsicWidth());
    	d.draw(canvas);
    	paint.setXfermode(null);
    }
    
    View.OnFocusChangeListener ed_focus_listener = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if(hasFocus){
				Log.d(Tag, "focus");
			}
			else {
				Log.d(Tag, "unfocus");
				sync_property(v);
			}
		}
    	
    };
    
    /*when press key "Done" or the edit text lose focus ¡Async the iTrack app property*/
    void sync_property(View v) {
    	EditText mEdit;
    	if (v instanceof EditText) {
    		mEdit = (EditText) v;
    		        	
    		switch(v.getId()) {
    		case R.id.screen_short_edge:
    			property.setProperty(iTrack_Properties.prop_screen_short_edge_width, mEdit.getText().toString());
    			break;
    		case R.id.viewable_width:
    			property.setProperty(iTrack_Properties.prop_viewable_width, mEdit.getText().toString());
    			break;
    		case R.id.viewable_height:
    			property.setProperty(iTrack_Properties.prop_viewable_height, mEdit.getText().toString());
    			break;
    		case R.id.well_pitch_x:
    			property.setProperty(iTrack_Properties.prop_well_pitch_x, mEdit.getText().toString());
    			break;
    		case R.id.well_pitch_y:
    			property.setProperty(iTrack_Properties.prop_well_pitch_y, mEdit.getText().toString());
    			break;
    		case R.id.wells_offset_x:
    			property.setProperty(iTrack_Properties.prop_wells_offset_x, mEdit.getText().toString());
    			break;
    		case R.id.wells_offset_y:
    			property.setProperty(iTrack_Properties.prop_wells_offset_y, mEdit.getText().toString());
    			break;
    		}
    	}
    	else
    		mEdit = null;
    }
}
