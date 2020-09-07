package sample;

import javafx.concurrent.Task;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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

    long screens = 0;

    boolean isFinished = false;

    public BufferedImage image = null;

    int numOfFinish = 0;
    float percentage = 0;

    long elapsedSec =0;
    long startNano;
    public Task<Void> processStreamAsTask(String url, String splitterPath, String outputPath, float rate, String saveName) {

        Task<Void> videoTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
                EmbeddedMediaPlayer mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
                FeatureDetector detector = new FeatureDetector(Imgcodecs.imread(splitterPath));

                MemeProcessor proceessor = new MemeProcessor(detector, display);

                mediaPlayer.videoSurface().set(videoSurfaceForImageView(display));
                mediaPlayer.media().play(url, " :network-caching=20000", ":no-sout-all", ":sout-keep", "--no-xlib");
                mediaPlayer.controls().setRate(rate);


                numOfFinish = 0;
                percentage = 0;


                mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

                    @Override
                    public void playing(MediaPlayer mediaPlayer) {
                        super.playing(mediaPlayer);
                       mediaPlayer.controls().setRate(0);
                        startNano = System.nanoTime();
                        mediaPlayer.audio().setMute(true);

//                        mediaPlayer.submit(new Runnable() {
//                            @Override
//                            public void run() {
//
//
//                                while(percentage < 0.99){
//
//                                    image = mediaPlayer.snapshots().get();
//                                    Java2DFrameConverter conv = new Java2DFrameConverter();
//                                    Frame frame = conv.convert(image);
//
//                                    if (frame != null) {
//                                        try {
//                                            proceessor.completeProcessFrame(frame, outputPath, saveName);
//                                            mediaPlayer.controls().nextFrame();
//
//                                            System.out.println(mediaPlayer.status().position());
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//
//
//                                }
//
//
//                                //System.out.println("Status: IS_PLAYING: " + mediaPlayer.status().isPlaying() + " IS_PLAYABLE: " + mediaPlayer.status().isPlayable());
//
//                            }
//                        });
                    }

                    @Override
                    public void positionChanged(MediaPlayer mediaPlayer, float pos) {

                        mediaPlayer.submit(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(pos);

                                mediaPlayer.controls().pause();
                                image = mediaPlayer.snapshots().get();

                                Java2DFrameConverter conv = new Java2DFrameConverter();
                                Frame frame = conv.convert(image);

                                if (frame != null) {
                                    try {
                                        proceessor.completeProcessFrame(frame, outputPath, saveName);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }


                                mediaPlayer.controls().play();




                                //System.out.println("Status: IS_PLAYING: " + mediaPlayer.status().isPlaying() + " IS_PLAYABLE: " + mediaPlayer.status().isPlayable());

                            }
                        });
                        elapsedSec = (System.nanoTime() - startNano) / (60 * 60 * 60 * 60 * 60);

                        percentage = pos;
                        float remainingTime = (100 - (pos*100))*(elapsedSec/(pos*100));

                        updateMessage(String.format("Percentage: %.2f Elapsed: %dm Remaining: %.2fm ",pos*100,elapsedSec/60,remainingTime/60));

                        if(pos > 0.995f){
                            mediaPlayer.release();
                            mediaPlayerFactory.release();
                            System.out.println("Finish - Done");

                        }
                    }


                    @Override
                    public void finished(MediaPlayer mediaPlayer) {
                        super.finished(mediaPlayer);
                        mediaPlayer.submit(new Runnable() {
                            @Override
                            public void run() {

                                if((++numOfFinish) >= 3){
                                    mediaPlayer.release();
                                    succeeded();
                                }
                                System.out.println("Finish");
                            }
                        });
                    }

                });

                // Cleanly dispose of the media player instance and any associated native resources
                //mediaPlayer.release();

                // Cleanly dispose of the media player factory and any associated native resources
              //  mediaPlayerFactory.release();

                return null;
            }
        };
        return videoTask;

    }

    public Task<Void> processVideoAsTask(String path, String outputPath, String splitterPath, int frameSkip, String saveName) throws IOException {
        Task<Void> processingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                FeatureDetector detector = new FeatureDetector(Imgcodecs.imread(splitterPath));
                FFmpegFrameGrabber frameGrabber = FFmpegFrameGrabber.createDefault(new File(path));
                JavaFXFrameConverter fxConverter = new JavaFXFrameConverter();
                //OpenCVFrameConverter.ToMat matconv = new OpenCVFrameConverter.ToMat();

                frameGrabber.start();


                //Frames and images
                Frame frame = frameGrabber.grabImage().clone();
                Frame previousFrame = null;
                //BufferedImage previousImage = null;

                // FeatureDetector detector = new FeatureDetector(Imgcodecs.imread(splitterPath));

                long startTime = System.nanoTime();
                long elapsedSeconds = 0;
                int memeCounter = 0;
                long processedFrames = 0;
                int statisticalMemeCounter = 0;
                long totalFrames = frameGrabber.getLengthInFrames();
                int maxFrameSkip = frameSkip;
                boolean isPreviousCutterFrame = false;

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
                            //analyze(matconv.convertToOrgOpenCvCoreMat(previousFrame));
                            BufferedImage croppedMeme = cropToSize(previousFrame);
                            display.setImage(fxConverter.convert(previousFrame));

                            ImageIO.write(croppedMeme, "png", memePath);

                            elapsedSeconds += (System.nanoTime() - startTime) / (60 * 60 * 60 * 60 * 60);

                            //System.out.println("Meme extract time: " +  (System.nanoTime() - startTime)/(60*60*60*60*60));
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

                    // System.out.println("file: " + splitterPath);
                    //previousImage = processedImage;
                    previousFrame = frame.clone();
                    frame = frameGrabber.grabImage().clone();
                    processedFrames++;

                    //frameskips
                    for (int i = 0; i < maxFrameSkip - 1; i++) {
                        frame = frameGrabber.grabImage().clone();
                        processedFrames++;
                    }

                    String statusText = String.format("Total frames: %d  Processed frames: %d  Memes found: %d %nAverage time: %.2fs Frameskip: %d  %nProgress: %.2f  Remaining time: %.2fm",
                            totalFrames,
                            processedFrames,
                            statisticalMemeCounter,
                            ((float) elapsedSeconds) / statisticalMemeCounter,
                            maxFrameSkip,
                            processedFrames / (float) totalFrames,
                            ((totalFrames - processedFrames) * ((float) elapsedSeconds / processedFrames)) / 60);

                    updateMessage(statusText);


                }

                frameGrabber.stop();
                succeeded();
                return null;
            }

        };

        return processingTask;
    }

    public void testVideoProcess(String path, String outputPath, String splitterPath, int frameSkip, String saveName) throws IOException {
        FeatureDetector detector = new FeatureDetector(Imgcodecs.imread(splitterPath));
        FFmpegFrameGrabber frameGrabber = FFmpegFrameGrabber.createDefault(new File(path));
        JavaFXFrameConverter fxConverter = new JavaFXFrameConverter();
        //OpenCVFrameConverter.ToMat matconv = new OpenCVFrameConverter.ToMat();

        frameGrabber.start();


        //Frames and images
        Frame frame = frameGrabber.grabImage().clone();
        Frame previousFrame = null;
        //BufferedImage previousImage = null;

        // FeatureDetector detector = new FeatureDetector(Imgcodecs.imread(splitterPath));

        long startTime = System.nanoTime();
        long elapsedSeconds = 0;
        int memeCounter = 0;
        long processedFrames = 0;
        int statisticalMemeCounter = 0;
        long totalFrames = frameGrabber.getLengthInFrames();
        int maxFrameSkip = frameSkip;
        boolean isPreviousCutterFrame = false;

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
                    //analyze(matconv.convertToOrgOpenCvCoreMat(previousFrame));
                    BufferedImage croppedMeme = cropToSize(previousFrame);
                    display.setImage(fxConverter.convert(previousFrame));

                    ImageIO.write(croppedMeme, "png", memePath);

                    elapsedSeconds += (System.nanoTime() - startTime) / (60 * 60 * 60 * 60 * 60);

                    //System.out.println("Meme extract time: " +  (System.nanoTime() - startTime)/(60*60*60*60*60));
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

            // System.out.println("file: " + splitterPath);
            //previousImage = processedImage;
            previousFrame = frame.clone();
            frame = frameGrabber.grabImage().clone();
            processedFrames++;

            //frameskips
            for (int i = 0; i < maxFrameSkip - 1; i++) {
                frame = frameGrabber.grabImage().clone();
                processedFrames++;
            }

            String statusText = String.format("Total frames: %d  Processed frames: %d  Memes found: %d %nAverage time: %.2fs Frameskip: %d  %nProgress: %.2f  Remaining time: %.2fm",
                    totalFrames,
                    processedFrames,
                    statisticalMemeCounter,
                    ((float) elapsedSeconds) / statisticalMemeCounter,
                    maxFrameSkip,
                    processedFrames / (float) totalFrames,
                    ((totalFrames - processedFrames) * ((float) elapsedSeconds / processedFrames)) / 60);

            //updateMessage(statusText);


        }

        frameGrabber.stop();
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
