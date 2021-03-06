package com.example.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import net.simonvt.menudrawer.SlidingDrawer;

public class MenuAdapter extends BaseAdapter {

    public interface MenuListener {

        void onActiveViewChanged(View v);
    }

    private Context mContext;

    private List<Object> mItems;

    private MenuListener mListener;

    private int mActivePosition = -1;

    public MenuAdapter(Context context, List<Object> items) {
        mContext = context;
        mItems = items;
    }

    public void setListener(MenuListener listener) {
        mListener = listener;
    }

    public void setActivePosition(int activePosition) {
        mActivePosition = activePosition;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof Item ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEnabled(int position) {
/*    	if (position==1)
    		return true;
    	else
    		return false;*/
		if (mOnRetrieveItemEnable != null)
			return mOnRetrieveItemEnable.getItemEnable(position);
		else
			return getItem(position) instanceof Item;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @SuppressLint("NewApi")
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Object item = getItem(position);
        TextView tv;
        //ViewGroup.LayoutParams lp;
        
        if (item instanceof Category) {
            if (v == null) {
                v = LayoutInflater.from(mContext).inflate(R.layout.menu_row_category, parent, false);
            }

            ((TextView) v).setText(((Category) item).mTitle);

        } else if (item instanceof Item) {
            if (v == null) {
                v = LayoutInflater.from(mContext).inflate(R.layout.menu_row_item, parent, false);
            }
            
            tv = (TextView) v;
            tv.setText(((Item) item).mTitle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            	if (((Item) item).mIconRes != 0)
            		tv.setCompoundDrawablesRelativeWithIntrinsicBounds(((Item) item).mIconRes, 0, 0, 0);
            	else
            		if (((Item) item).mDrawable != null)
            			tv.setCompoundDrawablesWithIntrinsicBounds(((Item) item).mDrawable, null, null, null);
            } else {
                //tv.setCompoundDrawablesWithIntrinsicBounds(((Item) item).mIconRes, 0, 0, 0);
                  tv.setCompoundDrawablesWithIntrinsicBounds(((Item) item).mDrawable, null, null, null);
            }
            
            tv.setTextColor (new ColorStateList (
          		   new int [] [] {
               	      //new int [] {android.R.attr.state_enabled, android.R.attr.state_selected},
          			  new int [] {android.R.attr.state_enabled},
          			  //new int [] {android.R.attr.state_selected},
          		      new int [] {}
          		   },
          		   new int [] {
          			  //Color.rgb (0, 255, 0),
          			  Color.rgb (0, 0, 0),
          		      //Color.rgb (255, 0, 0),
          		      Color.rgb (0xac, 0xa8, 0x99)            			  
          		   }
          		));
        }
        else if (item instanceof Item_spinner) {
            if (v == null) {
                v = LayoutInflater.from(mContext).inflate(R.layout.menu_row_spinner, parent, false);
            }
            
            Spinner spinner=(Spinner) v.findViewById(R.id.spinner1);
            ArrayAdapter<String> adapter=new ArrayAdapter<String>(mContext, R.layout.simple_spinner_item, ((Item_spinner) item).spinner_items);
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            if (((Item_spinner) item).mOnItemSelectedListener != null)
              spinner.setOnItemSelectedListener(((Item_spinner) item).mOnItemSelectedListener);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            	//spinner.setDropDownHorizontalOffset(((BaseListSample) this.mContext).mMenuDrawer.spinner_dropDownHorizontalOffset);
            	//spinner.setDropDownHorizontalOffset(-100);
            	spinner.setDropDownVerticalOffset(-100);
            
        	/*if (BaseListSample.this.mMenuDrawer instanceof SlidingDrawer)
                right = ViewHelper.getLeft(mContentContainer);
          	else
          		right = ViewHelper.getRight(this.mMenuContainer);*/

            //spinner.setOnItemSelectedListener(listener);
        }

        v.setTag(R.id.mdActiveViewPosition, position);

        if (position == mActivePosition) {
            mListener.onActiveViewChanged(v);
        }

        return v;
    }

    /*20131125 added by michael
    change the ListAdapter then ListView.SetAdapter(ListAdapter) to update the view content*/
    public Object removeItem(int position) {
      return mItems.remove(position);
    }
    
    OnRetrieveItemEnable mOnRetrieveItemEnable;
    /*20131127 added by michael
    define a abstract method or interface to retrieve newest item state at certain position*/
    public interface OnRetrieveItemEnable {
    	boolean getItemEnable(int position);
    }
    public void setOnRetrieveItemEnable(OnRetrieveItemEnable l) {
    	mOnRetrieveItemEnable = l;
    }
    
    public void syncItems(List<Object> items) {
    	this.mItems = items;
    }
}
