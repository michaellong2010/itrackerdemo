package com.example.demo;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Item {

    String mTitle;
    int mIconRes;
    //My_StateListDrawable mDrawable;
    Drawable mDrawable;

    Item(String title, int iconRes) {
        mTitle = title;
        mIconRes = iconRes;
    }
    
    Item(String title, My_StateListDrawable d) {
    	mTitle = title;
    	mDrawable = d;
    }
    
    Item(String title, BitmapDrawable d) {
    	mTitle = title;
    	mDrawable = d;
    }
}
