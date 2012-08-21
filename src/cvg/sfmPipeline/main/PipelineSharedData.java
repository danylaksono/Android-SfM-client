package cvg.sfmPipeline.main;


import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;
import cvg.sfmPipeline.main.PipelineOutMessage.Keypoints;
import cvg.sfmPipeline.main.PipelineOutMessage.cvMatProto;
import cvg.sfmPipeline.main.PipelineOutMessage.MetadataProto.SensorType;
/**
 * Shared data for the pipeline bundled into a static class. 
 * Contains various fields relevant to the reconstruction.
 * */
public final class PipelineSharedData {

	//--   Data Fields ---------------------------------------------------------------
    static private float[]		lastSensor 		 = {0,0,0}; 
    static private float[] 		lastGroundTr	 = {0,0,0,0,0,0};
    static private  byte[] 		image;	
    static private long 		lastImage;
	static private float[] 		cameraMatrix 	= {0,0,0,0,0,0,0,0,0};
    static private float[] 		camera2body 	= {0,0,0,0,0,0,0,0,0};
    static private SensorType 	sensorType 		= SensorType.GRAVITY;
    static public 	long		noFrames 		= 0l;
    static private long			timestamp;
    static private int 			camera 			= 0;
    static private String       ipAddress 		= "0";
    static public volatile boolean       		ready2send 		= true;
    static public boolean       ready2snap 		= true;
    static public boolean       calibDone 		= false;
    static public boolean 		doSURF  		= true;
    static public boolean 		doMatching  	= false;
    static public boolean 		sendJPEG  		= false;
    static public Frame 		firstFrame;
    static public Frame 		secondFrame;
    static public byte[] 		matchesProtoMessage;
    
    public static void addFrame(Frame newFrame){
    	if (noFrames > 1){
    		firstFrame = secondFrame;
    		secondFrame = newFrame;
    		return;
    	}
    	if (noFrames == 1){
    		secondFrame = newFrame;
    		return;
    	}
    	
    	if (noFrames == 0){
    		firstFrame = newFrame;
    		return;
    	}
    }
    
    public static void setDefaults() {
    	camera = 0;
    	ipAddress = "0";
    	ready2send = true;
    	ready2snap = true;
    	noFrames = 0l;
	}
    
    private PipelineSharedData(){}
    
    public static int getCam(){
    	return camera;
    }
    
    public static void setCam(int c){
    	camera = c;
    }
    
    public static String getLocalIpAddress() {
    	return ipAddress;
    }
    
    @SuppressWarnings("deprecation")
    public static void setLocalIpAddress(WifiManager wim){
    	ipAddress = Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress());
    }
    
    
    public static long getLastImage() {
		return lastImage;
	}

	public static void setLastImage(long lastImage) {
		PipelineSharedData.lastImage = lastImage;
	}
    
    public static float[] getLastGroundTr() {
		return lastGroundTr;
	}

	public static void setLastGroundTr(float[] lastGroundTr) {
		PipelineSharedData.lastGroundTr = lastGroundTr;
	}
    
	public static float[] getCameraMatrix() {
		return cameraMatrix;
	}

	public static void setCameraMatrix(float[] cameraMatrix) {
		PipelineSharedData.cameraMatrix = cameraMatrix;
	}

	public static float[] getCamera2body() {
		return camera2body;
	}

	public static void setCamera2body(float[] camera2body) {
		PipelineSharedData.camera2body = camera2body;
	}

	public static SensorType getSensorType() {
		return sensorType;
	}

	public static void setSensorType(SensorType sensorType) {
		PipelineSharedData.sensorType = sensorType;
	}

	public static float[] getLastSensor() {
		return lastSensor;
	}

	public static void setLastSensor(float[] lastSensor) {
		PipelineSharedData.lastSensor = lastSensor;
	}


	public static byte[] getImage() {
		return image;
	}

	public static void setImage(byte[] image) {
		PipelineSharedData.image = image;
	}

	public static long getTimestamp() {
		return timestamp;
	}

	public static void setTimestamp(long timestamp) {
		PipelineSharedData.timestamp = timestamp;
	}

	

	
    
}