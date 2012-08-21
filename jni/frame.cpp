/*
    <one line to give the program's name and a brief idea of what it does.>
    Copyright (C) 2012  <copyright holder> <email>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


#include "frame.h"
#include <opencv2/highgui/highgui.hpp>
#include <android/log.h>
#include <sstream>

Frame::Frame()
{
	__android_log_print(ANDROID_LOG_ERROR, "Native", "Constructor 1");
	// initialization of some fields
    x_subs = 3;
    y_subs = 3;
    for (size_t i = 0; i < x_subs*y_subs; i++)
    {
        curr_hess_thresholds.push_back(3);
    }

}

Frame::Frame(cv::Mat & input)
{
	__android_log_print(ANDROID_LOG_ERROR, "Native", "Constructor 2");
	input.copyTo(image);
}

void
Frame::addFrame(cv::Mat & input)
{
	__android_log_print(ANDROID_LOG_ERROR, "Native", "Add image method.");

    if(!image.empty())
    	__android_log_print(ANDROID_LOG_ERROR, "Native", "Image will be overwritten");

    input.copyTo(image);
}

void
Frame::extractFeatures(){
	double tic = (double)cv::getCPUTickCount();
//    cv::Mat imageIn = image;
//    float hess_thresholds[] = { 25, 50, 100, 200, 300, 400, 600, 800, 1000, 1500, 2000, 4000, 8000, 12000, 16000 };
//    size_t max_thresholds = sizeof(hess_thresholds) / sizeof(hess_thresholds[0]);
//
//    std::vector<cv::SurfFeatureDetector> detectors(x_subs*y_subs);
//
//    for (size_t i = 0; i < detectors.size(); i++)
//    {
//        std::vector<cv::KeyPoint> keypoints;
//        detectors[i] = cv::SurfFeatureDetector(hess_thresholds[curr_hess_thresholds[i]]);
//        int xstart  = 0 + (i%x_subs)*imageIn.cols/x_subs - (i%x_subs)*20;
//        int xend    = 0 + ((i%x_subs)+1)*imageIn.cols/x_subs;
//        int ystart  = 0 + (i/y_subs)*imageIn.rows/y_subs - (i/y_subs)*20;
//        int yend    = 0 + ((i/y_subs)+1)*imageIn.rows/y_subs;
//        cv::Mat detImage = imageIn(cv::Range(ystart, yend), cv::Range(xstart, xend));
//
//        detectors[i].detect(detImage, keypoints);
//
//        if (keypoints.size() > 100) curr_hess_thresholds[i] = (curr_hess_thresholds[i] + 1 < max_thresholds ? curr_hess_thresholds[i] + 1 : curr_hess_thresholds[i]);
//        if (keypoints.size() < 60) curr_hess_thresholds[i] = (curr_hess_thresholds[i] - 1 > 0 ? curr_hess_thresholds[i] - 1 : curr_hess_thresholds[i]);
//
//        for (size_t k = 0; k < keypoints.size(); k++)
//        {
//            keypoints[k].pt.x += xstart;
//            keypoints[k].pt.y += ystart;
//            if (keypoints.at(k).pt.x > 0 && keypoints.at(k).pt.x < imageIn.cols && keypoints.at(k).pt.y > 0 && keypoints.at(k).pt.y < imageIn.cols)
//            {
//                Keypoints.push_back(keypoints.at(k));
//            }
//        }
//    }
//    cv::SurfDescriptorExtractor extractor;
//    extractor.compute(imageIn, Keypoints, Descriptors);

	cv::SURF surfObj = cv::SURF(500); // extended SURF
//	surfObj.detect(image, Keypoints);
	surfObj.operator ()(image, cv::noArray(), Keypoints, Descriptors, false);

    double toc = ((double)cv::getCPUTickCount() - tic)/cv::getTickFrequency();
    __android_log_print(ANDROID_LOG_ERROR, "Native", "Keypoints %d, computed in %lf ms", Keypoints.size(), toc*1000);
}


void
Frame::writeProtoMat(sfm::cvMatProto & proto, cv::Mat & mat)
{
    proto.set_n_dims(mat.dims);
    size_t databytes = 0;
    for (int i = 0; i < mat.dims; i++)
    {
        sfm::CvMatDimProto *cur_dim = proto.add_dims();
        cur_dim->set_size(mat.size.p[i]);
        cur_dim->set_step(mat.step.p[i]);
        databytes += mat.size.p[i]*mat.step.p[i];
    }
    proto.set_type(mat.type());
    proto.set_bytedata((char *)(mat.data), databytes);
}


/**
 * Will build a regular proto message using the last image data
 * */
bool
Frame::buildFrameProtoMessage(bool doJPEG)
{	sfm::FrameProto frameProto;
	__android_log_print(ANDROID_LOG_ERROR, "Native","Building protobuf message");
	bool jpegGood = true;
	bool protoGood = true;
	if (Keypoints.size() > 0 && Descriptors.rows > 0)
	{
		__android_log_print(ANDROID_LOG_ERROR, "Native", "First kpt: %f", Keypoints[0].pt.x);
		sfm::cvMatProto * descriptorsProto = frameProto.mutable_descriptors();
        writeProtoMat(*descriptorsProto, Descriptors);
        sfm::Keypoints *  keypointsProto = frameProto.mutable_keypoints();
		for (int i = 0; i < Keypoints.size(); i++)
		{
           sfm::Keypoints_cvKeypoint * keypointPb = keypointsProto->add_keypoints();
           cv::KeyPoint keypointCV = Keypoints[i];
           keypointPb->set_ptx(keypointCV.pt.x);
           keypointPb->set_pty(keypointCV.pt.y);
           keypointPb->set_angle(keypointCV.angle);
           keypointPb->set_octave(keypointCV.octave);
           keypointPb->set_size(keypointCV.size);
           keypointPb->set_response(keypointCV.response);
		}
		__android_log_print(ANDROID_LOG_ERROR, "Native", "Keypoints length bytes: %d, descr in bytes : %d",
				keypointsProto->ByteSize(), descriptorsProto->ByteSize());
	}

	if(doJPEG)
	{
		double tic = (double)cv::getCPUTickCount();
		sfm::cvMatProto * imgJPEG = frameProto.add_images();
		imgJPEG->set_type(sfm::cvMatProto_ImageType_JPEG);
		std::vector<unsigned char> buffer;
		std::vector<int> params;
		params.push_back(CV_IMWRITE_JPEG_QUALITY);
		params.push_back(75);
		jpegGood = cv::imencode(".jpg", image, buffer, params );
		imgJPEG->set_bytedata((void *)&buffer[0], buffer.size());
		double toc = ((double)cv::getCPUTickCount() - tic)/cv::getTickFrequency();
		__android_log_print(ANDROID_LOG_ERROR, "Native", "Compression done in %lf ms",toc*1000);
	}

	int size = frameProto.ByteSize();
	signed char * buffer = new signed char[size];
	protoGood = frameProto.SerializeToArray(buffer, size);
	outFrame = buffer;
	sizeOut = (size_t)size;

	return jpegGood && protoGood;

}

Frame::~Frame()
{

}

