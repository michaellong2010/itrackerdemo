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

	Bitmap drawableToBitmap(Drawable drawable) // drawable 转换??bitmap
	{
		int width = drawable.getIntrinsicWidth(); // ??drawable ?�长�
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565; // ??drawable ?��??�格�
		Bitmap bitmap = Bitmap.createBitmap(width, height, config); // 建�?对�?
																	// bitmap
		Canvas canvas = new Canvas(bitmap); // 建�?对�? bitmap ?�画�
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas); // ??drawable ?�容?�到?��?�
		return bitmap;
	}
    
	Drawable zoomDrawable(Drawable drawable, int w, int h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable); // drawable 转换??bitmap
/*			Matrix matrix = new Matrix(); // ?�建?��??��??��? Matrix 对象
		float scaleWidth = ((float) w / width); // 计�?缩放比�?
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比�?
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true); // 建�??��? bitmap ，其?�容?�对??bitmap ?�缩?��??�图
*/			Bitmap newbmp = Bitmap.createScaledBitmap(oldbmp, w, h, true);
		return new BitmapDrawable(mContext.getResources(), newbmp); // ??bitmap 转换??drawable 并�??
	}
}