package com.example.demo;

import android.graphics.drawable.StateListDrawable;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

class My_StateListDrawable extends StateListDrawable {
	private Context mContext;
	
	public My_StateListDrawable(Context context) {
		mContext = context;
    }
	
	public void addState(int[] stateSet, Drawable drawable, int alpha) {
        int width, height;
        BitmapDrawable d, d1;
        //width = (int)Math.round(drawable.getIntrinsicWidth()*0.75);
        //height = (int)Math.round(drawable.getIntrinsicHeight()*0.75);
        width = drawable.getIntrinsicWidth();
        height = drawable.getIntrinsicHeight();
        if (drawable.getIntrinsicWidth()==36 && drawable.getIntrinsicHeight()==36)
        	super.addState(stateSet, drawable);
        else {
        width = 36;
        height = 36;
        //drawable.setAlpha(alpha);
        d = (BitmapDrawable)zoomDrawable(drawable, width, height);
        //d.setAlpha(255);
        //Log.d("alpha", Integer.toString(d.getPaint().getAlpha()));
        /*if (toGreyout) {
        	super.addState(stateSet, d);
        }
        else {
        	super.addState(stateSet, d);
        }*/
        /*ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

        d.setColorFilter(filter);*/

        Canvas c = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        if (d instanceof BitmapDrawable) {
        	d.setAlpha(alpha);
        	c.setBitmap(bitmap);
        	d.setBounds(0, 0, c.getWidth(), c.getHeight());
        	d.draw(c);
        	//c.drawBitmap(((BitmapDrawable) d).getBitmap(), 0, 0, ((BitmapDrawable) d).getPaint());
        }
        d1 = new BitmapDrawable(mContext.getResources(), bitmap);
        super.addState(stateSet, d1);
        }
	}

    @Override
    public void draw(Canvas canvas) {
    	//super.draw(zoomDrawable(canvas, width, height));
        super.draw(canvas);
    }

	Bitmap drawableToBitmap(Drawable drawable) // drawable ËΩ¨Êç¢??bitmap
	{
		/*20131204 modified by michael
		 * BitmapDrawable contain a Bitmap member instance in it°Fother kinds of Drawables other than BitmapmapDrawable must clone it's content through canvas hold a new Bitmap instance*/
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();  //returen the existed Bitmap instance in BitmapDrawable instance directly
			
			//Alternative°Gclone the drawable content to a newly Bitmap instance  
			/*Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888); // Âª∫Á?ÂØπÂ?
			Canvas canvas = new Canvas(bitmap); // Âª∫Á?ÂØπÂ? bitmap ?ÑÁîªÂ∏
			Paint mPaint;
			mPaint = ((BitmapDrawable) drawable).getPaint();
			//mPaint.setColor(0x0000FF00);
			Log.d("color:",  Integer.toHexString(mPaint.getColor()));
			Log.d("alpha:",  Integer.toHexString(mPaint.getAlpha()));
			//mPaint.setTextSize(20);
			Log.d("text size:",  Float.toString((mPaint.getTextSize())));
	        
			//ColorMatrix matrix = new ColorMatrix();
	        //matrix.setSaturation(0);
	        //drawable.setColorFilter(new ColorMatrixColorFilter(matrix));
//mPaint.setAlpha(0x40);
			//drawable.setColorFilter(new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN));
			canvas.drawBitmap(((BitmapDrawable) drawable).getBitmap(), 0, 0, mPaint);
			//canvas.drawText("knight", 0, 32, mPaint);
		    return bitmap;*/
		}
		else {
			int width = drawable.getIntrinsicWidth(); // ??drawable ?ÑÈïøÂÆ
			int height = drawable.getIntrinsicHeight();
			//Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565; // ??drawable ?ÑÈ??≤Ê†ºÂº
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // Âª∫Á?ÂØπÂ?
			Canvas canvas = new Canvas(); // Âª∫Á?ÂØπÂ? bitmap ?ÑÁîªÂ∏
			canvas.setBitmap(bitmap);
			//drawable.setBounds(0, 0, width, height);
			//drawable.draw(canvas); // ??drawable ?ÖÂÆπ?ªÂà∞?ªÂ?‰∏
			canvas.drawBitmap(((BitmapDrawable)drawable).getBitmap(), 0, 0, ((BitmapDrawable)drawable).getPaint());
			//canvas.drawBitmap(((BitmapDrawable) drawable).getBitmap(), 0, 0, null);
			
			/*
			 * Paint mPaint; mPaint = new Paint(); mPaint.setColor(0xFFFF0000);
			 * mPaint.setTextSize(20); canvas.drawText("kitt", 0, 0, mPaint);
			 */
			return bitmap;
		}
	}
    
	Drawable zoomDrawable(Drawable drawable, int w, int h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable); // drawable ËΩ¨Êç¢??bitmap
/*			Matrix matrix = new Matrix(); // ?õÂª∫?ç‰??æÁ??®Á? Matrix ÂØπË±°
		float scaleWidth = ((float) w / width); // ËÆ°Á?Áº©ÊîæÊØî‰?
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight); // ËÆæÁΩÆÁº©ÊîæÊØî‰?
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true); // Âª∫Á??∞Á? bitmap ÔºåÂÖ∂?ÖÂÆπ?ØÂØπ??bitmap ?ÑÁº©?æÂ??ÑÂõæ
*/			Bitmap newbmp = Bitmap.createScaledBitmap(oldbmp, w, h, true);
		return new BitmapDrawable(mContext.getResources(), newbmp); // ??bitmap ËΩ¨Êç¢??drawable Âπ∂Ë??
	}
	
	/*Drawable greyoutDrawable(Drawable drawable) {
		Bitmap grayscaleBitmap = Bitmap.createBitmap(colorBitmap.getWidth(),
				colorBitmap.getHeight(), Bitmap.Config.RGB_565);

		Canvas c = new Canvas(grayscaleBitmap);
		Paint p = new Paint();
		ColorMatrix cm = new ColorMatrix();

		cm.setSaturation(0);
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
		p.setColorFilter(filter);
		c.drawBitmap(colorBitmap, 0, 0, p);
	}*/
}