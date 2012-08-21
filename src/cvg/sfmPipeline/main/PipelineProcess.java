package cvg.sfmPipeline.main;

import com.google.protobuf.InvalidProtocolBufferException;

import cvg.sfmPipeline.main.PipelineOutMessage.FrameProto;
import cvg.sfmPipeline.main.MatchesOutMessage.MatchesProto;

import android.os.AsyncTask;
import android.util.Log;

public class PipelineProcess extends AsyncTask<Void, String, Void>{
	
	private UDPsend sendFrame;
	private UDPsend sendMatches;
	private FrameProto.Builder protoFrame;
	private boolean sendMatchesF = false;
	
	public PipelineProcess() {
		sendFrame = new UDPsend();
		sendMatches = new UDPsend();
	}

	@Override
	protected Void doInBackground(Void... params) {
		
		if(protoFrame != null)
			protoFrame.clear();
		
		protoFrame = FrameProto.newBuilder();
		
		PipelineSharedData.secondFrame = new Frame(); // native object
//		PipelineSharedData.addFrame(frame);
		
		if(PipelineSharedData.doSURF){
			PipelineSharedData.secondFrame.addFrame(new SWIGTYPE_p_cv__Mat( PipelineSharedData.getLastImage(), true));
			PipelineSharedData.secondFrame.extractFeatures();
			if (PipelineSharedData.doMatching && PipelineSharedData.noFrames > 0){
				MatcherFLANN matcher = new MatcherFLANN(PipelineSharedData.firstFrame, PipelineSharedData.secondFrame);
				matcher.matchFrames((int)PipelineSharedData.noFrames-1, (int)PipelineSharedData.noFrames);
				if(matcher.buildMatchProtoMessage()){
					// message is already built, ready to be sent
					PipelineSharedData.matchesProtoMessage = matcher.getMatches();
					sendMatchesF = true;
					Log.e("raw", "Matches length: " + PipelineSharedData.matchesProtoMessage.length);
				}
				
			}
		}
		
		if	(PipelineSharedData.secondFrame.buildFrameProtoMessage(PipelineSharedData.sendJPEG)){
			byte[] data = PipelineSharedData.secondFrame.getFrame();
			Log.e("raw", "Received length: " + data.length);
			try {
				protoFrame.mergeFrom(data);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}

		ProtoWriter.buildMessage(protoFrame);
		PipelineSharedData.noFrames++;
		
		PipelineSharedData.firstFrame = PipelineSharedData.secondFrame;
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		sendFrame.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ProtoWriter.getData());
		if(sendMatchesF){
			sendMatches.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, PipelineSharedData.matchesProtoMessage);
			Log.e("raw", "Matches length sent to UDP: " + PipelineSharedData.matchesProtoMessage.length);
		}
		super.onPostExecute(result);
	}
	
	
	static{
		System.loadLibrary("protobuf");
		System.loadLibrary("pipeline_native");
	}

}
