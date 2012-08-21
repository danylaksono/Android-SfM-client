package cvg.sfmPipeline.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class TCPread extends AsyncTask<Void, String, Void>{
	
	private String 	IPadd;
	private int	 	port;
	private Context UIcontext;
	
	public TCPread(Context ui, String IPhost, int comPort){
		IPadd = IPhost;
		port  = comPort;
		UIcontext = ui;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		Socket socket = null;
		DataInputStream dataInputStream = null;
		try {
			socket = new Socket(IPadd, port);
			publishProgress("connection");
			String line = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while(!isCancelled()){
				line = in.readLine();
				if(line != null)
					publishProgress(line);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// the server is probably not available yet
			publishProgress("host_not_found_123456");
			e.printStackTrace();
		}
		finally{
			if (socket != null){
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (dataInputStream != null){
				try {
					dataInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		// incoming commands (strings) from TCP are handled here.
		if(values[0].startsWith("snap")){
			PipelineActivity.grabFrame.performClick();
			PipelineActivity.console.append("\nremote grab frame command:");
		}
		
		if(values[0].contentEquals("q")){
			cancel(true);
			Toast.makeText(UIcontext,
	                "Connection terminated.",
	                Toast.LENGTH_LONG).show();
		}
		
		if(values[0].contentEquals("host_not_found_123456")){
			Toast.makeText(UIcontext,
	                "Connection failed. TCP server at " + IPadd + " : " + port +" is not found.",
	                Toast.LENGTH_LONG).show();
		}
		
		if(values[0].startsWith("connection"))
			PipelineActivity.console.append("\nTCP connected to " + IPadd + ":" + port);
		
		super.onProgressUpdate(values);
	}

}