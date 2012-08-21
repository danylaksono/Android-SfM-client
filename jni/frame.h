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


#ifndef FRAME_H
#define FRAME_H

#include <vector>


#include <opencv2/core/core.hpp>
#include <opencv2/nonfree/features2d.hpp>

#include "data.pb.h"

class Frame
{
private:
    size_t x_subs;
    size_t y_subs;
    std::vector<size_t> curr_hess_thresholds;
    cv::Mat image;
    std::vector<cv::KeyPoint> Keypoints;
    signed char * outFrame;
    size_t sizeOut;
    void writeProtoMat(sfm::cvMatProto &, cv::Mat &);

public:
    Frame();
    Frame(cv::Mat &);
    void extractFeatures();
    void addFrame(cv::Mat &);
    bool buildFrameProtoMessage(bool doJPEG);
    signed char * getFrame(size_t * len){*len = sizeOut;return outFrame;}
    cv::Mat Descriptors;
    virtual ~Frame();
    
};

#endif // FRAME_H
