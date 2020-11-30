package bartalos86;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.xfeatures2d.SURF;
import java.util.ArrayList;
import java.util.List;

public class FeatureDetector {
    private Frame splitterFrame;
    private Mat splitterDescriptions;
    OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();


    public FeatureDetector(Mat splitterFrameMat){

        //this.splitterFrame = splitterFrame;
        Mat splittter = splitterFrameMat;


        MatOfKeyPoint keyp2 = new MatOfKeyPoint();
        Mat descriptor2 = new Mat();
        double hessianThreshold = 400;
        int nOctaves = 4, nOctaveLayers = 3;
        boolean extended = false; boolean upright = false;
        SURF detector = SURF.create(hessianThreshold,nOctaves,nOctaveLayers,extended,upright);
        detector.detectAndCompute(splittter,new Mat(),keyp2,descriptor2);
        splitterDescriptions = descriptor2;
    }

    public long compare(Frame frame1){

        OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
        Mat img = matConverter.convertToOrgOpenCvCoreMat(frame1);


       /* Mat img = Imgcodecs.imread(imagePath1);
        Mat img2 = Imgcodecs.imread(imagePath2);*/

        //Keypoint detection
        double hessianThreshold = 400;
        int nOctaves = 4, nOctaveLayers = 3;
        boolean extended = false; boolean upright = false;

        SURF detector = SURF.create(hessianThreshold,nOctaves,nOctaveLayers,extended,upright);
        MatOfKeyPoint keyp1 = new MatOfKeyPoint();

        Mat descriptor1 = new Mat();
        detector.detectAndCompute(img,new Mat(),keyp1,descriptor1);

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        List<MatOfDMatch> knnMatches = new ArrayList<>();
        matcher.knnMatch(descriptor1, splitterDescriptions, knnMatches, 2);

        //Filter
        float ratioThreshold = 0.7f;
        long matchesCount = 0;
        List<DMatch> listOfGoodMatches = new ArrayList<>();
        for (int i = 0; i < knnMatches.size(); i++) {
            if(knnMatches.get(i).rows()>1){
                DMatch[] matches = knnMatches.get(i).toArray();
                if(matches[0].distance < ratioThreshold * matches[1].distance){
                   // listOfGoodMatches.add(matches[0]);
                    matchesCount++;
                }
            }
        }

        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromList(listOfGoodMatches);

      //  System.out.println("Matches: " + matchesCount);

        Mat imgMatches = new Mat();
      /* Features2d.drawMatches(img, keyp1, img2, keyp2, goodMatches, imgMatches, Scalar.all(-1),
                Scalar.all(-1), new MatOfByte(), Features2d.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS);*/



        //eturn matConverter.convert(imgMatches);
        return matchesCount;
    }
}
