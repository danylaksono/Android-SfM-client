package cvg.sfmPipeline.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import android.os.AsyncTask;
import android.util.Log;

public class UDPsend extends AsyncTask<byte[], Void, Void> {

	//--   Constants
	private static final int UDP_SERVER_PORT = 50050; 
	private String TAG = "UDPsend::ProtoLog";
	private static final String IPnet = PipelineSharedData.getLocalIpAddress(); 
	
	//--   Members
	private DatagramSocket socket;
	public boolean send = false;
	private String IP;
	private int messlen = 0;
	
	public UDPsend(){
		// obtain broadcast IP from net IP
		String[] splitted = IPnet.split("\\.");
		IP = splitted[0]+"."+splitted[1]+"."+splitted[2]+".255";
		Log.i(TAG, "UDP broadcast at: "+IP);
	}

	@Override
	protected Void doInBackground(byte[]... messageA) {
		while(!PipelineSharedData.ready2send){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(PipelineSharedData.ready2send){
			PipelineSharedData.ready2send = false;
			try {
				byte[] message = messageA[0];
				InetAddress thisAddress = InetAddress.getByName(IP);
				socket = new DatagramSocket();				
				messlen = message.length;
				// split the message (if necessary) and send through UDP
				NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(IPnet));
				int chunkNo = (int) Math.ceil((double)messlen / (double)ni.getMTU());
				String headerStr = "ProtobuffMessage_"+chunkNo;
				DatagramPacket header = new DatagramPacket(headerStr.getBytes(), headerStr.length(), 
						thisAddress, UDP_SERVER_PORT);
				socket.send(header);
				for (int i = 0; i < chunkNo; i++){
					int start = Math.min(messlen,i * ni.getMTU());
					int end = Math.min(messlen, (i+1)*ni.getMTU());
					byte[] thisChunk = Arrays.copyOfRange(message, start, end);
					DatagramPacket dp = new DatagramPacket(thisChunk, thisChunk.length, 
							thisAddress, UDP_SERVER_PORT);
					socket.send(dp);
				}
				Checksum check = new CRC32();
				check.update(message, 0, messlen);
				long CHKSUM = check.getValue();
				DatagramPacket checkPack = new DatagramPacket(String.valueOf(CHKSUM).getBytes(), 
						String.valueOf(CHKSUM).getBytes().length, thisAddress, UDP_SERVER_PORT);
				socket.send(checkPack);
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

    @Override
	protected void onPostExecute(Void result) {
    	PipelineSharedData.ready2send = true;
    	PipelineActivity.console.append("\n"+messlen+" bytes sent");
    	PipelineActivity.grabFrame.setEnabled(true);
		super.onPostExecute(result);
	}



}