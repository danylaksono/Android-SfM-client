package cvg.sfmPipeline.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class PipelineActivity extends Activity {
	
	private String 			TAG 		 = "PipelineActivity";
	private boolean 		isCalibrated = false;
	private FrameProcess 	mView;
	private PipelineCom 	sfmComm;
	public static TextView	console;
	private String			serverIP 	= "192.168.0.30";
	private int				UDPport 	= 60000;  
	public static Button	grabFrame;
	private ToggleButton	start;
	private ScrollView 		scroller;
	private MenuItem		frameOptions;
	
	private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(this) {
    	@Override
    	public void onManagerConnected(int status) {
    		switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					
					setContentView(R.layout.main);
					
					Log.i(TAG, "Native Libraries loaded successfully");
					
					mView = (FrameProcess) findViewById(R.id.camPreview);
					console = (TextView) findViewById(R.id.console);
					grabFrame = (Button) findViewById(R.id.grabFrame);
					grabFrame.setEnabled(false);
					start = (ToggleButton) findViewById(R.id.startSFM);
					
					start.setOnClickListener(clickStart);
					grabFrame.setOnClickListener(grabClick);
					scroller = (ScrollView) PipelineActivity.this.findViewById(R.id.scrollView1);
					if( !mView.openCamera() ) {
						AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
						ad.setCancelable(false);
						ad.setMessage("Fatal error: can't open camera!");
						ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						    }
						});
						ad.show();
					}
				} break;
				default:
				{
					super.onManagerConnected(status);
				} break;
			}
    	}
    };
	
    private View.OnClickListener grabClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			frameOptions.setEnabled(false); // once started, options cannot be changed
			grabFrame.setEnabled(false);
			new PipelineProcess().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			scroller.post(new Runnable() {
				@Override
				public void run() {
					scroller.smoothScrollBy(0, console.getBottom());
				}
			});
		}
	};
   
    private View.OnClickListener clickStart = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(start.isChecked()){
				if(isCalibrated){
					// start the communication processes
					console.append("starting wireless communications");
					sfmComm = new PipelineCom(PipelineActivity.this.getApplicationContext(), serverIP, UDPport);
					startService(new Intent(PipelineActivity.this, SensorService.class));
					console.append("\nsensor service started: gravity sensor");
					grabFrame.setEnabled(true);
				}
				else{
					Toast.makeText(getApplicationContext(),
			                "Calibration data not found. Perform application first!",
			                Toast.LENGTH_SHORT).show();
					start.setChecked(false);
				}
			}else{
				// stop communication processes (if any)
				console.append("\nstopped wireless communications");
				sfmComm.closeTCP();
				stopService(new Intent(PipelineActivity.this, SensorService.class));
				console.append("\nsensor service stopped");
				grabFrame.setEnabled(false);
			}
			scroller.post(new Runnable() {
				@Override
				public void run() {
					scroller.smoothScrollBy(0, console.getBottom());
				}
			});
				
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Trying to load OpenCV library");
        PipelineSharedData.setLocalIpAddress((WifiManager)getSystemService(WIFI_SERVICE));
        PipelineSharedData.noFrames = 0l;
        
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        	Log.e(TAG, "Cannot connect to OpenCV Manager");
        
    }
    

    @Override
	protected void onResume() {
    	if(PipelineSharedData.calibDone){
    		isCalibrated = loadCalib();
    		PipelineSharedData.calibDone = false; 
    	}
		super.onResume();
		if( mView != null && !mView.openCamera() ) {
			AlertDialog ad = new AlertDialog.Builder(this).create();  
			ad.setCancelable(false); // This blocks the 'BACK' button  
			ad.setMessage("Fatal error: can't open camera!");  
			ad.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {  
			    public void onClick(DialogInterface dialog, int which) {  
			        dialog.dismiss();                      
					finish();
			    }  
			});  
			ad.show();
		}
	}
    
	@Override
	protected void onDestroy() {
		String mess = "EXIT";
		new UDPsend().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mess.getBytes());
		stopService(new Intent(PipelineActivity.this, SensorService.class));
		super.onDestroy();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        frameOptions = menu.findItem(R.id.frameProcesOpts);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case R.id.loadCalib:
			isCalibrated = loadCalib();
			break;
		case R.id.doCalib:{
			Intent intent = new Intent(Intent.ACTION_MAIN);
			PackageManager manager = PipelineActivity.this.getPackageManager();
			intent = manager.getLaunchIntentForPackage("cvg.sfmPipeline.calibration");
			if(intent == null)
				showToast("Please install Calibration app first!", false);
			else{
				intent.addCategory("android.intent.category.LAUNCHER");
				startActivity(intent);
				PipelineSharedData.calibDone = true;
			}
			break;
		}
		case R.id.viewRecon:
			showToast("Not yet implemented!", true);
			break;
			
		case R.id.sendJPEG:
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			PipelineSharedData.sendJPEG = item.isChecked();
			break;
		case R.id.doMatching:
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			PipelineSharedData.doMatching = item.isChecked();
			break;
		case R.id.doSURF:
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			PipelineSharedData.doSURF = item.isChecked();
			break;
			
		case R.id.camInfo:
			if(isCalibrated)
				showParams();
			else
				showToast("Calibration data not found. Perform application first!", true);
			break;
		case R.id.sfmOpts:
			break;
		}
		return true;
	}
    
	private void showParams() {
		View newlay = getLayoutInflater().inflate(R.layout.viewmats, null);
		List<TextView> params = new ArrayList<TextView>();
		List<TextView> params2 = new ArrayList<TextView>();
		params.add((TextView) newlay.findViewById(R.id.matrix_1_1));
		params.add((TextView) newlay.findViewById(R.id.matrix_1_2)); 
		params.add((TextView) newlay.findViewById(R.id.matrix_1_3));
		params.add((TextView) newlay.findViewById(R.id.matrix_2_1));
		params.add((TextView) newlay.findViewById(R.id.matrix_2_2));
		params.add((TextView) newlay.findViewById(R.id.matrix_2_3));
		params.add((TextView) newlay.findViewById(R.id.matrix_3_1));
		params.add((TextView) newlay.findViewById(R.id.matrix_3_2));
		params.add((TextView) newlay.findViewById(R.id.matrix_3_3));
		
		params2.add((TextView) newlay.findViewById(R.id.imatrix_1_1));
		params2.add((TextView) newlay.findViewById(R.id.imatrix_1_2));
		params2.add((TextView) newlay.findViewById(R.id.imatrix_1_3));
		params2.add((TextView) newlay.findViewById(R.id.imatrix_2_1));
		params2.add((TextView) newlay.findViewById(R.id.imatrix_2_2));
		params2.add((TextView) newlay.findViewById(R.id.imatrix_2_3));
		params2.add((TextView) newlay.findViewById(R.id.imatrix_3_1));
		params2.add((TextView) newlay.findViewById(R.id.imatrix_3_2));
		params2.add((TextView) newlay.findViewById(R.id.imatrix_3_3));
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(newlay);
		builder.create().show();
		int i = 0;
		for (TextView textView : params) {
			textView.setText(numberDisplayFormatter(PipelineSharedData.getCamera2body()[i]));
			i++;
		}
		int j = 0;
		for (TextView textView : params2) {
			textView.setText(numberDisplayFormatter(PipelineSharedData.getCameraMatrix()[j]));
			j++;
		}
		
	}

	private static String numberDisplayFormatter(float value) {
        String displayedText = String.format("%.3f", value);
        if (value >= 0) {
            displayedText = " " + displayedText;
        }
        if (displayedText.length() > 6) {
            displayedText = displayedText.substring(0, 6);
        }
        while (displayedText.length() < 6) {
            displayedText = displayedText + " ";
        }
        return displayedText;
    }

	private boolean loadCalib(){
		String path = Environment.getExternalStorageDirectory().getPath()
        		+ "/CalibrationData/";
		File fileK = new File(path + "camMatrix.dat");
		File fileC2B = new File(path + "rotCam2imu.dat");
		float[] K = {0,0,0,0,0,0,0,0,0};
		float[] C2B = {0,0,0,0,0,0,0,0,0};
		if (fileK.exists() && fileC2B.exists()){
			try {
				FileInputStream fisK = new FileInputStream(fileK);
				FileInputStream fisC2B = new FileInputStream(fileC2B);
				
				ObjectInputStream oiiK 	 = new ObjectInputStream(fisK);
				ObjectInputStream oiiC2B = new ObjectInputStream(fisC2B);
				for(int i = 0; i < 9; i ++)
					K[i] = (float)oiiK.readDouble();
				for(int i = 0; i < 9; i ++)
					C2B[i] = (float)oiiC2B.readDouble();
				fisK.close();
				fisC2B.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			PipelineSharedData.setCameraMatrix(K);
			PipelineSharedData.setCamera2body(C2B);
			showToast("Calibration data successfully loaded!", true);
			return true;
		}
		else{
			showToast("Calibration data not found. Perform application first!", false);
			return false;
		}
	}
	
	private void showToast(String message, boolean isShort){
		int len = Toast.LENGTH_LONG;
		if (isShort)
			len = Toast.LENGTH_SHORT;
		
		Toast.makeText(getApplicationContext(), message, len).show();
	}

	@Override
	public void onBackPressed() {
		if (!start.isChecked())
			super.onBackPressed();
	}
	
	
}
