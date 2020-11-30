package bartalos86;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.xfeatures2d.SURF;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        String path = "D:\\Projektek\\Java\\MemeProcessor\\src\\main\\java\\bartalos86\\views\\Hub.fxml";
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


    public static void main(String[] args) throws IOException, TesseractException {

        System.loadLibrary("opencv_java440");
        System.loadLibrary("libvlccore");
        System.loadLibrary("libvlc");

        BufferedImage image = ImageIO.read(new File("Resources/testMeme2.png"));
        ImageProcessor.extractText(image);
        //BufferedImage image2 = ImageIO.read(new File("Resources/testMeme.png"));
        //ImageProcessor.extractText(image2);
        launch(args);
    }
}
