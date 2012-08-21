#ifndef MATCHER_H_
#define MATCHER_H_

#include <vector>
#include <opencv2/features2d/features2d.hpp>

#include "frame.h"

class Matcher
{

};

class MatcherFLANN : public Matcher
{
private:
    Frame first;
    Frame second;
    int firstId;
    int secondId;
    std::vector<cv::DMatch> matchesFrames;
    size_t sizeOut;
    signed char * outMatch;
    void match(const cv::Mat &, const cv::Mat &, std::vector<cv::DMatch>& );
    
public:
    MatcherFLANN(Frame &, Frame &);
    bool matchFrames(int, int);
    bool buildMatchProtoMessage();
    signed char * getMatches(size_t *len){*len = sizeOut; return outMatch;}
};

#endif /* MATCHER_H_ */
