package cvg.sfmPipeline.main;

import cvg.sfmPipeline.main.PipelineOutMessage.CameraBodyTransProto;
import cvg.sfmPipeline.main.PipelineOutMessage.CameraMatrixProto;
import cvg.sfmPipeline.main.PipelineOutMessage.FrameProto;
import cvg.sfmPipeline.main.PipelineOutMessage.MetadataProto;

public class ProtoWriter {
	static private final String TAG = "ProtoLog::Writer";
	
	private ProtoWriter(){
	}
	
	static private byte[] data;
	
	
	
	public static byte[] getData() {
		return data;
	}



	public static void setData(byte[] data) {
		ProtoWriter.data = data;
	}



	static public void buildMessage(FrameProto.Builder frame){
		//-- Add image metadata
		MetadataProto.Builder meta = MetadataProto.newBuilder();
		
		/**
		 * If ground truth vicon data wants to be actually collected,
		 * then please used the included UDPthread class to do so. For
		 * now, because of comatibility with the python server, the 
		 * vicon data is still packed, but its only zeros.
		 * */
		
		meta.setAngX( PipelineSharedData.getLastGroundTr()[0]);
		meta.setAngY( PipelineSharedData.getLastGroundTr()[1]);
		meta.setAngZ( PipelineSharedData.getLastGroundTr()[2]);
		meta.setPosX( PipelineSharedData.getLastGroundTr()[3]);
		meta.setPosY( PipelineSharedData.getLastGroundTr()[4]);
		meta.setPosZ( PipelineSharedData.getLastGroundTr()[5]);
		
		meta.setVal0( PipelineSharedData.getLastSensor()[0]);
		meta.setVal1( PipelineSharedData.getLastSensor()[1]);
		meta.setVal2( PipelineSharedData.getLastSensor()[2]);
		meta.setType( PipelineSharedData.getSensorType());
		
		meta.setTimestamp(PipelineSharedData.getTimestamp());
		
		//-- Add calibration data
		CameraMatrixProto.Builder K = CameraMatrixProto.newBuilder();
		
		float[] Kf = PipelineSharedData.getCameraMatrix();
		for (int i = 0; i < Kf.length; i++)
			K.addData(Kf[i]);
		CameraBodyTransProto.Builder C2B = CameraBodyTransProto.newBuilder();
		float[] C2Bf = PipelineSharedData.getCamera2body();
		for (int i = 0; i < C2Bf.length; i++)
			C2B.addData(C2Bf[i]);
		
		//-- Bundle everything
		
		frame.setMetadata(meta.build());
		frame.setCameraMatrix(K.build());
		frame.setCameraBodyTrans(C2B.build());
		frame.setId(3l); // should contain an identifier for the device! TODO
		frame.setSeq(PipelineSharedData.noFrames);
		
		setData( frame.build().toByteArray() );
	}
	
}