package cvg.sfmPipeline.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;

import android.os.AsyncTask;
import android.util.Log;
/**
 * This version of the app will not log the VICON data, instead the incoming data from the UDP 
 * will be used to control the app remotely.
**/
public class UDPthread extends AsyncTask<Void, String, Void> {

	//--   Constants
	private static final int MAX_UDP_DATAGRAM_LEN = 1024;
	private static final int UDP_SERVER_PORT = 50000; 
	private static final String TAG = "ProtoLog::UDPthread";
	private static final String IP = "192.168.0.255";
	
	//--   Members
	private DatagramSocket socket;
	private float[] lastValue = new float[]{0.f,0.f,0.f,0.f,0.f,0.f};

	
    @Override
    protected Void doInBackground(Void... a) {
    	try {
		    DatagramChannel channel = DatagramChannel.open();
    		InetAddress thisAddress = InetAddress.getByName(IP);
    		socket = channel.socket();
		    socket.setReuseAddress(true);
		    SocketAddress add = new InetSocketAddress(thisAddress, UDP_SERVER_PORT);
		    socket.bind(add);
		    byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
		    DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
		    while(!isCancelled()){
				socket.receive(dp);
		    	publishProgress(new String(lMsg, 0, dp.getLength()));
		    }
	    	
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    @Override
    protected void onProgressUpdate(String... message){
    	String[] tokens = message[0].split(", ");
    	int i = 0;
    	for (String s : tokens) {
    		lastValue[i] = Float.parseFloat(s);
			i++;
		}
    	PipelineSharedData.setLastGroundTr(lastValue);
    }
    @Override
    protected void onCancelled(){
    	if (socket != null){
    		socket.close();
    	}
    }
    
// TODO: use this instead of hardcoding the IP, port SHOULD be hardcoded
    private InetAddress getBroadcastAddress(){
    	Enumeration<NetworkInterface> interfaces;
    	try {
    		interfaces = NetworkInterface.getNetworkInterfaces();
    		while (interfaces.hasMoreElements()) {
    			NetworkInterface networkInterface = interfaces.nextElement();
    			if (networkInterface.isLoopback())
    				continue;
    			for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
    				InetAddress broadcast = interfaceAddress.getBroadcast();
    				if (broadcast == null)
    					continue;
    				return broadcast;
    			}
    		}
    	} catch (SocketException e1) {
    		e1.printStackTrace();
    	}
    	return null;
    }
    
}