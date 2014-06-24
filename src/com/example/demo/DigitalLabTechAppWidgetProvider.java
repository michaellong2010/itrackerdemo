package com.example.demo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

public class DigitalLabTechAppWidgetProvider extends AppWidgetProvider {
	String Tag = "Appwidget for digital lab bench advertisement";
	RemoteViews views;
	
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		views = new RemoteViews(context.getPackageName(), R.layout.ad2_appwidget_layout);
		views.setTextViewText(R.id.digital_lab_bench_ad_textView1, Html.fromHtml("<a href=" + "\"" + context.getResources().getString(R.string.ad2_hyperlink)+ "\"" + ">" + context.getResources().getString(R.string.advertise_appwidget2) + "</a> "));
		//views.setTextViewText(R.id.digital_lab_bench_ad_textView1, Html.fromHtml("<a href=\"http://www.digitallabbench.com\">Digital Lab Bench</a> "));
		//views.setTextViewText(R.id.digital_lab_bench_ad_textView1, context.getResources().getString(R.string.ad2_hyperlink));
		//views.setImageViewResource(R.id.maestrogen_ad_imagebutton1, R.drawable.maestrogen_logo1);
		// Create an Intent to launch ExampleActivity
        //Intent intent = new Intent(context, LogFileChooserActivity.class);
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + context.getResources().getString(R.string.ad2_hyperlink)));
		//startActivity(browserIntent);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, browserIntent, 0);
        views.setOnClickPendingIntent(R.id.customer_ad_imagebutton1, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, views);
		Log.d(Tag, "onUpdate");
	}
	
    public void onDeleted(Context context, int[] appWidgetIds) {
    	Log.d(Tag, "onDeleted"+Integer.toString(appWidgetIds.length));
    }
    
    public void onEnabled(Context context) {
    	Log.d(Tag, "onEnabled");
    }
    
    public void onDisabled(Context context) {
    	Log.d(Tag, "onDisabled");
    }
}
