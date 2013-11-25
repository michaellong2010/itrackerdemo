package com.example.demo;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListSample extends Activity implements MenuAdapter.MenuListener {

    private static final String STATE_ACTIVE_POSITION =
            "net.simonvt.menudrawer.samples.LeftDrawerSample.activePosition";

    protected MenuDrawer mMenuDrawer;

    protected MenuAdapter mAdapter;
    protected ListView mList;

    private int mActivePosition = 0;

    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);

        if (inState != null) {
            mActivePosition = inState.getInt(STATE_ACTIVE_POSITION);
        }

        //mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND, getDrawerPosition(), getDragMode());
        mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.OVERLAY, getDrawerPosition(), getDragMode());

        List<Object> items = new ArrayList<Object>();
        My_StateListDrawable d1;
        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.start_en));
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.start_dis));
        items.add(new Item("Start", d1));
        //d1 = null;
        
        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.pause_en));
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.pause_dis));
        items.add(new Item("Pause", d1));
        //d1 = null;
        
        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.stop_en));
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.stop_dis));
        items.add(new Item("Stop", d1));
        //d1 = null;
        
        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.previous_en));
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.previous_dis));
        items.add(new Item("Previous", d1));
        //d1 = null;
        
        d1 = new My_StateListDrawable(this);
        d1.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.next_en));
        d1.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.next_dis));
        items.add(new Item("Next", d1));
        //d1 = null;

        //items.add(new Item("Item 1", R.drawable.ic_action_refresh_dark));
        //items.add(new Item("Item 2", R.drawable.ic_action_select_all_dark));
        //items.add(new Category("Cat 1"));
        //items.add(new Item("Item 3", R.drawable.ic_action_refresh_dark));
        //items.add(new Item("Item 4", R.drawable.ic_action_select_all_dark));
        //items.add(new Category("Cat 2"));
        //items.add(new Item("Item 5", R.drawable.ic_action_refresh_dark));
        //items.add(new Item("Item 6", R.drawable.ic_action_select_all_dark));
        //items.add(new Category("Cat 3"));
        //items.add(new Item("Item 7", R.drawable.ic_action_refresh_dark));
        //items.add(new Item("Item 8", R.drawable.ic_action_select_all_dark));
        //items.add(new Category("Cat 4"));
        //items.add(new Item("Item 9", R.drawable.ic_action_refresh_dark));
        //items.add(new Item("Item 10", R.drawable.ic_action_select_all_dark));

        mList = new ListView(this);

        mAdapter = new MenuAdapter(this, items);
        mAdapter.setListener(this);
        mAdapter.setActivePosition(mActivePosition);

        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(mItemClickListener);

        mMenuDrawer.setMenuView(mList);
    }

    protected abstract void onMenuItemClicked(int position, Item item);

    protected abstract int getDragMode();

    protected abstract Position getDrawerPosition();

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mActivePosition = position;
            mMenuDrawer.setActiveView(view, position);
            mAdapter.setActivePosition(position);
            onMenuItemClicked(position, (Item) mAdapter.getItem(position));
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_ACTIVE_POSITION, mActivePosition);
    	super.onSaveInstanceState(outState);
    }

    @Override
    public void onActiveViewChanged(View v) {
        mMenuDrawer.setActiveView(v, mActivePosition);
    }
}
