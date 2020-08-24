package sample;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.opencv.core.CvType.CV_8U;

public class ImageProcessor {

    public static void processImage(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        Java2DFrameConverter jconv = new Java2DFrameConverter();
        ImageCropData cropData = getCropData(jconv.convert(image));
        saveImageWithCropData(image, path, cropData);
    }

    public static ImageCropData oldGetCropData(BufferedImage image) {

        OpenCVFrameConverter.ToMat matconv = new OpenCVFrameConverter.ToMat();
        Java2DFrameConverter buffConverter = new Java2DFrameConverter();
        Mat mat = matconv.convertToOrgOpenCvCoreMat(buffConverter.convert(image));

        int xOffset = 0;
        int minimalOffset = Integer.MAX_VALUE;
        int imageWidth = mat.cols();

        if (mat.rows() > 1) {
            for (int y = 0; y < mat.rows(); y++) {
                for (int x = 0; x < mat.cols(); x++) {

                    double[] colors = mat.get(y, x);
                    if (colors[0] > 60 || colors[1] > 60 || colors[2] > 60) {
                        xOffset = x;
                        break;
                    }

                }

                if (xOffset < minimalOffset) {
                    minimalOffset = xOffset;
                }

            }
            imageWidth -= minimalOffset * 2;
            return new ImageCropData(minimalOffset, imageWidth, mat.rows());
        }

        return new ImageCropData(0, imageWidth, mat.rows());
    }


    public static ImageCropData getCropData(Frame frame) throws IOException {
        OpenCVFrameConverter.ToMat matconv = new OpenCVFrameConverter.ToMat();
        Java2DFrameConverter buffConverter = new Java2DFrameConverter();
        Mat mat = matconv.convertToOrgOpenCvCoreMat(frame);
        Imgproc.cvtColor(mat, mat, CV_8U);

        int xOffset = 0;
        int minimalOffset = Integer.MAX_VALUE;
        double imageWidth = mat.cols();
        long averageSum = 0;
        long totalSum = 0;

        if (mat.rows() > 1) {

            for (int y = 0; y < mat.rows(); y++) {
                for (int x = 0; x < mat.cols(); x++) {
                    double[] colors = mat.get(y, x);
                    totalSum += colors[0] + colors[1] + colors[2];
                }

            }


            System.out.println("AVG: " + averageSum);
            Map<Integer, Integer> xMap = new HashMap<>();
            averageSum = totalSum / (mat.rows() * mat.cols());

            for (int y = 0; y < mat.rows(); y++) {
                for (int x = 0; x < mat.cols(); x++) {

                    double[] colors = mat.get(y, x);

                    double sum = colors[0] + colors[1] + colors[2];

                    if (sum > averageSum) {
                        double diff = sum - averageSum;

                        if (xMap.containsKey(x))
                            xMap.put(x, xMap.get(x) + (int) diff);
                        else
                            xMap.put(x, (int) diff);

                        break;
                    }


                }


            }

            int mostCommon = 0;
            for (int x : xMap.keySet()) {
                if (xMap.get(x) > mostCommon) {
                    mostCommon = xMap.get(x);
                    minimalOffset = x;
                }

            }

            int padding = 0;

            if (averageSum < 65)
                padding = 50;

            if (minimalOffset < padding)
                padding = 0;

            System.out.println("MINOFF: " + minimalOffset);
            imageWidth -= minimalOffset * 2 - padding;

            if (imageWidth < 300 || minimalOffset > mat.cols()) {
                padding = 35;
                imageWidth = (mat.cols() / 2.0) + padding;
                minimalOffset = (int) ((mat.cols() - imageWidth) / 2);
            }


            return new ImageCropData(minimalOffset - (padding / 2), (int) imageWidth, mat.rows());

        }

        System.out.println("No cropping");
        return new ImageCropData(0, (int) imageWidth, mat.rows());
    }

    private static void saveImageWithCropData(BufferedImage image, String path, ImageCropData cropData) throws IOException {
        ImageIO.write(image.getSubimage(cropData.getxOffset(), 0, cropData.getWidth(), cropData.getHeight()),
                "png",
                new File(path.replace(".png", "") + "-export.png"));
    }

}
