package bartalos86;

import javafx.concurrent.Task;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;


public class VideoProcessor {

    private final ImageView display;

    public VideoProcessor(ImageView display) {
        this.display = display;
    }

    public Task<Void> processVideoAsTask(String path, String outputPath, String splitterPath) throws IOException {
        return processVideoAsTask(path, outputPath, splitterPath, "Memes");
    }

    public Task<Void> processVideoAsTask(String path, String outputPath, String splitterPath, String saveName) throws IOException {
        return processVideoAsTask(path, outputPath, splitterPath, 0, saveName);
    }

    public static String formatHttpStream(String serverAddress) {
        StringBuilder sb = new StringBuilder(60);
        sb.append(":sout=#duplicate{dst=std{access=http,mux=ts,");
        sb.append("dst=");
        sb.append(serverAddress);
        sb.append("}}");
        return sb.toString();
    }

    public Task<Void> processVideoAsTask(String path, String outputPath, String splitterPath, int frameSkip, String saveName) throws IOException {
        Task<Void> processingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                FeatureDetector detector = new FeatureDetector(Imgcodecs.imread(splitterPath));
                FFmpegFrameGrabber frameGrabber = FFmpegFrameGrabber.createDefault(new File(path));
                JavaFXFrameConverter fxConverter = new JavaFXFrameConverter();


                frameGrabber.start();
                //Frames and images
                Frame frame = frameGrabber.grabImage().clone();
                Frame previousFrame = null;

                long startTime = System.nanoTime();
                long elapsedSeconds = 0;
                int memeCounter = 0;
                long processedFrames = 0;
                int statisticalMemeCounter = 0;
                long totalFrames = frameGrabber.getLengthInFrames();
                int maxFrameSkip = frameSkip;
                boolean isPreviousCutterFrame = false;

                StringBuilder descriptions = new StringBuilder();
                descriptions.append("[");

                boolean isFirstRun = true;

                if (frameSkip > 0)
                    isFirstRun = false;

                while (frame != null) {
                    BufferedImage processedImage = processFrame(frame);

                    if (previousFrame != null) {
                        boolean isCutterFrame = isCutterFrame(frame, detector);

                        //isMeme
                        if (isCutterFrame && !isPreviousCutterFrame) {
                            statisticalMemeCounter++;

                            File memePath = new File(outputPath + "/" + saveName + "/meme-" + ++memeCounter + ".png");
                            while (memePath.exists()) {
                                memePath = new File(outputPath + "/" + saveName + "/meme-" + ++memeCounter + ".png");
                            }
                            Files.createDirectories(memePath.toPath());
                            System.out.println("Meme detected");

                            BufferedImage croppedMeme = cropToSize(previousFrame);
                            String description = ImageProcessor.extractText(croppedMeme);

                            descriptions.append(String.format("{\"img\": \"meme-%d.png\",\"desc\": \"%s\"},\n",memeCounter,description));


                            display.setImage(fxConverter.convert(previousFrame));
                            ImageIO.write(croppedMeme, "png", memePath);

                            elapsedSeconds += (System.nanoTime() - startTime) / (60 * 60 * 60 * 60 * 60);
                            startTime = System.nanoTime();

                            if (isFirstRun)
                                maxFrameSkip++;

                        } else if (isFirstRun) {
                            if (isCutterFrame)
                                maxFrameSkip++;
                        }

                        if (isFirstRun)
                            if (!isCutterFrame && isPreviousCutterFrame)
                                isFirstRun = false;

                        isPreviousCutterFrame = isCutterFrame;

                    }

                    previousFrame = frame.clone();
                    frame = frameGrabber.grabImage();
                    processedFrames++;

                    //frameskips
                    for (int i = 0; i < maxFrameSkip - 1; i++) {
                        frame = frameGrabber.grabImage();
                        if(frame == null)
                            break;
                        processedFrames++;
                    }

                    String statusText = String.format("Total frames: %d  Processed frames: %d\n" +
                                    "Memes found: %d  Average time: %.2fs\nFrameskip: %d  Progress: %.2f\nRemaining time: %.2fm",
                            totalFrames,
                            processedFrames,
                            statisticalMemeCounter,
                            ((float) elapsedSeconds) / statisticalMemeCounter,
                            maxFrameSkip,
                            processedFrames / (float) totalFrames,
                            ((totalFrames - processedFrames) * ((float) elapsedSeconds / processedFrames)) / 60);

                    updateMessage(statusText);


                }
                descriptions.replace(descriptions.length()-2,descriptions.length()-1,"");
                descriptions.append("]");
                File descPath = new File(outputPath + "/" + saveName + "/descriptions.json");
                descPath.createNewFile();
                FileWriter writer = new FileWriter(descPath);
                writer.write(descriptions.toString());
                writer.flush();
                writer.close();
                System.out.println("Ended succesfully");

                frameGrabber.stop();
                succeeded();
                return null;
            }

        };

        return processingTask;
    }

    public BufferedImage processFrame(Frame frame) {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        return converter.convert(frame);
    }

    public Frame unprocessImage(BufferedImage image) {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        return converter.convert(image);
    }

    private long analyze(File path) throws IOException {
        return analyze(ImageIO.read(path));
    }

    private static long analyze(Mat mat) {
        System.out.println(Arrays.toString(mat.get(0, 0)));

        return 0;
    }

    private boolean isCutterFrame(BufferedImage image) throws IOException {
        if (String.valueOf(analyze(image)).startsWith("-1259")) {
            return true;
        }
        return false;
    }

    private boolean isCutterFrame(Frame frame, FeatureDetector detector) throws IOException {
        if (detector.compare(frame) > 1000) {
            return true;
        }
        return false;
    }

    public long analyze(BufferedImage image) throws IOException {
        long color = 0;
        long total = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                color += image.getRGB(x, y);
                total++;

            }
        }

        return color / total;
    }

    public BufferedImage cropToSize(Frame frame) throws IOException {
        Java2DFrameConverter buffConverter = new Java2DFrameConverter();
        ImageCropData cropData = ImageProcessor.getCropData(frame);

        return buffConverter.convert(frame).getSubimage(cropData.getxOffset(), 0, cropData.getWidth(), cropData.getHeight());

    }


}
