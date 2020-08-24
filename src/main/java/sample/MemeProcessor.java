package sample;

import javafx.scene.image.ImageView;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.JavaFXFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MemeProcessor {

    Frame previousFrame;
    boolean isPreviousCutterFrame;
    int memeCounter;
    JavaFXFrameConverter fxConverter;
    FeatureDetector detector;
    ImageView display;

    public MemeProcessor(FeatureDetector detector, ImageView display) {
        previousFrame = null;
        isPreviousCutterFrame = false;
        memeCounter = 0;
        fxConverter = new JavaFXFrameConverter();
        this.detector = detector;
        this.display = display;

    }

    public void completeProcessFrame(Frame frame, String outputPath, String saveName) throws IOException {
        if (previousFrame != null) {
            boolean isCutterFrame = isCutterFrame(frame);
            if (isCutterFrame && !isPreviousCutterFrame) {
                File memePath = getMemePath(outputPath, saveName);
                Files.createDirectories(memePath.toPath());
                System.out.println("Meme detected");
                BufferedImage croppedMeme = cropToSize(previousFrame);
                display.setImage(fxConverter.convert(previousFrame));

                ImageIO.write(croppedMeme, "png", memePath);

            }
            isPreviousCutterFrame = isCutterFrame;
        }
        previousFrame = frame.clone();
    }

    private File getMemePath(String outputPath, String subDir){
        File memePath = new File(outputPath + "/" + subDir + "/meme-" + ++memeCounter + ".png");
        while (memePath.exists()) {
            memePath = new File(outputPath + "/" + subDir + "/meme-" + ++memeCounter + ".png");
        }

        return memePath;
    }

    private boolean isCutterFrame(Frame frame) throws IOException {
        if (detector.compare(frame) > 1000) {
            return true;
        }
        return false;
    }

    public BufferedImage cropToSize(Frame frame) throws IOException {
        Java2DFrameConverter buffConverter = new Java2DFrameConverter();
        ImageCropData cropData = ImageProcessor.getCropData(frame);

        return buffConverter.convert(frame).getSubimage(cropData.getxOffset(), 0, cropData.getWidth(), cropData.getHeight());

    }
}
