package com.example.jakevitale.leftovers_app;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.io.FileNotFoundException;
import java.io.IOException;

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

import static android.R.attr.x;
import static android.R.attr.y;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.circle;
import static org.opencv.imgproc.Imgproc.resize;


public class MainActivity extends AppCompatActivity {

    ImageView  txt1, txt2, txt3, txt4;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        txt1 = (ImageView) findViewById(R.id.ImageView);
        txt2 = (ImageView) findViewById(R.id.ImageView2);
        txt3 = (ImageView) findViewById(R.id.ImageView3);
        txt4 = (ImageView) findViewById(R.id.target);

        Mat m = null;
        try {
            m = Utils.loadResource(this, R.drawable.black_small, CvType.CV_8UC3);
        } catch (IOException e) {
            Log.d(this.getClass().getSimpleName(), "Can't Load Image");
            e.printStackTrace();
        }

        Bitmap bm = Bitmap.createBitmap(m.cols(), m.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(m, bm);

        txt4.setImageBitmap(bm);

        txt1.setOnLongClickListener(longClickListener);
        txt2.setOnLongClickListener(longClickListener);
        txt3.setOnLongClickListener(longClickListener);

        txt4.setOnDragListener(dragListener);
    }

    View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            ClipData data = ClipData.newPlainText("","");
            View.DragShadowBuilder myShadowBuilder = new View.DragShadowBuilder(v);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(data, myShadowBuilder, v, 0);
                return true;
            } else {
                v.startDrag(data, myShadowBuilder, v, 0);
                return true;
            }

        }

    };

    View.OnDragListener dragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            int dragEvent = event.getAction();
            final View thisView = (View) event.getLocalState();
            switch (dragEvent){
                case DragEvent.ACTION_DRAG_STARTED:
                    //Log.d("string", "go");
                    final View view = (View) event.getLocalState();
                    if (view.getId() == R.id.ImageView) {
                        // txt4.setText("TextView is dragged");
                        Log.d("Item you're trying on", "Beanie");
                        find_face(v);
                    } else if (view.getId() == R.id.ImageView2) {
                        Log.d("Item you're trying on", "Coin Tee Black");
                    } else if (view.getId() == R.id.ImageView3) {
                        Log.d("Item you're trying on", "Coin Hoodie Grey");
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    //Log.d("drop", "beendropped");
                    // ImageView dropTarget = (ImageView) v;
                    View v2 = (View) event.getLocalState();
                    //Log.d("v", v.toString());
                    //Log.d("v2", v2.toString());
                    v2.setVisibility(View.INVISIBLE);
                    break;

            }

            return true;

        }


    };
    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            txt4.setImageBitmap(imageBitmap);
        }
    }

    public void find_face(View view)
    {
        CascadeClassifier cascadeClassifier = null;
        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
        try {
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        MatOfRect faces = new MatOfRect();

        ImageView image = (ImageView) findViewById(R.id.target);
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();

        Mat sourceImage = new Mat();
        Utils.bitmapToMat(bitmap, sourceImage);

        Imgproc.cvtColor(sourceImage, sourceImage, Imgproc.COLOR_RGBA2RGB);

        Log.d(this.getClass().getSimpleName(), "Here");

        cascadeClassifier.detectMultiScale(sourceImage, faces, 1.1, 2, 2,
                new Size(sourceImage.height()*0.2, sourceImage.height()*0.2), new Size());

        Rect[] facesArray = faces.toArray();
        Log.d(this.getClass().getSimpleName(), String.valueOf(facesArray.length));

        try {

            Imgproc.cvtColor(sourceImage, sourceImage, Imgproc.COLOR_RGB2RGBA);
            double x = (facesArray[0].br().x + facesArray[0].tl().x)*0.5;
            x = facesArray[0].tl().x;
            int xcoord = (int) x;
            double y = (facesArray[0].br().y + facesArray[0].tl().y)*0.4;
            //y = (facesArray[0].tl().y);
            int ycoord = (int) y;

            Point center_of_rect = new Point(xcoord, ycoord);
            //circle(sourceImage,center_of_rect,3, new Scalar(0,0,255));

            Mat m = null;
            try {
                m = Utils.loadResource(this, R.drawable.beanie2, CvType.CV_8UC4);
            } catch (IOException e) {
                Log.d(this.getClass().getSimpleName(), "Can't Load Image");
                e.printStackTrace();
            }



            double rowyd = y- (facesArray[0].br().y + facesArray[0].tl().y)*0.5;
            int rowy = 0;

            double colxd = facesArray[0].br().x;
            int colx = (int) colxd;

            Log.d("DIMS", String.valueOf(rowy));
            Log.d("DIMS", String.valueOf(ycoord));
            Log.d("DIMS", String.valueOf(xcoord));
            Log.d("DIMS", String.valueOf(colx));

            resize(m, m, new Size(colx-xcoord, ycoord-rowy), 0,0,INTER_CUBIC);

            m.copyTo(sourceImage.rowRange(rowy, ycoord).colRange(xcoord, colx));

        }
        catch (Exception e)
        {
            Log.d(this.getClass().getSimpleName(), "NO FACE FOUND");
        }





        //src.copyTo(dst.rowRange(1, 6).colRange(3, 10));

//        for (int i = 0; i <facesArray.length; i++)
//            Imgproc.rectangle(sourceImage, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

        Bitmap bm = Bitmap.createBitmap(sourceImage.cols(), sourceImage.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(sourceImage, bm);

        image.setImageBitmap(bm);

    }
}