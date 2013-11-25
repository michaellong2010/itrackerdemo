package com.example.demo;

public class Item {

    String mTitle;
    int mIconRes;
    My_StateListDrawable mDrawable;

    Item(String title, int iconRes) {
        mTitle = title;
        mIconRes = iconRes;
    }
    
    Item(String title, My_StateListDrawable d) {
    	mTitle = title;
    	mDrawable = d;
    }
}
