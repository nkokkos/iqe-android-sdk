#include <vector>
#include <iostream>
#include <numeric>

#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>

using namespace cv;
using namespace std;

class IQIndexImp;

struct MatchResult{
	vector<string> names;
	vector<Mat> images;
	vector<Mat> homo_images;
	vector<Mat> double_homo_images;
	vector<Mat> warp_images;
};

class IQIndex {
    
public:
    
    IQIndex(string settings_file);
    IQIndex();
    ~IQIndex();
    
    string data_dir;
    vector<string> obj_ids;
    map<string, vector<string> > obj_id2img_ids;
    map<string, string> obj_id2name;
    map<string, string> obj_id2meta;
	
    int num_images;
    
    int load(string, string);
    int store(string, string, string);
    int train();
    int train(int comm_rank);
    int match(Mat& img);
    int compute(const Mat& img, const char*, const char*);
    int compute(const Mat& img, vector<KeyPoint>& keypoints,
            Mat& descriptors);
    int represent(const Mat& img, string&);
    MatchResult getLastResult() const;
    float getP() const;
    void setP(float p);
    
private:
    void init();

    IQIndexImp *pimpl_;
    MatchResult m_last_result;
    float m_p;

    bool do_crop;
};

#ifdef ANDROID
IQIndex* get_instance();
#endif

class ORBSettings {
public:
    ORBSettings(string settings_file);
    ORBSettings();
    ~ORBSettings();

    int getEdgeThreshold() const;
    int getNfeatures() const;
    int getNlevels() const;
    int getPatchSize() const;
    float getScaleFactor() const;
    int getScoreType() const;
    int getWtaK() const;
    int getFirstLevel() const;
    int getNfeaturestrain() const;
    int getOctComps() const;
    int getLshKeySize() const;
    int getLshMpl() const;
    int getLshTableNumber() const;

private:
    int m_nfeatures;
    float m_scaleFactor;
    int m_nlevels;
    int m_edgeThreshold;
    int m_WTA_K; // Values: 2, 3, 4
    int m_scoreType; // Values: ORB::HARRIS_SCORE, ORB::FAST_SCORE
    int m_patchSize; // According to some meeting notes 41 works good.
    int m_firstLevel;
    int m_nfeaturestrain;
    int m_octComps; // #bit-comparisons = 8*m_octComps
    int m_lsh_tableNumber;
    int m_lsh_keySize;
    int m_lsh_mpl;
};



