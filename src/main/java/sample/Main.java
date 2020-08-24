package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.xfeatures2d.SURF;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;


public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        String path = "D:\\Projektek\\Java\\MemeProcessor\\src\\main\\java\\sample\\views\\Hub.fxml";
        System.out.println(getClass().getResource("/views/Hub.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("/views/Hub.fxml"));
       // Parent root = FXMLLoader.load(getClass().getResource("/views/Hub.fxml"));
        //Group root = new Group();
        primaryStage.setTitle("Meme extractor");
       Scene scene = new Scene(root, 800, 600);
       primaryStage.setScene(scene);
       primaryStage.show();


    }


    private static Image analyzeImage(String path) {
        Mat src = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
        if (src.empty()) {
            System.err.println("Cannot read image: ");
            System.exit(0);
        }
        //-- Step 1: Detect the keypoints using SURF Detector
        double hessianThreshold = 400;
        int nOctaves = 4, nOctaveLayers = 3;
        boolean extended = false, upright = false;
        SURF detector = SURF.create(hessianThreshold, nOctaves, nOctaveLayers, extended, upright);
        
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        detector.detect(src, keypoints);
        //-- Draw keypoints
        Features2d.drawKeypoints(src, keypoints, src);
        //-- Show detected (drawn) keypoints
        OpenCVFrameConverter.ToMat matconv = new OpenCVFrameConverter.ToMat();

        Frame frame = matconv.convert(src);
        JavaFXFrameConverter fxconv = new JavaFXFrameConverter();

        return fxconv.convert(frame);
    }


    public static void main(String[] args) throws IOException {

        System.loadLibrary("opencv_java440");
        System.loadLibrary("libvlccore");
        System.loadLibrary("libvlc");

        launch(args);
    }
}
