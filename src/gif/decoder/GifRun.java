// Created by JeffMeJones@gmail.com
package gif.decoder;


import com.example.demo.I_Tacker_Activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;


public class GifRun implements Runnable, Callback {

	
	public Bitmap bmb;
	public GIFDecode decode;
	public int ind;
	public int gifCount;
	public SurfaceHolder mSurfaceHolder ;
	boolean surfaceExists;
	private SurfaceView mSurfaceView;
	I_Tacker_Activity iTracker_activity;
	int Visibility = 1; 
	
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
			bmb = decode.getFrame(0);
			surfaceExists=true;
			Thread t = new Thread(this);
			t.start();
			iTracker_activity = (I_Tacker_Activity) theTHIS;
			mSurfaceView = v;
	}

	@SuppressWarnings("static-access")
	public void run() 
	{ 
		while (surfaceExists) {
			try {
				if ((iTracker_activity.mItrackerState & (1 << iTracker_activity.Itracker_State_isConnect)) == 0) {
					if (Visibility==1) {
					  mSurfaceView.setVisibility(View.INVISIBLE);
					  Visibility = 0; 
					}
				}
				else {
					if (Visibility == 0) {
						mSurfaceView.setVisibility(View.VISIBLE);
						Visibility = 1;
					}
					if ((iTracker_activity.mItrackerState & (1 << iTracker_activity.Itracker_State_isRunning)) == 1) {
						Canvas rCanvas = mSurfaceHolder.lockCanvas();
						rCanvas.drawBitmap(bmb, 0, 0, new Paint());
						// ImageView im = (ImageView)
						// findViewById(R.id.imageView1);
						// im.setImageBitmap(bmb);

						mSurfaceHolder.unlockCanvasAndPost(rCanvas);
						bmb = decode.next();
					} else {
						bmb = decode.getFrame(0);
						Canvas rCanvas = mSurfaceHolder.lockCanvas();
						rCanvas.drawBitmap(bmb, 0, 0, new Paint());
						mSurfaceHolder.unlockCanvasAndPost(rCanvas);
					}
				}
				Thread.sleep(100);
			} catch (Exception ex) {

			}
		}
		
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) 
	{
		
		
		
	}

	public void surfaceCreated(SurfaceHolder holder) 
	{
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		
		surfaceExists=false;
	}
	
}
