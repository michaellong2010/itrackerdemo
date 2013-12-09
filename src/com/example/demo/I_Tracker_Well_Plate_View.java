package com.example.demo;

import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;

public class I_Tracker_Well_Plate_View extends ImageView {
    static final int Wells_96 = 96;
    static final int Wells_384 = 384;
    
    private Context mContext;
    private int mWells;
    private double mwell_pitch_x;
    private double mwell_pitch_y;
    int X_holes, Y_holes;
    int well_pixels_x, well_pixels_y;
//char dimension in mm unit 
    double Label_cxChar;
	double Label_cyChar;
	int label_pixels_x, label_pixels_y;
	double Border_left;
	double Border_top;
/*
 20130311 added by michael
 touchable ROI (0, 0)~(mMaxTouchablePosX, mMaxTouchablePosY)
 mMaxTouchablePosX match the view width
 mMaxTouchablePosY calculate from the upper region
 */	
    public float mMaxTouchablePosX, mMaxTouchablePosY;
    public Paint mPaint, mPaint_text;

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
	public I_Tracker_Well_Plate_View(Context context, int wells) {
		super(context);
		mWells = wells;
		mContext = context;
		mMaxTouchablePosX = 600;
		//Pain implement integration function of Pen&Brush in MFC
		mPaint = new Paint();
		mPaint_text = new Paint();

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
			mwell_pitch_x = 9.580d;
			mwell_pitch_y = 7.5d;
			X_holes = 12;
			Y_holes = 8;
			Label_cxChar = 5;
			Label_cyChar = 5;
			Border_left = Label_cxChar + 1;
			Border_top = Label_cyChar + 1;
		} else if (wells == Wells_384) {
			mwell_pitch_x = 9.580d / 2;
			mwell_pitch_y = 7.5d / 2;
			X_holes = 24;
			Y_holes = 16;
			Label_cxChar = 3.3;
			Label_cyChar = 3;
			Border_left = Label_cxChar + 1;
			Border_top = Label_cyChar + 1;
		}
		
		Bmp_Well_Plate = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888);
		Canvas_Well_Plate = new Canvas();
		Canvas_Well_Plate.setBitmap(Bmp_Well_Plate);
		//Canvas_Well_Plate = new Canvas(Bmp_Well_Plate);
		setImageBitmap(Bmp_Well_Plate);
		mMaxTouchablePosY = convert_mm2pixel((2*Border_top+2*mwell_pitch_y+(Y_holes-1)*2*mwell_pitch_y-0.8)/2);
		
    	/*20131209 added by michael*/
    	lock1 = new Object();
    	lock2 = new Object();
	}

	/*20130325 added by michael*/
	/*draw wells on the bitmap
	 * this bitmap then set to I_Tracker_Well_Plate_View Canvas
	*/
	public void DrawBitmap() {
		int i, j, margin_x, margin_y;
		int chr = 'A';
		int radius_pixels;

		/* clear the whole bitmap content */
		mPaint_well_Stroke.setXfermode(new PorterDuffXfermode(Mode.CLEAR));  
    	Canvas_Well_Plate.drawPaint(mPaint_well_Stroke);
    	mPaint_well_Stroke.setXfermode(null);
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

				if (mwell_pitch_x > (Label_cxChar * Integer.toString(i + 1).length()))
					
/*					 margin_x = convert_mm2pixel(Border_left) +
					 convert_mm2pixel(i * mwell_pitch_x) +
					 (convert_mm2pixel(mwell_pitch_x) - label_pixels_x) / 2;*/
					 
					margin_x = convert_mm2pixel((2 * Border_left + 2 * i
							* mwell_pitch_x + mwell_pitch_x - (Label_cxChar * Integer.toString(i + 1).length())) / 2);
				else
					
/*					margin_x = convert_mm2pixel(Border_left) +
					 convert_mm2pixel(i * mwell_pitch_x) + (label_pixels_x -
					 convert_mm2pixel(mwell_pitch_x)) / 2;*/
					 
					margin_x = convert_mm2pixel((2 * Border_left + 2 * i
							* mwell_pitch_x + (Label_cxChar * Integer.toString(i + 1).length()) - mwell_pitch_x) / 2);

				Canvas_Well_Plate.drawText(Integer.toString(i + 1), margin_x, margin_y, mPaint_text);
			}

			
            for (i = 0, margin_x = 0, margin_y = 0; i < Y_holes; i++) {
				if (mwell_pitch_y > Label_cyChar)
					margin_y = convert_mm2pixel((2 * Border_top + 2 * i
							* mwell_pitch_y + mwell_pitch_y - Label_cyChar + 2 * Label_cyChar) / 2);
				else
					margin_y = convert_mm2pixel((2 * Border_top + 2 * i
							* mwell_pitch_y + Label_cyChar - mwell_pitch_y + 2 * Label_cyChar) / 2);
				Canvas_Well_Plate.drawText(Character.toString((char) (chr + i)), margin_x, margin_y, mPaint_text);
            }
            
            radius_pixels = (mwell_pitch_x > mwell_pitch_y) ? convert_mm2pixel((mwell_pitch_y-0.8)/2):convert_mm2pixel((mwell_pitch_x-0.8)/2);
            //mPaint.setColor(Color.BLUE);
            //mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            for (i = 0; i < Y_holes; i++) {
            	margin_y = convert_mm2pixel((2*Border_top+mwell_pitch_y+i*2*mwell_pitch_y)/2);
            	for (j = 0, margin_x = convert_mm2pixel((2*Border_left+mwell_pitch_x)/2); j < X_holes; j++) {
            		margin_x = convert_mm2pixel((2*Border_left+mwell_pitch_x+j*2*mwell_pitch_x)/2);

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
            //Draw_Client_Well_Region(Canvas_Well_Plate, 8, 93);
            //Draw_Client_Well_Region(Canvas_Well_Plate, 7, 93);
            //mMaxTouchablePosY = margin_y + radius_pixels;
		}
		else
			if (mWells == Wells_384) {
				mPaint_text.setColor(Color.WHITE);
				mPaint_text.setTextSize(convert_mm2pixel(Label_cxChar));
				//mPaint_text.setStyle(Paint.Style.STROKE);
				for (i = 0, margin_x = 0, margin_y = convert_mm2pixel(Label_cyChar); i < X_holes; i++) {
					//label_pixels_x = convert_mm2pixel(Label_cxChar * Integer.toString(i + 1).length());

					if (mwell_pitch_x > (Label_cxChar * Integer.toString(i + 1).length()))
						
	/*					 margin_x = convert_mm2pixel(Border_left) +
						 convert_mm2pixel(i * mwell_pitch_x) +
						 (convert_mm2pixel(mwell_pitch_x) - label_pixels_x) / 2;*/
						 
						margin_x = convert_mm2pixel((2 * Border_left + 2 * i
								* mwell_pitch_x + mwell_pitch_x - (Label_cxChar * Integer.toString(i + 1).length())) / 2);
					else
						
	/*					margin_x = convert_mm2pixel(Border_left) +
						 convert_mm2pixel(i * mwell_pitch_x) + (label_pixels_x -
						 convert_mm2pixel(mwell_pitch_x)) / 2;*/
						 
						margin_x = convert_mm2pixel((2 * Border_left + 2 * i
								* mwell_pitch_x + (Label_cxChar * Integer.toString(i + 1).length()) - mwell_pitch_x) / 2);

					Canvas_Well_Plate.drawText(Integer.toString(i + 1), margin_x, margin_y, mPaint_text);
				}

	            for (i = 0, margin_x = 0, margin_y = 0; i < Y_holes; i++) {
					if (mwell_pitch_y > Label_cyChar)
						margin_y = convert_mm2pixel((2 * Border_top + 2 * i
								* mwell_pitch_y + mwell_pitch_y - Label_cyChar + 2 * Label_cyChar) / 2);
					else
						margin_y = convert_mm2pixel((2 * Border_top + 2 * i
								* mwell_pitch_y + Label_cyChar - mwell_pitch_y + 2 * Label_cyChar) / 2);
					Canvas_Well_Plate.drawText(Character.toString((char) (chr + i)), margin_x, margin_y, mPaint_text);
	            }
	            radius_pixels = (mwell_pitch_x > mwell_pitch_y) ? convert_mm2pixel((mwell_pitch_y-0.8)/2):convert_mm2pixel((mwell_pitch_x-0.8)/2);
	            //mPaint.setColor(Color.BLUE);
	            //mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	            for (i = 0; i < Y_holes; i++) {
	            	margin_y = convert_mm2pixel((2*Border_top+mwell_pitch_y+i*2*mwell_pitch_y)/2);
	            	for (j = 0, margin_x = convert_mm2pixel((2*Border_left+mwell_pitch_x)/2); j < X_holes; j++) {
	            		margin_x = convert_mm2pixel((2*Border_left+mwell_pitch_x+j*2*mwell_pitch_x)/2);
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
	            //Draw_Client_Well_Region(Canvas_Well_Plate, 8, 94);
	            //Draw_Client_Well_Region(Canvas_Well_Plate, 7, 94);
	            //mMaxTouchablePosY = margin_y + radius_pixels;
			}
		//this.invalidate();
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
		int i, j, margin_x, margin_y;
		int chr = 'A';
		int radius_pixels;
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
	
	public int convert_mm2pixel(double value) {
		DisplayMetrics metrics = new DisplayMetrics();
		((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		
		return (int) (0.75*value * metrics.xdpi * (1.0f/25.4f));
	}
	
	/*20130325 night added by michael*/
	//only invalidate the well with histogram change
	private void Invalidate_Single_Well(int color_index, int well_x, int well_y) {
		int i, j, margin_x, margin_y;
		int radius_pixels;
		double mClient_well_pitch_x, mClient_well_pitch_y, adjust_radius_mm;
		int Client_Border_left, Client_Border_top;

		//mPaint_well_Stroke.setStrokeWidth(convert_mm2pixel(1));
		mClient_well_pitch_x = (mwell_pitch_x > mwell_pitch_y) ? mwell_pitch_x : mwell_pitch_y;
		mClient_well_pitch_y = (mwell_pitch_x > mwell_pitch_y) ? mwell_pitch_x : mwell_pitch_y;
		//mClient_well_pitch_x = mClient_well_pitch_y - 0.11;
		//mClient_well_pitch_y = mClient_well_pitch_x - 0.09;
		if (mWells==Wells_96) {
			adjust_radius_mm = 6;
			//mClient_well_pitch_x = mClient_well_pitch_y - 0.12;
			//mClient_well_pitch_y = mClient_well_pitch_x - 0.1;
			mClient_well_pitch_x = mClient_well_pitch_y - 0.11;
			mClient_well_pitch_y = mClient_well_pitch_x - 0.09;
			Client_Border_left = 7;
			Client_Border_top = 93;
		}
		else {
			adjust_radius_mm = 1.3;
			//mClient_well_pitch_x = mClient_well_pitch_y - 0.1;
			//mClient_well_pitch_y = mClient_well_pitch_x - 0.08;
			mClient_well_pitch_x = mClient_well_pitch_y - 0.115;
			mClient_well_pitch_y = mClient_well_pitch_x - 0.09;
			Client_Border_left = 7;
			Client_Border_top = 94;
		}
			
		//if (mWells==Wells_96) {
			/*update the upper well*/
            radius_pixels = (mwell_pitch_x > mwell_pitch_y) ? convert_mm2pixel((mwell_pitch_y-0.8)/2):convert_mm2pixel((mwell_pitch_x-0.8)/2);
            margin_y = convert_mm2pixel((2*Border_top+mwell_pitch_y+well_y*2*mwell_pitch_y)/2);
            margin_x = convert_mm2pixel((2*Border_left+mwell_pitch_x+well_x*2*mwell_pitch_x)/2);
			if (color_index==0) {
				mPaint = mPaint_well_Fill;
				//mPaint.setColor(Color.BLACK);
				mPaint.setXfermode(Xfermode_clear);
				Canvas_Well_Plate.drawRect(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels, mPaint);
				mPaint.setXfermode(null);
				//mPaint.setColor(Color.WHITE);
				mPaint = mPaint_well_Stroke;
				mPaint.setColor(Color.WHITE);
				//mPaint.setXfermode(Xfermode_clear);
			} else {
				mPaint = mPaint_well_Fill;
				if (color_index <= total_colors) {
					mPaint.setColor(Mark_Color_Table[color_index - 1]);
				} else {
					mPaint.setColor(Mark_Color_Table[total_colors - 1]);
				}
				mPaint.setXfermode(Xfermode_dst_over);
			}
			Canvas_Well_Plate.drawCircle(margin_x, margin_y, radius_pixels, mPaint);
			mPaint.setXfermode(null);
			this.invalidate(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels);
			
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
					this.invalidate(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels);
				}
				else {
					mPaint = mPaint_well_Fill;
					mPaint.setXfermode(Xfermode_clear);
					Canvas_Well_Plate.drawRect(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels, mPaint);
					mPaint.setXfermode(null);
					this.invalidate(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels);
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
				this.invalidate(margin_x-radius_pixels, margin_y-radius_pixels, margin_x+radius_pixels, margin_y+radius_pixels);
			}
		/*}
		else if (mWells==Wells_384){
			
		}*/
		//mPaint_well_Stroke.setStrokeWidth(convert_mm2pixel(0.5));
	}

	public void setWellColor(int a[][]) {
		int i = 0, j = 0, temp;
		//synchronized (Well_Color_index) {
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
					//synchronized(lock1) {
					if (Well_Color_index[i][j] != a[i][Y_holes-1-j]) {
						Invalidate_Single_Well(a[i][Y_holes-1-j], i, j);
						Well_Color_index[i][j] = a[i][Y_holes-1-j];
					}
					//}
				}
			}
		//}
	}
	
	
	public void ResetWell() {
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
    	DrawBitmap();
    	//invalidate();
	}
	
	public void setWell(int well_type) {
		mWells = well_type;

		if (well_type == Wells_96) {
			mwell_pitch_x = 9.580d;
			mwell_pitch_y = 7.5d;
			X_holes = 12;
			Y_holes = 8;
			Label_cxChar = 5;
			Label_cyChar = 5;
			Border_left = Label_cxChar + 1;
			Border_top = Label_cyChar + 1;
		} else if (well_type == Wells_384) {
			mwell_pitch_x = 9.580d / 2;
			mwell_pitch_y = 7.5d / 2;
			X_holes = 24;
			Y_holes = 16;
			Label_cxChar = 3.3;
			Label_cyChar = 3;
			Border_left = Label_cxChar + 1;
			Border_top = Label_cyChar + 1;
		}
		mMaxTouchablePosY = convert_mm2pixel((2*Border_top+2*mwell_pitch_y+(Y_holes-1)*2*mwell_pitch_y-0.8)/2);
		//this.invalidate();
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
		if (Last_Coord_X_Count > 0 && Last_Coord_Y_Count > 0) {
			if (Show_focus_coord) {
				Invalidate_Single_Well(Well_Color_index[Last_Coord_X][Last_Coord_Y], Last_Coord_X, Last_Coord_Y);
				Show_focus_coord = false;
			}
			else {
				Invalidate_Single_Well(0, Last_Coord_X, Last_Coord_Y);
				Show_focus_coord = true;
			}
		}
	}
	
	/*20131208 added by michael*/
	public void decrese_SingleWellColor(int focus_valid_coord) {
		int Coord_X, Coord_Y, Coord_X_Count, Coord_Y_Count; 

		if (focus_valid_coord != -1) {
			Coord_X = (focus_valid_coord) & I_Tracker_Device.Coord_X_Mask;
			Coord_Y = (focus_valid_coord >>> I_Tracker_Device.Coord_Y_shift) & I_Tracker_Device.Coord_X_Mask;
			Coord_Y = Y_holes - 1 - Coord_Y;
			Coord_X_Count = (focus_valid_coord >>> I_Tracker_Device.Coord_X_Count_shift) & I_Tracker_Device.Coord_X_Count_Mask;
			Coord_Y_Count = (focus_valid_coord >>> I_Tracker_Device.Coord_Y_Count_shift) & I_Tracker_Device.Coord_X_Count_Mask;
			
			if (Coord_X >= 0 && Coord_Y >= 0) {
				//synchronized(lock1) {
					Well_Color_index[Coord_X][Coord_Y]--;
					Invalidate_Single_Well(Well_Color_index[Coord_X][Coord_Y], Coord_X, Coord_Y);
				//}
			}
		}
		else {
			Coord_X_Count = -1;
			Coord_Y_Count = -1;
		}
	}
	
	public void increase_SingleWellColor(int focus_valid_coord) {
		int Coord_X, Coord_Y, Coord_X_Count, Coord_Y_Count; 

		if (focus_valid_coord != -1) {
			Coord_X = (focus_valid_coord) & I_Tracker_Device.Coord_X_Mask;
			Coord_Y = (focus_valid_coord >>> I_Tracker_Device.Coord_Y_shift) & I_Tracker_Device.Coord_X_Mask;
			Coord_Y = Y_holes - 1 - Coord_Y;
			Coord_X_Count = (focus_valid_coord >>> I_Tracker_Device.Coord_X_Count_shift) & I_Tracker_Device.Coord_X_Count_Mask;
			Coord_Y_Count = (focus_valid_coord >>> I_Tracker_Device.Coord_Y_Count_shift) & I_Tracker_Device.Coord_X_Count_Mask;
			
			if (Coord_X >= 0 && Coord_Y >= 0) {
				//synchronized(lock1) {
					Well_Color_index[Coord_X][Coord_Y]++;
					Invalidate_Single_Well(Well_Color_index[Coord_X][Coord_Y], Coord_X, Coord_Y);
				//}
			}
		}
		else {
			Coord_X_Count = -1;
			Coord_Y_Count = -1;
		}
	}
}
