package com.example.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.util.Log;

public class iTrack_Properties extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final String Tag = "iTrack_Properties";
	
	private File sdcard = Environment.getExternalStorageDirectory();
	String property_filename = "//iTrack_app_property";
	public static final String prop_screen_short_edge_width = "Screen_Short_Edge_CM";
	public static final String prop_viewable_width = "Viewable_Width_MM";
	public static final String prop_viewable_height = "Viewable_Height_MM";
	public static final String prop_well_pitch_x = "Well_Pitch_X_MM";
	public static final String prop_well_pitch_y = "Well_Pitch_Y_MM";
	public static final String prop_wells_offset_x = "Wells_Offset_X_MM";
	public static final String prop_wells_offset_y = "Wells_Offset_Y_MM";
	public static final String prop_portrait = "Portrait_Selection";
	public static final double def_prop_screen_short_edge_cm = 12.1;
	public static final double def_prop_viewable_width_mm = 116;
	public static final double def_prop_viewable_height_mm = 60;
	public static final double def_well_pitch_x_mm = 9.25d;
	public static final double def_well_pitch_y_mm = 9.25d;
	public static final double def_wells_offset_x_mm = 5.5d;
	public static final double def_wells_offset_y_mm = 90d;
	

	iTrack_Properties() {
		
	}
	
	iTrack_Properties(String external_property_filename) {
		property_filename = external_property_filename;
	}
	
	void load_property() {
		File f;
		FileInputStream fis;
		
		f = new File(sdcard.getPath() + property_filename);
		if (f.exists()) {
			try {
				fis = new FileInputStream(f);
				this.load(fis);
				fis.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			
		}
		
		if (getProperty(prop_screen_short_edge_width) != null) {
			
		}
		else {
			setProperty(prop_screen_short_edge_width, Double.toString(def_prop_screen_short_edge_cm));
			Log.d(Tag, getProperty(prop_screen_short_edge_width));
		}
		
		if (getProperty(prop_viewable_width) != null) {
			
		}
		else {
			setProperty(prop_viewable_width, Double.toString(def_prop_viewable_width_mm));
			Log.d(Tag, getProperty(prop_viewable_width));
		}
		
		if (getProperty(prop_viewable_height) != null) {
			
		}
		else {
			setProperty(prop_viewable_height, Double.toString(def_prop_viewable_height_mm));
			Log.d(Tag, getProperty(prop_viewable_height));
		}
		
		if (getProperty(prop_well_pitch_x) != null) {
			
		}
		else {
			setProperty(prop_well_pitch_x, Double.toString(def_well_pitch_x_mm));
			Log.d(Tag, getProperty(prop_well_pitch_x));
		}
		
		if (getProperty(prop_well_pitch_y) != null) {
			
		}
		else {
			setProperty(prop_well_pitch_y, Double.toString(def_well_pitch_y_mm));
			Log.d(Tag, getProperty(prop_well_pitch_y));
		}
		
		if (getProperty(prop_wells_offset_x) != null) {
			
		}
		else {
			setProperty(prop_wells_offset_x, Double.toString(def_wells_offset_x_mm));
			Log.d(Tag, getProperty(prop_wells_offset_x));
		}
		
		if (getProperty(prop_wells_offset_y) != null) {
			
		}
		else {
			setProperty(prop_wells_offset_y, Double.toString(def_wells_offset_y_mm));
			Log.d(Tag, getProperty(prop_wells_offset_y));
		}
		
		if (getProperty(prop_portrait) != null) {
			
		}
		else {
			setProperty(prop_portrait, Integer.toString(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
			Log.d(Tag, getProperty(prop_portrait));
		}
	}
	
	void flush() {
		FileOutputStream fos;
		File f;
		
		f = new File(sdcard.getPath() + property_filename);
		try {
			if (f.exists())
				fos = new FileOutputStream(f);
			else
				fos = new FileOutputStream(f.getPath());
			if (fos != null) {
				this.store(fos, "");
				fos.close();
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
