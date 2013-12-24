// Created by JeffMeJones@gmail.com
package gif.decoder;


import com.example.demo.I_Tacker_Activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;


public class GifRun implements Runnable, Callback {

	
	public Bitmap bmp, bmp1;
	public GIFDecode decode;
	public int ind;
	public int gifCount;
	public SurfaceHolder mSurfaceHolder ;
	boolean surfaceExists;
	private SurfaceView mSurfaceView;
	I_Tacker_Activity iTracker_activity;
	int Visibility = 1; 
	Thread t;
	boolean triger_Update = false;
	int background_color;
	
	public void LoadGiff(SurfaceView v, android.content.Context theTHIS, int R_drawable)
	{		
		//InputStream Raw= context.getResources().openRawResource(R.drawable.image001);
	       mSurfaceHolder = v.getHolder();
	       mSurfaceHolder.addCallback(this);
	       decode = new GIFDecode();
	       decode.read(theTHIS.getResources().openRawResource(R_drawable));
	       ind = 0;
			// decode.
			gifCount = decode.getFrameCount();
			bmp1 = bmp = decode.getFrame(0);
			surfaceExists=true;
			t = new Thread(this);
			//t.start();
			iTracker_activity = (I_Tacker_Activity) theTHIS;
			mSurfaceView = v;
			
			TypedValue a = new TypedValue();
			theTHIS.getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
			if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
			    // windowBackground is a color
				background_color = a.data;
			} else {
			    // windowBackground is not a color, probably a drawable
			    //Drawable d = activity.getResources().getDrawable(a.resourceId);
				background_color = theTHIS.getResources().getColor(android.R.color.background_dark);
			}
	}

	@SuppressWarnings("static-access")
	public void run() 
	{ 
		Canvas rCanvas;
		Paint paint, paint_clear;
		paint = new Paint();
		paint_clear = new Paint();
		paint_clear.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		//Log.d("Status indicator", "thread priority: " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
		while (surfaceExists) {
			rCanvas = null;
			try {
				//if (triger_Update) {
				if ((iTracker_activity.mItrackerState & (1 << iTracker_activity.Itracker_State_isConnect)) == 0) {
					rCanvas = mSurfaceHolder.lockCanvas();
                    if (rCanvas != null) {
                      rCanvas.drawColor(background_color);
                      rCanvas.drawBitmap(bmp, 0, 0, paint_clear);
					  //mSurfaceHolder.unlockCanvasAndPost(rCanvas);
                    }
                    break;
					/*if (Visibility==1) {
					  mSurfaceView.setVisibility(View.INVISIBLE);
					  mSurfaceHolder = mSurfaceView.getHolder();
					  Visibility = 0; 
					}*/
				}
				else {
					/*if (Visibility == 0) {
						mSurfaceView.setVisibility(View.VISIBLE);
						mSurfaceHolder = mSurfaceView.getHolder();
						Visibility = 1;
					}*/
					if ((iTracker_activity.mItrackerState & (1 << iTracker_activity.Itracker_State_isRunning)) == 1) {
						rCanvas = mSurfaceHolder.lockCanvas();
						rCanvas.drawBitmap(bmp1, 0, 0, paint);
						// ImageView im = (ImageView)
						// findViewById(R.id.imageView1);
						// im.setImageBitmap(bmb);
                        //if (rCanvas != null)
						  //mSurfaceHolder.unlockCanvasAndPost(rCanvas);
						bmp1 = decode.next();
						Thread.sleep(600);
						//triger_Update = true;
					} else {
						//bmb = decode.getFrame(0);
						rCanvas = mSurfaceHolder.lockCanvas();
						rCanvas.drawBitmap(bmp, 0, 0, paint);
						//if (rCanvas != null)
						  //mSurfaceHolder.unlockCanvasAndPost(rCanvas);
						break;
						//triger_Update = false;
					}
				}
				//}
				//Thread.sleep(600);
			} catch (Exception ex) {

			}
			finally { 
				/*20131217 added by michael*/
				if (rCanvas != null)
					  mSurfaceHolder.unlockCanvasAndPost(rCanvas);
			}
		}
		
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) 
	{
		
		
		
	}

	public void surfaceCreated(SurfaceHolder holder) 
	{
		if (t.getState()==Thread.State.TERMINATED) {
			t = new Thread(this);
			/*20131224 added by michael
			 * before starting a thread¡Ait would be better to set the adaptive worker thread priority less than main thread¡A such as Process.THREAD_PRIORITY_BACKGROUND */
			t.setPriority(Thread.currentThread().getPriority() - 1);
			t.start();
		}
		else
			if (t.getState()==Thread.State.NEW) {
				/*20131224 added by michael
				 * before starting a thread¡Ait would be better to set the adaptive worker thread priority less than main thread¡A such as Process.THREAD_PRIORITY_BACKGROUND */
				t.setPriority(Thread.currentThread().getPriority() - 1);
				t.start();
			}
		surfaceExists=true;
		Log.d("surfaceview create", t.getState().toString());
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		
		surfaceExists=false;
		triger_Update = false;
		Log.d("surfaceview destroy", t.getState().toString());
        boolean retry = true;
        while (retry) {
            try {
                t.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
	}
	
	public void Resume_thread() {
		/*Log.d("Thread status", t.getState().toString());*/
		if (t.getState()==Thread.State.TERMINATED) {
			t = new Thread(this);
			/*20131224 added by michael
			 * before starting a thread¡Ait would be better to set the adaptive worker thread priority less than main thread¡A such as Process.THREAD_PRIORITY_BACKGROUND */
			t.setPriority(Thread.currentThread().getPriority() - 1);
			t.start();
		}
		else
			if (t.getState()==Thread.State.NEW) {
				/*20131224 added by michael
				 * before starting a thread¡Ait would be better to set the adaptive worker thread priority less than main thread¡A such as Process.THREAD_PRIORITY_BACKGROUND */
				t.setPriority(Thread.currentThread().getPriority() - 1);
				t.start();
			}
		surfaceExists=true;
		/*if (surfaceExists) {
			triger_Update = true;
		}*/
	}
}
