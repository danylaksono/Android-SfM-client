package cvg.sfmPipeline.main;

import android.content.Context;
import android.os.AsyncTask;

/**
 * @author Federico Camposeco
 * Class to seamlessly initialize the communication process for
 * the application. UDP will be transmitted containing sensor
 * data, SURF/SIFT features from the latest frame, and optiona-
 * lly the image itself. 
 */
public class PipelineCom {
	
	private TCPread tcpRead;
	
	public PipelineCom(Context ui, String serverIP, int comPort){
		tcpRead = new TCPread(ui, serverIP, comPort);

		// start the TCP thread
		tcpRead.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	public void closeTCP(){
		tcpRead.cancel(true);
	}
	
}
