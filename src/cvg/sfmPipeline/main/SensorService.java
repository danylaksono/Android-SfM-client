package cvg.sfmPipeline.main;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import cvg.sfmPipeline.main.PipelineOutMessage.MetadataProto.SensorType;

public class SensorService extends Service implements SensorEventListener{
	
	//-- Members
	private SensorManager 	manager;
	private Sensor			sensor;
	private int				sensorType;
	
	
	//-- Service
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		manager.unregisterListener(this);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		sensorType = Sensor.TYPE_GRAVITY; // FIXME: do not hardcode
		PipelineSharedData.setSensorType(SensorType.GRAVITY);
		manager = (SensorManager)getSystemService(SENSOR_SERVICE);
		sensor  = manager.getDefaultSensor(sensorType);
		manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
		TimeStamp.restart();
		return super.onStartCommand(intent, flags, startId);
	}

	
	//-- SensorEvenListener
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
        // avoid google's implementations of the value estimation
        // to get only the iNemo values.
        if (!sensor.getVendor().equalsIgnoreCase("Google Inc.")){
        	PipelineSharedData.setLastSensor(event.values);
        	TimeStamp.updateTime(event.timestamp);
        }
	}
	
	private static class TimeStamp {
		private static  long timestamp;
		private static long startime;
		private static boolean started = false;
		
		private TimeStamp(){}
		
		/**
		 * Update the timestamp field using the latest sensor timestamp acquired. 
		 * */
		public static void updateTime(long t){
			if (started){
				timestamp = t - startime;
				PipelineSharedData.setTimestamp(timestamp/1000000); // return milliseconds
			}else{
				startime = t;
				timestamp = 0;
				started = true;
			}
		}
		
		public static void restart(){
			started = false;
		}
	}

}
