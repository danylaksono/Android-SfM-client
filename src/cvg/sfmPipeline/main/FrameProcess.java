package cvg.sfmPipeline.main;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

public class FrameProcess extends VideoGrabber {
	private final String TAG = "FrameProcess";
    private Mat mGray;
	
	public FrameProcess(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        synchronized (this) {
            mGray = new Mat();
        }
        super.surfaceCreated(holder);
	}

	@Override
	protected Bitmap processFrame(VideoCapture capture) {
		capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
		
		Bitmap bmp = Bitmap.createBitmap(mGray.cols(), mGray.rows(), Bitmap.Config.ARGB_8888);
		
		// store the pointer to the latest image
		PipelineSharedData.setLastImage(mGray.getNativeObjAddr());
		
        try {
        	Utils.matToBitmap(mGray, bmp);
        } catch(Exception e) {
        	Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
            bmp.recycle();
            bmp = null;
        }
        return bmp;
	}
	
	@Override
    public void run() {
        super.run();

        synchronized (this) {
            if (mGray != null)
                mGray.release();
            mGray = null;
        }
    }

}
