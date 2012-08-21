package cvg.sfmPipeline.main;

import android.util.Log;

public class Timer {

	private static long start;
	
	private Timer(){}
	
	public static void init(){
		start = System.nanoTime();
	}
	
	public static void toc(String funcName){// millis
		int elapsed = (int)(System.nanoTime() - start)/1000000;
		Log.e("Timer", elapsed + " ms in " + funcName);
		start = System.nanoTime();
	}
}
