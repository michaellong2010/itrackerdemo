package com.example.demo;

import android.widget.AdapterView;

public class Item_spinner {
    public String [] spinner_items;
    public AdapterView.OnItemSelectedListener mOnItemSelectedListener;
	
    /*20140207 added by michael
     * Spinner¡BArrayAdapter*/
	Item_spinner(String [] items) {
		spinner_items = items;
		mOnItemSelectedListener = null;
	}

    /*20140210 added by michael
     * Spinner¡BArrayAdapter*/
	Item_spinner(String [] items, AdapterView.OnItemSelectedListener l) {
		spinner_items = items;
		mOnItemSelectedListener = l;
	}
}
