package my.home.finder;


import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.nio.ByteBuffer;

import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_highgui.cvShowImage;
import static org.bytedeco.javacpp.opencv_highgui.cvWaitKey;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Created by karimov on 11.02.2016.
 */
public class Main {
    public static void main(String[] args) {
        analyse();
    }

    private static void analyse() {
        opencv_core.IplImage img = opencv_imgcodecs.cvLoadImage("check.png");
        opencv_core.CvSize cvSizeGray = opencv_core.cvSize(img.width(), img.height());
        opencv_core.IplImage gry = cvCreateImage(cvSizeGray, img.depth(), 1);
        cvCvtColor(img, gry, CV_BGR2GRAY);

        opencv_core.IplImage binary = cvCreateImage(cvSizeGray, gry.depth(), 1);
        ByteBuffer buffer = binary.createBuffer();
        int pixels[][] = new int[binary.width()][binary.height()];

        cvAdaptiveThreshold(gry, binary, 250, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY, 7, 6);
        for (int y = 0; y < binary.height() - 1; y++)
            for (int x = 0; x < binary.width() - 1; x++) {
                int index = y * binary.widthStep() + x * binary.nChannels();
                int value = buffer.get(index) & 0xFF;
                pixels[x][y] = value == 250 ? 0 : 1;
            }

        cvShowImage("binary image", binary);
        cvWaitKey();
    }
}
