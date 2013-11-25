package com.example.demo;

import android.graphics.drawable.StateListDrawable;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

class My_StateListDrawable extends StateListDrawable {
	private Context mContext;
	
	public My_StateListDrawable(Context context) {
		mContext = context;
    }
	
	public void addState(int[] stateSet, Drawable drawable) {
        int width, height;
        BitmapDrawable d;
        width = (int)Math.round(drawable.getIntrinsicWidth()*0.75);
        height = (int)Math.round(drawable.getIntrinsicHeight()*0.75);
        d = (BitmapDrawable)zoomDrawable(drawable, width, height);
        super.addState(stateSet, d);
	}

    @Override
    public void draw(Canvas canvas) {
    	//super.draw(zoomDrawable(canvas, width, height));
        super.draw(canvas);
    }

	Bitmap drawableToBitmap(Drawable drawable) // drawable è½¬æ¢??bitmap
	{
		int width = drawable.getIntrinsicWidth(); // ??drawable ?„é•¿å®
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565; // ??drawable ?„é??²æ ¼å¼
		Bitmap bitmap = Bitmap.createBitmap(width, height, config); // å»ºç?å¯¹å?
																	// bitmap
		Canvas canvas = new Canvas(bitmap); // å»ºç?å¯¹å? bitmap ?„ç”»å¸
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas); // ??drawable ?…å®¹?»åˆ°?»å?ä¸
		return bitmap;
	}
    
	Drawable zoomDrawable(Drawable drawable, int w, int h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable); // drawable è½¬æ¢??bitmap
/*			Matrix matrix = new Matrix(); // ?›å»º?ä??¾ç??¨ç? Matrix å¯¹è±¡
		float scaleWidth = ((float) w / width); // è®¡ç?ç¼©æ”¾æ¯”ä?
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight); // è®¾ç½®ç¼©æ”¾æ¯”ä?
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true); // å»ºç??°ç? bitmap ï¼Œå…¶?…å®¹?¯å¯¹??bitmap ?„ç¼©?¾å??„å›¾
*/			Bitmap newbmp = Bitmap.createScaledBitmap(oldbmp, w, h, true);
		return new BitmapDrawable(mContext.getResources(), newbmp); // ??bitmap è½¬æ¢??drawable å¹¶è??
	}
}