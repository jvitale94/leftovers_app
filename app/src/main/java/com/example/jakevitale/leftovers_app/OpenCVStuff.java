package com.example.jakevitale.leftovers_app;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.jakevitale.leftovers_app.R;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;

//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        if (!OpenCVLoader.initDebug()) {
//            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
//        } else {
//            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
//        }
//
//        String inputFileName="simm_01";
//        String inputExtension = "jpg";
//        String inputDir = "/Users/jakevitale/Desktop";  // use the cache directory for i/o
//        String outputDir = "/Users/jakevitale/Desktop";
//        String outputExtension = "png";
//        String inputFilePath = inputDir + File.separator + inputFileName + "." + inputExtension;
//
//
//        Log.d (this.getClass().getSimpleName(), "loading " + inputFilePath + "...");
//        Mat image = Imgcodecs.imread(inputFilePath);
//        Log.d (this.getClass().getSimpleName(), "width of " + inputFileName + ": " + image.width());
//// if width is 0 then it did not read your image.
//
//
//// for the canny edge detection algorithm, play with these to see different results
//        int threshold1 = 70;
//        int threshold2 = 100;
//
//        Mat im_canny = new Mat();  // you have to initialize output image before giving it to the Canny method
//        Imgproc.Canny(image, im_canny, threshold1, threshold2);
//        String cannyFilename = outputDir + File.separator + inputFileName + "_canny-" + threshold1 + "-" + threshold2 + "." + outputExtension;
//        Log.d (this.getClass().getSimpleName(), "Writing " + cannyFilename);
//        Imgcodecs.imwrite(cannyFilename, im_canny);
//    }
//
//    public void changeimg(View view)
//    {
//        Log.d(this.getClass().getSimpleName(), "BUTTON CLICKED");
//
//        Mat m = null;
//        try {
//            m = Utils.loadResource(this, R.drawable.black_small, CvType.CV_8UC3);
//        } catch (IOException e) {
//            Log.d(this.getClass().getSimpleName(), "AT LEAST YOU'RE TRYING!!!");
//            e.printStackTrace();
//        }
//
//        // Imgproc.putText(m, "hi there ;)", new Point(30,80), Core.FONT_HERSHEY_SCRIPT_SIMPLEX, 2.2, new Scalar(200,200,0),2);
//
//        Bitmap bm = Bitmap.createBitmap(m.cols(), m.rows(),Bitmap.Config.RGB_565);
//        Utils.matToBitmap(m, bm);
//
//        ImageView iv = (ImageView) findViewById(R.id.imageView1);
//        iv.setImageBitmap(bm);
//    }
//}

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import org.opencv.android.*;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class OpenCVStuff extends Activity
        implements CvCameraViewListener {

    private CameraBridgeViewBase openCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private Mat grayscaleImage;
    private int absoluteFaceSize;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void initializeOpenCVDependencies() {

        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);


            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "BAD LOADING");
        }

        // And we are ready to go
        openCvCameraView.enableView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        openCvCameraView = new JavaCameraView(this, -1);
        setContentView(openCvCameraView);
        openCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        grayscaleImage = new Mat(height, width, CvType.CV_8UC4);

        // The faces will be a 20% of the height of the screen
        absoluteFaceSize = (int) (height * 0.2);
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(Mat aInputFrame) {
        // Create a grayscale image
        Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);

        MatOfRect faces = new MatOfRect();

        // Use the classifier to detect faces
        if (cascadeClassifier != null) {
            Log.d(this.getClass().getSimpleName(), "NOT NULL");
            cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }

        // If there are any faces found, draw a rectangle around it
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i <facesArray.length; i++)
            Imgproc.rectangle(aInputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

        return aInputFrame;
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }
}
