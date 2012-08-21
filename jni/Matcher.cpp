#include "Matcher.h"
#include "matches.pb.h"

MatcherFLANN::MatcherFLANN(Frame & frame1, Frame & frame2)
{
    first = frame1;
    second = frame2;
}

void MatcherFLANN::match(const cv::Mat &preDesc, const cv::Mat &curDesc, 
                         std::vector<cv::DMatch>& matches)
{
    matches.clear();
    
    std::vector< cv::DMatch > raw_matches;
    
    cv::FlannBasedMatcher flannMatcher;
    flannMatcher.match(preDesc, curDesc, raw_matches);
    double max_dist = 0; double min_dist = 100;      
    
    //-- Quick calculation of max and min distances between keypoints
    for( int i = 0; i < preDesc.rows; i++ )
    { 
        double dist = raw_matches[i].distance;
        if( dist < min_dist ) min_dist = dist;
        if( dist > max_dist ) max_dist = dist;
    }
    
    for( int i = 0; i < preDesc.rows; i++ )
    { 
        if( raw_matches[i].distance < 5*min_dist )
            matches.push_back( raw_matches[i]);
    }
}


bool MatcherFLANN::matchFrames(int seq1, int seq2)
{
    if(first.Descriptors.rows > 0 && second.Descriptors.rows > 0)
    {
        firstId = seq1;
        secondId = seq2;
        match(first.Descriptors, second.Descriptors, matchesFrames);
        return matchesFrames.size() > 0;
    }else
        return false;
}

bool MatcherFLANN::buildMatchProtoMessage()
{
    if(matchesFrames.size() > 0)
    {
        sfm::MatchesProto matchProto;
        matchProto.set_imagelseq(firstId);
        matchProto.set_imagerseq(secondId);
        for(int i(0); i < matchesFrames.size(); i++)
        {
            sfm::DMatches * protoDMatches = matchProto.add_matches();
            protoDMatches->set_distance(matchesFrames[i].distance);
            protoDMatches->set_imgidx(matchesFrames[i].imgIdx);
            protoDMatches->set_queryidx(matchesFrames[i].queryIdx);
            protoDMatches->set_trainidx(matchesFrames[i].trainIdx);
        }
        int size = matchProto.ByteSize();
        signed char * buffer = new signed char[size];
        bool protoGood = matchProto.SerializeToArray(buffer, size);
        outMatch = buffer;
        sizeOut = (size_t)size;
        
        return protoGood;
    }else
        return false;
}
