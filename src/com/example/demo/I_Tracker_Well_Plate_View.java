package com.example.demo;

import java.util.Arrays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

@SuppressLint("NewApi")
public class I_Tracker_Well_Plate_View extends ImageView implements View.OnAttachStateChangeListener {
    static final int Wells_96 = 96;
    static final int Wells_384 = 384;
    
    private Context mContext;
    private int mWells;
    private double mwell_pitch_x;
    private double mwell_pitch_y;
    public static int X_holes, Y_holes;
    int well_pixels_x, well_pixels_y;
//char dimension in mm unit 
    double Label_cxChar;
	double Label_cyChar;
	int label_pixels_x, label_pixels_y;
	double Border_left;
	double Border_top;
/*20140616 added by michael
 * total UI viewable region dimension 
 */
	double Viewable_height, Viewable_width;
/*20140821 added by michael*/
	double Screen_Short_Width_cm;
	
/*
 20130311 added by michael
 touchable ROI (0, 0)~(mMaxTouchablePosX, mMaxTouchablePosY)
 mMaxTouchablePosX match the view width
 mMaxTouchablePosY calculate from the upper region
 */	
    public float mMaxTouchablePosX, mMaxTouchablePosY;
    public Paint mPaint, mPaint_text, mPaint_transparent, mPaint_transparent1;

/*20130318 added by michael*/
    public Paint mPaint_well_Stroke, mPaint_well_Fill;
/*    COLORREF Mark_Color_Table[] = {
    		  RGB(255, 0, 0), //RED = 
    		  RGB(0, 255, 0),  //GREEN = 
    		  RGB(0, 0, 255),  //BLUE = 
    		  RGB(255, 255, 0),  //YELLOW = 
    		  RGB(255, 165, 0),  //ORANGE = 
    		  RGB(160, 32, 240),  //PURPLE = 
    		  RGB(75, 0, 130)  //INDIGO = 
    		};*/
    int [] Mark_Color_Table;
    private int [][] Well_Color_index;
    private int total_colors;
/*20130325 added by michael*/
    Bitmap Bmp_Well_Plate;
    Canvas Canvas_Well_Plate;
/*20130327 added by michael*/
    public int Last_Coord_X, Last_Coord_Y, Last_Coord_X_Count, Last_Coord_Y_Count;
    boolean Show_focus_coord;
/*20131206 added by michael*/
    PorterDuffXfermode Xfermode_clear = new PorterDuffXfermode(Mode.CLEAR);
    PorterDuffXfermode Xfermode_src = new PorterDuffXfermode(Mode.SRC);
    PorterDuffXfermode Xfermode_src_out = new PorterDuffXfermode(Mode.SRC_OUT);
    PorterDuffXfermode Xfermode_src_over = new PorterDuffXfermode(Mode.SRC_OVER);
    PorterDuffXfermode Xfermode_dst_over = new PorterDuffXfermode(Mode.DST_OVER);
    PorterDuffXfermode Xfermode_dst = new PorterDuffXfermode(Mode.DST);
/*20131209 added by michael*/
    Object lock1, lock2;
/*20140129 added by michael*/
    DisplayMetrics metrics;

/*20140129 added by michael*/
    public int screen_width_pixel() {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
    		return metrics.widthPixels;
    	}
    	else
    		return 600;
    }
    public int screen_height_pixel() {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
    		return metrics.heightPixels;
    	}
    	else
    		return 800;
    }
/*20131213 added by michael*/
    protected int multi_pipettes_well_gap = 0;

/*20140821 added by michael*/
    public static final int Draw_Upper_Region_Only = 0;
    public static final int Draw_Lower_Region_Only = 1;
    public static final int Draw_Whole_Region = 0;
    int mDrawRegion;
    boolean force_invalide;
    private double mwells_offset_x;
    private double mwells_offset_y;
/*20140827 added by michael
 * bit field represent Led & Sensor failure status */
	public int X_Led_failure, X_Sensor_failure, Y_Led_failure, Y_Sensor_failure;
    BitmapDrawable Led_Sensor_Failure_Icon, Only_Led_Failure_Icon, Only_Sensor_Failure_Icon;
    boolean attachToWindow;
    
	public I_Tracker_Well_Plate_View(Context context, int wells) {
		super(context);
		mWells = wells;
		mContext = context;
    	/*20140129 added by michael*/
    	metrics = new DisplayMetrics();
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
    		((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(metrics);
    	}
    	else {
    		((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics((metrics));
    	}
		mMaxTouchablePosX = screen_width_pixel();
		//Pain implement integration function of Pen&Brush in MFC
		mPaint = new Paint();
		mPaint_text = new Paint();
		/*20131217 added by michael*/
		mPaint_transparent = new Paint();
		mPaint_transparent.setStyle(Style.FILL_AND_STROKE);
		mPaint_transparent.setColor(Color.TRANSPARENT);
		mPaint_transparent.setXfermode(Xfermode_src_out);
		/*20131219 added by michael*/
		mPaint_transparent1 = new Paint();
		mPaint_transparent1.setStyle(Style.FILL_AND_STROKE);
		mPaint_transparent1.setColor(Color.WHITE);
		mPaint_transparent1.setXfermode(Xfermode_src_over);
		
		/*20140825 added by michael*/
    	Screen_Short_Width_cm = 12.1;

		/*20130318 added by michael*/
		//create two Pain, one is stroke type the other is fill type
		mPaint_well_Stroke = new Paint();
		mPaint_well_Stroke.setStyle(Paint.Style.STROKE);
        //float st_w = mPaint_well.getStrokeWidth();
		mPaint_well_Stroke.setStrokeWidth(convert_mm2pixel(0.5));
		mPaint_well_Stroke.setColor(Color.WHITE);
		mPaint_well_Fill = new Paint();
		mPaint_well_Fill.setStyle(Paint.Style.FILL);
		//mPaint_well_Fill.setStrokeWidth(convert_mm2pixel(1));
		mPaint_well_Fill.setColor(Color.WHITE);
		Mark_Color_Table = new int []
				{Color.RED, Color.GREEN, Color.BLUE, Color.rgb(255, 255, 0),
				Color.rgb(255, 165, 0), Color.rgb(160, 32, 240), Color.rgb(75, 0, 130)};
		total_colors = Mark_Color_Table.length;
		Well_Color_index = new int[24][16];
    	for (int i = 0; i < Well_Color_index.length; i++)
    		Arrays.fill(Well_Color_index[i], 0x00);
		
		if (wells == Wells_96) {
			//mwell_pitch_x = 9.580d;
			mwell_pitch_x = 8.0d;
			mwell_pitch_y = 7.5d;
			X_holes = 12;
			Y_holes = 8;
			Label_cxChar = 5;
			Label_cyChar = 5;
			Border_left = Label_cxChar + 1;
			Border_top = Label_cyChar + 1;
			/*20131217 added by michael*/
            this.mPaint_well_Stroke.setXfermode(null);
            Viewable_height = 60;
		} else if (wells == Wells_384) {
			mwell_pitch_x = 9.580d / 2;
			mwell_pitch_y = 7.5d / 2;
			X_holes = 24;
			Y_holes = 16;
			Label_cxChar = 3.3;
			Label_cyChar = 3;
			Border_left = Label_cxChar + 1;
			Border_top = Label_cyChar + 1;
			/*20131217 added by michael*/
			this.mPaint_well_Stroke.setXfermode(Xfermode_clear);
			Viewable_height = 60;
		}
		
		Bmp_Well_Plate = Bitmap.createBitmap(screen_width_pixel(), screen_height_pixel(), Bitmap.Config.ARGB_8888);
		Canvas_Well_Plate = new Canvas();
		Canvas_Well_Plate.setBitmap(Bmp_Well_Plate);
		//Canvas_Well_Plate = new Canvas(Bmp_Well_Plate);
		setImageBitmap(Bmp_Well_Plate);
		//mMaxTouchablePosY = convert_mm2pixel((2*Border_top+2*mwell_pitch_y+(Y_holes-1)*2*mwell_pitch_y-0.8)/2);
		mMaxTouchablePosY = (float)convert_mm2pixel(Viewable_height);
		
    	/*20131209 added by michael*/
    	lock1 = new Object();
    	lock2 = new Object();
    	mPaint_transparent.setTextSize(convert_mm2pixel(Label_cxChar));
    	mPaint_transparent1.setTextSize(convert_mm2pixel(Label_cxChar));
    	
    	mDrawRegion = Draw_Whole_Region;
    	force_invalide = false;
    	X_Led_failure = X_Sensor_failure = Y_Led_failure = Y_Sensor_failure = 0;
    	if ( context.getResources().getDrawable(R.drawable.childish_cross) instanceof BitmapDrawable)
    		Led_Sensor_Failure_Icon = (BitmapDrawable) context.getResources().getDrawable(R.drawable.childish_cross);
    	else
    		Led_Sensor_Failure_Icon = null;
    	if ( context.getResources().getDrawable(R.drawable.led_failure) instanceof BitmapDrawable)
    		Only_Led_Failure_Icon = (BitmapDrawable) context.getResources().getDrawable(R.drawable.led_failure);
    	else
    		Only_Led_Failure_Icon = null;
    	if ( context.getResources().getDrawable(R.drawable.sensor_failure) instanceof BitmapDrawable)
    		Only_Sensor_Failure_Icon = (BitmapDrawable) context.getResources().getDrawable(R.drawable.sensor_failure);
    	else
    		Only_Sensor_Failure_Icon = null;
    	
    	addOnAttachStateChangeListener(this);
    	attachToWindow = false;
	}

	/*20130325 added by michael*/
	/*draw wells on the bitmap
	 * this bitmap then set to I_Tracker_Well_Plate_View Canvas
	*/
	public void DrawBitmap(boolean Update_Label_Only) {
		//int i, j, margin_x, margin_y;
		int i, j, left, right, top, bottom;
		float margin_x, margin_y;
		int chr = 'A';
		//int radius_pixels;
		float radius_pixels;
		double mDisplay_well_pitch_x, mDisplay_well_pitch_y;
		double label_width, label_height;
		Rect dst_rect = new Rect();
		Bitmap led_sensor_failure_bmp;

		if (mWells == Wells_96) {
			mDisplay_well_pitch_x = (Viewable_width - Border_left) / X_holes;
			mDisplay_well_pitch_y = (Viewable_height - Border_top) / Y_holes;
		}
		else {
			mDisplay_well_pitch_x = (Viewable_width - Border_left) / X_holes;
			mDisplay_well_pitch_y = (Viewable_height - Border_top) / Y_holes;
		}
        //Border_top = (mDisplay_well_pitch_x > mDisplay_well_pitch_y) ? mDisplay_well_pitch_y : mDisplay_well_pitch_x;
        //Border_left = Border_top -2;
		//Label_cxChar = Border_top - 1;
		//Label_cyChar = Label_cxChar;
		label_width = Label_cxChar / Math.pow(2, 0.5);  
		label_height = Label_cyChar / Math.pow(2, 0.5); 
		/* clear the whole bitmap content */
		if (Update_Label_Only == false) {
		mPaint_well_Stroke.setXfermode(new PorterDuffXfermode(Mode.CLEAR));  
    	Canvas_Well_Plate.drawPaint(mPaint_well_Stroke);
    	mPaint_well_Stroke.setXfermode(null);
		}
		else {
			mPaint_well_Stroke.setXfermode(new PorterDuffXfermode(Mode.CLEAR));  
	    	Canvas_Well_Plate.drawRect(0, 0, convert_mm2pixel(Viewable_width), convert_mm2pixel(Label_cyChar), mPaint_well_Stroke);
	    	Canvas_Well_Plate.drawRect(0, 0, convert_mm2pixel(Label_cxChar), convert_mm2pixel(Viewable_height), mPaint_well_Stroke);
	    	mPaint_well_Stroke.setXfermode(null);
		}
/*		mPaint.setColor(Color.BLUE);
		//mPaint.setStrokeWidth(convert_mm2pixel(1));
		mPaint.setTextSize(24);
		//mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		Canvas_Well_Plate.drawText("T", 0, 20, mPaint);*/
		if (mWells == Wells_96) {
			mPaint_text.setColor(Color.WHITE);
			mPaint_text.setTextSize(convert_mm2pixel(Label_cxChar));
			//mPaint_text.setStyle(Paint.Style.STROKE);
			for (i = 0, margin_x = 0, margin_y = convert_mm2pixel(Label_cyChar); i < X_holes; i++) {
				//label_pixels_x = convert_mm2pixel(Label_cxChar * Integer.toString(i + 1).length());

				if (mDisplay_well_pitch_x > (label_width * Integer.toString(i + 1).length()))
					
/*					 margin_x = convert_mm2pixel(Border_left) +
					 convert_mm2pixel(i * mwell_pitch_x) +
					 (convert_mm2pixel(mwell_pitch_x) - label_pixels_x) / 2;*/
					 
					margin_x = convert_mm2pixel((2 * Border_left + 2 * i
							* mDisplay_well_pitch_x + mDisplay_well_pitch_x - (label_width * Integer.toString(i + 1).length())) / 2);
				else
					
/*					margin_x = convert_mm2pixel(Border_left) +
					 convert_mm2pixel(i * mwell_pitch_x) + (label_pixels_x -
					 convert_mm2pixel(mwell_pitch_x)) / 2;*/
					 
					margin_x = convert_mm2pixel((2 * Border_left + 2 * i
							* mDisplay_well_pitch_x + (label_width * Integer.toString(i + 1).length()) - mDisplay_well_pitch_x) / 2);

				if ((this.X_Led_failure & 1 << (2 * i)) != 0 || (this.X_Led_failure & 1 << (2 * i + 1)) != 0) {
					left = (int) convert_mm2pixel(Border_left + i * mDisplay_well_pitch_x);
					right = (int) convert_mm2pixel(Border_left + (i + 1)* mDisplay_well_pitch_x);
					top = 0;
					bottom = (int) margin_y;
					dst_rect.set(left, top, right, bottom);
					if ((this.X_Sensor_failure & 1 << (2 * i)) != 0 || (this.X_Sensor_failure & 1 << (2 * i + 1)) != 0) {
						//Led & Sensor both fail
						if (Led_Sensor_Failure_Icon != null) {
							led_sensor_failure_bmp = Led_Sensor_Failure_Icon.getBitmap();
							Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Led_Sensor_Failure_Icon.getPaint());
						}
					}
					else {
						//only Led fail
						if (Only_Led_Failure_Icon != null) {
							led_sensor_failure_bmp = Only_Led_Failure_Icon.getBitmap();
							Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Only_Led_Failure_Icon.getPaint());
						}
					}					
				}
				else
					if ((this.X_Sensor_failure & 1 << (2 * i)) != 0 || (this.X_Sensor_failure & 1 << (2 * i + 1)) != 0) {
						//only Sensor fail
						left = (int) convert_mm2pixel(Border_left + i * mDisplay_well_pitch_x);
						right = (int) convert_mm2pixel(Border_left + (i + 1)* mDisplay_well_pitch_x);
						top = 0;
						bottom = (int) margin_y;
						dst_rect.set(left, top, right, bottom);
						if (Only_Sensor_Failure_Icon != null) {
							led_sensor_failure_bmp = Only_Sensor_Failure_Icon.getBitmap();
							Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Only_Sensor_Failure_Icon.getPaint());
						}
					}
					else {
						/*left = (int) convert_mm2pixel(Border_left + i * mDisplay_well_pitch_x);
						right = (int) convert_mm2pixel(Border_left + (i + 1)* mDisplay_well_pitch_x);
						top = 0;
						bottom = (int) margin_y;
						dst_rect.set(left, top, right, bottom);
						if (Only_Sensor_Failure_Icon != null) {
							led_sensor_failure_bmp = Only_Sensor_Failure_Icon.getBitmap();
							Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Only_Sensor_Failure_Icon.getPaint());
						}*/
					}
				/*if ((this.X_Led_failure & 1 << (2 * i)) != 0 || (this.X_Led_failure & 1 << (2 * i + 1)) != 0
						|| (this.X_Sensor_failure & 1 << (2 * i)) != 0 || (this.X_Sensor_failure & 1 << (2 * i + 1)) != 0) {

					if (Led_Sensor_Failure_Icon != null) {
						led_sensor_failure_bmp = Led_Sensor_Failure_Icon.getBitmap();
						Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Led_Sensor_Failure_Icon.getPaint());
					}
				}
				else {
					//Canvas_Well_Plate.drawText(Integer.toString(i + 1), margin_x, margin_y, mPaint_text);

					if (Led_Sensor_Failure_Icon != null) {
						led_sensor_failure_bmp = Led_Sensor_Failure_Icon.getBitmap();
						Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Led_Sensor_Failure_Icon.getPaint());
					}
				}*/
				Canvas_Well_Plate.drawText(Integer.toString(i + 1), margin_x, margin_y, mPaint_text);
			}

			
            for (i = 0, margin_x = 0, margin_y = 0; i < Y_holes; i++) {
				if (mDisplay_well_pitch_y > label_height)
					margin_y = convert_mm2pixel((2 * Border_top + 2 * i
							* mDisplay_well_pitch_y + mDisplay_well_pitch_y - label_height + 2 * label_height) / 2);
				else
					margin_y = convert_mm2pixel((2 * Border_top + 2 * i
							* mDisplay_well_pitch_y + label_height - mDisplay_well_pitch_y + 2 * label_height) / 2);
				if ((this.Y_Led_failure & 1 << (2 * i)) != 0 || (this.Y_Led_failure & 1 << (2 * i + 1)) != 0) {
					left = 0;
					right = (int) convert_mm2pixel(Label_cxChar);
					top = (int) convert_mm2pixel(Border_top + i * mDisplay_well_pitch_y);
					bottom = (int) convert_mm2pixel(Border_top + (i + 1) * mDisplay_well_pitch_y);
					dst_rect.set(left, top, right, bottom);
					if ((this.Y_Sensor_failure & 1 << (2 * i)) != 0 || (this.Y_Sensor_failure & 1 << (2 * i + 1)) != 0) {
						//Led & Sensor both fail
						if (Led_Sensor_Failure_Icon != null) {
							led_sensor_failure_bmp = Led_Sensor_Failure_Icon.getBitmap();
							Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Led_Sensor_Failure_Icon.getPaint());
						}
					}
					else {
						//only Led fail
						if (Only_Led_Failure_Icon != null) {
							led_sensor_failure_bmp = Only_Led_Failure_Icon.getBitmap();
							Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Only_Led_Failure_Icon.getPaint());
						}
					}					
				}
				else
					if ((this.Y_Sensor_failure & 1 << (2 * i)) != 0 || (this.Y_Sensor_failure & 1 << (2 * i + 1)) != 0) {
						//only Sensor fail
						left = 0;
						right = (int) convert_mm2pixel(Label_cxChar);
						top = (int) convert_mm2pixel(Border_top + i * mDisplay_well_pitch_y);
						bottom = (int) convert_mm2pixel(Border_top + (i + 1) * mDisplay_well_pitch_y);
						dst_rect.set(left, top, right, bottom);
						if (Only_Sensor_Failure_Icon != null) {
							led_sensor_failure_bmp = Only_Sensor_Failure_Icon.getBitmap();
							Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Only_Sensor_Failure_Icon.getPaint());
						}
					}
					else {
						/*left = 0;
						right = (int) convert_mm2pixel(Label_cxChar);
						top = (int) convert_mm2pixel(Border_top + i * mDisplay_well_pitch_y);
						bottom = (int) convert_mm2pixel(Border_top + (i + 1) * mDisplay_well_pitch_y);
						dst_rect.set(left, top, right, bottom);
						if (Only_Sensor_Failure_Icon != null) {
							led_sensor_failure_bmp = Only_Sensor_Failure_Icon.getBitmap();
							Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Only_Sensor_Failure_Icon.getPaint());
						}*/
					}
				Canvas_Well_Plate.drawText(Character.toString((char) (chr + i)), margin_x, margin_y, mPaint_text);
            }
            
            if (Update_Label_Only == false) {
            radius_pixels = (mDisplay_well_pitch_x > mDisplay_well_pitch_y) ? convert_mm2pixel((mDisplay_well_pitch_y-1)/2):convert_mm2pixel((mDisplay_well_pitch_x-1)/2);
            //mPaint.setColor(Color.BLUE);
            //mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            for (i = 0; i < Y_holes; i++) {
            	margin_y = convert_mm2pixel((2*Border_top+mDisplay_well_pitch_y+i*2*mDisplay_well_pitch_y)/2);
            	for (j = 0, margin_x = convert_mm2pixel((2*Border_left+mDisplay_well_pitch_x)/2); j < X_holes; j++) {
            		margin_x = convert_mm2pixel((2*Border_left+mDisplay_well_pitch_x+j*2*mDisplay_well_pitch_x)/2);

/*//     			   memdc.Ellipse(Border_left+(i+1)*m_pitch_to_pixels-10, Border_top+(j+1)*m_pitch_to_pixels-10, Border_left+(i+1)*m_pitch_to_pixels+10, Border_top+(j+1)*m_pitch_to_pixels+10);
    				if (!Valid_Coord_Histogram[i][j]) {
    				  memdc.SelectObject(pOldbrush);
    				}
    				else
    			       if (Valid_Coord_Histogram[i][j] <= 7) {
    			         memdc.SelectObject(&m_Mark_brush[Valid_Coord_Histogram[i][j]-1]);
    			       }
    				   else
    				      memdc.SelectObject(&m_Mark_brush[6]);
    //20121101 modified by michael
    				if (Well_Selection==0) {
    				  memdc.Ellipse(Border_left+(i+1)*m_pitch_to_pixels-10, Border_top+(Y_holes-j)*m_pitch_to_pixels-10, Border_left+(i+1)*m_pitch_to_pixels+10, Border_top+(Y_holes-j)*m_pitch_to_pixels+10);
    				}
    				else {
    				   memdc.Ellipse(Border_left+ (m_pitch_to_pixels-5)/2 + 15 +(i)*m_pitch_to_pixels-22, Border_top+(m_pitch_to_pixels-5)/2 + 15+(Y_holes-j-1)*m_pitch_to_pixels-22, Border_left+ (m_pitch_to_pixels-5)/2 + 15+(i)*m_pitch_to_pixels+22, Border_top+(m_pitch_to_pixels-5)/2 + 15+(Y_holes-j-1)*m_pitch_to_pixels+22);				
    				}*/
					//synchronized (Well_Color_index) {
						/*if (Well_Color_index[j][i] == 0) {
							mPaint = mPaint_well_Stroke;
						} else {
							mPaint = mPaint_well_Fill;
							if (Well_Color_index[j][i] <= total_colors) {
								mPaint.setColor(Mark_Color_Table[Well_Color_index[j][i] - 1]);
							} else {
								mPaint.setColor(Mark_Color_Table[total_colors - 1]);
							}
						}*/
					//}
            		Canvas_Well_Plate.drawCircle(margin_x, margin_y, radius_pixels, mPaint_well_Stroke);
            	}
            }
            }
            //Draw_Client_Well_Region(Canvas_Well_Plate, 5, 70);
            //Draw_Client_Well_Region(Canvas_Well_Plate, 8, 93);
            //Draw_Client_Well_Region(Canvas_Well_Plate, 7, 93);
            //mMaxTouchablePosY = margin_y + radius_pixels;
		}
		else
			if (mWells == Wells_384) {
				/*20131217 added by michael*/
				mPaint_well_Stroke.setXfermode(this.Xfermode_clear);
				mPaint_text.setColor(Color.WHITE);
				mPaint_text.setTextSize(convert_mm2pixel(Label_cxChar));
				//mPaint_text.setStyle(Paint.Style.STROKE);
				for (i = 0, margin_x = 0, margin_y = convert_mm2pixel(Label_cyChar); i < X_holes; i++) {
					//label_pixels_x = convert_mm2pixel(Label_cxChar * Integer.toString(i + 1).length());

					if (mDisplay_well_pitch_x > (label_width * Integer.toString(i + 1).length()))
						
	/*					 margin_x = convert_mm2pixel(Border_left) +
						 convert_mm2pixel(i * mwell_pitch_x) +
						 (convert_mm2pixel(mwell_pitch_x) - label_pixels_x) / 2;*/
						 
						margin_x = convert_mm2pixel((2 * Border_left + 2 * i
								* mDisplay_well_pitch_x + mDisplay_well_pitch_x - (label_width * Integer.toString(i + 1).length())) / 2);
					else
						
	/*					margin_x = convert_mm2pixel(Border_left) +
						 convert_mm2pixel(i * mwell_pitch_x) + (label_pixels_x -
						 convert_mm2pixel(mwell_pitch_x)) / 2;*/
						 
						margin_x = convert_mm2pixel((2 * Border_left + 2 * i
								* mDisplay_well_pitch_x + (label_width * Integer.toString(i + 1).length() - mDisplay_well_pitch_x)) / 2);

					if ((this.X_Led_failure & 1 << (i)) != 0) {
						left = (int) convert_mm2pixel(Border_left + i * mDisplay_well_pitch_x);
						right = (int) convert_mm2pixel(Border_left + (i + 1)* mDisplay_well_pitch_x);
						top = 0;
						bottom = (int) margin_y;
						dst_rect.set(left, top, right, bottom);
						if ((this.X_Sensor_failure & 1 << (i)) != 0) {
							//Led & Sensor both fail
							if (Led_Sensor_Failure_Icon != null) {
								led_sensor_failure_bmp = Led_Sensor_Failure_Icon.getBitmap();
								Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Led_Sensor_Failure_Icon.getPaint());
							}
						}
						else {
							//only Led fail
							if (Only_Led_Failure_Icon != null) {
								led_sensor_failure_bmp = Only_Led_Failure_Icon.getBitmap();
								Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Only_Led_Failure_Icon.getPaint());
							}
						}					
					}
					else
						if ((this.X_Sensor_failure & 1 << (i)) != 0) {
							//only Sensor fail
							left = (int) convert_mm2pixel(Border_left + i * mDisplay_well_pitch_x);
							right = (int) convert_mm2pixel(Border_left + (i + 1)* mDisplay_well_pitch_x);
							top = 0;
							bottom = (int) margin_y;
							dst_rect.set(left, top, right, bottom);
							if (Only_Sensor_Failure_Icon != null) {
								led_sensor_failure_bmp = Only_Sensor_Failure_Icon.getBitmap();
								Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Only_Sensor_Failure_Icon.getPaint());
							}
						}
						else {
							/*left = (int) convert_mm2pixel(Border_left + i * mDisplay_well_pitch_x);
							right = (int) convert_mm2pixel(Border_left + (i + 1)* mDisplay_well_pitch_x);
							top = 0;
							bottom = (int) margin_y;
							dst_rect.set(left, top, right, bottom);
							if (Led_Sensor_Failure_Icon != null) {
								led_sensor_failure_bmp = Led_Sensor_Failure_Icon.getBitmap();
								Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Led_Sensor_Failure_Icon.getPaint());
							}*/
						}

					Canvas_Well_Plate.drawText(Integer.toString(i + 1), margin_x, margin_y, mPaint_text);
				}

	            for (i = 0, margin_x = 0, margin_y = 0; i < Y_holes; i++) {
					if (mDisplay_well_pitch_y > label_height)
						margin_y = convert_mm2pixel((2 * Border_top + 2 * i
								* mDisplay_well_pitch_y + mDisplay_well_pitch_y - label_height + 2 * label_height) / 2);
					else
						margin_y = convert_mm2pixel((2 * Border_top + 2 * i
								* mDisplay_well_pitch_y + label_height - mDisplay_well_pitch_y + 2 * label_height) / 2);
					if ((this.Y_Led_failure & 1 << (i)) != 0) {
						left = 0;
						right = (int) convert_mm2pixel(Label_cxChar);
						top = (int) convert_mm2pixel(Border_top + i * mDisplay_well_pitch_y);
						bottom = (int) convert_mm2pixel(Border_top + (i + 1) * mDisplay_well_pitch_y);
						dst_rect.set(left, top, right, bottom);
						if ((this.Y_Sensor_failure & 1 << (i)) != 0) {
							//Led & Sensor both fail
							if (Led_Sensor_Failure_Icon != null) {
								led_sensor_failure_bmp = Led_Sensor_Failure_Icon.getBitmap();
								Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Led_Sensor_Failure_Icon.getPaint());
							}
						}
						else {
							//only Led fail
							if (Only_Led_Failure_Icon != null) {
								led_sensor_failure_bmp = Only_Led_Failure_Icon.getBitmap();
								Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Only_Led_Failure_Icon.getPaint());
							}
						}					
					}
					else
						if ((this.Y_Sensor_failure & 1 << (i)) != 0) {
							//only Sensor fail
							left = 0;
							right = (int) convert_mm2pixel(Label_cxChar);
							top = (int) convert_mm2pixel(Border_top + i * mDisplay_well_pitch_y);
							bottom = (int) convert_mm2pixel(Border_top + (i + 1) * mDisplay_well_pitch_y);
							dst_rect.set(left, top, right, bottom);
							if (Only_Sensor_Failure_Icon != null) {
								led_sensor_failure_bmp = Only_Sensor_Failure_Icon.getBitmap();
								Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Only_Sensor_Failure_Icon.getPaint());
							}
						}
						else {
							/*left = 0;
							right = (int) convert_mm2pixel(Label_cxChar);
							top = (int) convert_mm2pixel(Border_top + i * mDisplay_well_pitch_y);
							bottom = (int) convert_mm2pixel(Border_top + (i + 1) * mDisplay_well_pitch_y);
							dst_rect.set(left, top, right, bottom);
							if (Only_Sensor_Failure_Icon != null) {
								led_sensor_failure_bmp = Only_Sensor_Failure_Icon.getBitmap();
								Canvas_Well_Plate.drawBitmap(led_sensor_failure_bmp, null, dst_rect, Only_Sensor_Failure_Icon.getPaint());
							}*/
						}
					Canvas_Well_Plate.drawText(Character.toString((char) (chr + i)), margin_x, margin_y, mPaint_text);
	            }
	            if (Update_Label_Only == false) {
	            radius_pixels = (mDisplay_well_pitch_x > mDisplay_well_pitch_y) ? convert_mm2pixel((mDisplay_well_pitch_y-1)/2):convert_mm2pixel((mDisplay_well_pitch_x-1)/2);
	            //mPaint.setColor(Color.BLUE);
	            //mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	            for (i = 0; i < Y_holes; i++) {
	            	margin_y = convert_mm2pixel((2*Border_top+mDisplay_well_pitch_y+i*2*mDisplay_well_pitch_y)/2);
	            	for (j = 0, margin_x = convert_mm2pixel((2*Border_left+mDisplay_well_pitch_x)/2); j < X_holes; j++) {
	            		margin_x = convert_mm2pixel((2*Border_left+mDisplay_well_pitch_x+j*2*mDisplay_well_pitch_x)/2);
	            		//pick up the correspond pen&brush setting for Paint
					//synchronized (Well_Color_index) {
						/*if (Well_Color_index[j][i] == 0) {
							mPaint = mPaint_well_Stroke;
						} else {
							mPaint = mPaint_well_Fill;
							if (Well_Color_index[j][i] <= total_colors) {
								mPaint.setColor(Mark_Color_Table[Well_Color_index[j][i] - 1]);
							} else {
								mPaint.setColor(Mark_Color_Table[total_colors - 1]);
							}
						}*/
					//}
	            		Canvas_Well_Plate.drawCircle(margin_x, margin_y, radius_pixels, mPaint_well_Stroke);
	            	}
	            }
	            }
	            //Draw_Client_Well_Region(Canvas_Well_Plate, 8, 94);
	            //Draw_Client_Well_Region(Canvas_Well_Plate, 7, 94);
	            //mMaxTouchablePosY = margin_y + radius_pixels;
			}
		//this.invalidate();
		if(Update_Label_Only == true) {
            this.invalidate(0, 0, (int)convert_mm2pixel(Viewable_width), (int)convert_mm2pixel(Label_cyChar));
            this.invalidate(0, 0, (int)convert_mm2pixel(Label_cxChar), (int)convert_mm2pixel(Viewable_height));
		}
	}

	//@Override
	protected void onDraw(Canvas canvas) {
		//DrawBitmap();
		super.onDraw(canvas);
      /*Paint paint = new Paint();  
      paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));  
      canvas.drawPaint(paint);  
      paint.setXfermode(new PorterDuffXfermode(Mode.SRC));*/
		//canvas.drawColor(Color.BLACK);
		//canvas.drawBitmap(Bmp_Well_Plate, 0, 0, null);
	}
	
	private void Draw_Client_Well_Region(Canvas canvas, int Client_Border_left, int Client_Border_top)
	{
		//int i, j, margin_x, margin_y;
		int i, j;
		float margin_x, margin_y;
		int chr = 'A';
		//int radius_pixels;
		float radius_pixels;
//Pain implement integration function of Pen&Brush in MFC
		//Paint mPaint = new Paint();
		double mClient_well_pitch_x, mClient_well_pitch_y;
		
		mPaint_well_Stroke.setStrokeWidth(convert_mm2pixel(1));
		mClient_well_pitch_x = (mwell_pitch_x > mwell_pitch_y) ? mwell_pitch_x : mwell_pitch_y;
		mClient_well_pitch_y = (mwell_pitch_x > mwell_pitch_y) ? mwell_pitch_x : mwell_pitch_y;
		if (mWells== Wells_384) {
			mClient_well_pitch_x = mClient_well_pitch_y - 0.1;
			mClient_well_pitch_y = mClient_well_pitch_x - 0.08;
			//radius_pixels = (mClient_well_pitch_x > mClient_well_pitch_y) ? convert_mm2pixel((mClient_well_pitch_y-0.7)/2):convert_mm2pixel((mClient_well_pitch_x-0.7)/2);
			radius_pixels = (mClient_well_pitch_x > mClient_well_pitch_y) ? convert_mm2pixel((mClient_well_pitch_y-1.3)/2):convert_mm2pixel((mClient_well_pitch_x-1.3)/2);
            //mPaint.setColor(Color.BLUE);
            //mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            for (i = 0; i < Y_holes; i++) {
            	margin_y = convert_mm2pixel((2*Client_Border_top+mClient_well_pitch_y+i*2*mClient_well_pitch_y)/2);
            	for (j = 0, margin_x = convert_mm2pixel((2*Client_Border_left+mClient_well_pitch_x)/2); j < X_holes; j++) {
            		margin_x = convert_mm2pixel((2*Client_Border_left+mClient_well_pitch_x+j*2*mClient_well_pitch_x)/2);
					//synchronized (Well_Color_index) {
						if (Well_Color_index[j][i] == 0) {
							mPaint = mPaint_well_Stroke;
							if (I_Tacker_Activity.mWell_View_Display_Mode==0)
								canvas.drawCircle(margin_x, margin_y, radius_pixels, mPaint);
							else {
								mPaint.setColor(Color.BLACK);
								canvas.drawRect(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels, mPaint);
								mPaint.setColor(Color.WHITE);
							}

						} else {
							mPaint = mPaint_well_Fill;
							if (Well_Color_index[j][i] <= total_colors) {
								mPaint.setColor(Mark_Color_Table[Well_Color_index[j][i] - 1]);
							} else {
								mPaint.setColor(Mark_Color_Table[total_colors - 1]);
							}
						    canvas.drawCircle(margin_x, margin_y, radius_pixels, mPaint);
						}
					//}
            	}
            }	
		}
		else 
			if (mWells == Wells_96) {
				mClient_well_pitch_x = mClient_well_pitch_y - 0.12;
				mClient_well_pitch_y = mClient_well_pitch_x - 0.1;
	            //radius_pixels = (mClient_well_pitch_x > mClient_well_pitch_y) ? convert_mm2pixel((mClient_well_pitch_y-1)/2):convert_mm2pixel((mClient_well_pitch_x-1)/2);
				radius_pixels = (mClient_well_pitch_x > mClient_well_pitch_y) ? convert_mm2pixel((mClient_well_pitch_y-6)/2):convert_mm2pixel((mClient_well_pitch_x-6)/2);
	            //mPaint.setColor(Color.BLUE);
	            //mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	            for (i = 0; i < Y_holes; i++) {
	            	margin_y = convert_mm2pixel((2*Client_Border_top+mClient_well_pitch_y+i*2*mClient_well_pitch_y)/2);
	            	for (j = 0, margin_x = convert_mm2pixel((2*Client_Border_left+mClient_well_pitch_x)/2); j < X_holes; j++) {
					margin_x = convert_mm2pixel((2 * Client_Border_left
							+ mClient_well_pitch_x + j * 2
							* mClient_well_pitch_x) / 2);
					//synchronized (Well_Color_index) {
						if (Well_Color_index[j][i] == 0) {
							mPaint = mPaint_well_Stroke;
							if (I_Tacker_Activity.mWell_View_Display_Mode==0) {
								canvas.drawCircle(margin_x, margin_y, radius_pixels, mPaint);
							}
							else {
								mPaint.setColor(Color.BLACK);
								canvas.drawRect(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels, mPaint);
								mPaint.setColor(Color.WHITE);			
							}

						} else {
							mPaint = mPaint_well_Fill;
							if (Well_Color_index[j][i] <= total_colors) {
								mPaint.setColor(Mark_Color_Table[Well_Color_index[j][i] - 1]);
							} else {
								mPaint.setColor(Mark_Color_Table[total_colors - 1]);
							}
                            canvas.drawCircle(margin_x, margin_y, radius_pixels, mPaint);
						}
					//}
	            	}
	            }							
			}
		mPaint_well_Stroke.setStrokeWidth(convert_mm2pixel(0.5));
	}
	
	public float convert_mm2pixel(double value) {
		//DisplayMetrics metrics = new DisplayMetrics();
		//((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(metrics);
		
		//return (int) (0.75*value * metrics.xdpi * (1.0f/25.4f));
		//return (int) (value * metrics.densityDpi * (1.0f/25.4f) * (12.7 / 12.1));
		//return (float) (value * metrics.densityDpi * (1.0f/25.4f));
		//return (int) (value * metrics.densityDpi * (1.0f/25.4f) * ((2.54 * screen_width_pixel() / metrics.densityDpi) / Screen_Short_Width_cm));
		return (int) (value * (1.0f/25.4f) * ((2.54 * screen_width_pixel()) / Screen_Short_Width_cm));
		
		//return (int) ((metrics.densityDpi/metrics.xdpi) * (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, (float) value,  metrics)));
	}
	
	/*20130325 night added by michael*/
	//only invalidate the well with histogram change
	private void Invalidate_Single_Well(int color_index, int well_x, int well_y) {
		//int i, j, margin_x, margin_y, chrs;
		int i, j, chrs;
		float margin_x, margin_y;
		//int radius_pixels;
		float radius_pixels;
		double mClient_well_pitch_x, mClient_well_pitch_y, adjust_radius_mm;
		double Client_Border_left, Client_Border_top;
		double mDisplay_well_pitch_x, mDisplay_well_pitch_y;

		//mPaint_well_Stroke.setStrokeWidth(convert_mm2pixel(1));
		mClient_well_pitch_x = mwell_pitch_x;
		mClient_well_pitch_y = mwell_pitch_y;
		if (mWells == Wells_96) {
			mDisplay_well_pitch_x = (Viewable_width - Border_left) / X_holes;
			mDisplay_well_pitch_y = (Viewable_height - Border_top) / Y_holes;
		}
		else {
			mDisplay_well_pitch_x = (Viewable_width - Border_left) / X_holes;
			mDisplay_well_pitch_y = (Viewable_height - Border_top) / Y_holes;
		}
        //Border_top = (mDisplay_well_pitch_x > mDisplay_well_pitch_y) ? mDisplay_well_pitch_y : mDisplay_well_pitch_x;
        //Border_left = Border_top;
		//mClient_well_pitch_x = (mwell_pitch_x > mwell_pitch_y) ? mwell_pitch_x : mwell_pitch_y;
		//mClient_well_pitch_y = (mwell_pitch_x > mwell_pitch_y) ? mwell_pitch_x : mwell_pitch_y;
		//mClient_well_pitch_x = mClient_well_pitch_y - 0.11;
		//mClient_well_pitch_y = mClient_well_pitch_x - 0.09;
		if (mWells==Wells_96) {
			//adjust_radius_mm = 6;
			adjust_radius_mm = 3;
			//mClient_well_pitch_x = mClient_well_pitch_y - 0.12;
			//mClient_well_pitch_y = mClient_well_pitch_x - 0.1;
			//mClient_well_pitch_x = mClient_well_pitch_y - 0.11;
			//mClient_well_pitch_y = mClient_well_pitch_x - 0.09;
			//Client_Border_left = 7;
			//Client_Border_top = 93;
			//Client_Border_left = 5.5;
			//Client_Border_top = 90;
			Client_Border_left = mwells_offset_x;
			Client_Border_top = mwells_offset_y;
		}
		else {
			adjust_radius_mm = 2;
			//mClient_well_pitch_x = mClient_well_pitch_y - 0.1;
			//mClient_well_pitch_y = mClient_well_pitch_x - 0.08;
			//mClient_well_pitch_x = mClient_well_pitch_y - 0.115;
			//mClient_well_pitch_y = mClient_well_pitch_x - 0.09;
			//Client_Border_left = 7;
			//Client_Border_top = 94;
			//Client_Border_left = 5.5;
			//Client_Border_top = 89.5;
			Client_Border_left = mwells_offset_x;
			Client_Border_top = mwells_offset_y;
		}
			
		//if (mWells==Wells_96) {
			/*update the upper well*/
            radius_pixels = (mDisplay_well_pitch_x > mDisplay_well_pitch_y) ? convert_mm2pixel((mDisplay_well_pitch_y-1)/2):convert_mm2pixel((mDisplay_well_pitch_x-1)/2);
            margin_y = convert_mm2pixel((2*Border_top+mDisplay_well_pitch_y+well_y*2*mDisplay_well_pitch_y)/2);
            margin_x = convert_mm2pixel((2*Border_left+mDisplay_well_pitch_x+well_x*2*mDisplay_well_pitch_x)/2);
			if (color_index==0) {
				mPaint = mPaint_well_Fill;
				//mPaint.setColor(Color.BLACK);
				mPaint.setXfermode(Xfermode_clear);
				if (mWells == Wells_96)
					Canvas_Well_Plate.drawRect(margin_x-radius_pixels-2, margin_y-radius_pixels-2, margin_x+radius_pixels+2, margin_y+radius_pixels+2, mPaint);
				else
					Canvas_Well_Plate.drawRect(margin_x-radius_pixels-3, margin_y-radius_pixels-3, margin_x+radius_pixels+3, margin_y+radius_pixels+3, mPaint);
				mPaint.setXfermode(null);
				//mPaint.setColor(Color.WHITE);
				mPaint = mPaint_well_Stroke;
				mPaint.setColor(Color.WHITE);
				if (mWells == Wells_96)
					  Canvas_Well_Plate.drawCircle(margin_x, margin_y, radius_pixels, mPaint);
			} else {
				mPaint = mPaint_well_Stroke;
				mPaint.setColor(Color.WHITE);
				if (mWells == Wells_96)
					  Canvas_Well_Plate.drawCircle(margin_x, margin_y, radius_pixels, mPaint);

				mPaint = mPaint_well_Fill;
				if (color_index <= total_colors) {
					mPaint.setColor(Mark_Color_Table[color_index - 1]);
				} else {
					mPaint.setColor(Mark_Color_Table[total_colors - 1]);
				}
				//mPaint.setXfermode(Xfermode_dst_over);
				mPaint.setXfermode(Xfermode_src_over);
				
				if (mWells == Wells_96)
					  Canvas_Well_Plate.drawCircle(margin_x, margin_y, radius_pixels-1, mPaint);
				else
					Canvas_Well_Plate.drawCircle(margin_x, margin_y, radius_pixels+2, mPaint);
				mPaint.setXfermode(null);
			}
			/*if (mWells == Wells_96)
			  Canvas_Well_Plate.drawCircle(margin_x, margin_y, radius_pixels, mPaint);
			else
				Canvas_Well_Plate.drawCircle(margin_x, margin_y, radius_pixels+2, mPaint);
			if (mPaint == mPaint_well_Fill)
				mPaint.setXfermode(null);*/
			/*20131217 added by michael*/
			//if (color_index > 0 && this.mWells == this.Wells_96) {
			if (color_index > 0) {
				chrs = Integer.toString(color_index).length();
				if (color_index <= 6) {
					if (chrs == 1) {
						Canvas_Well_Plate.drawText(Integer.toString(color_index), margin_x-convert_mm2pixel(Label_cxChar/3), margin_y+convert_mm2pixel(Label_cxChar/3), mPaint_transparent);
					}
					else {
						Canvas_Well_Plate.drawText(Integer.toString(color_index), margin_x-convert_mm2pixel(chrs*Label_cxChar/3), margin_y+convert_mm2pixel(Label_cxChar/3), mPaint_transparent);	
					}
				}
				else {
					if (chrs == 1) {
						Canvas_Well_Plate.drawText(Integer.toString(color_index), margin_x-convert_mm2pixel(Label_cxChar/3), margin_y+convert_mm2pixel(Label_cxChar/3), mPaint_transparent1);
					}
					else {
						Canvas_Well_Plate.drawText(Integer.toString(color_index), margin_x-convert_mm2pixel(chrs*Label_cxChar/3), margin_y+convert_mm2pixel(Label_cxChar/3), mPaint_transparent1);	
					}
				}
			}
			this.invalidate((int)(margin_x-radius_pixels-3), (int)(margin_y-radius_pixels-3), (int)(margin_x+radius_pixels+3), (int)(margin_y+radius_pixels+3));
			
			/*update the lower well*/
			radius_pixels = (mClient_well_pitch_x > mClient_well_pitch_y) ? convert_mm2pixel((mClient_well_pitch_y-adjust_radius_mm)/2):convert_mm2pixel((mClient_well_pitch_x-adjust_radius_mm)/2);
			margin_y = convert_mm2pixel((2*Client_Border_top+mClient_well_pitch_y+well_y*2*mClient_well_pitch_y)/2);
			margin_x = convert_mm2pixel((2*Client_Border_left+mClient_well_pitch_x+well_x*2*mClient_well_pitch_x)/2);
			if (color_index==0) {
				if (I_Tacker_Activity.mWell_View_Display_Mode==0) {
					mPaint = mPaint_well_Fill;
					mPaint.setXfermode(Xfermode_clear);
					Canvas_Well_Plate.drawRect(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels, mPaint);
					mPaint.setXfermode(null);
					mPaint = mPaint_well_Stroke;
					Canvas_Well_Plate.drawCircle(margin_x, margin_y, radius_pixels, mPaint);
					this.invalidate((int)(margin_x-radius_pixels), (int)(margin_y-radius_pixels), (int)(margin_x+radius_pixels), (int)(margin_y+radius_pixels));
				}
				else {
					mPaint = mPaint_well_Fill;
					mPaint.setXfermode(Xfermode_clear);
					Canvas_Well_Plate.drawRect(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels, mPaint);
					mPaint.setXfermode(null);
					this.invalidate((int)(margin_x-radius_pixels), (int)(margin_y-radius_pixels), (int)(margin_x+radius_pixels), (int)(margin_y+radius_pixels));
					/*mPaint = mPaint_well_Fill;
					mPaint.setColor(Color.BLACK);
					Canvas_Well_Plate.drawRect(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels, mPaint);
					this.invalidate(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels);
					mPaint.setColor(Color.WHITE);*/			
				}

			} else {
				mPaint = mPaint_well_Fill;
				if (color_index <= total_colors) {
					mPaint.setColor(Mark_Color_Table[color_index - 1]);
				} else {
					mPaint.setColor(Mark_Color_Table[total_colors - 1]);
				}
				Canvas_Well_Plate.drawCircle(margin_x, margin_y, radius_pixels, mPaint);
				this.invalidate((int)(margin_x-radius_pixels), (int)(margin_y-radius_pixels), (int)(margin_x+radius_pixels), (int)(margin_y+radius_pixels));
			}
		/*}
		else if (mWells==Wells_384){
			
		}*/
		//mPaint_well_Stroke.setStrokeWidth(convert_mm2pixel(0.5));
	}

	public void setWellColor(int a[][]) {
		int i = 0, j = 0, temp;
		//synchronized (Well_Color_index) {
		//synchronized(lock1) {
			//for (i = 0; i < a.length; i++) {
			for (i = 0; i < X_holes; i++) {
				// System.arraycopy( aArray, 0, copy, 0, aArray.length );
				//System.arraycopy(a[i], 0, Well_Color_index[i], 0, a[i].length);
				// Well_Color_index = a;
				//for (j = 0; j < Y_holes/2; j++) {
				for (j = 0; j < Y_holes; j++) {
/*					temp = Well_Color_index[i][j];
					Well_Color_index[i][j] = Well_Color_index[i][Y_holes-1-j];
					Well_Color_index[i][Y_holes-1-j] = temp;*/
					
					/*20130325 night added by michael*/
					//compare current color index with well histogram
					if ((Well_Color_index[i][j] != a[i][Y_holes-1-j]) || force_invalide == true) {
						Invalidate_Single_Well(a[i][Y_holes-1-j], i, j);
						Well_Color_index[i][j] = a[i][Y_holes-1-j];
					}
				}
			}
		//}
	}
	
	
	public void ResetWell() {
    	/*20140616 added by michael
    	 * reset the valid coordinate count*/
    	Last_Coord_X_Count = Last_Coord_Y_Count = 0;
    	for (int i = 0; i < Well_Color_index.length; i++)
    		Arrays.fill(Well_Color_index[i], 0x00);
    	//Canvas_Well_Plate.drawColor(Color.BLUE, PorterDuff.Mode.CLEAR);
    	//Canvas_Well_Plate.drawRGB(255, 0, 0);
    	//Canvas_Well_Plate.drawARGB(150, 255, 0, 0);
    	//Bmp_Well_Plate.eraseColor(Color.BLACK);
    	//mPaint = this.mPaint_well_Fill;
    	//mPaint.setColor(Color.TRANSPARENT);
    	//mPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
    	//Canvas_Well_Plate.drawRect(0, 0, 600, 800, mPaint);
    	//Canvas_Well_Plate.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
/*    	Xfermode xmode = mPaint_well_Stroke.getXfermode();
    	mPaint_well_Stroke.setColor(Color.TRANSPARENT);
    	mPaint_well_Stroke.setXfermode(new PorterDuffXfermode(Mode.CLEAR));  
    	Canvas_Well_Plate.drawPaint(mPaint_well_Stroke);
    	mPaint_well_Stroke.setXfermode(new PorterDuffXfermode(Mode.SRC));*/
    	set_led_sensor_failure(0, 0, 0, 0);
    	DrawBitmap(false);
    	//invalidate();
	}
	
	public void setWell(int well_type) {
		mWells = well_type;

		if (well_type == Wells_96) {
			//mwell_pitch_x = 9.580d;
			mwell_pitch_x = 9.25d;
			mwell_pitch_y = 9.25d;
			X_holes = 12;
			Y_holes = 8;
			Label_cxChar = 5;
			Label_cyChar = 5;
			Border_left = Label_cxChar + 1;
			Border_top = Label_cyChar + 1;
			/*20131213 added by michael*/
			multi_pipettes_well_gap = 1;
			/*20131217 added by michael*/
			mPaint_well_Stroke.setXfermode(null);
			/*20140616 added by michael*/
			Viewable_height = 60;
			Viewable_width = 116; 
			mwells_offset_x = 5.5d;
			mwells_offset_y = 90d;
		} else if (well_type == Wells_384) {
			mwell_pitch_x = 9.25d / 2;
			mwell_pitch_y = 9.25d / 2;
			X_holes = 24;
			Y_holes = 16;
			Label_cxChar = 3.3;
			Label_cyChar = 3;
			Border_left = Label_cxChar + 1;
			Border_top = Label_cyChar + 1;
			/*20131213 added by michael*/
			multi_pipettes_well_gap = 2;
			/*20131217 added by michael*/
			mPaint_well_Stroke.setXfermode(Xfermode_clear);
			/*20140616 added by michael*/
			Viewable_height = 60;
			Viewable_width = 116;
			mwells_offset_x = 5.5d;
			mwells_offset_y = 89.5d;
		}
        /*20140616 modified by michael*/
		//mMaxTouchablePosY = convert_mm2pixel((2*Border_top+2*mwell_pitch_y+(Y_holes-1)*2*mwell_pitch_y-0.8)/2);
		mMaxTouchablePosY = (float)convert_mm2pixel(Viewable_height);
		//this.invalidate();
		/*20131213 added by michael*/
		mPaint_transparent.setTextSize(convert_mm2pixel(Label_cxChar));
		mPaint_transparent1.setTextSize(convert_mm2pixel(Label_cxChar));
	}
	
	/*20140821 added by michael*/
	public void setWell(int well_type, iTrack_Properties properties, boolean force_invalid_well) {
		setWell(well_type);
		
		/*Override well layout parameter with the i-track app property*/
		if (properties != null) {
			Screen_Short_Width_cm = Double.parseDouble((properties.getProperty(iTrack_Properties.prop_screen_short_edge_width, Double.toString(iTrack_Properties.def_prop_screen_short_edge_cm))));
			Viewable_width = Double.parseDouble((properties.getProperty(iTrack_Properties.prop_viewable_width, Double.toString(iTrack_Properties.def_prop_viewable_width_mm))));
			Viewable_height = Double.parseDouble((properties.getProperty(iTrack_Properties.prop_viewable_height, Double.toString(iTrack_Properties.def_prop_viewable_height_mm))));
			mwell_pitch_x = Double.parseDouble((properties.getProperty(iTrack_Properties.prop_well_pitch_x, Double.toString(iTrack_Properties.def_well_pitch_x_mm))));
			mwell_pitch_y = Double.parseDouble((properties.getProperty(iTrack_Properties.prop_well_pitch_y, Double.toString(iTrack_Properties.def_well_pitch_y_mm))));
			if (well_type == Wells_96) {

			}
			else {
				mwell_pitch_x = mwell_pitch_x /2;
				mwell_pitch_y = mwell_pitch_y /2;
			}
			mwells_offset_x =  Double.parseDouble((properties.getProperty(iTrack_Properties.prop_wells_offset_x, Double.toString(iTrack_Properties.def_wells_offset_x_mm))));
			mwells_offset_y =  Double.parseDouble((properties.getProperty(iTrack_Properties.prop_wells_offset_y, Double.toString(iTrack_Properties.def_wells_offset_y_mm))));
			mMaxTouchablePosY = (float)convert_mm2pixel(Viewable_height);
		}
		force_invalide = force_invalid_well;
		//force_invalide = false;
	}
	
	/*20130327 added by michael*/
	public void set_focus_coord(int focus_valid_coord) {
		if (Show_focus_coord)
			blink_last_well();

		if (focus_valid_coord != -1) {
			Last_Coord_X = (focus_valid_coord) & I_Tracker_Device.Coord_X_Mask;
			Last_Coord_Y = (focus_valid_coord >>> I_Tracker_Device.Coord_Y_shift) & I_Tracker_Device.Coord_X_Mask;
			Last_Coord_Y = Y_holes - 1 - Last_Coord_Y;
			Last_Coord_X_Count = (focus_valid_coord >>> I_Tracker_Device.Coord_X_Count_shift) & I_Tracker_Device.Coord_X_Count_Mask;
			Last_Coord_Y_Count = (focus_valid_coord >>> I_Tracker_Device.Coord_Y_Count_shift) & I_Tracker_Device.Coord_X_Count_Mask;
			Show_focus_coord = false;
		}
		else {
			Last_Coord_X_Count = -1;
			Last_Coord_Y_Count = -1;
		}
	}
	
	public void blink_last_well() {
		int x, y, i, j;
		
		if (Last_Coord_X_Count > 0 && Last_Coord_Y_Count > 0) {
			if (Show_focus_coord) {
				//Invalidate_Single_Well(Well_Color_index[Last_Coord_X][Last_Coord_Y], Last_Coord_X, Last_Coord_Y);
				for (i = 0, x = Last_Coord_X; i < Last_Coord_X_Count; i++, x+= multi_pipettes_well_gap) {
					for (j = 0, y = Last_Coord_Y; j < Last_Coord_Y_Count; j++, y-= multi_pipettes_well_gap) {
						Invalidate_Single_Well(Well_Color_index[x][y], x, y);
					}
				}
				Show_focus_coord = false;
			}
			else {
				//Invalidate_Single_Well(0, Last_Coord_X, Last_Coord_Y);
				for (i = 0, x = Last_Coord_X; i < Last_Coord_X_Count; i++, x+= multi_pipettes_well_gap) {
					for (j = 0, y = Last_Coord_Y; j < Last_Coord_Y_Count; j++, y-= multi_pipettes_well_gap) {
						Invalidate_Single_Well(0, x, y);
					}
				}
				Show_focus_coord = true;
			}
		}
	}
	
	/*20131208 added by michael*/
	public void decrese_SingleWellColor(int focus_valid_coord) {
		int Coord_X, Coord_Y, Coord_X_Count, Coord_Y_Count, x, y, i, j; 

		if (focus_valid_coord != -1) {
			Coord_X = (focus_valid_coord) & I_Tracker_Device.Coord_X_Mask;
			Coord_Y = (focus_valid_coord >>> I_Tracker_Device.Coord_Y_shift) & I_Tracker_Device.Coord_X_Mask;
			Coord_Y = Y_holes - 1 - Coord_Y;
			Coord_X_Count = (focus_valid_coord >>> I_Tracker_Device.Coord_X_Count_shift) & I_Tracker_Device.Coord_X_Count_Mask;
			Coord_Y_Count = (focus_valid_coord >>> I_Tracker_Device.Coord_Y_Count_shift) & I_Tracker_Device.Coord_X_Count_Mask;
			
			if (Coord_X >= 0 && Coord_Y >= 0) {
				//synchronized(lock1) {
				for (i = 0, x = Coord_X; i < Coord_X_Count; i++, x += multi_pipettes_well_gap) {
					for (j = 0, y = Coord_Y; j < Coord_Y_Count; j++, y -= multi_pipettes_well_gap) {
						Well_Color_index[x][y]--;
						Invalidate_Single_Well(Well_Color_index[x][y], x, y);
					}
				}
				//}
			}
		}
		else {
			Coord_X_Count = -1;
			Coord_Y_Count = -1;
		}
	}
	
	public void increase_SingleWellColor(int focus_valid_coord) {
		int Coord_X, Coord_Y, Coord_X_Count, Coord_Y_Count, x, y, i, j; 

		if (focus_valid_coord != -1) {
			Coord_X = (focus_valid_coord) & I_Tracker_Device.Coord_X_Mask;
			Coord_Y = (focus_valid_coord >>> I_Tracker_Device.Coord_Y_shift) & I_Tracker_Device.Coord_X_Mask;
			Coord_Y = Y_holes - 1 - Coord_Y;
			Coord_X_Count = (focus_valid_coord >>> I_Tracker_Device.Coord_X_Count_shift) & I_Tracker_Device.Coord_X_Count_Mask;
			Coord_Y_Count = (focus_valid_coord >>> I_Tracker_Device.Coord_Y_Count_shift) & I_Tracker_Device.Coord_X_Count_Mask;
			
			if (Coord_X >= 0 && Coord_Y >= 0) {
				//synchronized(lock1) {
				for (i = 0, x = Coord_X; i < Coord_X_Count; i++, x += multi_pipettes_well_gap) {
					for (j = 0, y = Coord_Y; j < Coord_Y_Count; j++, y -= multi_pipettes_well_gap) {
						Well_Color_index[x][y]++;
						Invalidate_Single_Well(Well_Color_index[x][y], x, y);
					}
				}
				//}
			}
		}
		else {
			Coord_X_Count = -1;
			Coord_Y_Count = -1;
		}
	}
	
	public void set_led_sensor_failure(int x_led_failure, int x_sensor_failure, int y_led_failure, int y_sensor_failure) {
		X_Led_failure = x_led_failure;
		X_Sensor_failure = x_sensor_failure;
		Y_Led_failure = y_led_failure;
		Y_Sensor_failure = y_sensor_failure;
	}
	@Override
	public void onViewAttachedToWindow(View v) {
		// TODO Auto-generated method stub
		attachToWindow = true;
	}
	@Override
	public void onViewDetachedFromWindow(View v) {
		// TODO Auto-generated method stub
		attachToWindow = false;
	}
}
