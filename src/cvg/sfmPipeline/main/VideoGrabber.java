package cvg.sfmPipeline.main;

import java.util.List;

import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.Highgui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public abstract class VideoGrabber extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "sfmpipe::SurfaceView";

    private SurfaceHolder       mHolder;
    private VideoCapture        mCamera;
    private FpsMeter            mFps;

    public VideoGrabber(Context context, AttributeSet attrs) {
        super(context, attrs); 
        mHolder = getHolder();
        mHolder.addCallback(this);
        mFps = new FpsMeter();
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public boolean openCamera() {
        Log.i(TAG, "openCamera");
        synchronized (this) {
	        releaseCamera();
	        mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);//use Highgui.CV_CAP_ANDROID+1 for front camera
	        if (!mCamera.isOpened()) {
	            mCamera.release();
	            mCamera = null;
	            Log.e(TAG, "Failed to open native camera");
	            return false;
	        }
	    }
        return true;
    }
    
    public void releaseCamera() {
        Log.i(TAG, "releaseCamera");
        synchronized (this) {
	        if (mCamera != null) {
	                mCamera.release();
	                mCamera = null;
            }
        }
    }
    
    public void setupCamera(int width, int height) {
        Log.i(TAG, "setupCamera("+width+", "+height+")");
        synchronized (this) {
            if (mCamera != null && mCamera.isOpened()) {
                List<Size> sizes = mCamera.getSupportedPreviewSizes();
                int mFrameWidth = width;
                int mFrameHeight = height;

                // selecting best camera size to match width x height
                {
                    double minDiff = Double.MAX_VALUE;
                    for (Size size : sizes) {
                        if (Math.abs(size.height - height) < minDiff) {
                            mFrameWidth = (int) size.width;
                            mFrameHeight = (int) size.height;
                            minDiff = Math.abs(size.height - height);
                        }
                    }
                }

                mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mFrameWidth);
                mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mFrameHeight);
//                mCamera.set(Highgui.CV_CAP_ANDROID_FOCUS_MODE_INFINITY, 1);
            }
        }

    }
    
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
        // two options, try to fill the surface holder or 
        // use the standard resolution for the pipeline (the one
        // used for calibration) 640x480.
        setupCamera(640, 480);
        changeSurfaceSize(640, 480);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        (new Thread(this)).start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        releaseCamera();
    }
    
    private SurfaceView changeSurfaceSize(int width, int height){
    	SurfaceView trueview = (SurfaceView)((View)getParent()).findViewById(R.id.camPreview);
		ViewGroup.LayoutParams params = trueview.getLayoutParams();
		params.height = height;
		params.width = width;
		trueview.setLayoutParams(params);
		return trueview;
    }
    
    /**
     * Main method to process the data aquired by the opencv video 
     * capture objects. This process frame method should call the
     * appropriate native (or java) functions for the pipeline.
     * */
    protected abstract Bitmap processFrame(VideoCapture capture);

    public void run() {
        Log.i(TAG, "Starting processing thread");
        mFps.init();
        while (true) {
            Bitmap bmp = null;
            synchronized (this) {
                if (mCamera == null)
                    break;

                if (!mCamera.grab()) {
                    Log.e(TAG, "mCamera.grab() failed");
                    break;
                }
                bmp = processFrame(mCamera);
                mFps.measure();
            }

            if (bmp != null) {
                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(bmp, (canvas.getWidth() - bmp.getWidth()) / 2, (canvas.getHeight() - bmp.getHeight()) / 2, null);
                    mFps.draw(canvas, (canvas.getWidth() - bmp.getWidth()) / 2, (canvas.getHeight() - bmp.getHeight()) / 2);
                    mHolder.unlockCanvasAndPost(canvas);
                }
                bmp.recycle();
            }
        }

        Log.i(TAG, "Finishing processing thread");
    }
}