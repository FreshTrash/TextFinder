package my.home.finder;


import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.nio.ByteBuffer;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.cvShowImage;
import static org.bytedeco.javacpp.opencv_highgui.cvWaitKey;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Created by karimov on 11.02.2016.
 */
public class Main {
    private static final int RANGE = 4;
    private static final int threadCount = 1;

    public static void main(String[] args) throws InterruptedException {
        analyse();
    }


    private static void analyse() throws InterruptedException {
        final opencv_core.IplImage img = opencv_imgcodecs.cvLoadImage("check1.png");
        // cvShowImage("binary image", img);
        //  cvWaitKey(1000);

        opencv_core.CvSize cvSizeGray = opencv_core.cvSize(img.width(), img.height());
        opencv_core.IplImage gry = cvCreateImage(cvSizeGray, img.depth(), 1);

        cvCvtColor(img, gry, CV_BGR2GRAY);
        // cvShowImage("binary image", gry);
        // cvWaitKey(1000);

        final opencv_core.IplImage binary = cvCreateImage(cvSizeGray, gry.depth(), 1);
        // int pixels[][] = new int[binary.width()][binary.height()];
        cvAdaptiveThreshold(gry, binary, 250, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY, 7, 6);
        final int partWidth = Math.round(binary.width() / threadCount);
        //int partHeight = Math.round(binary.height()/threadCount);
        //  analysePath(binary, img, RANGE, RANGE,  partWidth - RANGE, binary.height()-RANGE);
        if (threadCount == 1) {
            analysePath(binary, img, RANGE, RANGE,  partWidth - RANGE, binary.height()-RANGE);
        } else {
            for (int i = 0; i < threadCount; i++) {
                final int finalI = i;
                new Thread(new Runnable() {
                    public void run() {
                        System.out.println((finalI * partWidth + RANGE) + " " + ((finalI + 1) * partWidth - RANGE));
                        analysePath(binary, img, finalI * partWidth + RANGE, RANGE, (finalI + 1) * partWidth - RANGE, binary.height() - RANGE);
                    }
                }).start();
            }
        }
        cvShowImage("binary image", img);
        cvWaitKey();
    }

    private static void analysePath(IplImage binary, IplImage src, int startX, int startY, int endX, int endY) {
        final ByteBuffer buffer = binary.createBuffer();
        for (int x = startX; x < endX; x += RANGE)
            for (int y = startY; y < endY; y += Math.round(RANGE / 2)) {

                // int index = y * binary.widthStep() + x * binary.nChannels();
                // int value = buffer.get(index) & 0xFF;
                // pixels[x][y] = value == 250 ? 0 : 1;
                check(x, y, binary, src, buffer);

                // cvShowImage("binary image", src);
                //cvWaitKey(1);
            }
    }

    private static void check(int x, int y, opencv_core.IplImage img, opencv_core.IplImage filter, ByteBuffer buffer) {
        int summ = 0;
        for (int i = x - RANGE; i < x + RANGE; i++)
            for (int j = y - RANGE; j < y + RANGE; j++) {
                int index = j * img.widthStep() + i * img.nChannels();
                int value = buffer.get(index) & 0xFF;
                if (value == 0) summ++;
            }
        if (summ > Math.round(((float) (RANGE * RANGE) / 100) * 80)) {
            for (int i = x - RANGE; i < x + RANGE; i++)
                for (int j = y - RANGE; j < y + RANGE; j++) {
                    opencv_core.CvScalar rgb = cvGet2D(img, j, i);
                    double gray = (rgb.val(0) + rgb.val(2) + rgb.val(1)) / 3;
                    opencv_core.CvScalar scalar = new opencv_core.CvScalar();
                    scalar.setVal(0, gray);
                    scalar.setVal(1, gray);
                    scalar.setVal(2, gray);
                    cvSet2D(filter, j, i, scalar);
                }
        }
    }
}
